package com.ruoyi.nocontact.report.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import javax.validation.constraints.NotBlank;

/**
 * 报告模板对象 nc_report_template
 */
public class ReportTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long templateId;
    private String templateName;
    private String reportType;
    private String sections;
    private String dataScope;
    private String styleConfig;
    private Integer versionNo;
    private String status;

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    @NotBlank(message = "模板名称不能为空")
    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

    public String getReportType()
    {
        return reportType;
    }

    public void setReportType(String reportType)
    {
        this.reportType = reportType;
    }

    public String getSections()
    {
        return sections;
    }

    public void setSections(String sections)
    {
        this.sections = sections;
    }

    public String getDataScope()
    {
        return dataScope;
    }

    public void setDataScope(String dataScope)
    {
        this.dataScope = dataScope;
    }

    public String getStyleConfig()
    {
        return styleConfig;
    }

    public void setStyleConfig(String styleConfig)
    {
        this.styleConfig = styleConfig;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
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
