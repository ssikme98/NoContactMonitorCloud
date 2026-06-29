package com.ruoyi.nocontact.report.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;
import javax.validation.constraints.NotBlank;

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
    private String snapshotContent;
    private Date generatedTime;

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

    public String getReportPeriod()
    {
        return reportPeriod;
    }

    public void setReportPeriod(String reportPeriod)
    {
        this.reportPeriod = reportPeriod;
    }

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
}
