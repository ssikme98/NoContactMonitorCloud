package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import com.ruoyi.nocontact.warning.mapper.WarningMessageMapper;
import com.ruoyi.nocontact.warning.mapper.WarningRuleMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningEvaluationServiceImplTest
{
    private WarningEvaluationServiceImpl service;

    @Mock
    private FusionCollectionBatchMapper batchMapper;

    @Mock
    private WarningRuleMapper ruleMapper;

    @Mock
    private WarningMessageMapper messageMapper;

    @BeforeEach
    void setUp()
    {
        service = new WarningEvaluationServiceImpl();
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "ruleMapper", ruleMapper);
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);
    }

    @Test
    void closedConcurrentOpenMessageUpdateFallsBackToNewPendingMessage()
    {
        when(batchMapper.selectBatchById(5001L)).thenReturn(batch());
        when(batchMapper.selectItemsByBatchId(5001L)).thenReturn(Collections.singletonList(item()));
        when(ruleMapper.selectEnabledRulesByIndicator(1003L)).thenReturn(Collections.singletonList(rule()));
        when(messageMapper.selectOpenMessagesByScope(any(WarningMessage.class)))
                .thenReturn(Collections.singletonList(openMessage()));
        when(messageMapper.updateMessageHit(any(WarningMessage.class))).thenReturn(0);
        when(messageMapper.insertMessage(any(WarningMessage.class))).thenReturn(1);

        int rows = service.evaluateApprovedBatch(5001L, "admin");

        assertEquals(1, rows);
        verify(messageMapper).updateMessageHit(any(WarningMessage.class));
        ArgumentCaptor<WarningMessage> captor = ArgumentCaptor.forClass(WarningMessage.class);
        verify(messageMapper).insertMessage(captor.capture());
        assertEquals("pending", captor.getValue().getMessageStatus());
        assertEquals("3002:1003:200:433100:2035-06", captor.getValue().getBusinessKey());
        assertEquals(Integer.valueOf(1), captor.getValue().getHitCount());
    }

    @Test
    void scheduledMissingRuleCreatesMessageWhenCurrentItemAbsent()
    {
        when(ruleMapper.selectScheduledRules()).thenReturn(Collections.singletonList(missingRule()));
        when(batchMapper.countApprovedCurrentItemByScope(any(FusionCollectionItem.class))).thenReturn(0);
        when(batchMapper.selectDeptNameById(200L)).thenReturn("省数据局");
        when(messageMapper.selectOpenMessagesByScope(any(WarningMessage.class))).thenReturn(Collections.emptyList());
        when(messageMapper.insertMessage(any(WarningMessage.class))).thenReturn(1);

        int rows = service.evaluateScheduledRules("2035-06", "job");

        assertEquals(1, rows);
        ArgumentCaptor<WarningMessage> captor = ArgumentCaptor.forClass(WarningMessage.class);
        verify(messageMapper).insertMessage(captor.capture());
        assertEquals("3003:1003:200:433100:2035-06", captor.getValue().getBusinessKey());
        assertEquals(Long.valueOf(200L), captor.getValue().getDeptId());
        assertEquals("省数据局", captor.getValue().getResponsibleUnitName());
    }

    @Test
    void scheduledRuleSkipsWhenCurrentItemExists()
    {
        when(ruleMapper.selectScheduledRules()).thenReturn(Collections.singletonList(missingRule()));
        when(batchMapper.countApprovedCurrentItemByScope(any(FusionCollectionItem.class))).thenReturn(1);

        int rows = service.evaluateScheduledRules("2035-06", "job");

        assertEquals(0, rows);
        verify(messageMapper, never()).insertMessage(any(WarningMessage.class));
    }

    @Test
    void scheduledOverdueRuleSkipsCurrentPeriodBeforeDueDate()
    {
        when(ruleMapper.selectScheduledRules()).thenReturn(Collections.singletonList(overdueRule()));

        int rows = service.evaluateScheduledRules(YearMonth.from(LocalDate.now()).toString(), "job");

        assertEquals(0, rows);
        verify(batchMapper, never()).countApprovedCurrentItemByScope(any(FusionCollectionItem.class));
        verify(messageMapper, never()).insertMessage(any(WarningMessage.class));
    }

    @Test
    void scheduledOverdueRuleCreatesMessageAfterDueDate()
    {
        when(ruleMapper.selectScheduledRules()).thenReturn(Collections.singletonList(overdueRule()));
        when(batchMapper.countApprovedCurrentItemByScope(any(FusionCollectionItem.class))).thenReturn(0);
        when(batchMapper.selectDeptNameById(200L)).thenReturn("省数据局");
        when(messageMapper.selectOpenMessagesByScope(any(WarningMessage.class))).thenReturn(Collections.emptyList());
        when(messageMapper.insertMessage(any(WarningMessage.class))).thenReturn(1);

        int rows = service.evaluateScheduledRules(YearMonth.from(LocalDate.now()).minusMonths(1).toString(), "job");

        assertEquals(1, rows);
        verify(messageMapper).insertMessage(any(WarningMessage.class));
    }

    @Test
    void duplicateOpenBusinessKeyFallsBackToAtomicHitUpdate()
    {
        when(ruleMapper.selectScheduledRules()).thenReturn(Collections.singletonList(missingRule()));
        when(batchMapper.countApprovedCurrentItemByScope(any(FusionCollectionItem.class))).thenReturn(0);
        when(batchMapper.selectDeptNameById(200L)).thenReturn("省数据局");
        when(messageMapper.selectOpenMessagesByScope(any(WarningMessage.class))).thenReturn(Collections.emptyList());
        when(messageMapper.insertMessage(any(WarningMessage.class))).thenThrow(new DuplicateKeyException("duplicate"));
        when(messageMapper.updateOpenMessageHitByBusinessKey(any(WarningMessage.class))).thenReturn(1);

        int rows = service.evaluateScheduledRules("2035-06", "job");

        assertEquals(1, rows);
        verify(messageMapper).updateOpenMessageHitByBusinessKey(any(WarningMessage.class));
    }

    private FusionCollectionBatch batch()
    {
        FusionCollectionBatch batch = new FusionCollectionBatch();
        batch.setBatchId(5001L);
        batch.setBatchStatus("approved");
        batch.setDeptId(200L);
        batch.setResponsibleUnitId(200L);
        batch.setResponsibleUnitName("省数据局");
        batch.setRegionCode("433100");
        batch.setRegionName("湘西州");
        batch.setPeriodKey("2035-06");
        return batch;
    }

    private FusionCollectionItem item()
    {
        FusionCollectionItem item = new FusionCollectionItem();
        item.setItemId(6001L);
        item.setIndicatorId(1003L);
        item.setIndicatorName("数字政务能力");
        item.setRawValue("70");
        item.setValueDecimal(new BigDecimal("70"));
        return item;
    }

    private WarningRule rule()
    {
        WarningRule rule = new WarningRule();
        rule.setRuleId(3002L);
        rule.setRuleName("数字政务能力低于基准橙色预警");
        rule.setIndicatorId(1003L);
        rule.setIndicatorName("数字政务能力");
        rule.setStatus("0");
        rule.setWarningLevel("2");
        rule.setTriggerCondition("lt");
        rule.setThresholdValue(new BigDecimal("80"));
        rule.setPushChannels("site,email");
        rule.setPushTargets("监测项负责人");
        return rule;
    }

    private WarningRule missingRule()
    {
        WarningRule rule = rule();
        rule.setRuleId(3003L);
        rule.setRuleName("数字政务能力缺报预警");
        rule.setTriggerCondition("missing");
        rule.setThresholdValue(BigDecimal.ZERO);
        rule.setResponsibleUnitId(200L);
        rule.setRegionCode("433100");
        rule.setRegionName("湘西州");
        rule.setPeriodType("month");
        return rule;
    }

    private WarningRule overdueRule()
    {
        WarningRule rule = missingRule();
        rule.setRuleId(3004L);
        rule.setRuleName("数字政务能力逾期预警");
        rule.setTriggerCondition("overdue");
        return rule;
    }

    private WarningMessage openMessage()
    {
        WarningMessage message = new WarningMessage();
        message.setMessageId(4001L);
        message.setMessageStatus("pending");
        message.setBusinessKey("3002:1003:200:433100:2035-06");
        message.setHitCount(1);
        return message;
    }
}
