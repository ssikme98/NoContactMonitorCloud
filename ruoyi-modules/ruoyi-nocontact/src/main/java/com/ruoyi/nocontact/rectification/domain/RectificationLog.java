package com.ruoyi.nocontact.rectification.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 问题整改流转日志对象 nc_rectification_log
 */
public class RectificationLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long issueId;
    private String fromStatus;
    private String toStatus;
    private String actionName;
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

    public Long getIssueId()
    {
        return issueId;
    }

    public void setIssueId(Long issueId)
    {
        this.issueId = issueId;
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

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
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
