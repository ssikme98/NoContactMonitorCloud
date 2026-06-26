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
                if (!matchesIndicator(rule, item))
                {
                    continue;
                }
                WarningEvaluationInput input = toInput(batch, item);
                WarningEvaluationResult result = evaluator.evaluate(rule, input);
                if (!result.isMatched())
                {
                    continue;
                }
                String businessKey = buildBusinessKey(rule, batch, item);
                WarningMessage existing = findOpenMessage(openMessages, businessKey);
                if (existing == null)
                {
                    plan.getMessagesToInsert().add(toMessage(rule, batch, item, businessKey));
                }
                else
                {
                    plan.getMessagesToUpdate().add(toUpdatedMessage(existing, rule, batch, item));
                }
            }
        }
        return plan;
    }

    private boolean matchesIndicator(WarningRule rule, FusionCollectionItem item)
    {
        return rule != null && item != null && rule.getIndicatorId() != null
                && rule.getIndicatorId().equals(item.getIndicatorId());
    }

    private WarningEvaluationInput toInput(FusionCollectionBatch batch, FusionCollectionItem item)
    {
        WarningEvaluationInput input = new WarningEvaluationInput();
        input.setIndicatorId(item.getIndicatorId());
        input.setIndicatorName(item.getIndicatorName());
        input.setResponsibleUnitId(batch.getResponsibleUnitId());
        input.setResponsibleUnitName(batch.getResponsibleUnitName());
        input.setRegionCode(batch.getRegionCode());
        input.setRegionName(batch.getRegionName());
        input.setPeriodKey(batch.getPeriodKey());
        input.setCurrentValue(item.getValueDecimal());
        input.setValuePresent(item.getValueDecimal() != null || StringUtils.isNotBlank(item.getRawValue()));
        input.setOverdue(false);
        return input;
    }

    private WarningMessage toMessage(WarningRule rule, FusionCollectionBatch batch, FusionCollectionItem item,
            String businessKey)
    {
        Date now = new Date();
        WarningMessage message = new WarningMessage();
        message.setRuleId(rule.getRuleId());
        message.setRuleName(rule.getRuleName());
        message.setIndicatorId(item.getIndicatorId());
        message.setIndicatorName(StringUtils.defaultIfBlank(item.getIndicatorName(), rule.getIndicatorName()));
        message.setWarningLevel(rule.getWarningLevel());
        message.setResponsibleUnitId(batch.getResponsibleUnitId());
        message.setResponsibleUnitName(batch.getResponsibleUnitName());
        message.setRegionCode(batch.getRegionCode());
        message.setRegionName(batch.getRegionName());
        message.setPeriodKey(batch.getPeriodKey());
        message.setCurrentValue(item.getValueDecimal());
        message.setThresholdValue(rule.getThresholdValue());
        message.setPushChannels(rule.getPushChannels());
        message.setReceivers(rule.getPushTargets());
        message.setMessageStatus("pending");
        message.setBusinessKey(businessKey);
        message.setHitCount(1);
        message.setSourceBatchId(batch.getBatchId());
        message.setSourceItemId(item.getItemId());
        message.setTriggerTime(now);
        message.setLatestHitTime(now);
        return message;
    }

    private WarningMessage toUpdatedMessage(WarningMessage existing, WarningRule rule, FusionCollectionBatch batch,
            FusionCollectionItem item)
    {
        WarningMessage update = new WarningMessage();
        update.setMessageId(existing.getMessageId());
        update.setBusinessKey(existing.getBusinessKey());
        update.setRuleId(rule.getRuleId());
        update.setIndicatorId(item.getIndicatorId());
        update.setCurrentValue(item.getValueDecimal());
        update.setThresholdValue(rule.getThresholdValue());
        update.setSourceBatchId(batch.getBatchId());
        update.setSourceItemId(item.getItemId());
        update.setHitCount(existing.getHitCount() == null ? 1 : existing.getHitCount() + 1);
        update.setLatestHitTime(new Date());
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

    private String buildBusinessKey(WarningRule rule, FusionCollectionBatch batch, FusionCollectionItem item)
    {
        return rule.getRuleId() + ":" + item.getIndicatorId() + ":" + batch.getResponsibleUnitId() + ":"
                + batch.getPeriodKey();
    }
}
