package com.ruoyi.nocontact.warning.service;

import com.ruoyi.nocontact.warning.domain.WarningRule;
import java.util.List;

/**
 * 预警规则Service接口
 *
 * @author ruoyi
 */
public interface IWarningRuleService
{
    public List<WarningRule> selectRuleList(WarningRule rule);

    public WarningRule selectRuleById(Long ruleId);

    public int insertRule(WarningRule rule);

    public int updateRule(WarningRule rule);

    public int updateRuleStatus(Long ruleId, String status, String operName);

    public int deleteRuleByIds(Long[] ruleIds);
}
