package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 营商监测指标对象 nc_fusion_indicator
 *
 * @author ruoyi
 */
public class FusionIndicator extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long indicatorId;

    @Excel(name = "指标内部编号")
    private String indicatorCode;

    @Excel(name = "外部编码")
    private String externalCode;

    @Excel(name = "指标体系")
    private String systemName;

    @Excel(name = "指标分类")
    private String categoryName;

    @Excel(name = "版本号")
    private Integer versionNo;

    @Excel(name = "生命周期状态")
    private String lifecycleStatus;

    @Excel(name = "年份")
    private String yearName;

    @Excel(name = "一级指标")
    private String firstLevel;

    @Excel(name = "二级指标")
    private String secondLevel;

    @Excel(name = "具体指标")
    private String indicatorName;

    @Excel(name = "周期类型")
    private String periodType;

    @Excel(name = "数据类型")
    private String dataType;

    @Excel(name = "计分规则")
    private String scoringRule;

    @Excel(name = "算法类型")
    private String algorithmType;

    @Excel(name = "算法参数")
    private String algorithmParams;

    @Excel(name = "责任单位")
    private String responsibleUnit;

    @Excel(name = "地区编码")
    private String regionCode;

    @Excel(name = "地区名称")
    private String regionName;

    @Excel(name = "数据来源")
    private String dataSource;

    @Excel(name = "标签")
    private String tagNames;

    @Excel(name = "排序")
    private Integer sortOrder;

    @Excel(name = "状态")
    private String status;

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
