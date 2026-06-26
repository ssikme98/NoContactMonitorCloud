package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 数据采集明细对象 nc_fusion_collection_item
 */
public class FusionCollectionItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Long batchId;
    private Long deptId;
    private Long responsibleUnitId;
    private String responsibleUnitName;
    private String regionCode;
    private String regionName;
    private String periodKey;
    private Long indicatorId;
    private String indicatorName;
    private String indicatorCode;
    private String rawValue;
    private BigDecimal valueDecimal;
    private String valueText;
    private Date valueDate;
    private String validationStatus;
    private String validationMessage;
    private String isCurrent;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
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

    public String getPeriodKey()
    {
        return periodKey;
    }

    public void setPeriodKey(String periodKey)
    {
        this.periodKey = periodKey;
    }

    public Long getIndicatorId()
    {
        return indicatorId;
    }

    public void setIndicatorId(Long indicatorId)
    {
        this.indicatorId = indicatorId;
    }

    public String getIndicatorName()
    {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName)
    {
        this.indicatorName = indicatorName;
    }

    public String getIndicatorCode()
    {
        return indicatorCode;
    }

    public void setIndicatorCode(String indicatorCode)
    {
        this.indicatorCode = indicatorCode;
    }

    public String getRawValue()
    {
        return rawValue;
    }

    public void setRawValue(String rawValue)
    {
        this.rawValue = rawValue;
    }

    public BigDecimal getValueDecimal()
    {
        return valueDecimal;
    }

    public void setValueDecimal(BigDecimal valueDecimal)
    {
        this.valueDecimal = valueDecimal;
    }

    public String getValueText()
    {
        return valueText;
    }

    public void setValueText(String valueText)
    {
        this.valueText = valueText;
    }

    public Date getValueDate()
    {
        return valueDate;
    }

    public void setValueDate(Date valueDate)
    {
        this.valueDate = valueDate;
    }

    public String getValidationStatus()
    {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus)
    {
        this.validationStatus = validationStatus;
    }

    public String getValidationMessage()
    {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage)
    {
        this.validationMessage = validationMessage;
    }

    public String getIsCurrent()
    {
        return isCurrent;
    }

    public void setIsCurrent(String isCurrent)
    {
        this.isCurrent = isCurrent;
    }
}
