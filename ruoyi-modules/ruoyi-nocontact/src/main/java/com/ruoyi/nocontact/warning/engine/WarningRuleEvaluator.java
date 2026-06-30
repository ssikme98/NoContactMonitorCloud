package com.ruoyi.nocontact.warning.engine;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import java.math.BigDecimal;

/**
 * 预警规则纯计算器，不依赖 Spring、Mapper 或页面参数。
 */
public class WarningRuleEvaluator
{
    public WarningEvaluationResult evaluate(WarningRule rule, WarningEvaluationInput input)
    {
        if (rule == null || input == null || !"0".equals(rule.getStatus()))
        {
            return WarningEvaluationResult.notMatched(rule, input);
        }
        if (rule.getIndicatorId() != null && input.getIndicatorId() != null
                && !rule.getIndicatorId().equals(input.getIndicatorId()))
        {
            return WarningEvaluationResult.notMatched(rule, input);
        }

        String condition = StringUtils.defaultString(rule.getTriggerCondition()).trim();
        boolean matched;
        if ("missing".equals(condition))
        {
            matched = !input.isValuePresent();
        }
        else if ("overdue".equals(condition))
        {
            matched = input.isOverdue();
        }
        else if ("outside_range".equals(condition))
        {
            matched = isOutsideRange(input.getCurrentValue(), rule.getThresholdValue(), rule.getThresholdValueMax());
        }
        else
        {
            matched = compare(input.getCurrentValue(), rule.getThresholdValue(), condition);
        }
        return matched ? WarningEvaluationResult.matched(rule, input) : WarningEvaluationResult.notMatched(rule, input);
    }

    private boolean isOutsideRange(BigDecimal value, BigDecimal min, BigDecimal max)
    {
        if (value == null || min == null || max == null)
        {
            return false;
        }
        return value.compareTo(min) < 0 || value.compareTo(max) > 0;
    }

    private boolean compare(BigDecimal value, BigDecimal threshold, String condition)
    {
        if (value == null || threshold == null)
        {
            return false;
        }
        int result = value.compareTo(threshold);
        if ("lt".equals(condition))
        {
            return result < 0;
        }
        if ("lte".equals(condition))
        {
            return result <= 0;
        }
        if ("gt".equals(condition))
        {
            return result > 0;
        }
        if ("gte".equals(condition))
        {
            return result >= 0;
        }
        if ("eq".equals(condition))
        {
            return result == 0;
        }
        if ("ne".equals(condition))
        {
            return result != 0;
        }
        return false;
    }
}
