package com.ruoyi.nocontact.warning.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.nocontact.warning.service.IWarningEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预警评估内部调度入口。
 */
@RestController
@RequestMapping("/warning/evaluation")
public class WarningEvaluationController
{
    @Autowired
    private IWarningEvaluationService warningEvaluationService;

    @InnerAuth
    @PostMapping("/scheduled/{periodKey}")
    public R<Integer> evaluateScheduledRules(@PathVariable String periodKey)
    {
        return R.ok(warningEvaluationService.evaluateScheduledRules(periodKey, "job"));
    }
}
