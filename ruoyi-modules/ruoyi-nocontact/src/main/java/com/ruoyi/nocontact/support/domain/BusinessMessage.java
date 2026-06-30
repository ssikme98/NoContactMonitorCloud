package com.ruoyi.nocontact.support.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 业务消息对象
 */
public class BusinessMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private String messageType;
    private String title;
    private String content;
    private String businessType;
    private Long businessId;
    private String jumpTarget;
    private String readStatus;
    private Long receiverUserId;
    private String receiverUserName;
    private Date eventTime;

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public Long getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    public String getJumpTarget()
    {
        return jumpTarget;
    }

    public void setJumpTarget(String jumpTarget)
    {
        this.jumpTarget = jumpTarget;
    }

    public String getReadStatus()
    {
        return readStatus;
    }

    public void setReadStatus(String readStatus)
    {
        this.readStatus = readStatus;
    }

    public Long getReceiverUserId()
    {
        return receiverUserId;
    }

    public void setReceiverUserId(Long receiverUserId)
    {
        this.receiverUserId = receiverUserId;
    }

    public String getReceiverUserName()
    {
        return receiverUserName;
    }

    public void setReceiverUserName(String receiverUserName)
    {
        this.receiverUserName = receiverUserName;
    }

    public Date getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(Date eventTime)
    {
        this.eventTime = eventTime;
    }
}
