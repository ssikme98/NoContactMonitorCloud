package com.ruoyi.nocontact.support.domain;

/**
 * 跨模块公共设置视图
 */
public class SupportPublicSettings
{
    private String amapFrontendKey;
    private String amapSecurityJsCode;
    private String fileBasePath;
    private String warningPushEnabled;
    private String reportDefaultPeriod;
    private String integrationGlobalEnabled;

    public String getAmapFrontendKey()
    {
        return amapFrontendKey;
    }

    public void setAmapFrontendKey(String amapFrontendKey)
    {
        this.amapFrontendKey = amapFrontendKey;
    }

    public String getAmapSecurityJsCode()
    {
        return amapSecurityJsCode;
    }

    public void setAmapSecurityJsCode(String amapSecurityJsCode)
    {
        this.amapSecurityJsCode = amapSecurityJsCode;
    }

    public String getFileBasePath()
    {
        return fileBasePath;
    }

    public void setFileBasePath(String fileBasePath)
    {
        this.fileBasePath = fileBasePath;
    }

    public String getWarningPushEnabled()
    {
        return warningPushEnabled;
    }

    public void setWarningPushEnabled(String warningPushEnabled)
    {
        this.warningPushEnabled = warningPushEnabled;
    }

    public String getReportDefaultPeriod()
    {
        return reportDefaultPeriod;
    }

    public void setReportDefaultPeriod(String reportDefaultPeriod)
    {
        this.reportDefaultPeriod = reportDefaultPeriod;
    }

    public String getIntegrationGlobalEnabled()
    {
        return integrationGlobalEnabled;
    }

    public void setIntegrationGlobalEnabled(String integrationGlobalEnabled)
    {
        this.integrationGlobalEnabled = integrationGlobalEnabled;
    }
}
