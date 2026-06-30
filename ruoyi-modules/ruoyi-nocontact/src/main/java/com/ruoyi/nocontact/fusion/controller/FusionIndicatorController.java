package com.ruoyi.nocontact.fusion.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.Logical;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import com.ruoyi.nocontact.fusion.domain.dto.FusionIndicatorForm;
import com.ruoyi.nocontact.fusion.service.IFusionIndicatorService;
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
 * 营商监测指标Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fusion/indicator")
public class FusionIndicatorController extends BaseController
{
    @Autowired
    private IFusionIndicatorService indicatorService;

    @RequiresPermissions("nocontact:fusion:indicator:list")
    @GetMapping("/list")
    public TableDataInfo list(FusionIndicator indicator)
    {
        startPage();
        List<FusionIndicator> list = indicatorService.selectIndicatorList(indicator);
        return getDataTable(list);
    }

    @RequiresPermissions(value = {"nocontact:fusion:indicator:list", "nocontact:fusion:task:list", "nocontact:warning:rule:list"}, logical = Logical.OR)
    @GetMapping("/options")
    public AjaxResult options(FusionIndicator indicator)
    {
        indicator.setLifecycleStatus("enabled");
        indicator.setStatus("0");
        return success(indicatorService.selectIndicatorList(indicator));
    }

    @RequiresPermissions("nocontact:fusion:indicator:query")
    @GetMapping("/{indicatorId}")
    public AjaxResult getInfo(@PathVariable Long indicatorId)
    {
        return success(indicatorService.selectIndicatorById(indicatorId));
    }

    @Log(title = "营商监测指标", businessType = BusinessType.EXPORT)
    @RequiresPermissions("nocontact:fusion:indicator:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FusionIndicator indicator)
    {
        List<FusionIndicator> list = indicatorService.selectIndicatorList(indicator);
        ExcelUtil<FusionIndicator> util = new ExcelUtil<FusionIndicator>(FusionIndicator.class);
        util.exportExcel(response, list, "营商监测指标");
    }

    @RequiresPermissions("nocontact:fusion:indicator:add")
    @Log(title = "营商监测指标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FusionIndicatorForm form)
    {
        FusionIndicator indicator = form.toEntity();
        indicator.setIndicatorId(null);
        indicator.setCreateBy(SecurityUtils.getUsername());
        return toAjax(indicatorService.insertIndicator(indicator));
    }

    @RequiresPermissions("nocontact:fusion:indicator:edit")
    @Log(title = "营商监测指标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FusionIndicatorForm form)
    {
        FusionIndicator indicator = form.toEntity();
        if (indicator.getIndicatorId() == null)
        {
            return error("指标ID不能为空");
        }
        indicator.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(indicatorService.updateIndicator(indicator));
    }

    @RequiresPermissions("nocontact:fusion:indicator:add")
    @Log(title = "营商监测指标复制草稿", businessType = BusinessType.INSERT)
    @PostMapping("/{indicatorId}/copyDraft")
    public AjaxResult copyDraft(@PathVariable Long indicatorId)
    {
        return success(indicatorService.copyIndicatorDraft(indicatorId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:fusion:indicator:remove")
    @Log(title = "营商监测指标", businessType = BusinessType.DELETE)
    @DeleteMapping("/{indicatorIds}")
    public AjaxResult remove(@PathVariable Long[] indicatorIds)
    {
        return toAjax(indicatorService.deleteIndicatorByIds(indicatorIds));
    }
}
