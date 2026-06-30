package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import com.ruoyi.nocontact.warning.mapper.WarningRuleMapper;
import com.ruoyi.nocontact.warning.service.IWarningRuleService;
import java.math.BigDecimal;
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
        normalizeRule(rule);
        if (StringUtils.isBlank(rule.getStatus()))
        {
            rule.setStatus("0");
        }
        validateRule(rule);
        rule.setCreateTime(DateUtils.getNowDate());
        return ruleMapper.insertRule(rule);
    }

    @Override
    public int updateRule(WarningRule rule)
    {
        normalizeRule(rule);
        validateRule(rule);
        rule.setUpdateTime(DateUtils.getNowDate());
        return ruleMapper.updateRule(rule);
    }

    @Override
    public int updateRuleStatus(Long ruleId, String status, String operName)
    {
        if (!"0".equals(status) && !"1".equals(status))
        {
            throw new ServiceException("不支持的预警规则状态：" + status);
        }
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

    private void normalizeRule(WarningRule rule)
    {
        if (rule == null)
        {
            return;
        }
        if (StringUtils.isBlank(rule.getStatus()))
        {
            rule.setStatus("0");
        }
        if (StringUtils.isBlank(rule.getThresholdType()))
        {
            rule.setThresholdType("target");
        }
        if (!"outside_range".equals(rule.getTriggerCondition()))
        {
            rule.setThresholdValueMax(null);
        }
        if (isScheduledCondition(rule.getTriggerCondition()) && rule.getThresholdValue() == null)
        {
            rule.setThresholdValue(BigDecimal.ZERO);
        }
    }

    private void validateRule(WarningRule rule)
    {
        if (rule == null)
        {
            throw new ServiceException("预警规则不能为空");
        }
        String condition = StringUtils.defaultString(rule.getTriggerCondition()).trim();
        if (!isSupportedCondition(condition))
        {
            throw new ServiceException("不支持的触发条件：" + condition);
        }
        if (!isWarningLevel(rule.getWarningLevel()))
        {
            throw new ServiceException("不支持的预警级别：" + rule.getWarningLevel());
        }
        if (!"0".equals(rule.getStatus()) && !"1".equals(rule.getStatus()))
        {
            throw new ServiceException("不支持的预警规则状态：" + rule.getStatus());
        }
        if (StringUtils.isNotBlank(rule.getPeriodType()) && !isPeriodType(rule.getPeriodType()))
        {
            throw new ServiceException("不支持的周期类型：" + rule.getPeriodType());
        }
        if (isScheduledCondition(condition) && rule.getResponsibleUnitId() == null)
        {
            throw new ServiceException("缺报或逾期规则必须绑定责任单位");
        }
        if (isScheduledCondition(condition) && StringUtils.isBlank(rule.getPeriodType()))
        {
            throw new ServiceException("缺报或逾期规则必须设置周期类型");
        }
        if ("outside_range".equals(condition))
        {
            validateRange(rule.getThresholdValue(), rule.getThresholdValueMax());
        }
        else if (!"missing".equals(condition) && !"overdue".equals(condition) && rule.getThresholdValue() == null)
        {
            throw new ServiceException("阈值不能为空");
        }
    }

    private void validateRange(BigDecimal min, BigDecimal max)
    {
        if (min == null || max == null)
        {
            throw new ServiceException("区间规则必须填写阈值下限和上限");
        }
        if (max.compareTo(min) < 0)
        {
            throw new ServiceException("阈值上限不能小于阈值下限");
        }
    }

    private boolean isSupportedCondition(String condition)
    {
        return "lt".equals(condition) || "lte".equals(condition) || "gt".equals(condition) || "gte".equals(condition)
                || "eq".equals(condition) || "ne".equals(condition) || "outside_range".equals(condition)
                || "missing".equals(condition) || "overdue".equals(condition);
    }

    private boolean isScheduledCondition(String condition)
    {
        return "missing".equals(condition) || "overdue".equals(condition);
    }

    private boolean isWarningLevel(String warningLevel)
    {
        return "1".equals(warningLevel) || "2".equals(warningLevel) || "3".equals(warningLevel);
    }

    private boolean isPeriodType(String periodType)
    {
        return "month".equals(periodType) || "quarter".equals(periodType) || "year".equals(periodType);
    }
}
