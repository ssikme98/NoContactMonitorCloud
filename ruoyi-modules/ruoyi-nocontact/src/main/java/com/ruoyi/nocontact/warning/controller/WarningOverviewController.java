package com.ruoyi.nocontact.warning.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监测预警概览
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/warning/overview")
public class WarningOverviewController
{
    @GetMapping
    public AjaxResult overview()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("moduleName", "监测预警");
        data.put("status", "ready");
        return AjaxResult.success(data);
    }
}
