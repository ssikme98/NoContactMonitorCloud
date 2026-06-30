package com.ruoyi.nocontact.integration.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.integration.adapter.ExternalDataAdapter;
import com.ruoyi.nocontact.integration.adapter.ExternalDataAdapterRegistry;
import com.ruoyi.nocontact.integration.adapter.JsonExternalDataAdapter;
import com.ruoyi.nocontact.integration.domain.ExternalDataRecord;
import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import com.ruoyi.nocontact.integration.domain.ExternalSyncBatch;
import com.ruoyi.nocontact.integration.domain.ExternalSyncPayload;
import com.ruoyi.nocontact.integration.mapper.ExternalIntegrationMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalIntegrationServiceImplTest
{
    private ExternalIntegrationServiceImpl service;

    @Mock
    private ExternalIntegrationMapper integrationMapper;

    @Mock
    private FusionCollectionBatchMapper collectionBatchMapper;

    @Mock
    private ExternalDataAdapterRegistry adapterRegistry;

    @Mock
    private ExternalDataAdapter adapter;

    @BeforeEach
    void setUp()
    {
        service = new ExternalIntegrationServiceImpl();
        ReflectionTestUtils.setField(service, "integrationMapper", integrationMapper);
        ReflectionTestUtils.setField(service, "collectionBatchMapper", collectionBatchMapper);
        ReflectionTestUtils.setField(service, "adapterRegistry", adapterRegistry);
    }

    @Test
    void syncPersistsPayloadThenCreatesStandardCollectionBatch() throws Exception
    {
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenReturn(Collections.singletonList(record("EXT-001", "v1")));
        when(integrationMapper.selectPayloadByUnique(any(ExternalSyncPayload.class))).thenReturn(null);
        when(integrationMapper.insertPayload(any(ExternalSyncPayload.class))).thenReturn(1);
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9001L);
            return 1;
        });
        when(collectionBatchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch batch = invocation.getArgument(0);
            batch.setBatchId(7001L);
            return 1;
        });

        ExternalSyncBatch result = service.syncConfig(12L, "admin");

        assertEquals("success", result.getBatchStatus());
        ArgumentCaptor<ExternalSyncPayload> payloadCaptor = ArgumentCaptor.forClass(ExternalSyncPayload.class);
        verify(integrationMapper).insertPayload(payloadCaptor.capture());
        assertEquals(Long.valueOf(9001L), payloadCaptor.getValue().getSyncBatchId());
        assertEquals("province-platform", payloadCaptor.getValue().getSourceSystem());
        assertEquals("EXT-001", payloadCaptor.getValue().getExternalId());
        verify(collectionBatchMapper).insertBatch(any(FusionCollectionBatch.class));
        verify(collectionBatchMapper).insertItem(any(FusionCollectionItem.class));
        verify(integrationMapper).insertLog(any());
    }

    @Test
    void duplicatePayloadDoesNotCreateAnotherBusinessBatch() throws Exception
    {
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenReturn(Collections.singletonList(record("EXT-001", "v1")));
        when(integrationMapper.selectPayloadByUnique(any(ExternalSyncPayload.class))).thenReturn(existingPayload());
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9002L);
            return 1;
        });

        ExternalSyncBatch result = service.syncConfig(12L, "admin");

        assertEquals(Long.valueOf(0L), result.getSuccessCount());
        assertEquals(Long.valueOf(1L), result.getSkippedCount());
        verify(collectionBatchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
        verify(collectionBatchMapper, never()).insertItem(any(FusionCollectionItem.class));
    }

    @Test
    void failureLogDoesNotExposeToken() throws Exception
    {
        ExternalIntegrationConfig config = config();
        config.setEndpointUrl("https://example.test/data?token=secret-token");
        config.setAuthCredential("secret-token");
        when(integrationMapper.selectConfigById(12L)).thenReturn(config);
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenThrow(new IllegalStateException("remote failed"));
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9003L);
            return 1;
        });

        ExternalSyncBatch result = service.syncConfig(12L, "admin");

        assertEquals("failed", result.getBatchStatus());
        assertFalse(result.getRequestSummary().contains("secret-token"));
        assertFalse(result.getErrorMessage().contains("secret-token"));
    }

    @Test
    void syncRejectsDisabledConfig()
    {
        ExternalIntegrationConfig config = config();
        config.setStatus("1");
        when(integrationMapper.selectConfigById(12L)).thenReturn(config);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.syncConfig(12L, "admin"));

        assertEquals("停用状态的对接配置不允许执行同步或重试", exception.getMessage());
        verify(integrationMapper, never()).insertSyncBatch(any(ExternalSyncBatch.class));
    }

    @Test
    void configQueriesDoNotExposeStoredCredential()
    {
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(integrationMapper.selectConfigList(any(ExternalIntegrationConfig.class))).thenReturn(Collections.singletonList(config()));

        ExternalIntegrationConfig detail = service.selectConfigById(12L);
        List<ExternalIntegrationConfig> list = service.selectConfigList(new ExternalIntegrationConfig());

        assertNull(detail.getAuthCredential());
        assertNull(list.get(0).getAuthCredential());
    }

    @Test
    void blankCredentialUpdateDoesNotClearStoredCredential()
    {
        ExternalIntegrationConfig config = config();
        config.setAuthCredential("");

        service.updateConfig(config, "admin");

        assertNull(config.getAuthCredential());
        verify(integrationMapper).updateConfig(config);
    }

    @Test
    void testConnectionPullsAdapterBeforeSuccess() throws Exception
    {
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenReturn(Collections.singletonList(record("EXT-001", "v1")));

        service.testConnection(12L, "admin");

        verify(adapter).pull(any(ExternalIntegrationConfig.class));
        verify(integrationMapper).insertLog(any());
    }

    @Test
    void invalidExternalRecordStaysInPayloadAndDoesNotEnterCollectionModel() throws Exception
    {
        ExternalDataRecord invalid = record("EXT-BAD", "v1");
        invalid.setIndicatorId(null);
        invalid.setIndicatorCode("");
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenReturn(Collections.singletonList(invalid));
        when(integrationMapper.selectPayloadByUnique(any(ExternalSyncPayload.class))).thenReturn(null);
        when(integrationMapper.insertPayload(any(ExternalSyncPayload.class))).thenReturn(1);
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9004L);
            return 1;
        });

        ExternalSyncBatch result = service.syncConfig(12L, "admin");

        assertEquals("failed", result.getBatchStatus());
        assertEquals(Long.valueOf(0L), result.getSuccessCount());
        assertEquals(Long.valueOf(1L), result.getFailureCount());
        verify(collectionBatchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
        verify(collectionBatchMapper, never()).insertItem(any(FusionCollectionItem.class));
        ArgumentCaptor<ExternalSyncPayload> payloadCaptor = ArgumentCaptor.forClass(ExternalSyncPayload.class);
        verify(integrationMapper).updatePayload(payloadCaptor.capture());
        assertEquals("failed", payloadCaptor.getValue().getPayloadStatus());
        assertEquals("指标不能为空", payloadCaptor.getValue().getConvertMessage());
    }

    @Test
    void mixedSyncResultsAreMarkedPartialFailed() throws Exception
    {
        ExternalDataRecord invalid = record("EXT-BAD", "v1");
        invalid.setIndicatorId(null);
        invalid.setIndicatorCode("");
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenReturn(Arrays.asList(record("EXT-001", "v1"), invalid));
        when(integrationMapper.selectPayloadByUnique(any(ExternalSyncPayload.class))).thenReturn(null);
        when(integrationMapper.insertPayload(any(ExternalSyncPayload.class))).thenReturn(1);
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9005L);
            return 1;
        });
        when(collectionBatchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch batch = invocation.getArgument(0);
            batch.setBatchId(7002L);
            return 1;
        });

        ExternalSyncBatch result = service.syncConfig(12L, "admin");

        assertEquals("partial_failed", result.getBatchStatus());
        assertEquals(Long.valueOf(1L), result.getSuccessCount());
        assertEquals(Long.valueOf(1L), result.getFailureCount());
        assertTrue(result.getResponseSummary().contains("失败1条"));
    }

    @Test
    void retryReusesFailedPayloadForRetryBatch() throws Exception
    {
        JsonExternalDataAdapter jsonAdapter = new JsonExternalDataAdapter();
        ExternalSyncBatch sourceBatch = new ExternalSyncBatch();
        sourceBatch.setSyncBatchId(9007L);
        sourceBatch.setConfigId(12L);
        sourceBatch.setBatchStatus("failed");
        sourceBatch.setRetryCount(0L);
        ExternalSyncPayload failedPayload = new ExternalSyncPayload();
        failedPayload.setPayloadId(8100L);
        failedPayload.setSyncBatchId(9007L);
        failedPayload.setSourceSystem("province-platform");
        failedPayload.setExternalId("EXT-RETRY");
        failedPayload.setVersionHash("v1");
        failedPayload.setPayloadStatus("failed");
        failedPayload.setPayloadContent("{\"sourceSystem\":\"province-platform\",\"externalId\":\"EXT-RETRY\",\"version\":\"v1\",\"indicatorId\":1003,\"indicatorCode\":\"NC-1003\",\"indicatorName\":\"数字政务能力\",\"responsibleUnitId\":200,\"responsibleUnitName\":\"省数据局\",\"regionCode\":\"430100\",\"regionName\":\"长沙市\",\"periodType\":\"month\",\"periodKey\":\"2026-06\",\"rawValue\":\"72\",\"valueDecimal\":\"72\"}");

        when(integrationMapper.selectSyncBatchById(9007L)).thenReturn(sourceBatch);
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(integrationMapper.selectPayloadListByBatchId(9007L)).thenReturn(Collections.singletonList(failedPayload));
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(jsonAdapter);
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9008L);
            return 1;
        });
        when(collectionBatchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch batch = invocation.getArgument(0);
            batch.setBatchId(7008L);
            return 1;
        });

        ExternalSyncBatch result = service.retryBatch(9007L, "admin");

        assertEquals("success", result.getBatchStatus());
        verify(integrationMapper, never()).insertPayload(any(ExternalSyncPayload.class));
        verify(integrationMapper, org.mockito.Mockito.atLeastOnce()).updatePayload(org.mockito.ArgumentMatchers.argThat(item ->
                item != null && Long.valueOf(8100L).equals(item.getPayloadId()) && Long.valueOf(9008L).equals(item.getSyncBatchId())));
        assertEquals(Long.valueOf(1L), result.getSuccessCount());
        verify(collectionBatchMapper).insertBatch(any(FusionCollectionBatch.class));
        verify(collectionBatchMapper).insertItem(any(FusionCollectionItem.class));
    }

    @Test
    void retryPullFailureBatchRerunsAdapterPull() throws Exception
    {
        ExternalSyncBatch sourceBatch = new ExternalSyncBatch();
        sourceBatch.setSyncBatchId(9011L);
        sourceBatch.setConfigId(12L);
        sourceBatch.setBatchStatus("failed");
        when(integrationMapper.selectSyncBatchById(9011L)).thenReturn(sourceBatch);
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(integrationMapper.selectPayloadListByBatchId(9011L)).thenReturn(Collections.emptyList());
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenReturn(Collections.singletonList(record("EXT-RETRY-PULL", "v1")));
        when(integrationMapper.selectPayloadByUnique(any(ExternalSyncPayload.class))).thenReturn(null);
        when(integrationMapper.insertPayload(any(ExternalSyncPayload.class))).thenReturn(1);
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9012L);
            return 1;
        });
        when(collectionBatchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch batch = invocation.getArgument(0);
            batch.setBatchId(7012L);
            return 1;
        });

        ExternalSyncBatch result = service.retryBatch(9011L, "admin");

        assertEquals("success", result.getBatchStatus());
        verify(adapter).pull(any(ExternalIntegrationConfig.class));
        verify(collectionBatchMapper).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void duplicateInsertConflictIsTreatedAsSkip() throws Exception
    {
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenReturn(Collections.singletonList(record("EXT-001", "v1")));
        when(integrationMapper.selectPayloadByUnique(any(ExternalSyncPayload.class))).thenReturn(null, existingPayload());
        when(integrationMapper.insertPayload(any(ExternalSyncPayload.class))).thenReturn(0);
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9013L);
            return 1;
        });

        ExternalSyncBatch result = service.syncConfig(12L, "admin");

        assertEquals("success", result.getBatchStatus());
        assertEquals(Long.valueOf(1L), result.getSkippedCount());
        verify(collectionBatchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void failedItemInsertRemovesInsertedBatch() throws Exception
    {
        when(integrationMapper.selectConfigById(12L)).thenReturn(config());
        when(adapterRegistry.resolve(any(ExternalIntegrationConfig.class))).thenReturn(adapter);
        when(adapter.pull(any(ExternalIntegrationConfig.class))).thenReturn(Collections.singletonList(record("EXT-ROLLBACK", "v1")));
        when(integrationMapper.selectPayloadByUnique(any(ExternalSyncPayload.class))).thenReturn(null);
        when(integrationMapper.insertPayload(any(ExternalSyncPayload.class))).thenReturn(1);
        when(integrationMapper.insertSyncBatch(any(ExternalSyncBatch.class))).thenAnswer(invocation -> {
            ExternalSyncBatch batch = invocation.getArgument(0);
            batch.setSyncBatchId(9006L);
            return 1;
        });
        when(collectionBatchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch batch = invocation.getArgument(0);
            batch.setBatchId(7003L);
            return 1;
        });
        org.mockito.Mockito.doThrow(new RuntimeException("insert item failed")).when(collectionBatchMapper).insertItem(any(FusionCollectionItem.class));

        ExternalSyncBatch result = service.syncConfig(12L, "admin");

        assertEquals("failed", result.getBatchStatus());
        verify(collectionBatchMapper).deleteBatchById(7003L);
    }

    private ExternalIntegrationConfig config()
    {
        ExternalIntegrationConfig config = new ExternalIntegrationConfig();
        config.setConfigId(12L);
        config.setIntegrationName("省平台营商指标");
        config.setPlatformName("省平台");
        config.setIntegrationType("api");
        config.setEndpointUrl("mock://province-platform");
        config.setAuthType("token");
        config.setAuthCredential("secret-token");
        config.setStatus("0");
        return config;
    }

    private ExternalDataRecord record(String externalId, String version)
    {
        ExternalDataRecord record = new ExternalDataRecord();
        record.setSourceSystem("province-platform");
        record.setExternalId(externalId);
        record.setVersion(version);
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
        record.setPayload("{\"externalId\":\"" + externalId + "\",\"version\":\"" + version + "\"}");
        return record;
    }

    private ExternalSyncPayload existingPayload()
    {
        ExternalSyncPayload payload = new ExternalSyncPayload();
        payload.setPayloadId(8001L);
        payload.setSourceSystem("province-platform");
        payload.setExternalId("EXT-001");
        payload.setVersionHash("v1");
        payload.setPayloadStatus("converted");
        return payload;
    }
}
