package com.ruoyi.nocontact.fusion.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
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

    @RequiresPermissions("fusion:indicator:list")
    @GetMapping("/list")
    public TableDataInfo list(FusionIndicator indicator)
    {
        startPage();
        List<FusionIndicator> list = indicatorService.selectIndicatorList(indicator);
        return getDataTable(list);
    }

    @GetMapping("/options")
    public AjaxResult options(FusionIndicator indicator)
    {
        return success(indicatorService.selectIndicatorList(indicator));
    }

    @RequiresPermissions("fusion:indicator:query")
    @GetMapping("/{indicatorId}")
    public AjaxResult getInfo(@PathVariable Long indicatorId)
    {
        return success(indicatorService.selectIndicatorById(indicatorId));
    }

    @Log(title = "营商监测指标", businessType = BusinessType.EXPORT)
    @RequiresPermissions("fusion:indicator:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FusionIndicator indicator)
    {
        List<FusionIndicator> list = indicatorService.selectIndicatorList(indicator);
        ExcelUtil<FusionIndicator> util = new ExcelUtil<FusionIndicator>(FusionIndicator.class);
        util.exportExcel(response, list, "营商监测指标");
    }

    @RequiresPermissions("fusion:indicator:add")
    @Log(title = "营商监测指标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FusionIndicator indicator)
    {
        indicator.setCreateBy(SecurityUtils.getUsername());
        return toAjax(indicatorService.insertIndicator(indicator));
    }

    @RequiresPermissions("fusion:indicator:edit")
    @Log(title = "营商监测指标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FusionIndicator indicator)
    {
        indicator.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(indicatorService.updateIndicator(indicator));
    }

    @RequiresPermissions("fusion:indicator:remove")
    @Log(title = "营商监测指标", businessType = BusinessType.DELETE)
    @DeleteMapping("/{indicatorIds}")
    public AjaxResult remove(@PathVariable Long[] indicatorIds)
    {
        return toAjax(indicatorService.deleteIndicatorByIds(indicatorIds));
    }
}
