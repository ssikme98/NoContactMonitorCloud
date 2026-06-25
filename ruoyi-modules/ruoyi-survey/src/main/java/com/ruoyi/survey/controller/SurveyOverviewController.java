package com.ruoyi.survey.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问卷模块概览
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/overview")
public class SurveyOverviewController
{
    @RequiresPermissions("survey:overview:query")
    @GetMapping
    public AjaxResult overview()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("serviceName", "ruoyi-survey");
        data.put("moduleName", "问卷调研管理");
        data.put("status", "ready");
        data.put("port", 9204);
        return AjaxResult.success(data);
    }
}
