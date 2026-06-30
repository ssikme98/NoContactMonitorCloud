package com.ruoyi.nocontact.support.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.support.domain.BusinessMessage;
import com.ruoyi.nocontact.support.mapper.BusinessMessageMapper;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessMessageServiceImpl implements IBusinessMessageService
{
    public static final String WARNING_GENERATED = "warning_generated";
    public static final String RECTIFICATION_DISPATCHED = "rectification_dispatched";
    public static final String AUDIT_REJECTED = "audit_rejected";
    public static final String SURVEY_DISPATCHED = "survey_dispatched";
    public static final String REPORT_GENERATED = "report_generated";

    @Autowired
    private BusinessMessageMapper messageMapper;

    @Override
    public List<BusinessMessage> selectCurrentUserMessages(BusinessMessage query)
    {
        query.setReceiverUserName(SecurityUtils.getUsername());
        return messageMapper.selectMessageList(query);
    }

    @Override
    public int markRead(Long messageId)
    {
        BusinessMessage existing = messageMapper.selectMessageById(messageId);
        if (existing == null || !SecurityUtils.getUsername().equals(existing.getReceiverUserName()))
        {
            throw new ServiceException("消息不存在或无权访问");
        }
        BusinessMessage update = new BusinessMessage();
        update.setMessageId(messageId);
        update.setReadStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return messageMapper.updateReadStatus(update);
    }

    @Override
    public int countUnreadCurrentUser()
    {
        return messageMapper.countUnreadByUserName(SecurityUtils.getUsername());
    }

    @Override
    public int createMessage(BusinessMessage message)
    {
        validateMessage(message);
        if (message.getReadStatus() == null)
        {
            message.setReadStatus("0");
        }
        if (message.getEventTime() == null)
        {
            message.setEventTime(DateUtils.getNowDate());
        }
        message.setCreateTime(DateUtils.getNowDate());
        return messageMapper.insertMessage(message);
    }

    private void validateMessage(BusinessMessage message)
    {
        if (message == null)
        {
            throw new ServiceException("业务消息不能为空");
        }
        if (message.getBusinessId() == null)
        {
            throw new ServiceException("业务消息缺少业务ID");
        }
        if (message.getBusinessType() == null || message.getBusinessType().trim().isEmpty())
        {
            throw new ServiceException("业务消息缺少业务类型");
        }
        if (message.getJumpTarget() == null || message.getJumpTarget().trim().isEmpty())
        {
            throw new ServiceException("业务消息缺少跳转地址");
        }
        if (message.getReceiverUserName() == null || message.getReceiverUserName().trim().isEmpty())
        {
            throw new ServiceException("业务消息缺少接收人");
        }
    }
}
