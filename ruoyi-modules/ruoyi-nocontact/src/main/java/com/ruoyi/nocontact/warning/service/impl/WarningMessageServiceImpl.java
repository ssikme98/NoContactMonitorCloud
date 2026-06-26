package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.nocontact.common.NocontactDataScopeHelper;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.domain.WarningMessageHandleLog;
import com.ruoyi.nocontact.warning.mapper.WarningMessageMapper;
import com.ruoyi.nocontact.warning.service.IWarningMessageService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预警消息Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class WarningMessageServiceImpl implements IWarningMessageService
{
    @Autowired
    private WarningMessageMapper messageMapper;

    @Override
    public List<WarningMessage> selectMessageList(WarningMessage message)
    {
        NocontactDataScopeHelper.applyDataScope(message, "m", "dept_id", "m", "create_by");
        return messageMapper.selectMessageList(message);
    }

    @Override
    public WarningMessage selectMessageById(Long messageId)
    {
        WarningMessage message = selectScopedMessage(messageId);
        if (message != null)
        {
            message.setHandleLogs(messageMapper.selectHandleLogsByMessageId(messageId));
        }
        return message;
    }

    @Override
    public int updateMessageStatus(Long messageId, String status, String opinion, String operName)
    {
        WarningMessage existing = selectScopedMessage(messageId);
        if (existing == null)
        {
            throw new ServiceException("预警消息不存在");
        }
        assertAllowedTransition(existing.getMessageStatus(), status);
        WarningMessage message = new WarningMessage();
        message.setMessageId(messageId);
        message.setMessageStatus(status);
        message.setExpectedStatus(existing.getMessageStatus());
        message.setHandleOpinion(opinion);
        message.setUpdateBy(operName);
        message.setUpdateTime(DateUtils.getNowDate());
        if ("closed".equals(status) || "ignored".equals(status))
        {
            message.setHandleTime(DateUtils.getNowDate());
        }
        int rows = messageMapper.updateMessageStatus(message);
        if (rows == 0)
        {
            throw new ServiceException("预警消息状态已变化，请刷新后重试");
        }
        if (rows > 0)
        {
            insertHandleLog(existing.getMessageId(), existing.getMessageStatus(), status, opinion, operName);
        }
        return rows;
    }

    @Override
    public int deleteMessageByIds(Long[] messageIds)
    {
        if (messageIds != null)
        {
            for (Long messageId : messageIds)
            {
                if (selectScopedMessage(messageId) == null)
                {
                    throw new ServiceException("预警消息不存在或无权删除");
                }
            }
        }
        return messageMapper.deleteMessageByIds(messageIds);
    }

    @Override
    public Map<String, Object> selectDashboard()
    {
        WarningMessage scope = new WarningMessage();
        NocontactDataScopeHelper.applyDataScope(scope, "m", "dept_id", "m", "create_by");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("summary", messageMapper.selectDashboardSummary(scope));
        data.put("levelStats", messageMapper.selectLevelStats(scope));
        data.put("regionStats", messageMapper.selectRegionStats(scope));
        data.put("indicatorStats", messageMapper.selectIndicatorStats(scope));
        data.put("trendStats", messageMapper.selectTrendStats(scope));
        return data;
    }

    private void assertAllowedTransition(String fromStatus, String toStatus)
    {
        if (!"processing".equals(toStatus) && !"closed".equals(toStatus) && !"ignored".equals(toStatus))
        {
            throw new ServiceException("不支持的预警状态：" + toStatus);
        }
        if ("closed".equals(fromStatus) || "ignored".equals(fromStatus))
        {
            throw new ServiceException("已关闭或已忽略的预警不允许再次处理");
        }
        if ("pending".equals(fromStatus))
        {
            return;
        }
        if ("processing".equals(fromStatus) && ("closed".equals(toStatus) || "ignored".equals(toStatus)))
        {
            return;
        }
        if (fromStatus == null && "processing".equals(toStatus))
        {
            return;
        }
        throw new ServiceException("预警状态不允许从" + fromStatus + "变更为" + toStatus);
    }

    private void insertHandleLog(Long messageId, String fromStatus, String toStatus, String opinion, String operName)
    {
        WarningMessageHandleLog log = new WarningMessageHandleLog();
        log.setMessageId(messageId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setHandleBy(operName);
        log.setHandleTime(DateUtils.getNowDate());
        log.setHandleOpinion(opinion);
        log.setCreateBy(operName);
        log.setCreateTime(DateUtils.getNowDate());
        messageMapper.insertHandleLog(log);
    }

    private WarningMessage selectScopedMessage(Long messageId)
    {
        WarningMessage query = new WarningMessage();
        query.setMessageId(messageId);
        NocontactDataScopeHelper.applyDataScope(query, "m", "dept_id", "m", "create_by");
        return messageMapper.selectMessageByScope(query);
    }
}
