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

    @Excel(name = "年份")
    private String yearName;

    @Excel(name = "一级指标")
    private String firstLevel;

    @Excel(name = "二级指标")
    private String secondLevel;

    @Excel(name = "具体指标")
    private String indicatorName;

    @Excel(name = "计分规则")
    private String scoringRule;

    @Excel(name = "责任单位")
    private String responsibleUnit;

    @Excel(name = "数据来源")
    private String dataSource;

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

    public String getScoringRule()
    {
        return scoringRule;
    }

    public void setScoringRule(String scoringRule)
    {
        this.scoringRule = scoringRule;
    }

    public String getResponsibleUnit()
    {
        return responsibleUnit;
    }

    public void setResponsibleUnit(String responsibleUnit)
    {
        this.responsibleUnit = responsibleUnit;
    }

    public String getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(String dataSource)
    {
        this.dataSource = dataSource;
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
