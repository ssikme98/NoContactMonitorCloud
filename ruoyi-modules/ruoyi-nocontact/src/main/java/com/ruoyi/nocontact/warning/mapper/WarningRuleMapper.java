package com.ruoyi.nocontact.warning.mapper;

import com.ruoyi.nocontact.warning.domain.WarningRule;
import java.util.List;

/**
 * 预警规则Mapper接口
 *
 * @author ruoyi
 */
public interface WarningRuleMapper
{
    public List<WarningRule> selectRuleList(WarningRule rule);

    public WarningRule selectRuleById(Long ruleId);

    public List<WarningRule> selectEnabledRulesByIndicator(Long indicatorId);

    public List<WarningRule> selectScheduledRules();

    public int insertRule(WarningRule rule);

    public int updateRule(WarningRule rule);

    public int updateRuleStatus(WarningRule rule);

    public int deleteRuleByIds(Long[] ruleIds);
}
