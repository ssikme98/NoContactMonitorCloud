package com.ruoyi.nocontact.warning.engine;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import java.util.Date;
import java.util.List;

/**
 * 从已审核采集批次生成预警消息的纯业务流程。
 */
public class WarningGenerationWorkflow
{
    private final WarningRuleEvaluator evaluator;

    public WarningGenerationWorkflow(WarningRuleEvaluator evaluator)
    {
        this.evaluator = evaluator;
    }

    public WarningGenerationPlan evaluate(FusionCollectionBatch batch, List<FusionCollectionItem> items,
            List<WarningRule> rules, List<WarningMessage> openMessages)
    {
        WarningGenerationPlan plan = new WarningGenerationPlan();
        if (batch == null || !"approved".equals(batch.getBatchStatus()) || items == null || rules == null)
        {
            return plan;
        }
        for (FusionCollectionItem item : items)
        {
            for (WarningRule rule : rules)
            {
                WarningEvaluationInput input = toInput(batch, item);
                evaluateInput(plan, rule, input, batch.getBatchId(), item.getItemId(), openMessages);
            }
        }
        return plan;
    }

    public WarningGenerationPlan evaluateScheduled(List<WarningEvaluationInput> inputs, List<WarningRule> rules,
            List<WarningMessage> openMessages)
    {
        WarningGenerationPlan plan = new WarningGenerationPlan();
        if (inputs == null || rules == null)
        {
            return plan;
        }
        for (WarningEvaluationInput input : inputs)
        {
            for (WarningRule rule : rules)
            {
                evaluateInput(plan, rule, input, null, null, openMessages);
            }
        }
        return plan;
    }

    private void evaluateInput(WarningGenerationPlan plan, WarningRule rule, WarningEvaluationInput input,
            Long sourceBatchId, Long sourceItemId, List<WarningMessage> openMessages)
    {
        if (!matchesRule(rule, input))
        {
            return;
        }
        WarningEvaluationResult result = evaluator.evaluate(rule, input);
        if (!result.isMatched())
        {
            return;
        }
        String businessKey = buildBusinessKey(rule, input);
        WarningMessage existing = findOpenMessage(openMessages, businessKey);
        if (existing == null)
        {
            plan.getMessagesToInsert().add(toMessage(rule, input, businessKey, sourceBatchId, sourceItemId));
        }
        else
        {
            plan.getMessagesToUpdate().add(toUpdatedMessage(existing, rule, input, sourceBatchId, sourceItemId));
        }
    }

    private boolean matchesRule(WarningRule rule, WarningEvaluationInput input)
    {
        if (rule == null || input == null || rule.getIndicatorId() == null
                || !rule.getIndicatorId().equals(input.getIndicatorId()))
        {
            return false;
        }
        if (rule.getResponsibleUnitId() != null && !rule.getResponsibleUnitId().equals(input.getResponsibleUnitId()))
        {
            return false;
        }
        if (StringUtils.isNotBlank(rule.getRegionCode())
                && !StringUtils.equals(rule.getRegionCode(), input.getRegionCode()))
        {
            return false;
        }
        return StringUtils.isBlank(rule.getPeriodType()) || StringUtils.equals(rule.getPeriodType(), input.getPeriodType());
    }

    private WarningEvaluationInput toInput(FusionCollectionBatch batch, FusionCollectionItem item)
    {
        WarningEvaluationInput input = new WarningEvaluationInput();
        input.setIndicatorId(item.getIndicatorId());
        input.setIndicatorName(item.getIndicatorName());
        input.setDeptId(batch.getDeptId());
        input.setResponsibleUnitId(batch.getResponsibleUnitId());
        input.setResponsibleUnitName(batch.getResponsibleUnitName());
        input.setRegionCode(batch.getRegionCode());
        input.setRegionName(batch.getRegionName());
        input.setPeriodType(batch.getPeriodType());
        input.setPeriodKey(batch.getPeriodKey());
        input.setCurrentValue(item.getValueDecimal());
        input.setValuePresent(item.getValueDecimal() != null || StringUtils.isNotBlank(item.getRawValue()));
        input.setOverdue(false);
        return input;
    }

    private WarningMessage toMessage(WarningRule rule, WarningEvaluationInput input, String businessKey,
            Long sourceBatchId, Long sourceItemId)
    {
        Date now = new Date();
        WarningMessage message = new WarningMessage();
        message.setRuleId(rule.getRuleId());
        message.setRuleName(rule.getRuleName());
        message.setIndicatorId(input.getIndicatorId());
        message.setIndicatorName(StringUtils.defaultIfBlank(input.getIndicatorName(), rule.getIndicatorName()));
        message.setWarningLevel(rule.getWarningLevel());
        message.setDeptId(input.getDeptId());
        message.setResponsibleUnitId(input.getResponsibleUnitId());
        message.setResponsibleUnitName(input.getResponsibleUnitName());
        message.setRegionCode(input.getRegionCode());
        message.setRegionName(input.getRegionName());
        message.setPeriodKey(input.getPeriodKey());
        message.setCurrentValue(input.getCurrentValue());
        message.setThresholdValue(rule.getThresholdValue());
        message.setPushChannels(rule.getPushChannels());
        message.setReceivers(rule.getPushTargets());
        message.setMessageStatus("pending");
        message.setBusinessKey(businessKey);
        message.setHitCount(1);
        message.setSourceBatchId(sourceBatchId);
        message.setSourceItemId(sourceItemId);
        message.setTriggerTime(now);
        message.setLatestHitTime(now);
        return message;
    }

    private WarningMessage toUpdatedMessage(WarningMessage existing, WarningRule rule, WarningEvaluationInput input,
            Long sourceBatchId, Long sourceItemId)
    {
        WarningMessage update = toMessage(rule, input, existing.getBusinessKey(), sourceBatchId, sourceItemId);
        update.setMessageId(existing.getMessageId());
        update.setHitCount(existing.getHitCount() == null ? 1 : existing.getHitCount() + 1);
        return update;
    }

    private WarningMessage findOpenMessage(List<WarningMessage> openMessages, String businessKey)
    {
        if (openMessages == null)
        {
            return null;
        }
        for (WarningMessage message : openMessages)
        {
            if (message != null && businessKey.equals(message.getBusinessKey()) && isOpen(message.getMessageStatus()))
            {
                return message;
            }
        }
        return null;
    }

    private boolean isOpen(String status)
    {
        return !"closed".equals(status) && !"ignored".equals(status) && !"handled".equals(status)
                && !"archived".equals(status);
    }

    private String buildBusinessKey(WarningRule rule, WarningEvaluationInput input)
    {
        return rule.getRuleId() + ":" + input.getIndicatorId() + ":" + input.getResponsibleUnitId() + ":"
                + StringUtils.defaultString(input.getRegionCode()) + ":" + input.getPeriodKey();
    }
}
