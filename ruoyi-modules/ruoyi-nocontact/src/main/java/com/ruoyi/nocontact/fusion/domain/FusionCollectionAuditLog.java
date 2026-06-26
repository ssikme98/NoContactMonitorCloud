package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 数据采集审核日志对象 nc_fusion_collection_audit_log
 */
public class FusionCollectionAuditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long batchId;
    private String fromStatus;
    private String toStatus;
    private String actionName;
    private String auditBy;
    private Date auditTime;
    private String auditOpinion;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
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

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditOpinion()
    {
        return auditOpinion;
    }

    public void setAuditOpinion(String auditOpinion)
    {
        this.auditOpinion = auditOpinion;
    }
}
