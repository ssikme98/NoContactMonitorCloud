package com.ruoyi.nocontact.integration.mapper;

import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import com.ruoyi.nocontact.integration.domain.ExternalSyncBatch;
import com.ruoyi.nocontact.integration.domain.ExternalSyncLog;
import com.ruoyi.nocontact.integration.domain.ExternalSyncPayload;
import java.util.List;

public interface ExternalIntegrationMapper
{
    List<ExternalIntegrationConfig> selectConfigList(ExternalIntegrationConfig config);

    ExternalIntegrationConfig selectConfigById(Long configId);

    int insertConfig(ExternalIntegrationConfig config);

    int updateConfig(ExternalIntegrationConfig config);

    int deleteConfigByIds(Long[] configIds);

    int insertLog(ExternalSyncLog log);

    List<ExternalSyncLog> selectLogList(ExternalSyncLog log);

    int insertSyncBatch(ExternalSyncBatch batch);

    int updateSyncBatch(ExternalSyncBatch batch);

    ExternalSyncBatch selectSyncBatchById(Long syncBatchId);

    List<ExternalSyncBatch> selectSyncBatchList(ExternalSyncBatch batch);

    int insertPayload(ExternalSyncPayload payload);

    int updatePayload(ExternalSyncPayload payload);

    ExternalSyncPayload selectPayloadByUnique(ExternalSyncPayload payload);

    List<ExternalSyncPayload> selectPayloadListByBatchId(Long syncBatchId);
}
