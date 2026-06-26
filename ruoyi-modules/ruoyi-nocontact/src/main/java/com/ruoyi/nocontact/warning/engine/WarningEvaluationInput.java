package com.ruoyi.nocontact.warning.engine;

import java.math.BigDecimal;

/**
 * 规则计算输入，来自已审核的采集明细。
 */
public class WarningEvaluationInput
{
    private Long indicatorId;
    private String indicatorName;
    private Long deptId;
    private Long responsibleUnitId;
    private String responsibleUnitName;
    private String regionCode;
    private String regionName;
    private String periodType;
    private String periodKey;
    private BigDecimal currentValue;
    private boolean valuePresent;
    private boolean overdue;

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

    public String getPeriodKey()
    {
        return periodKey;
    }

    public void setPeriodKey(String periodKey)
    {
        this.periodKey = periodKey;
    }

    public BigDecimal getCurrentValue()
    {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue)
    {
        this.currentValue = currentValue;
    }

    public boolean isValuePresent()
    {
        return valuePresent;
    }

    public void setValuePresent(boolean valuePresent)
    {
        this.valuePresent = valuePresent;
    }

    public boolean isOverdue()
    {
        return overdue;
    }

    public void setOverdue(boolean overdue)
    {
        this.overdue = overdue;
    }
}
