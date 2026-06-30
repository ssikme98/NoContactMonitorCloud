package com.ruoyi.nocontact.support.service.impl;

import com.ruoyi.common.core.web.domain.AjaxResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmapGeocodeServiceImplTest
{
    @Test
    void blankGeocodeKeyReturnsManualFallback()
    {
        AmapGeocodeServiceImpl service = new StubAmapGeocodeServiceImpl("", null);

        AjaxResult result = service.geocodeEnterprise("长沙市", "岳麓区麓谷大道 1 号");
        Map<?, ?> data = (Map<?, ?>) result.get(AjaxResult.DATA_TAG);

        assertTrue(result.isSuccess());
        assertEquals("未配置地理编码 Key，请手工维护坐标", result.get(AjaxResult.MSG_TAG));
        assertEquals(Boolean.TRUE, data.get("manualAllowed"));
    }

    @Test
    void successfulGeocodeReturnsLongitudeLatitude()
    {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("geocodes", java.util.Collections.singletonList(new HashMap<String, Object>() {{
            put("location", "112.938814,28.228209");
            put("formatted_address", "湖南省长沙市岳麓区麓谷大道1号");
        }}));
        AmapGeocodeServiceImpl service = new StubAmapGeocodeServiceImpl("server-key", payload);

        AjaxResult result = service.geocodeEnterprise("长沙市", "岳麓区麓谷大道 1 号");
        Map<?, ?> data = (Map<?, ?>) result.get(AjaxResult.DATA_TAG);

        assertTrue(result.isSuccess());
        assertEquals("112.938814", String.valueOf(data.get("longitude")));
        assertEquals("28.228209", String.valueOf(data.get("latitude")));
    }

    @Test
    void invalidPayloadReturnsManualFallback()
    {
        AmapGeocodeServiceImpl service = new StubAmapGeocodeServiceImpl("server-key", new HashMap<String, Object>());
        AjaxResult result = service.geocodeEnterprise("长沙市", "岳麓区麓谷大道 1 号");
        Map<?, ?> data = (Map<?, ?>) result.get(AjaxResult.DATA_TAG);

        assertTrue(result.isSuccess());
        assertEquals("地址解析失败，请手工维护坐标", result.get(AjaxResult.MSG_TAG));
        assertEquals(Boolean.TRUE, data.get("manualAllowed"));
    }

    private static class StubAmapGeocodeServiceImpl extends AmapGeocodeServiceImpl
    {
        private final String geocodeKey;
        private final Map<String, Object> payload;

        private StubAmapGeocodeServiceImpl(String geocodeKey, Map<String, Object> payload)
        {
            this.geocodeKey = geocodeKey;
            this.payload = payload;
        }

        @Override
        protected String loadGeocodeKey()
        {
            return geocodeKey;
        }

        @Override
        protected Map<String, Object> requestGeocode(String geocodeKey, String regionName, String address)
        {
            return payload;
        }
    }
}
