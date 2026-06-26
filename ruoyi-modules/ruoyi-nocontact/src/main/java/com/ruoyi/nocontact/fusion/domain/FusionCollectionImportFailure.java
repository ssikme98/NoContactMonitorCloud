package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 数据采集导入失败明细对象 nc_fusion_collection_import_failure。
 */
public class FusionCollectionImportFailure extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long failureId;
    private Long deptId;
    private String importBatchName;
    private String sourceRecordId;
    private Integer rowNum;
    private String fieldName;
    private String rawValue;
    private String failureReason;

    public Long getFailureId()
    {
        return failureId;
    }

    public void setFailureId(Long failureId)
    {
        this.failureId = failureId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getImportBatchName()
    {
        return importBatchName;
    }

    public void setImportBatchName(String importBatchName)
    {
        this.importBatchName = importBatchName;
    }

    public String getSourceRecordId()
    {
        return sourceRecordId;
    }

    public void setSourceRecordId(String sourceRecordId)
    {
        this.sourceRecordId = sourceRecordId;
    }

    public Integer getRowNum()
    {
        return rowNum;
    }

    public void setRowNum(Integer rowNum)
    {
        this.rowNum = rowNum;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getRawValue()
    {
        return rawValue;
    }

    public void setRawValue(String rawValue)
    {
        this.rawValue = rawValue;
    }

    public String getFailureReason()
    {
        return failureReason;
    }

    public void setFailureReason(String failureReason)
    {
        this.failureReason = failureReason;
    }
}
