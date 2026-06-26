package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.annotation.Excel;
import java.math.BigDecimal;

/**
 * 数据采集Excel导入行。
 */
public class FusionCollectionImportRow
{
    @Excel(name = "批次名称")
    private String batchName;

    @Excel(name = "责任单位ID")
    private Long responsibleUnitId;

    @Excel(name = "责任单位")
    private String responsibleUnitName;

    @Excel(name = "地区编码")
    private String regionCode;

    @Excel(name = "地区名称")
    private String regionName;

    @Excel(name = "周期类型")
    private String periodType;

    @Excel(name = "业务期间")
    private String periodKey;

    @Excel(name = "指标ID")
    private Long indicatorId;

    @Excel(name = "指标编码")
    private String indicatorCode;

    @Excel(name = "指标名称")
    private String indicatorName;

    @Excel(name = "原始值")
    private String rawValue;

    @Excel(name = "数值")
    private BigDecimal valueDecimal;

    private Integer rowNum;

    public String getBatchName()
    {
        return batchName;
    }

    public void setBatchName(String batchName)
    {
        this.batchName = batchName;
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

    public Integer getRowNum()
    {
        return rowNum;
    }

    public void setRowNum(Integer rowNum)
    {
        this.rowNum = rowNum;
    }
}
