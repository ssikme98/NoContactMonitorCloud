package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
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
        return messageMapper.selectMessageList(message);
    }

    @Override
    public WarningMessage selectMessageById(Long messageId)
    {
        return messageMapper.selectMessageById(messageId);
    }

    @Override
    public int updateMessageStatus(Long messageId, String status, String operName)
    {
        WarningMessage message = new WarningMessage();
        message.setMessageId(messageId);
        message.setMessageStatus(status);
        message.setUpdateBy(operName);
        message.setUpdateTime(DateUtils.getNowDate());
        if ("handled".equals(status) || "archived".equals(status))
        {
            message.setHandleTime(DateUtils.getNowDate());
        }
        return messageMapper.updateMessageStatus(message);
    }

    @Override
    public int deleteMessageByIds(Long[] messageIds)
    {
        return messageMapper.deleteMessageByIds(messageIds);
    }

    @Override
    public Map<String, Object> selectDashboard()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("summary", messageMapper.selectDashboardSummary());
        data.put("levelStats", messageMapper.selectLevelStats());
        data.put("regionStats", messageMapper.selectRegionStats());
        data.put("indicatorStats", messageMapper.selectIndicatorStats());
        data.put("trendStats", messageMapper.selectTrendStats());
        return data;
    }
}
