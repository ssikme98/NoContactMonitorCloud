package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import com.ruoyi.nocontact.warning.engine.WarningGenerationPlan;
import com.ruoyi.nocontact.warning.engine.WarningGenerationWorkflow;
import com.ruoyi.nocontact.warning.engine.WarningRuleEvaluator;
import com.ruoyi.nocontact.warning.mapper.WarningMessageMapper;
import com.ruoyi.nocontact.warning.mapper.WarningRuleMapper;
import com.ruoyi.nocontact.warning.service.IWarningEvaluationService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
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
        int rows = 0;
        for (WarningMessage message : plan.getMessagesToInsert())
        {
            message.setCreateBy(operName);
            message.setCreateTime(DateUtils.getNowDate());
            rows += messageMapper.insertMessage(message);
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
                message.setCreateBy(operName);
                message.setCreateTime(DateUtils.getNowDate());
                rows += messageMapper.insertMessage(message);
            }
            else
            {
                rows += updated;
            }
        }
        return rows;
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
