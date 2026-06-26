package com.ruoyi.nocontact.warning.engine;

import com.ruoyi.nocontact.warning.domain.WarningRule;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarningRuleEvaluatorTest
{
    private final WarningRuleEvaluator evaluator = new WarningRuleEvaluator();

    @Test
    void activeLessThanRuleMatchesApprovedValueBelowThreshold()
    {
        WarningRule rule = rule("lt", new BigDecimal("80"), null);
        WarningEvaluationInput input = input(new BigDecimal("72"), true, false);

        WarningEvaluationResult result = evaluator.evaluate(rule, input);

        assertTrue(result.isMatched());
    }

    @Test
    void disabledRuleDoesNotMatch()
    {
        WarningRule rule = rule("lt", new BigDecimal("80"), null);
        rule.setStatus("1");
        WarningEvaluationInput input = input(new BigDecimal("72"), true, false);

        WarningEvaluationResult result = evaluator.evaluate(rule, input);

        assertFalse(result.isMatched());
    }

    @Test
    void rangeRuleMatchesValueOutsideRange()
    {
        WarningRule rule = rule("outside_range", new BigDecimal("60"), new BigDecimal("90"));
        WarningEvaluationInput input = input(new BigDecimal("95"), true, false);

        WarningEvaluationResult result = evaluator.evaluate(rule, input);

        assertTrue(result.isMatched());
    }

    @Test
    void missingRuleMatchesAbsentValue()
    {
        WarningRule rule = rule("missing", BigDecimal.ZERO, null);
        WarningEvaluationInput input = input(null, false, false);

        WarningEvaluationResult result = evaluator.evaluate(rule, input);

        assertTrue(result.isMatched());
    }

    @Test
    void overdueRuleMatchesOverdueInput()
    {
        WarningRule rule = rule("overdue", BigDecimal.ZERO, null);
        WarningEvaluationInput input = input(null, false, true);

        WarningEvaluationResult result = evaluator.evaluate(rule, input);

        assertTrue(result.isMatched());
    }

    private WarningRule rule(String condition, BigDecimal threshold, BigDecimal max)
    {
        WarningRule rule = new WarningRule();
        rule.setRuleId(1L);
        rule.setRuleName("数字政务能力预警");
        rule.setIndicatorId(1003L);
        rule.setIndicatorName("数字政务能力");
        rule.setWarningLevel("2");
        rule.setTriggerCondition(condition);
        rule.setThresholdValue(threshold);
        rule.setThresholdValueMax(max);
        rule.setStatus("0");
        return rule;
    }

    private WarningEvaluationInput input(BigDecimal value, boolean present, boolean overdue)
    {
        WarningEvaluationInput input = new WarningEvaluationInput();
        input.setIndicatorId(1003L);
        input.setIndicatorName("数字政务能力");
        input.setResponsibleUnitId(200L);
        input.setResponsibleUnitName("省数据局");
        input.setRegionCode("433100");
        input.setRegionName("湘西州");
        input.setPeriodKey("2026-06");
        input.setCurrentValue(value);
        input.setValuePresent(present);
        input.setOverdue(overdue);
        return input;
    }
}
