package com.ruoyi.nocontact.integration.adapter;

import com.ruoyi.nocontact.integration.domain.ExternalDataRecord;
import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import java.util.List;

public interface ExternalDataAdapter
{
    public List<ExternalDataRecord> pull(ExternalIntegrationConfig config) throws Exception;

    default void acknowledge(ExternalIntegrationConfig config, List<ExternalDataRecord> successRecords) throws Exception
    {
    }
}
