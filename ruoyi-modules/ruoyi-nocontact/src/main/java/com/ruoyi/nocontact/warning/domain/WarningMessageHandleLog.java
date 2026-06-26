package com.ruoyi.nocontact.warning.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 预警消息处理日志对象 nc_warning_message_handle_log
 */
public class WarningMessageHandleLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long messageId;
    private String fromStatus;
    private String toStatus;
    private String handleBy;
    private Date handleTime;
    private String handleOpinion;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public String getFromStatus()
    {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus)
    {
        this.fromStatus = fromStatus;
    }

    public String getToStatus()
    {
        return toStatus;
    }

    public void setToStatus(String toStatus)
    {
        this.toStatus = toStatus;
    }

    public String getHandleBy()
    {
        return handleBy;
    }

    public void setHandleBy(String handleBy)
    {
        this.handleBy = handleBy;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    public String getHandleOpinion()
    {
        return handleOpinion;
    }

    public void setHandleOpinion(String handleOpinion)
    {
        this.handleOpinion = handleOpinion;
    }
}
