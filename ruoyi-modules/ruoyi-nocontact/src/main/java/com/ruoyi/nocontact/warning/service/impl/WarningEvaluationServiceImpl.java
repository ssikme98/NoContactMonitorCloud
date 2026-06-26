package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import com.ruoyi.nocontact.warning.engine.WarningEvaluationInput;
import com.ruoyi.nocontact.warning.engine.WarningGenerationPlan;
import com.ruoyi.nocontact.warning.engine.WarningGenerationWorkflow;
import com.ruoyi.nocontact.warning.engine.WarningRuleEvaluator;
import com.ruoyi.nocontact.warning.mapper.WarningMessageMapper;
import com.ruoyi.nocontact.warning.mapper.WarningRuleMapper;
import com.ruoyi.nocontact.warning.service.IWarningEvaluationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 预警评估Service业务层处理
 */
@Service
public class WarningEvaluationServiceImpl implements IWarningEvaluationService
{
    private final WarningGenerationWorkflow workflow = new WarningGenerationWorkflow(new WarningRuleEvaluator());

    @Autowired
    private FusionCollectionBatchMapper batchMapper;

    @Autowired
    private WarningRuleMapper ruleMapper;

    @Autowired
    private WarningMessageMapper messageMapper;

    @Override
    public int evaluateApprovedBatch(Long batchId, String operName)
    {
        FusionCollectionBatch batch = batchMapper.selectBatchById(batchId);
        if (batch == null || !"approved".equals(batch.getBatchStatus()))
        {
            return 0;
        }
        List<FusionCollectionItem> items = batchMapper.selectItemsByBatchId(batchId);
        List<WarningRule> rules = selectRules(items);
        WarningMessage query = new WarningMessage();
        query.setDeptId(batch.getDeptId());
        query.setResponsibleUnitId(batch.getResponsibleUnitId());
        query.setRegionCode(batch.getRegionCode());
        query.setPeriodKey(batch.getPeriodKey());
        List<WarningMessage> existing = messageMapper.selectOpenMessagesByScope(query);
        WarningGenerationPlan plan = workflow.evaluate(batch, items, rules, existing);
        return persistPlan(plan, operName);
    }

    @Override
    public int evaluateScheduledRules(String periodKey, String operName)
    {
        if (StringUtils.isBlank(periodKey))
        {
            throw new ServiceException("预警重算周期不能为空");
        }
        String periodType = resolvePeriodType(periodKey);
        int rows = 0;
        List<WarningRule> rules = ruleMapper.selectScheduledRules();
        for (WarningRule rule : rules)
        {
            if (!hasScheduledScope(rule) || !StringUtils.equals(periodType, rule.getPeriodType())
                    || !isDueScheduledRule(rule, periodKey, periodType) || hasCurrentItem(rule, periodKey))
            {
                continue;
            }
            WarningEvaluationInput input = toScheduledInput(rule, periodKey);
            WarningMessage query = new WarningMessage();
            query.setDeptId(input.getDeptId());
            query.setResponsibleUnitId(input.getResponsibleUnitId());
            query.setRegionCode(input.getRegionCode());
            query.setPeriodKey(input.getPeriodKey());
            List<WarningMessage> existing = messageMapper.selectOpenMessagesByScope(query);
            WarningGenerationPlan plan = workflow.evaluateScheduled(Collections.singletonList(input),
                    Collections.singletonList(rule), existing);
            rows += persistPlan(plan, operName);
        }
        return rows;
    }

    private int persistPlan(WarningGenerationPlan plan, String operName)
    {
        int rows = 0;
        for (WarningMessage message : plan.getMessagesToInsert())
        {
            rows += insertOrUpdateOpenMessage(message, operName);
        }
        for (WarningMessage message : plan.getMessagesToUpdate())
        {
            message.setUpdateBy(operName);
            message.setUpdateTime(DateUtils.getNowDate());
            int updated = messageMapper.updateMessageHit(message);
            if (updated == 0)
            {
                message.setMessageId(null);
                message.setMessageStatus("pending");
                message.setHitCount(1);
                rows += insertOrUpdateOpenMessage(message, operName);
            }
            else
            {
                rows += updated;
            }
        }
        return rows;
    }

    private int insertOrUpdateOpenMessage(WarningMessage message, String operName)
    {
        message.setCreateBy(operName);
        message.setCreateTime(DateUtils.getNowDate());
        try
        {
            return messageMapper.insertMessage(message);
        }
        catch (DuplicateKeyException e)
        {
            message.setUpdateBy(operName);
            message.setUpdateTime(DateUtils.getNowDate());
            return messageMapper.updateOpenMessageHitByBusinessKey(message);
        }
    }

    private boolean hasScheduledScope(WarningRule rule)
    {
        return rule != null && rule.getIndicatorId() != null && rule.getResponsibleUnitId() != null
                && StringUtils.isNotBlank(rule.getPeriodType());
    }

    private boolean isDueScheduledRule(WarningRule rule, String periodKey, String periodType)
    {
        return !"overdue".equals(rule.getTriggerCondition()) || isPastPeriod(periodKey, periodType);
    }

    private boolean isPastPeriod(String periodKey, String periodType)
    {
        LocalDate today = LocalDate.now();
        if ("month".equals(periodType))
        {
            return !today.isBefore(YearMonth.parse(periodKey).plusMonths(1).atDay(1));
        }
        if ("quarter".equals(periodType))
        {
            int year = Integer.valueOf(periodKey.substring(0, 4));
            int quarter = Integer.valueOf(periodKey.substring(6, 7));
            int nextQuarterMonth = quarter * 3 + 1;
            if (nextQuarterMonth > 12)
            {
                return !today.isBefore(LocalDate.of(year + 1, 1, 1));
            }
            return !today.isBefore(LocalDate.of(year, nextQuarterMonth, 1));
        }
        return !today.isBefore(LocalDate.of(Integer.valueOf(periodKey) + 1, 1, 1));
    }

    private String resolvePeriodType(String periodKey)
    {
        if (periodKey.matches("\\d{4}-(0[1-9]|1[0-2])"))
        {
            return "month";
        }
        if (periodKey.matches("\\d{4}-Q[1-4]"))
        {
            return "quarter";
        }
        if (periodKey.matches("\\d{4}"))
        {
            return "year";
        }
        throw new ServiceException("不支持的预警重算周期：" + periodKey);
    }


    private boolean hasCurrentItem(WarningRule rule, String periodKey)
    {
        FusionCollectionItem item = new FusionCollectionItem();
        item.setIndicatorId(rule.getIndicatorId());
        item.setResponsibleUnitId(rule.getResponsibleUnitId());
        item.setRegionCode(rule.getRegionCode());
        item.setPeriodKey(periodKey);
        return batchMapper.countApprovedCurrentItemByScope(item) > 0;
    }

    private WarningEvaluationInput toScheduledInput(WarningRule rule, String periodKey)
    {
        WarningEvaluationInput input = new WarningEvaluationInput();
        input.setIndicatorId(rule.getIndicatorId());
        input.setIndicatorName(rule.getIndicatorName());
        input.setDeptId(rule.getResponsibleUnitId());
        input.setResponsibleUnitId(rule.getResponsibleUnitId());
        input.setResponsibleUnitName(StringUtils.defaultIfBlank(rule.getResponsibleUnitName(),
                batchMapper.selectDeptNameById(rule.getResponsibleUnitId())));
        input.setRegionCode(rule.getRegionCode());
        input.setRegionName(rule.getRegionName());
        input.setPeriodType(StringUtils.defaultIfBlank(rule.getPeriodType(), "month"));
        input.setPeriodKey(periodKey);
        input.setValuePresent(false);
        input.setOverdue("overdue".equals(rule.getTriggerCondition()));
        return input;
    }

    private List<WarningRule> selectRules(List<FusionCollectionItem> items)
    {
        List<WarningRule> rules = new ArrayList<WarningRule>();
        Set<Long> indicatorIds = new HashSet<Long>();
        if (items == null)
        {
            return rules;
        }
        for (FusionCollectionItem item : items)
        {
            if (item.getIndicatorId() != null && indicatorIds.add(item.getIndicatorId()))
            {
                rules.addAll(ruleMapper.selectEnabledRulesByIndicator(item.getIndicatorId()));
            }
        }
        return rules;
    }
}
