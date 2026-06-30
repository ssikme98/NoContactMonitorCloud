package com.ruoyi.nocontact.integration.adapter;

import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExternalDataAdapterRegistry
{
    @Autowired
    private JsonExternalDataAdapter jsonExternalDataAdapter;

    public ExternalDataAdapter resolve(ExternalIntegrationConfig config)
    {
        return jsonExternalDataAdapter;
    }
}
