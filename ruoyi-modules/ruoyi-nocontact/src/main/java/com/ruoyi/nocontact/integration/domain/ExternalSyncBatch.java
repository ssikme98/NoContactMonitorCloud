package com.ruoyi.nocontact.integration.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 外部系统同步批次对象 nc_external_sync_batch
 */
public class ExternalSyncBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long syncBatchId;
    private Long configId;
    private String integrationName;
    private String sourceSystem;
    private String batchStatus;
    private Long successCount;
    private Long failureCount;
    private Long skippedCount;
    private Long retryCount;
    private String requestSummary;
    private String responseSummary;
    private String errorMessage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishedTime;

    public Long getSyncBatchId()
    {
        return syncBatchId;
    }

    public void setSyncBatchId(Long syncBatchId)
    {
        this.syncBatchId = syncBatchId;
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

    public String getSourceSystem()
    {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem)
    {
        this.sourceSystem = sourceSystem;
    }

    public String getBatchStatus()
    {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus)
    {
        this.batchStatus = batchStatus;
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

    public Long getSkippedCount()
    {
        return skippedCount;
    }

    public void setSkippedCount(Long skippedCount)
    {
        this.skippedCount = skippedCount;
    }

    public Long getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(Long retryCount)
    {
        this.retryCount = retryCount;
    }

    public String getRequestSummary()
    {
        return requestSummary;
    }

    public void setRequestSummary(String requestSummary)
    {
        this.requestSummary = requestSummary;
    }

    public String getResponseSummary()
    {
        return responseSummary;
    }

    public void setResponseSummary(String responseSummary)
    {
        this.responseSummary = responseSummary;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public Date getStartedTime()
    {
        return startedTime;
    }

    public void setStartedTime(Date startedTime)
    {
        this.startedTime = startedTime;
    }

    public Date getFinishedTime()
    {
        return finishedTime;
    }

    public void setFinishedTime(Date finishedTime)
    {
        this.finishedTime = finishedTime;
    }
}
