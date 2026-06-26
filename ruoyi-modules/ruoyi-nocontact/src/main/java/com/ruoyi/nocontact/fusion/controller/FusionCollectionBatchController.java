package com.ruoyi.nocontact.fusion.controller;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.service.IFusionCollectionBatchService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据采集批次Controller
 */
@RestController
@RequestMapping("/fusion/collection")
public class FusionCollectionBatchController extends BaseController
{
    @Autowired
    private IFusionCollectionBatchService batchService;

    @RequiresPermissions("fusion:collection:list")
    @GetMapping("/list")
    public TableDataInfo list(FusionCollectionBatch batch)
    {
        startPage();
        List<FusionCollectionBatch> list = batchService.selectBatchList(batch);
        return getDataTable(list);
    }

    @RequiresPermissions("fusion:collection:query")
    @GetMapping("/{batchId}")
    public AjaxResult getInfo(@PathVariable Long batchId)
    {
        return success(batchService.selectBatchById(batchId));
    }

    @RequiresPermissions("fusion:collection:query")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(batchService.selectBatchSummary());
    }

    @RequiresPermissions("fusion:collection:add")
    @Log(title = "数据采集批次", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody FusionCollectionBatch batch)
    {
        return toAjax(batchService.submitBatch(batch, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("fusion:collection:audit")
    @Log(title = "数据采集审核通过", businessType = BusinessType.UPDATE)
    @PutMapping("/{batchId}/approve")
    public AjaxResult approve(@PathVariable Long batchId, @RequestParam(required = false) String opinion)
    {
        return toAjax(batchService.approveBatch(batchId, opinion, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("fusion:collection:audit")
    @Log(title = "数据采集审核驳回", businessType = BusinessType.UPDATE)
    @PutMapping("/{batchId}/reject")
    public AjaxResult reject(@PathVariable Long batchId, @RequestParam(required = false) String opinion)
    {
        return toAjax(batchService.rejectBatch(batchId, opinion, SecurityUtils.getUsername()));
    }
}
