package com.ruoyi.nocontact.integration.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 外部系统同步原始记录对象 nc_external_sync_payload
 */
public class ExternalSyncPayload extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long payloadId;
    private Long syncBatchId;
    private Long collectionBatchId;
    private Long collectionItemId;
    private String sourceSystem;
    private String externalId;
    private String versionHash;
    private String payloadContent;
    private String payloadStatus;
    private String convertMessage;
    private Date convertedTime;

    public Long getPayloadId()
    {
        return payloadId;
    }

    public void setPayloadId(Long payloadId)
    {
        this.payloadId = payloadId;
    }

    public Long getSyncBatchId()
    {
        return syncBatchId;
    }

    public void setSyncBatchId(Long syncBatchId)
    {
        this.syncBatchId = syncBatchId;
    }

    public Long getCollectionBatchId()
    {
        return collectionBatchId;
    }

    public void setCollectionBatchId(Long collectionBatchId)
    {
        this.collectionBatchId = collectionBatchId;
    }

    public Long getCollectionItemId()
    {
        return collectionItemId;
    }

    public void setCollectionItemId(Long collectionItemId)
    {
        this.collectionItemId = collectionItemId;
    }

    public String getSourceSystem()
    {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem)
    {
        this.sourceSystem = sourceSystem;
    }

    public String getExternalId()
    {
        return externalId;
    }

    public void setExternalId(String externalId)
    {
        this.externalId = externalId;
    }

    public String getVersionHash()
    {
        return versionHash;
    }

    public void setVersionHash(String versionHash)
    {
        this.versionHash = versionHash;
    }

    public String getPayloadContent()
    {
        return payloadContent;
    }

    public void setPayloadContent(String payloadContent)
    {
        this.payloadContent = payloadContent;
    }

    public String getPayloadStatus()
    {
        return payloadStatus;
    }

    public void setPayloadStatus(String payloadStatus)
    {
        this.payloadStatus = payloadStatus;
    }

    public String getConvertMessage()
    {
        return convertMessage;
    }

    public void setConvertMessage(String convertMessage)
    {
        this.convertMessage = convertMessage;
    }

    public Date getConvertedTime()
    {
        return convertedTime;
    }

    public void setConvertedTime(Date convertedTime)
    {
        this.convertedTime = convertedTime;
    }
}
