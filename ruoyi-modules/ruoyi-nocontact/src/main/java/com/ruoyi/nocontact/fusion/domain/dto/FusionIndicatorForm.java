package com.ruoyi.nocontact.fusion.domain.dto;

import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 营商监测指标写入表单。
 */
public class FusionIndicatorForm
{
    private Long indicatorId;

    private String indicatorCode;

    private String externalCode;

    private String systemName;

    private String categoryName;

    private Integer versionNo;

    private String lifecycleStatus;

    private String yearName;

    private String firstLevel;

    private String secondLevel;

    private String indicatorName;

    private String periodType;

    private String dataType;

    private String scoringRule;

    private String algorithmType;

    private String algorithmParams;

    private String responsibleUnit;

    private String regionCode;

    private String regionName;

    private String dataSource;

    private String tagNames;

    private Integer sortOrder;

    private String status;

    public FusionIndicator toEntity()
    {
        FusionIndicator indicator = new FusionIndicator();
        indicator.setIndicatorId(indicatorId);
        indicator.setIndicatorCode(indicatorCode);
        indicator.setExternalCode(externalCode);
        indicator.setSystemName(systemName);
        indicator.setCategoryName(categoryName);
        indicator.setVersionNo(versionNo);
        indicator.setLifecycleStatus(lifecycleStatus);
        indicator.setYearName(yearName);
        indicator.setFirstLevel(firstLevel);
        indicator.setSecondLevel(secondLevel);
        indicator.setIndicatorName(indicatorName);
        indicator.setPeriodType(periodType);
        indicator.setDataType(dataType);
        indicator.setScoringRule(scoringRule);
        indicator.setAlgorithmType(algorithmType);
        indicator.setAlgorithmParams(algorithmParams);
        indicator.setResponsibleUnit(responsibleUnit);
        indicator.setRegionCode(regionCode);
        indicator.setRegionName(regionName);
        indicator.setDataSource(dataSource);
        indicator.setTagNames(tagNames);
        indicator.setSortOrder(sortOrder);
        indicator.setStatus(status);
        return indicator;
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

    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    public String getSystemName()
    {
        return systemName;
    }

    public void setSystemName(String systemName)
    {
        this.systemName = systemName;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
    }

    public String getLifecycleStatus()
    {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(String lifecycleStatus)
    {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getYearName()
    {
        return yearName;
    }

    public void setYearName(String yearName)
    {
        this.yearName = yearName;
    }

    public String getFirstLevel()
    {
        return firstLevel;
    }

    public void setFirstLevel(String firstLevel)
    {
        this.firstLevel = firstLevel;
    }

    public String getSecondLevel()
    {
        return secondLevel;
    }

    public void setSecondLevel(String secondLevel)
    {
        this.secondLevel = secondLevel;
    }

    @NotBlank(message = "指标名称不能为空")
    @Size(max = 200, message = "指标名称长度不能超过200个字符")
    public String getIndicatorName()
    {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName)
    {
        this.indicatorName = indicatorName;
    }

    public String getPeriodType()
    {
        return periodType;
    }

    public void setPeriodType(String periodType)
    {
        this.periodType = periodType;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getScoringRule()
    {
        return scoringRule;
    }

    public void setScoringRule(String scoringRule)
    {
        this.scoringRule = scoringRule;
    }

    public String getAlgorithmType()
    {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType)
    {
        this.algorithmType = algorithmType;
    }

    public String getAlgorithmParams()
    {
        return algorithmParams;
    }

    public void setAlgorithmParams(String algorithmParams)
    {
        this.algorithmParams = algorithmParams;
    }

    public String getResponsibleUnit()
    {
        return responsibleUnit;
    }

    public void setResponsibleUnit(String responsibleUnit)
    {
        this.responsibleUnit = responsibleUnit;
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

    public String getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(String dataSource)
    {
        this.dataSource = dataSource;
    }

    public String getTagNames()
    {
        return tagNames;
    }

    public void setTagNames(String tagNames)
    {
        this.tagNames = tagNames;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
