package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import com.ruoyi.nocontact.warning.mapper.WarningRuleMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WarningRuleServiceImplTest
{
    @Mock
    private WarningRuleMapper ruleMapper;

    private WarningRuleServiceImpl service;

    @BeforeEach
    void setUp()
    {
        service = new WarningRuleServiceImpl();
        ReflectionTestUtils.setField(service, "ruleMapper", ruleMapper);
    }

    @Test
    void outsideRangeRuleRequiresMaxThreshold()
    {
        WarningRule rule = rule("outside_range");
        rule.setThresholdValue(new BigDecimal("60"));

        assertThrows(ServiceException.class, () -> service.insertRule(rule));
        verify(ruleMapper, never()).insertRule(any(WarningRule.class));
    }

    @Test
    void unknownConditionIsRejected()
    {
        WarningRule rule = rule("between");

        assertThrows(ServiceException.class, () -> service.updateRule(rule));
        verify(ruleMapper, never()).updateRule(any(WarningRule.class));
    }

    @Test
    void scheduledRuleRequiresResponsibleUnitScope()
    {
        WarningRule rule = rule("missing");

        assertThrows(ServiceException.class, () -> service.insertRule(rule));
        verify(ruleMapper, never()).insertRule(any(WarningRule.class));
    }

    @Test
    void statusUpdateRejectsInvalidStatus()
    {
        assertThrows(ServiceException.class, () -> service.updateRuleStatus(1L, "enabled", "admin"));
        verify(ruleMapper, never()).updateRuleStatus(any(WarningRule.class));
    }

    @Test
    void scheduledRuleRequiresPeriodType()
    {
        WarningRule rule = rule("overdue");
        rule.setResponsibleUnitId(200L);

        assertThrows(ServiceException.class, () -> service.insertRule(rule));
        verify(ruleMapper, never()).insertRule(any(WarningRule.class));
    }

    @Test
    void nonRangeRuleClearsMaxThresholdOnUpdate()
    {
        WarningRule rule = rule("lt");
        rule.setThresholdValueMax(new BigDecimal("90"));

        service.updateRule(rule);

        ArgumentCaptor<WarningRule> captor = ArgumentCaptor.forClass(WarningRule.class);
        verify(ruleMapper).updateRule(captor.capture());
        assertNull(captor.getValue().getThresholdValueMax());
    }

    private WarningRule rule(String condition)
    {
        WarningRule rule = new WarningRule();
        rule.setRuleId(1L);
        rule.setRuleName("数字政务能力预警");
        rule.setIndicatorId(1003L);
        rule.setIndicatorName("数字政务能力");
        rule.setWarningLevel("2");
        rule.setThresholdType("target");
        rule.setThresholdValue(new BigDecimal("80"));
        rule.setTriggerCondition(condition);
        rule.setTriggerFrequency("daily");
        rule.setPushChannels("site");
        rule.setStatus("0");
        return rule;
    }
}
