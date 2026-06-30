package com.ruoyi.nocontact.integration.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;
import javax.validation.constraints.NotBlank;

/**
 * 外部系统对接配置对象 nc_external_integration_config
 */
public class ExternalIntegrationConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;
    private String integrationName;
    private String platformName;
    private String integrationType;
    private String endpointUrl;
    private String authType;
    private String authCredential;
    private String syncFrequency;
    private String syncMode;
    private String mappingRule;
    private String transformRule;
    private String retryPolicy;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSyncTime;
    private String lastSyncStatus;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    @NotBlank(message = "对接名称不能为空")
    public String getIntegrationName()
    {
        return integrationName;
    }

    public void setIntegrationName(String integrationName)
    {
        this.integrationName = integrationName;
    }

    public String getPlatformName()
    {
        return platformName;
    }

    public void setPlatformName(String platformName)
    {
        this.platformName = platformName;
    }

    public String getIntegrationType()
    {
        return integrationType;
    }

    public void setIntegrationType(String integrationType)
    {
        this.integrationType = integrationType;
    }

    public String getEndpointUrl()
    {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl)
    {
        this.endpointUrl = endpointUrl;
    }

    public String getAuthType()
    {
        return authType;
    }

    public void setAuthType(String authType)
    {
        this.authType = authType;
    }

    public String getAuthCredential()
    {
        return authCredential;
    }

    public void setAuthCredential(String authCredential)
    {
        this.authCredential = authCredential;
    }

    public String getSyncFrequency()
    {
        return syncFrequency;
    }

    public void setSyncFrequency(String syncFrequency)
    {
        this.syncFrequency = syncFrequency;
    }

    public String getSyncMode()
    {
        return syncMode;
    }

    public void setSyncMode(String syncMode)
    {
        this.syncMode = syncMode;
    }

    public String getMappingRule()
    {
        return mappingRule;
    }

    public void setMappingRule(String mappingRule)
    {
        this.mappingRule = mappingRule;
    }

    public String getTransformRule()
    {
        return transformRule;
    }

    public void setTransformRule(String transformRule)
    {
        this.transformRule = transformRule;
    }

    public String getRetryPolicy()
    {
        return retryPolicy;
    }

    public void setRetryPolicy(String retryPolicy)
    {
        this.retryPolicy = retryPolicy;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getLastSyncTime()
    {
        return lastSyncTime;
    }

    public void setLastSyncTime(Date lastSyncTime)
    {
        this.lastSyncTime = lastSyncTime;
    }

    public String getLastSyncStatus()
    {
        return lastSyncStatus;
    }

    public void setLastSyncStatus(String lastSyncStatus)
    {
        this.lastSyncStatus = lastSyncStatus;
    }
}
