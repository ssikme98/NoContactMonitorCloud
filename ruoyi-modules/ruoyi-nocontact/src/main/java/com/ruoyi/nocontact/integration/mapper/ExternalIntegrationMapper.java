package com.ruoyi.nocontact.integration.mapper;

import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import com.ruoyi.nocontact.integration.domain.ExternalSyncLog;
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
}
