package com.ruoyi.nocontact.integration.adapter;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import java.net.URL;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonExternalDataAdapterTest
{
    @Test
    void rejectsPrivateOrLocalEndpointBeforeOpeningConnection() throws Exception
    {
        ExternalIntegrationConfig config = new ExternalIntegrationConfig();
        config.setEndpointUrl("http://127.0.0.1:9/internal");

        ServiceException exception = assertThrows(ServiceException.class, () -> new JsonExternalDataAdapter().pull(config));

        assertTrue(exception.getMessage().contains("不允许访问内网地址"));
    }

    @Test
    void rejectsRedirectTargetToPrivateAddress() throws Exception
    {
        JsonExternalDataAdapter adapter = new JsonExternalDataAdapter();
        URL redirectTarget = adapter.resolveRedirect(new URL("https://example.com/api"), "http://127.0.0.1:8080/internal");

        ServiceException exception = assertThrows(ServiceException.class, () -> adapter.validateEndpoint(redirectTarget));

        assertTrue(exception.getMessage().contains("不允许访问内网地址"));
    }
}
