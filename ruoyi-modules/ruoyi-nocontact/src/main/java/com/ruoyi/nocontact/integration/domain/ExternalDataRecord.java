package com.ruoyi.nocontact.integration.domain;

import java.math.BigDecimal;

/**
 * 外部系统拉取后的标准化单条记录。
 */
public class ExternalDataRecord
{
    private String sourceSystem;
    private String externalId;
    private String version;
    private Long indicatorId;
    private String indicatorCode;
    private String indicatorName;
    private Long responsibleUnitId;
    private String responsibleUnitName;
    private String regionCode;
    private String regionName;
    private String periodType;
    private String periodKey;
    private String rawValue;
    private BigDecimal valueDecimal;
    private String valueText;
    private String payload;

    public String getSourceSystem()
    {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem)
    {
        this.sourceSystem = sourceSystem;
    }

    public String getExternalId()
    {
        return externalId;
    }

    public void setExternalId(String externalId)
    {
        this.externalId = externalId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public Long getIndicatorId()
    {
        return indicatorId;
    }

    public void setIndicatorId(Long indicatorId)
    {
        this.indicatorId = indicatorId;
    }

    public String getIndicatorCode()
    {
        return indicatorCode;
    }

    public void setIndicatorCode(String indicatorCode)
    {
        this.indicatorCode = indicatorCode;
    }

    public String getIndicatorName()
    {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName)
    {
        this.indicatorName = indicatorName;
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

    public String getPeriodKey()
    {
        return periodKey;
    }

    public void setPeriodKey(String periodKey)
    {
        this.periodKey = periodKey;
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

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }
}
