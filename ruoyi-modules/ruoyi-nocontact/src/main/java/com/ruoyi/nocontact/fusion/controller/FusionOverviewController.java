package com.ruoyi.nocontact.fusion.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据融合概览
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fusion/overview")
public class FusionOverviewController
{
    @GetMapping
    public AjaxResult overview()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("moduleName", "数据融合");
        data.put("status", "ready");
        return AjaxResult.success(data);
    }
}
