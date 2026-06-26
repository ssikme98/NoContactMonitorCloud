package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.warning.service.IWarningEvaluationService;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FusionCollectionBatchServiceImplTest
{
    private FusionCollectionBatchServiceImpl service;

    @Mock
    private FusionCollectionBatchMapper batchMapper;

    @Mock
    private IWarningEvaluationService warningEvaluationService;

    @BeforeEach
    void setUp()
    {
        service = new FusionCollectionBatchServiceImpl();
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "warningEvaluationService", warningEvaluationService);
    }

    @Test
    void detailIncludesItemsAndAuditLogs()
    {
        FusionCollectionBatch batch = batch("pending_audit");
        when(batchMapper.selectBatchById(5001L)).thenReturn(batch);
        when(batchMapper.selectItemsByBatchId(5001L)).thenReturn(Collections.emptyList());
        when(batchMapper.selectAuditLogsByBatchId(5001L)).thenReturn(Collections.emptyList());

        FusionCollectionBatch result = service.selectBatchById(5001L);

        assertEquals(Collections.emptyList(), result.getItems());
        assertEquals(Collections.emptyList(), result.getAuditLogs());
    }

    @Test
    void approvingPendingBatchUpdatesStatusAndTriggersWarningEvaluation()
    {
        when(batchMapper.selectBatchById(5001L)).thenReturn(batch("pending_audit"));
        when(batchMapper.updateBatchStatus(any(FusionCollectionBatch.class))).thenReturn(1);

        int rows = service.approveBatch(5001L, "通过", "admin");

        assertEquals(1, rows);
        verify(batchMapper).insertAuditLog(any());
        verify(warningEvaluationService).evaluateApprovedBatch(5001L, "admin");
    }

    @Test
    void approvingNonPendingBatchIsRejected()
    {
        when(batchMapper.selectBatchById(5001L)).thenReturn(batch("approved"));

        assertThrows(ServiceException.class, () -> service.approveBatch(5001L, "重复审核", "admin"));

        verify(batchMapper, never()).updateBatchStatus(any());
        verify(warningEvaluationService, never()).evaluateApprovedBatch(5001L, "admin");
    }

    @Test
    void rejectingPendingBatchDoesNotTriggerWarningEvaluation()
    {
        when(batchMapper.selectBatchById(5001L)).thenReturn(batch("pending_audit"));
        when(batchMapper.updateBatchStatus(any(FusionCollectionBatch.class))).thenReturn(1);

        int rows = service.rejectBatch(5001L, "数据缺失", "admin");

        assertEquals(1, rows);
        verify(batchMapper).insertAuditLog(any());
        verify(warningEvaluationService, never()).evaluateApprovedBatch(5001L, "admin");
    }

    private FusionCollectionBatch batch(String status)
    {
        FusionCollectionBatch batch = new FusionCollectionBatch();
        batch.setBatchId(5001L);
        batch.setBatchStatus(status);
        return batch;
    }
}
