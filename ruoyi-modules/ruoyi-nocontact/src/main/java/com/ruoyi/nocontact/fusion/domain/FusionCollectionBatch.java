package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;
import java.util.List;

/**
 * 数据采集批次对象 nc_fusion_collection_batch
 */
public class FusionCollectionBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long batchId;
    private Long taskId;
    private String batchName;
    private String sourceType;
    private String sourceName;
    private String sourceRecordId;
    private Long deptId;
    private Long responsibleUnitId;
    private String responsibleUnitName;
    private String regionCode;
    private String regionName;
    private String periodType;
    private Integer periodYear;
    private Integer periodQuarter;
    private Integer periodMonth;
    private String periodKey;
    private String batchStatus;
    private String expectedStatus;
    private String submitBy;
    private Date submitTime;
    private String auditBy;
    private Date auditTime;
    private String auditOpinion;
    private Integer itemCount;
    private List<FusionCollectionItem> items;
    private List<FusionCollectionAuditLog> auditLogs;

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getBatchName()
    {
        return batchName;
    }

    public void setBatchName(String batchName)
    {
        this.batchName = batchName;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

    public String getSourceRecordId()
    {
        return sourceRecordId;
    }

    public void setSourceRecordId(String sourceRecordId)
    {
        this.sourceRecordId = sourceRecordId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getResponsibleUnitId()
    {
        return responsibleUnitId;
    }

    public void setResponsibleUnitId(Long responsibleUnitId)
    {
        this.responsibleUnitId = responsibleUnitId;
    }

    public String getResponsibleUnitName()
    {
        return responsibleUnitName;
    }

    public void setResponsibleUnitName(String responsibleUnitName)
    {
        this.responsibleUnitName = responsibleUnitName;
    }

    public String getRegionCode()
    {
        return regionCode;
    }

    public void setRegionCode(String regionCode)
    {
        this.regionCode = regionCode;
    }

    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    public String getPeriodType()
    {
        return periodType;
    }

    public void setPeriodType(String periodType)
    {
        this.periodType = periodType;
    }

    public Integer getPeriodYear()
    {
        return periodYear;
    }

    public void setPeriodYear(Integer periodYear)
    {
        this.periodYear = periodYear;
    }

    public Integer getPeriodQuarter()
    {
        return periodQuarter;
    }

    public void setPeriodQuarter(Integer periodQuarter)
    {
        this.periodQuarter = periodQuarter;
    }

    public Integer getPeriodMonth()
    {
        return periodMonth;
    }

    public void setPeriodMonth(Integer periodMonth)
    {
        this.periodMonth = periodMonth;
    }

    public String getPeriodKey()
    {
        return periodKey;
    }

    public void setPeriodKey(String periodKey)
    {
        this.periodKey = periodKey;
    }

    public String getBatchStatus()
    {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus)
    {
        this.batchStatus = batchStatus;
    }

    public String getExpectedStatus()
    {
        return expectedStatus;
    }

    public void setExpectedStatus(String expectedStatus)
    {
        this.expectedStatus = expectedStatus;
    }

    public String getSubmitBy()
    {
        return submitBy;
    }

    public void setSubmitBy(String submitBy)
    {
        this.submitBy = submitBy;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
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

    public Integer getItemCount()
    {
        return itemCount;
    }

    public void setItemCount(Integer itemCount)
    {
        this.itemCount = itemCount;
    }

    public List<FusionCollectionItem> getItems()
    {
        return items;
    }

    public void setItems(List<FusionCollectionItem> items)
    {
        this.items = items;
    }

    public List<FusionCollectionAuditLog> getAuditLogs()
    {
        return auditLogs;
    }

    public void setAuditLogs(List<FusionCollectionAuditLog> auditLogs)
    {
        this.auditLogs = auditLogs;
    }
}
