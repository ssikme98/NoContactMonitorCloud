package com.ruoyi.nocontact.report.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 报告生成任务对象 nc_report_generation_task
 */
public class ReportGenerationTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;
    private String taskName;
    private Long templateId;
    private String templateName;
    private String reportPeriod;
    private String reportScope;
    private String generateMode;
    private String taskStatus;
    private String generatedFileName;
    private String generatedWordFileName;
    private String generatedExcelFileName;
    private Integer templateVersion;
    private String generatedBy;
    private String snapshotContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generatedTime;
    private String reportPeriodType;
    private String scopeType;
    private String scopeValue;
    private String scopeRegionName;
    private Long scopeIndicatorId;
    private String reportScopeLabel;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    @NotBlank(message = "任务名称不能为空")
    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    @NotNull(message = "模板不能为空")
    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

    @NotBlank(message = "报告周期不能为空")
    public String getReportPeriod()
    {
        return reportPeriod;
    }

    public void setReportPeriod(String reportPeriod)
    {
        this.reportPeriod = reportPeriod;
    }

    @NotBlank(message = "报告范围不能为空")
    public String getReportScope()
    {
        return reportScope;
    }

    public void setReportScope(String reportScope)
    {
        this.reportScope = reportScope;
    }

    public String getGenerateMode()
    {
        return generateMode;
    }

    public void setGenerateMode(String generateMode)
    {
        this.generateMode = generateMode;
    }

    public String getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public String getGeneratedFileName()
    {
        return generatedFileName;
    }

    public void setGeneratedFileName(String generatedFileName)
    {
        this.generatedFileName = generatedFileName;
    }

    public String getGeneratedWordFileName()
    {
        return generatedWordFileName;
    }

    public void setGeneratedWordFileName(String generatedWordFileName)
    {
        this.generatedWordFileName = generatedWordFileName;
    }

    public String getGeneratedExcelFileName()
    {
        return generatedExcelFileName;
    }

    public void setGeneratedExcelFileName(String generatedExcelFileName)
    {
        this.generatedExcelFileName = generatedExcelFileName;
    }

    public Integer getTemplateVersion()
    {
        return templateVersion;
    }

    public void setTemplateVersion(Integer templateVersion)
    {
        this.templateVersion = templateVersion;
    }

    public String getGeneratedBy()
    {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy)
    {
        this.generatedBy = generatedBy;
    }

    public String getSnapshotContent()
    {
        return snapshotContent;
    }

    public void setSnapshotContent(String snapshotContent)
    {
        this.snapshotContent = snapshotContent;
    }

    public Date getGeneratedTime()
    {
        return generatedTime;
    }

    public void setGeneratedTime(Date generatedTime)
    {
        this.generatedTime = generatedTime;
    }

    public String getReportPeriodType()
    {
        return reportPeriodType;
    }

    public void setReportPeriodType(String reportPeriodType)
    {
        this.reportPeriodType = reportPeriodType;
    }

    public String getScopeType()
    {
        return scopeType;
    }

    public void setScopeType(String scopeType)
    {
        this.scopeType = scopeType;
    }

    public String getScopeValue()
    {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue)
    {
        this.scopeValue = scopeValue;
    }

    public String getScopeRegionName()
    {
        return scopeRegionName;
    }

    public void setScopeRegionName(String scopeRegionName)
    {
        this.scopeRegionName = scopeRegionName;
    }

    public Long getScopeIndicatorId()
    {
        return scopeIndicatorId;
    }

    public void setScopeIndicatorId(Long scopeIndicatorId)
    {
        this.scopeIndicatorId = scopeIndicatorId;
    }

    public String getReportScopeLabel()
    {
        return reportScopeLabel;
    }

    public void setReportScopeLabel(String reportScopeLabel)
    {
        this.reportScopeLabel = reportScopeLabel;
    }
}
