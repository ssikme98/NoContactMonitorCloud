package com.ruoyi.nocontact.report.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 报告快照归档对象 nc_report_generation_snapshot
 */
public class ReportGenerationSnapshot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long snapshotId;
    private Long taskId;
    private String taskName;
    private String templateName;
    private String reportPeriod;
    private String reportScope;
    private String generatedWordFileName;
    private String generatedExcelFileName;
    private Integer templateVersion;
    private String generatedBy;
    private String snapshotContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generatedTime;

    public Long getSnapshotId()
    {
        return snapshotId;
    }

    public void setSnapshotId(Long snapshotId)
    {
        this.snapshotId = snapshotId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
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
}
