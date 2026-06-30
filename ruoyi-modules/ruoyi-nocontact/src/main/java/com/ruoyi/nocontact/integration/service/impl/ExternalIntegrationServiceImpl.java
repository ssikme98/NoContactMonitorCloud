package com.ruoyi.nocontact.integration.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.integration.adapter.ExternalDataAdapter;
import com.ruoyi.nocontact.integration.adapter.ExternalDataAdapterRegistry;
import com.ruoyi.nocontact.integration.domain.ExternalDataRecord;
import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import com.ruoyi.nocontact.integration.domain.ExternalSyncBatch;
import com.ruoyi.nocontact.integration.domain.ExternalSyncLog;
import com.ruoyi.nocontact.integration.domain.ExternalSyncPayload;
import com.ruoyi.nocontact.integration.mapper.ExternalIntegrationMapper;
import com.ruoyi.nocontact.integration.service.IExternalIntegrationService;
import com.ruoyi.nocontact.integration.adapter.JsonExternalDataAdapter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalIntegrationServiceImpl implements IExternalIntegrationService
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ExternalIntegrationMapper integrationMapper;

    @Autowired
    private FusionCollectionBatchMapper collectionBatchMapper;

    @Autowired
    private ExternalDataAdapterRegistry adapterRegistry;

    @Override
    public List<ExternalIntegrationConfig> selectConfigList(ExternalIntegrationConfig config)
    {
        List<ExternalIntegrationConfig> list = integrationMapper.selectConfigList(config);
        for (ExternalIntegrationConfig item : list)
        {
            hideCredential(item);
        }
        return list;
    }

    @Override
    public ExternalIntegrationConfig selectConfigById(Long configId)
    {
        ExternalIntegrationConfig config = integrationMapper.selectConfigById(configId);
        hideCredential(config);
        return config;
    }

    @Override
    public int insertConfig(ExternalIntegrationConfig config, String operName)
    {
        config.setCreateBy(operName);
        config.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(config.getStatus()))
        {
            config.setStatus("0");
        }
        return integrationMapper.insertConfig(config);
    }

    @Override
    public int updateConfig(ExternalIntegrationConfig config, String operName)
    {
        config.setUpdateBy(operName);
        config.setUpdateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(config.getAuthCredential()))
        {
            config.setAuthCredential(null);
        }
        return integrationMapper.updateConfig(config);
    }

    @Override
    public int deleteConfigByIds(Long[] configIds)
    {
        return integrationMapper.deleteConfigByIds(configIds);
    }

    @Override
    public ExternalSyncLog testConnection(Long configId, String operName)
    {
        ExternalIntegrationConfig config = requireConfig(configId);
        long start = System.currentTimeMillis();
        ExternalSyncLog log;
        try
        {
            ExternalDataAdapter adapter = adapterRegistry.resolve(config);
            List<ExternalDataRecord> records = adapter.pull(config);
            log = newLog(config, safeEndpoint(config), "success", records == null ? 0L : (long) records.size(), 0L,
                    System.currentTimeMillis() - start, null, operName, 0L);
        }
        catch (Exception e)
        {
            log = newLog(config, safeEndpoint(config), "failed", 0L, 1L, System.currentTimeMillis() - start,
                    e.getMessage(), operName, 0L);
        }
        integrationMapper.insertLog(log);
        return log;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExternalSyncBatch syncConfig(Long configId, String operName)
    {
        ExternalIntegrationConfig config = requireConfig(configId);
        requireEnabled(config);
        ExternalSyncBatch batch = startBatch(config, 0L, operName);
        long start = System.currentTimeMillis();
        try
        {
            ExternalDataAdapter adapter = adapterRegistry.resolve(config);
            List<ExternalDataRecord> records = adapter.pull(config);
            convertRecords(config, batch, records, operName);
            if (nullToZero(batch.getFailureCount()) == 0)
            {
                adapter.acknowledge(config, records);
            }
            summarizeBatch(batch);
        }
        catch (Exception e)
        {
            batch.setBatchStatus("failed");
            batch.setFailureCount(1L);
            batch.setErrorMessage(sanitize(config, e.getMessage()));
        }
        finishBatch(config, batch, operName, System.currentTimeMillis() - start);
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExternalSyncBatch retryBatch(Long syncBatchId, String operName)
    {
        ExternalSyncBatch failed = integrationMapper.selectSyncBatchById(syncBatchId);
        if (failed == null)
        {
            throw new ServiceException("同步批次不存在");
        }
        if (!canRetry(failed.getBatchStatus()))
        {
            throw new ServiceException("只有失败或部分失败批次允许重试");
        }
        ExternalIntegrationConfig config = requireConfig(failed.getConfigId());
        requireEnabled(config);
        ExternalSyncBatch retry = startBatch(config, nullToZero(failed.getRetryCount()) + 1, operName);
        long start = System.currentTimeMillis();
        try
        {
            List<ExternalSyncPayload> payloads = integrationMapper.selectPayloadListByBatchId(failed.getSyncBatchId());
            if (payloads == null || payloads.isEmpty())
            {
                rerunSourcePull(config, retry, operName);
            }
            else
            {
                int retriedCount = retryFailedPayloads(config, payloads, retry, operName);
                if (retriedCount == 0)
                {
                    throw new ServiceException("原批次没有可重试的失败记录");
                }
            }
            summarizeBatch(retry);
        }
        catch (Exception e)
        {
            retry.setBatchStatus("failed");
            retry.setErrorMessage(sanitize(config, e.getMessage()));
        }
        retry.setRetryCount(nullToZero(failed.getRetryCount()) + 1);
        finishBatch(config, retry, operName, System.currentTimeMillis() - start);
        return retry;
    }

    @Override
    public List<ExternalSyncLog> selectLogList(ExternalSyncLog log)
    {
        return integrationMapper.selectLogList(log);
    }

    @Override
    public List<ExternalSyncBatch> selectSyncBatchList(ExternalSyncBatch batch)
    {
        return integrationMapper.selectSyncBatchList(batch);
    }

    private void convertRecords(ExternalIntegrationConfig config, ExternalSyncBatch batch, List<ExternalDataRecord> records, String operName)
    {
        batch.setSuccessCount(0L);
        batch.setFailureCount(0L);
        batch.setSkippedCount(0L);
        if (records == null)
        {
            return;
        }
        for (ExternalDataRecord record : records)
        {
            ExternalSyncPayload candidate = buildPayload(batch, record, operName);
            ExternalSyncPayload existing = integrationMapper.selectPayloadByUnique(candidate);
            if (existing != null && sameBatch(existing, batch.getSyncBatchId()))
            {
                batch.setSkippedCount(batch.getSkippedCount() + 1);
                continue;
            }
            ExternalSyncPayload payload = preparePayloadAttempt(batch, record, candidate, existing, operName);
            if (payload == null)
            {
                batch.setSkippedCount(batch.getSkippedCount() + 1);
                continue;
            }
            try
            {
                validateRecord(record);
                convertPayload(config, payload, record, operName);
                batch.setSuccessCount(batch.getSuccessCount() + 1);
            }
            catch (Exception e)
            {
                payload.setPayloadStatus("failed");
                payload.setConvertMessage(sanitize(config, e.getMessage()));
                payload.setUpdateBy(operName);
                payload.setUpdateTime(DateUtils.getNowDate());
                integrationMapper.updatePayload(payload);
                batch.setFailureCount(batch.getFailureCount() + 1);
            }
        }
    }

    private int retryFailedPayloads(ExternalIntegrationConfig config, List<ExternalSyncPayload> payloads, ExternalSyncBatch retryBatch,
            String operName) throws Exception
    {
        retryBatch.setSuccessCount(0L);
        retryBatch.setFailureCount(0L);
        retryBatch.setSkippedCount(0L);
        int retriedCount = 0;
        for (ExternalSyncPayload payload : payloads)
        {
            if (!"failed".equals(payload.getPayloadStatus()))
            {
                retryBatch.setSkippedCount(retryBatch.getSkippedCount() + 1);
                continue;
            }
            retriedCount++;
            try
            {
                ExternalDataRecord record = parsePayloadRecord(config, payload);
                ExternalSyncPayload retryPayload = reuseFailedPayload(retryBatch, payload, operName);
                integrationMapper.updatePayload(retryPayload);
                validateRecord(record);
                convertPayload(config, retryPayload, record, operName);
                retryBatch.setSuccessCount(retryBatch.getSuccessCount() + 1);
            }
            catch (Exception e)
            {
                markPayloadFailed(payload.getPayloadId(), config, operName, e);
                retryBatch.setFailureCount(retryBatch.getFailureCount() + 1);
                retryBatch.setErrorMessage(sanitize(config, e.getMessage()));
            }
        }
        return retriedCount;
    }

    private ExternalSyncPayload reuseFailedPayload(ExternalSyncBatch retryBatch, ExternalSyncPayload payload, String operName)
    {
        payload.setSyncBatchId(retryBatch.getSyncBatchId());
        payload.setPayloadStatus("pending");
        payload.setConvertMessage("");
        payload.setUpdateBy(operName);
        payload.setUpdateTime(DateUtils.getNowDate());
        payload.setRemark("retry-batch:" + retryBatch.getSyncBatchId());
        return payload;
    }

    private void convertPayload(ExternalIntegrationConfig config, ExternalSyncPayload payload, ExternalDataRecord record, String operName)
    {
        FusionCollectionBatch collectionBatch = buildCollectionBatch(config, record, operName);
        try
        {
            collectionBatchMapper.insertBatch(collectionBatch);
            FusionCollectionItem item = buildCollectionItem(collectionBatch, record, operName);
            collectionBatchMapper.insertItem(item);
            payload.setCollectionBatchId(collectionBatch.getBatchId());
            payload.setCollectionItemId(item.getItemId());
            payload.setPayloadStatus("converted");
            payload.setConvertMessage("已转为标准采集模型");
            payload.setConvertedTime(DateUtils.getNowDate());
            payload.setUpdateBy(operName);
            payload.setUpdateTime(DateUtils.getNowDate());
            integrationMapper.updatePayload(payload);
        }
        catch (RuntimeException e)
        {
            if (collectionBatch.getBatchId() != null)
            {
                collectionBatchMapper.deleteBatchById(collectionBatch.getBatchId());
            }
            throw e;
        }
    }

    private ExternalDataRecord parsePayloadRecord(ExternalIntegrationConfig config, ExternalSyncPayload payload) throws Exception
    {
        ExternalDataAdapter adapter = adapterRegistry.resolve(config);
        if (!(adapter instanceof JsonExternalDataAdapter))
        {
            throw new ServiceException("当前对接类型暂不支持失败记录重试");
        }
        Map<String, Object> map = objectMapper.readValue(payload.getPayloadContent(), new TypeReference<Map<String, Object>>() {});
        return ((JsonExternalDataAdapter) adapter).pullRecord(config, map, payload.getPayloadContent());
    }

    private ExternalSyncPayload createPayloadAttempt(ExternalSyncBatch batch, String sourceSystem, String externalId,
            String versionHash, String payloadContent, String operName, Long previousPayloadId)
    {
        ExternalSyncPayload payload = new ExternalSyncPayload();
        payload.setSyncBatchId(batch.getSyncBatchId());
        payload.setSourceSystem(sourceSystem);
        payload.setExternalId(externalId);
        payload.setVersionHash(versionHash);
        payload.setPayloadContent(payloadContent);
        payload.setPayloadStatus("pending");
        payload.setCreateBy(operName);
        payload.setCreateTime(DateUtils.getNowDate());
        payload.setRemark(previousPayloadId == null ? null : "retry-from-payload:" + previousPayloadId);
        return payload;
    }

    private void summarizeBatch(ExternalSyncBatch batch)
    {
        if (nullToZero(batch.getFailureCount()) > 0)
        {
            batch.setBatchStatus(nullToZero(batch.getSuccessCount()) > 0 ? "partial_failed" : "failed");
        }
        else
        {
            batch.setBatchStatus("success");
        }
        batch.setResponseSummary("拉取" + (nullToZero(batch.getSuccessCount()) + nullToZero(batch.getFailureCount()) + nullToZero(batch.getSkippedCount()))
                + "条，新增" + batch.getSuccessCount() + "条，失败" + batch.getFailureCount() + "条，跳过" + batch.getSkippedCount() + "条");
    }

    private boolean canRetry(String batchStatus)
    {
        return "failed".equals(batchStatus) || "partial_failed".equals(batchStatus);
    }

    private void validateRecord(ExternalDataRecord record)
    {
        if (record.getIndicatorId() == null || StringUtils.isBlank(record.getIndicatorCode()))
        {
            throw new ServiceException("指标不能为空");
        }
        if (record.getResponsibleUnitId() == null)
        {
            throw new ServiceException("责任单位不能为空");
        }
        if (StringUtils.isBlank(record.getPeriodType()) || StringUtils.isBlank(record.getPeriodKey()))
        {
            throw new ServiceException("业务周期不能为空");
        }
        if (StringUtils.isBlank(record.getRawValue()) && record.getValueDecimal() == null && StringUtils.isBlank(record.getValueText()))
        {
            throw new ServiceException("指标值不能为空");
        }
    }

    private ExternalSyncBatch startBatch(ExternalIntegrationConfig config, Long retryCount, String operName)
    {
        ExternalSyncBatch batch = new ExternalSyncBatch();
        batch.setConfigId(config.getConfigId());
        batch.setIntegrationName(config.getIntegrationName());
        batch.setSourceSystem(StringUtils.defaultIfBlank(config.getPlatformName(), config.getIntegrationName()));
        batch.setBatchStatus("running");
        batch.setSuccessCount(0L);
        batch.setFailureCount(0L);
        batch.setSkippedCount(0L);
        batch.setRetryCount(retryCount);
        batch.setRequestSummary(safeEndpoint(config));
        batch.setStartedTime(DateUtils.getNowDate());
        batch.setCreateBy(operName);
        batch.setCreateTime(DateUtils.getNowDate());
        integrationMapper.insertSyncBatch(batch);
        return batch;
    }

    private void rerunSourcePull(ExternalIntegrationConfig config, ExternalSyncBatch retryBatch, String operName) throws Exception
    {
        ExternalDataAdapter adapter = adapterRegistry.resolve(config);
        List<ExternalDataRecord> records = adapter.pull(config);
        convertRecords(config, retryBatch, records, operName);
        if (nullToZero(retryBatch.getFailureCount()) == 0)
        {
            adapter.acknowledge(config, records);
        }
    }

    private void finishBatch(ExternalIntegrationConfig config, ExternalSyncBatch batch, String operName, long durationMs)
    {
        batch.setFinishedTime(DateUtils.getNowDate());
        batch.setUpdateBy(operName);
        batch.setUpdateTime(DateUtils.getNowDate());
        integrationMapper.updateSyncBatch(batch);
        integrationMapper.insertLog(newLog(config, batch.getRequestSummary(), batch.getBatchStatus(), batch.getSuccessCount(),
                batch.getFailureCount(), durationMs, batch.getErrorMessage(), operName, batch.getRetryCount()));
        config.setLastSyncTime(batch.getFinishedTime());
        config.setLastSyncStatus(batch.getBatchStatus());
        config.setUpdateBy(operName);
        config.setUpdateTime(DateUtils.getNowDate());
        integrationMapper.updateConfig(config);
    }

    private ExternalSyncPayload buildPayload(ExternalSyncBatch batch, ExternalDataRecord record, String operName)
    {
        ExternalSyncPayload payload = new ExternalSyncPayload();
        payload.setSyncBatchId(batch.getSyncBatchId());
        payload.setSourceSystem(record.getSourceSystem());
        payload.setExternalId(record.getExternalId());
        payload.setVersionHash(resolveVersionHash(record));
        payload.setPayloadContent(record.getPayload());
        payload.setPayloadStatus("pending");
        payload.setCreateBy(operName);
        payload.setCreateTime(DateUtils.getNowDate());
        return payload;
    }

    private FusionCollectionBatch buildCollectionBatch(ExternalIntegrationConfig config, ExternalDataRecord record, String operName)
    {
        FusionCollectionBatch batch = new FusionCollectionBatch();
        batch.setBatchName(config.getIntegrationName() + "-" + record.getExternalId());
        batch.setSourceType("api");
        batch.setSourceName(config.getIntegrationName());
        batch.setSourceRecordId(record.getExternalId());
        batch.setDeptId(record.getResponsibleUnitId());
        batch.setResponsibleUnitId(record.getResponsibleUnitId());
        batch.setResponsibleUnitName(record.getResponsibleUnitName());
        batch.setRegionCode(record.getRegionCode());
        batch.setRegionName(record.getRegionName());
        batch.setPeriodType(record.getPeriodType());
        batch.setPeriodKey(record.getPeriodKey());
        fillPeriodParts(batch);
        batch.setBatchStatus("pending_audit");
        batch.setItemCount(1);
        batch.setCreateBy(operName);
        batch.setCreateTime(DateUtils.getNowDate());
        return batch;
    }

    private FusionCollectionItem buildCollectionItem(FusionCollectionBatch batch, ExternalDataRecord record, String operName)
    {
        FusionCollectionItem item = new FusionCollectionItem();
        item.setBatchId(batch.getBatchId());
        item.setDeptId(batch.getDeptId());
        item.setResponsibleUnitId(batch.getResponsibleUnitId());
        item.setResponsibleUnitName(batch.getResponsibleUnitName());
        item.setRegionCode(batch.getRegionCode());
        item.setRegionName(batch.getRegionName());
        item.setPeriodKey(batch.getPeriodKey());
        item.setIndicatorId(record.getIndicatorId());
        item.setIndicatorName(record.getIndicatorName());
        item.setIndicatorCode(record.getIndicatorCode());
        item.setRawValue(record.getRawValue());
        item.setValueDecimal(record.getValueDecimal());
        item.setValueText(record.getValueText());
        item.setValidationStatus("valid");
        item.setIsCurrent("0");
        item.setCreateBy(operName);
        item.setCreateTime(DateUtils.getNowDate());
        return item;
    }

    private ExternalIntegrationConfig requireConfig(Long configId)
    {
        ExternalIntegrationConfig config = integrationMapper.selectConfigById(configId);
        if (config == null)
        {
            throw new ServiceException("对接配置不存在");
        }
        if (StringUtils.isBlank(config.getEndpointUrl()))
        {
            throw new ServiceException("接口地址不能为空");
        }
        return config;
    }

    private void requireEnabled(ExternalIntegrationConfig config)
    {
        if (!"0".equals(StringUtils.defaultIfBlank(config.getStatus(), "0")))
        {
            throw new ServiceException("停用状态的对接配置不允许执行同步或重试");
        }
    }

    private ExternalSyncPayload preparePayloadAttempt(ExternalSyncBatch batch, ExternalDataRecord record, ExternalSyncPayload candidate,
            ExternalSyncPayload existing, String operName)
    {
        if (existing != null && !"failed".equals(existing.getPayloadStatus()))
        {
            return null;
        }
        if (existing != null)
        {
            existing.setSyncBatchId(batch.getSyncBatchId());
            existing.setPayloadContent(record.getPayload());
            existing.setPayloadStatus("pending");
            existing.setConvertMessage("");
            existing.setUpdateBy(operName);
            existing.setUpdateTime(DateUtils.getNowDate());
            integrationMapper.updatePayload(existing);
            return existing;
        }
        ExternalSyncPayload payload = createPayloadAttempt(batch, record.getSourceSystem(), record.getExternalId(),
                candidate.getVersionHash(), record.getPayload(), operName, null);
        int inserted = integrationMapper.insertPayload(payload);
        if (inserted > 0)
        {
            return payload;
        }
        ExternalSyncPayload concurrent = integrationMapper.selectPayloadByUnique(candidate);
        if (concurrent == null)
        {
            throw new ServiceException("同步幂等记录写入失败");
        }
        if (!"failed".equals(concurrent.getPayloadStatus()))
        {
            return null;
        }
        concurrent.setSyncBatchId(batch.getSyncBatchId());
        concurrent.setPayloadContent(record.getPayload());
        concurrent.setPayloadStatus("pending");
        concurrent.setConvertMessage("");
        concurrent.setUpdateBy(operName);
        concurrent.setUpdateTime(DateUtils.getNowDate());
        integrationMapper.updatePayload(concurrent);
        return concurrent;
    }

    private void markPayloadFailed(Long payloadId, ExternalIntegrationConfig config, String operName, Exception e)
    {
        ExternalSyncPayload failedAttempt = new ExternalSyncPayload();
        failedAttempt.setPayloadId(payloadId);
        failedAttempt.setPayloadStatus("failed");
        failedAttempt.setConvertMessage(sanitize(config, e.getMessage()));
        failedAttempt.setUpdateBy(operName);
        failedAttempt.setUpdateTime(DateUtils.getNowDate());
        integrationMapper.updatePayload(failedAttempt);
    }

    private ExternalSyncLog newLog(ExternalIntegrationConfig config, String summary, String status, Long success, Long failure,
            long durationMs, String error, String operName, Long retryCount)
    {
        ExternalSyncLog log = new ExternalSyncLog();
        log.setConfigId(config.getConfigId());
        log.setIntegrationName(config.getIntegrationName());
        log.setRequestSummary(sanitize(config, summary));
        log.setResponseStatus(status);
        log.setSuccessCount(success);
        log.setFailureCount(failure);
        log.setDurationMs(durationMs);
        log.setErrorMessage(sanitize(config, error));
        log.setRetryCount(retryCount == null ? 0 : retryCount.intValue());
        log.setSyncTime(DateUtils.getNowDate());
        log.setCreateBy(operName);
        log.setCreateTime(DateUtils.getNowDate());
        return log;
    }

    private String safeEndpoint(ExternalIntegrationConfig config)
    {
        return sanitize(config, config.getEndpointUrl());
    }

    private String sanitize(ExternalIntegrationConfig config, String text)
    {
        if (text == null)
        {
            return "";
        }
        String result = text;
        if (StringUtils.isNotBlank(config.getAuthCredential()))
        {
            result = result.replace(config.getAuthCredential(), "***");
        }
        return result.replaceAll("(?i)(token=)[^&\\s]+", "$1***");
    }

    private Long nullToZero(Long value)
    {
        return value == null ? 0L : value;
    }

    private boolean sameBatch(ExternalSyncPayload payload, Long syncBatchId)
    {
        return payload != null && payload.getSyncBatchId() != null && payload.getSyncBatchId().equals(syncBatchId);
    }

    private String resolveVersionHash(ExternalDataRecord record)
    {
        if (StringUtils.isNotBlank(record.getVersion()))
        {
            return record.getVersion();
        }
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(StringUtils.defaultString(record.getPayload()).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash)
            {
                builder.append(Character.forDigit((item >> 4) & 0xF, 16));
                builder.append(Character.forDigit(item & 0xF, 16));
            }
            return builder.toString();
        }
        catch (Exception e)
        {
            throw new ServiceException("生成同步版本摘要失败");
        }
    }

    private void hideCredential(ExternalIntegrationConfig config)
    {
        if (config != null)
        {
            config.setAuthCredential(null);
        }
    }

    private void fillPeriodParts(FusionCollectionBatch batch)
    {
        String periodKey = batch.getPeriodKey();
        if (StringUtils.isBlank(periodKey))
        {
            return;
        }
        if ("month".equals(batch.getPeriodType()) && periodKey.length() == 7)
        {
            batch.setPeriodYear(Integer.valueOf(periodKey.substring(0, 4)));
            batch.setPeriodMonth(Integer.valueOf(periodKey.substring(5, 7)));
        }
        else if ("quarter".equals(batch.getPeriodType()) && periodKey.length() == 7)
        {
            batch.setPeriodYear(Integer.valueOf(periodKey.substring(0, 4)));
            batch.setPeriodQuarter(Integer.valueOf(periodKey.substring(6, 7)));
        }
        else if ("year".equals(batch.getPeriodType()) && periodKey.length() == 4)
        {
            batch.setPeriodYear(Integer.valueOf(periodKey));
        }
    }
}
