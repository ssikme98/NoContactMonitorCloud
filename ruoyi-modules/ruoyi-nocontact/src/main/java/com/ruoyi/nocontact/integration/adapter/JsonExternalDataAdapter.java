package com.ruoyi.nocontact.integration.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.integration.domain.ExternalDataRecord;
import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JsonExternalDataAdapter implements ExternalDataAdapter
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ExternalDataRecord> pull(ExternalIntegrationConfig config) throws Exception
    {
        if (StringUtils.startsWith(config.getEndpointUrl(), "mock://"))
        {
            return Collections.singletonList(mockRecord(config));
        }
        List<ExternalDataRecord> records = new ArrayList<ExternalDataRecord>();
        PullPage currentPage = pullPage(config, null, null);
        int pageGuard = 0;
        while (true)
        {
            records.addAll(currentPage.getRecords());
            if (!currentPage.hasNext())
            {
                return records;
            }
            if (++pageGuard > 50)
            {
                throw new ServiceException("外部系统分页次数过多");
            }
            currentPage = pullPage(config, currentPage.getNextPageToken(), currentPage.getNextPageNumber());
        }
    }

    public ExternalDataRecord pullRecord(ExternalIntegrationConfig config, Map<String, Object> map, String payload)
    {
        return toRecord(config, map, payload);
    }

    private PullPage pullPage(ExternalIntegrationConfig config, String nextPageToken, Integer nextPageNumber) throws Exception
    {
        String json = readJson(openValidatedConnection(config, buildPageUrl(config, nextPageToken, nextPageNumber), 0));
        Object parsed = objectMapper.readValue(json, Object.class);
        if (parsed instanceof List)
        {
            return new PullPage(toRecords(config, (List<?>) parsed), null, null);
        }
        if (!(parsed instanceof Map))
        {
            throw new ServiceException("外部系统响应必须是数组或包含records数组");
        }
        Map<String, Object> body = objectMapper.convertValue(parsed, new TypeReference<Map<String, Object>>() {});
        Object recordValue = body.get("records");
        if (!(recordValue instanceof List))
        {
            throw new ServiceException("外部系统响应必须是数组或包含records数组");
        }
        return new PullPage(toRecords(config, (List<?>) recordValue), text(body, "nextPageToken",
                text(body, "nextToken", "")), parseInteger(body.get("nextPage")));
    }

    private List<ExternalDataRecord> toRecords(ExternalIntegrationConfig config, List<?> rows) throws Exception
    {
        List<ExternalDataRecord> records = new ArrayList<ExternalDataRecord>();
        for (Object row : rows)
        {
            Map<String, Object> map = objectMapper.convertValue(row, new TypeReference<Map<String, Object>>() {});
            records.add(pullRecord(config, map, objectMapper.writeValueAsString(map)));
        }
        return records;
    }

    private URL buildPageUrl(ExternalIntegrationConfig config, String nextPageToken, Integer nextPageNumber) throws Exception
    {
        URL baseUrl = new URL(config.getEndpointUrl());
        if (StringUtils.isNotBlank(nextPageToken))
        {
            return appendQuery(baseUrl, "pageToken", nextPageToken);
        }
        if (nextPageNumber != null)
        {
            return appendQuery(baseUrl, "page", String.valueOf(nextPageNumber));
        }
        return baseUrl;
    }

    URL appendQuery(URL baseUrl, String key, String value) throws MalformedURLException
    {
        String delimiter = StringUtils.contains(baseUrl.toString(), "?") ? "&" : "?";
        return new URL(baseUrl.toString() + delimiter + key + "=" + encode(value));
    }

    void validateEndpoint(URL url) throws Exception
    {
        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol))
        {
            throw new ServiceException("外部系统地址只支持HTTP或HTTPS");
        }
        String host = url.getHost();
        if (StringUtils.isBlank(host))
        {
            throw new ServiceException("外部系统地址主机不能为空");
        }
        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress address : addresses)
        {
            if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                    || address.isLinkLocalAddress() || address.isSiteLocalAddress())
            {
                throw new ServiceException("不允许访问内网地址");
            }
        }
    }

    private HttpURLConnection openValidatedConnection(ExternalIntegrationConfig config, URL url, int redirectCount) throws Exception
    {
        if (redirectCount > 5)
        {
            throw new ServiceException("外部系统重定向次数过多");
        }
        validateEndpoint(url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        applyAuthHeader(connection, config);
        int status = connection.getResponseCode();
        if (status >= 300 && status < 400)
        {
            String location = connection.getHeaderField("Location");
            connection.disconnect();
            if (StringUtils.isBlank(location))
            {
                throw new ServiceException("外部系统重定向地址不能为空");
            }
            return openValidatedConnection(config, resolveRedirect(url, location), redirectCount + 1);
        }
        if (status < 200 || status >= 300)
        {
            connection.disconnect();
            throw new ServiceException("外部系统响应失败：" + status);
        }
        return connection;
    }

    URL resolveRedirect(URL currentUrl, String location) throws MalformedURLException
    {
        return new URL(currentUrl, location);
    }

    private void applyAuthHeader(HttpURLConnection connection, ExternalIntegrationConfig config)
    {
        if (StringUtils.isBlank(config.getAuthCredential()))
        {
            return;
        }
        String authType = StringUtils.defaultIfBlank(config.getAuthType(), "token");
        if ("token".equals(authType) || "oauth2".equals(authType))
        {
            connection.setRequestProperty("Authorization", "Bearer " + config.getAuthCredential());
            return;
        }
        if ("password".equals(authType) || "basic".equals(authType))
        {
            String encoded = Base64.getEncoder().encodeToString(config.getAuthCredential().getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encoded);
            return;
        }
        throw new ServiceException("当前认证方式暂不支持");
    }

    private String readJson(HttpURLConnection connection) throws Exception
    {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                body.append(line);
            }
        }
        finally
        {
            connection.disconnect();
        }
        return body.toString();
    }

    private ExternalDataRecord mockRecord(ExternalIntegrationConfig config)
    {
        ExternalDataRecord record = new ExternalDataRecord();
        record.setSourceSystem(StringUtils.defaultIfBlank(config.getPlatformName(), "mock-platform"));
        record.setExternalId("MOCK-" + config.getConfigId());
        record.setVersion("v1");
        record.setIndicatorId(1003L);
        record.setIndicatorCode("NC-1003");
        record.setIndicatorName("数字政务能力");
        record.setResponsibleUnitId(200L);
        record.setResponsibleUnitName("省数据局");
        record.setRegionCode("430100");
        record.setRegionName("长沙市");
        record.setPeriodType("month");
        record.setPeriodKey("2026-06");
        record.setRawValue("72");
        record.setValueDecimal(new BigDecimal("72"));
        record.setPayload("{\"mock\":true}");
        return record;
    }

    private ExternalDataRecord toRecord(ExternalIntegrationConfig config, Map<String, Object> map, String payload)
    {
        ExternalDataRecord record = new ExternalDataRecord();
        record.setSourceSystem(text(map, "sourceSystem", StringUtils.defaultIfBlank(config.getPlatformName(), config.getIntegrationName())));
        record.setExternalId(text(map, "externalId", null));
        record.setVersion(text(map, "version", text(map, "versionHash", "")));
        record.setIndicatorId(parseLong(map.get("indicatorId")));
        record.setIndicatorCode(text(map, "indicatorCode", ""));
        record.setIndicatorName(text(map, "indicatorName", ""));
        record.setResponsibleUnitId(parseLong(map.get("responsibleUnitId")));
        record.setResponsibleUnitName(text(map, "responsibleUnitName", ""));
        record.setRegionCode(text(map, "regionCode", ""));
        record.setRegionName(text(map, "regionName", ""));
        record.setPeriodType(text(map, "periodType", "month"));
        record.setPeriodKey(text(map, "periodKey", ""));
        record.setRawValue(text(map, "rawValue", text(map, "value", "")));
        record.setValueDecimal(parseDecimal(map.get("valueDecimal"), record.getRawValue()));
        record.setValueText(text(map, "valueText", ""));
        record.setPayload(payload);
        if (StringUtils.isBlank(record.getExternalId()))
        {
            throw new ServiceException("外部记录externalId不能为空");
        }
        return record;
    }

    private String text(Map<String, Object> map, String key, String defaultValue)
    {
        Object value = map.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private Long parseLong(Object value)
    {
        if (value == null || StringUtils.isBlank(String.valueOf(value)))
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private BigDecimal parseDecimal(Object value, String fallback)
    {
        String text = value == null ? fallback : String.valueOf(value);
        if (StringUtils.isBlank(text))
        {
            return null;
        }
        try
        {
            return new BigDecimal(text);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private Integer parseInteger(Object value)
    {
        if (value == null || StringUtils.isBlank(String.valueOf(value)))
        {
            return null;
        }
        try
        {
            return Integer.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private String encode(String value)
    {
        try
        {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        }
        catch (Exception e)
        {
            throw new ServiceException("分页参数编码失败");
        }
    }

    private static final class PullPage
    {
        private final List<ExternalDataRecord> records;
        private final String nextPageToken;
        private final Integer nextPageNumber;

        private PullPage(List<ExternalDataRecord> records, String nextPageToken, Integer nextPageNumber)
        {
            this.records = records;
            this.nextPageToken = StringUtils.defaultIfBlank(nextPageToken, null);
            this.nextPageNumber = nextPageNumber;
        }

        private List<ExternalDataRecord> getRecords()
        {
            return records;
        }

        private String getNextPageToken()
        {
            return nextPageToken;
        }

        private Integer getNextPageNumber()
        {
            return nextPageNumber;
        }

        private boolean hasNext()
        {
            return StringUtils.isNotBlank(nextPageToken) || nextPageNumber != null;
        }
    }
}
