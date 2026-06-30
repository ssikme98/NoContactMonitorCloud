package com.ruoyi.nocontact.support.service.impl;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.nocontact.support.service.IAmapGeocodeService;
import com.ruoyi.system.api.RemoteConfigService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.R;

@Service
public class AmapGeocodeServiceImpl implements IAmapGeocodeService
{
    @Autowired
    private RemoteConfigService remoteConfigService;

    @Override
    public AjaxResult geocodeEnterprise(String regionName, String address)
    {
        String geocodeKey = loadGeocodeKey();
        if (StringUtils.isBlank(geocodeKey))
        {
            return manualFallback("未配置地理编码 Key，请手工维护坐标", regionName, address);
        }

        Map<String, Object> payload;
        try
        {
            payload = requestGeocode(geocodeKey, regionName, address);
        }
        catch (RuntimeException ex)
        {
            return manualFallback("地址解析失败，请手工维护坐标", regionName, address);
        }

        if (payload == null || payload.get("geocodes") == null)
        {
            return manualFallback("地址解析失败，请手工维护坐标", regionName, address);
        }

        List<?> geocodes = (List<?>) payload.get("geocodes");
        if (geocodes.isEmpty())
        {
            return manualFallback("地址解析失败，请手工维护坐标", regionName, address);
        }

        Map<?, ?> first = (Map<?, ?>) geocodes.get(0);
        String location = String.valueOf(first.get("location"));
        if (StringUtils.isBlank(location) || !location.contains(","))
        {
            return manualFallback("地址解析失败，请手工维护坐标", regionName, address);
        }

        String[] parts = location.split(",");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("longitude", parts[0]);
        result.put("latitude", parts[1]);
        result.put("formattedAddress", first.get("formatted_address"));
        result.put("manualAllowed", Boolean.TRUE);
        return AjaxResult.success("地址解析成功", result);
    }

    protected String loadGeocodeKey()
    {
        R<String> response = remoteConfigService.getConfigKey("nocontact.amap.geocodeKey", SecurityConstants.INNER);
        return response == null || !R.isSuccess(response) ? "" : response.getData();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> requestGeocode(String geocodeKey, String regionName, String address)
    {
        try
        {
            String url = "https://restapi.amap.com/v3/geocode/geo?key="
                    + URLEncoder.encode(geocodeKey, "UTF-8")
                    + "&city=" + URLEncoder.encode(StringUtils.defaultIfBlank(regionName, ""), "UTF-8")
                    + "&address=" + URLEncoder.encode(StringUtils.defaultIfBlank(address, ""), "UTF-8");
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }
            reader.close();
            return JSON.parseObject(builder.toString(), Map.class);
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("地理编码调用失败：" + ex.getMessage(), ex);
        }
    }

    private AjaxResult manualFallback(String message, String regionName, String address)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("manualAllowed", Boolean.TRUE);
        result.put("address", address);
        result.put("regionName", regionName);
        return AjaxResult.success(message, result);
    }
}
