package com.ruoyi.nocontact.warning.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.warning.domain.WarningRule;
import com.ruoyi.nocontact.warning.domain.dto.WarningRuleForm;
import com.ruoyi.nocontact.warning.service.IWarningRuleService;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预警规则Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/warning/rule")
public class WarningRuleController extends BaseController
{
    @Autowired
    private IWarningRuleService ruleService;

    @RequiresPermissions("nocontact:warning:rule:list")
    @GetMapping("/list")
    public TableDataInfo list(WarningRule rule)
    {
        startPage();
        List<WarningRule> list = ruleService.selectRuleList(rule);
        return getDataTable(list);
    }

    @RequiresPermissions("nocontact:warning:rule:query")
    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@PathVariable Long ruleId)
    {
        return success(ruleService.selectRuleById(ruleId));
    }

    @Log(title = "预警规则", businessType = BusinessType.EXPORT)
    @RequiresPermissions("nocontact:warning:rule:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WarningRule rule)
    {
        List<WarningRule> list = ruleService.selectRuleList(rule);
        ExcelUtil<WarningRule> util = new ExcelUtil<WarningRule>(WarningRule.class);
        util.exportExcel(response, list, "预警规则");
    }

    @RequiresPermissions("nocontact:warning:rule:add")
    @Log(title = "预警规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WarningRuleForm form)
    {
        WarningRule rule = form.toEntity();
        rule.setRuleId(null);
        rule.setCreateBy(SecurityUtils.getUsername());
        return toAjax(ruleService.insertRule(rule));
    }

    @RequiresPermissions("nocontact:warning:rule:edit")
    @Log(title = "预警规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WarningRuleForm form)
    {
        WarningRule rule = form.toEntity();
        if (rule.getRuleId() == null)
        {
            return error("规则ID不能为空");
        }
        rule.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(ruleService.updateRule(rule));
    }

    @RequiresPermissions("nocontact:warning:rule:edit")
    @Log(title = "预警规则状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{ruleId}/status/{status}")
    public AjaxResult changeStatus(@PathVariable Long ruleId, @PathVariable String status)
    {
        return toAjax(ruleService.updateRuleStatus(ruleId, status, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:warning:rule:remove")
    @Log(title = "预警规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(ruleService.deleteRuleByIds(ruleIds));
    }
}
