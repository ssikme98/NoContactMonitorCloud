package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import com.ruoyi.nocontact.warning.mapper.WarningRuleMapper;
import com.ruoyi.nocontact.warning.service.IWarningRuleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 预警规则Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class WarningRuleServiceImpl implements IWarningRuleService
{
    @Autowired
    private WarningRuleMapper ruleMapper;

    @Override
    public List<WarningRule> selectRuleList(WarningRule rule)
    {
        return ruleMapper.selectRuleList(rule);
    }

    @Override
    public WarningRule selectRuleById(Long ruleId)
    {
        return ruleMapper.selectRuleById(ruleId);
    }

    @Override
    public int insertRule(WarningRule rule)
    {
        rule.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(rule.getStatus()))
        {
            rule.setStatus("0");
        }
        return ruleMapper.insertRule(rule);
    }

    @Override
    public int updateRule(WarningRule rule)
    {
        rule.setUpdateTime(DateUtils.getNowDate());
        return ruleMapper.updateRule(rule);
    }

    @Override
    public int updateRuleStatus(Long ruleId, String status, String operName)
    {
        WarningRule rule = new WarningRule();
        rule.setRuleId(ruleId);
        rule.setStatus(status);
        rule.setUpdateBy(operName);
        rule.setUpdateTime(DateUtils.getNowDate());
        return ruleMapper.updateRuleStatus(rule);
    }

    @Override
    public int deleteRuleByIds(Long[] ruleIds)
    {
        return ruleMapper.deleteRuleByIds(ruleIds);
    }
}
