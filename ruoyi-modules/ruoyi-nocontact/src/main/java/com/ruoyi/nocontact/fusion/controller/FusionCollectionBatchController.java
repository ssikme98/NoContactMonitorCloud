package com.ruoyi.nocontact.fusion.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionImportFailure;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionImportRow;
import com.ruoyi.nocontact.fusion.service.IFusionCollectionBatchService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据采集批次Controller
 */
@RestController
@RequestMapping("/fusion/collection")
public class FusionCollectionBatchController extends BaseController
{
    @Autowired
    private IFusionCollectionBatchService batchService;

    @RequiresPermissions("nocontact:fusion:collection:list")
    @GetMapping("/list")
    public TableDataInfo list(FusionCollectionBatch batch)
    {
        startPage();
        List<FusionCollectionBatch> list = batchService.selectBatchList(batch);
        return getDataTable(list);
    }

    @RequiresPermissions("nocontact:fusion:collection:query")
    @GetMapping("/{batchId}")
    public AjaxResult getInfo(@PathVariable Long batchId)
    {
        return success(batchService.selectBatchById(batchId));
    }

    @RequiresPermissions("nocontact:fusion:collection:query")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(batchService.selectBatchSummary());
    }

    @RequiresPermissions("nocontact:fusion:collection:query")
    @GetMapping("/importFailures")
    public TableDataInfo importFailures(FusionCollectionImportFailure failure)
    {
        startPage();
        List<FusionCollectionImportFailure> list = batchService.selectImportFailures(failure);
        return getDataTable(list);
    }

    @RequiresPermissions("nocontact:fusion:collection:add")
    @Log(title = "数据采集批次", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody FusionCollectionBatch batch)
    {
        return toAjax(batchService.submitBatch(batch, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:fusion:collection:import")
    @Log(title = "数据采集导入", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<FusionCollectionImportRow> util = new ExcelUtil<FusionCollectionImportRow>(FusionCollectionImportRow.class);
        List<FusionCollectionImportRow> rows = util.importExcel(file.getInputStream());
        Map<String, Object> result = batchService.importCollection(rows, SecurityUtils.getUsername());
        String msg = "导入完成：成功 " + result.get("successRows") + " 行，失败 " + result.get("failureRows")
                + " 行，生成待审核批次 " + result.get("batchCount") + " 个。";
        if (((Integer) result.get("failureRows")) > 0)
        {
            msg += "失败明细可在页面查看，导入批次号：" + result.get("importBatchName");
        }
        return AjaxResult.success(msg, result);
    }

    @RequiresPermissions("nocontact:fusion:collection:import")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws IOException
    {
        ExcelUtil<FusionCollectionImportRow> util = new ExcelUtil<FusionCollectionImportRow>(FusionCollectionImportRow.class);
        util.importTemplateExcel(response, "采集导入模板");
    }

    @RequiresPermissions("nocontact:fusion:collection:audit")
    @Log(title = "数据采集审核通过", businessType = BusinessType.UPDATE)
    @PutMapping("/{batchId}/approve")
    public AjaxResult approve(@PathVariable Long batchId, @RequestParam(required = false) String opinion)
    {
        return toAjax(batchService.approveBatch(batchId, opinion, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:fusion:collection:audit")
    @Log(title = "数据采集审核驳回", businessType = BusinessType.UPDATE)
    @PutMapping("/{batchId}/reject")
    public AjaxResult reject(@PathVariable Long batchId, @RequestParam(required = false) String opinion)
    {
        return toAjax(batchService.rejectBatch(batchId, opinion, SecurityUtils.getUsername()));
    }
}
