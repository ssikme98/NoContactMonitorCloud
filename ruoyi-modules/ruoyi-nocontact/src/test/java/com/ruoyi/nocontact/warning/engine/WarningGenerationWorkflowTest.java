package com.ruoyi.nocontact.warning.engine;

import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarningGenerationWorkflowTest
{
    private final WarningGenerationWorkflow workflow = new WarningGenerationWorkflow(new WarningRuleEvaluator());

    @Test
    void approvedBatchHitCreatesPendingWarningMessage()
    {
        WarningGenerationPlan plan = workflow.evaluate(batch("approved"), Collections.singletonList(item("72")),
                Collections.singletonList(rule()), Collections.<WarningMessage>emptyList());

        assertEquals(1, plan.getMessagesToInsert().size());
        WarningMessage message = plan.getMessagesToInsert().get(0);
        assertEquals("pending", message.getMessageStatus());
        assertEquals("3002:1003:200:433100:2026-06", message.getBusinessKey());
        assertEquals(Long.valueOf(200L), message.getDeptId());
        assertEquals(Long.valueOf(200L), message.getResponsibleUnitId());
        assertEquals("省数据局", message.getResponsibleUnitName());
        assertEquals("433100", message.getRegionCode());
        assertEquals("2026-06", message.getPeriodKey());
        assertEquals(new BigDecimal("72"), message.getCurrentValue());
        assertEquals(Integer.valueOf(1), message.getHitCount());
    }

    @Test
    void unapprovedBatchDoesNotGenerateWarning()
    {
        WarningGenerationPlan plan = workflow.evaluate(batch("pending_audit"), Collections.singletonList(item("72")),
                Collections.singletonList(rule()), Collections.<WarningMessage>emptyList());

        assertTrue(plan.getMessagesToInsert().isEmpty());
        assertTrue(plan.getMessagesToUpdate().isEmpty());
    }

    @Test
    void repeatedHitUpdatesExistingOpenMessage()
    {
        WarningMessage existing = new WarningMessage();
        existing.setMessageId(4002L);
        existing.setBusinessKey("3002:1003:200:433100:2026-06");
        existing.setMessageStatus("pending");
        existing.setHitCount(1);

        WarningGenerationPlan plan = workflow.evaluate(batch("approved"), Collections.singletonList(item("70")),
                Collections.singletonList(rule()), Arrays.asList(existing));

        assertTrue(plan.getMessagesToInsert().isEmpty());
        assertEquals(1, plan.getMessagesToUpdate().size());
        assertEquals(Long.valueOf(4002L), plan.getMessagesToUpdate().get(0).getMessageId());
        assertEquals(Integer.valueOf(2), plan.getMessagesToUpdate().get(0).getHitCount());
        assertEquals(new BigDecimal("70"), plan.getMessagesToUpdate().get(0).getCurrentValue());
    }

    @Test
    void sameUnitAndPeriodDifferentRegionCreatesSeparateMessage()
    {
        WarningMessage existing = new WarningMessage();
        existing.setMessageId(4002L);
        existing.setBusinessKey("3002:1003:200:433101:2026-06");
        existing.setMessageStatus("pending");
        existing.setHitCount(1);

        WarningGenerationPlan plan = workflow.evaluate(batch("approved"), Collections.singletonList(item("70")),
                Collections.singletonList(rule()), Arrays.asList(existing));

        assertEquals(1, plan.getMessagesToInsert().size());
        assertTrue(plan.getMessagesToUpdate().isEmpty());
        assertEquals("3002:1003:200:433100:2026-06", plan.getMessagesToInsert().get(0).getBusinessKey());
    }

    private FusionCollectionBatch batch(String status)
    {
        FusionCollectionBatch batch = new FusionCollectionBatch();
        batch.setBatchId(5001L);
        batch.setTaskId(2002L);
        batch.setBatchStatus(status);
        batch.setDeptId(200L);
        batch.setResponsibleUnitId(200L);
        batch.setResponsibleUnitName("省数据局");
        batch.setRegionCode("433100");
        batch.setRegionName("湘西州");
        batch.setPeriodType("month");
        batch.setPeriodYear(2026);
        batch.setPeriodMonth(6);
        batch.setPeriodKey("2026-06");
        return batch;
    }

    private FusionCollectionItem item(String value)
    {
        FusionCollectionItem item = new FusionCollectionItem();
        item.setItemId(6001L);
        item.setBatchId(5001L);
        item.setIndicatorId(1003L);
        item.setIndicatorName("数字政务能力");
        item.setRawValue(value);
        item.setValueDecimal(new BigDecimal(value));
        item.setValidationStatus("valid");
        return item;
    }

    private WarningRule rule()
    {
        WarningRule rule = new WarningRule();
        rule.setRuleId(3002L);
        rule.setRuleName("数字政务能力低于基准橙色预警");
        rule.setIndicatorId(1003L);
        rule.setIndicatorName("数字政务能力");
        rule.setWarningLevel("2");
        rule.setTriggerCondition("lt");
        rule.setThresholdValue(new BigDecimal("80"));
        rule.setPushChannels("site,email");
        rule.setPushTargets("监测项负责人");
        rule.setStatus("0");
        return rule;
    }
}
