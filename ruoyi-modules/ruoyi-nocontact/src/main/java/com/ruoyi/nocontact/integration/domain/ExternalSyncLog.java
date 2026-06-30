package com.ruoyi.nocontact.integration.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 外部系统同步日志对象 nc_external_sync_log
 */
public class ExternalSyncLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long configId;
    private String integrationName;
    private String requestSummary;
    private String responseStatus;
    private Long successCount;
    private Long failureCount;
    private Long durationMs;
    private String errorMessage;
    private Integer retryCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date syncTime;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    public String getIntegrationName()
    {
        return integrationName;
    }

    public void setIntegrationName(String integrationName)
    {
        this.integrationName = integrationName;
    }

    public String getRequestSummary()
    {
        return requestSummary;
    }

    public void setRequestSummary(String requestSummary)
    {
        this.requestSummary = requestSummary;
    }

    public String getResponseStatus()
    {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus)
    {
        this.responseStatus = responseStatus;
    }

    public Long getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(Long successCount)
    {
        this.successCount = successCount;
    }

    public Long getFailureCount()
    {
        return failureCount;
    }

    public void setFailureCount(Long failureCount)
    {
        this.failureCount = failureCount;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public void setDurationMs(Long durationMs)
    {
        this.durationMs = durationMs;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount)
    {
        this.retryCount = retryCount;
    }

    public Date getSyncTime()
    {
        return syncTime;
    }

    public void setSyncTime(Date syncTime)
    {
        this.syncTime = syncTime;
    }
}
