package com.ruoyi.nocontact.warning.engine;

import com.ruoyi.nocontact.warning.domain.WarningRule;
import java.math.BigDecimal;

/**
 * 规则计算结果。
 */
public class WarningEvaluationResult
{
    private final boolean matched;
    private final WarningRule rule;
    private final WarningEvaluationInput input;
    private final BigDecimal currentValue;
    private final BigDecimal thresholdValue;

    private WarningEvaluationResult(boolean matched, WarningRule rule, WarningEvaluationInput input)
    {
        this.matched = matched;
        this.rule = rule;
        this.input = input;
        this.currentValue = input == null ? null : input.getCurrentValue();
        this.thresholdValue = rule == null ? null : rule.getThresholdValue();
    }

    public static WarningEvaluationResult matched(WarningRule rule, WarningEvaluationInput input)
    {
        return new WarningEvaluationResult(true, rule, input);
    }

    public static WarningEvaluationResult notMatched(WarningRule rule, WarningEvaluationInput input)
    {
        return new WarningEvaluationResult(false, rule, input);
    }

    public boolean isMatched()
    {
        return matched;
    }

    public WarningRule getRule()
    {
        return rule;
    }

    public WarningEvaluationInput getInput()
    {
        return input;
    }

    public BigDecimal getCurrentValue()
    {
        return currentValue;
    }

    public BigDecimal getThresholdValue()
    {
        return thresholdValue;
    }
}
