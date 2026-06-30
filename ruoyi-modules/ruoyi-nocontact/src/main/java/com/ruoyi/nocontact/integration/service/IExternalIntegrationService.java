package com.ruoyi.nocontact.integration.service;

import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import com.ruoyi.nocontact.integration.domain.ExternalSyncBatch;
import com.ruoyi.nocontact.integration.domain.ExternalSyncLog;
import java.util.List;

public interface IExternalIntegrationService
{
    public List<ExternalIntegrationConfig> selectConfigList(ExternalIntegrationConfig config);

    public ExternalIntegrationConfig selectConfigById(Long configId);

    public int insertConfig(ExternalIntegrationConfig config, String operName);

    public int updateConfig(ExternalIntegrationConfig config, String operName);

    public int deleteConfigByIds(Long[] configIds);

    public ExternalSyncLog testConnection(Long configId, String operName);

    public ExternalSyncBatch syncConfig(Long configId, String operName);

    public ExternalSyncBatch retryBatch(Long syncBatchId, String operName);

    public List<ExternalSyncLog> selectLogList(ExternalSyncLog log);

    public List<ExternalSyncBatch> selectSyncBatchList(ExternalSyncBatch batch);
}
