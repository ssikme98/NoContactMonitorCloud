package com.ruoyi.nocontact.fusion.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionTask;
import com.ruoyi.nocontact.fusion.service.IFusionCollectionTaskService;
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
 * 数据采集任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fusion/task")
public class FusionCollectionTaskController extends BaseController
{
    @Autowired
    private IFusionCollectionTaskService taskService;

    @RequiresPermissions("fusion:task:list")
    @GetMapping("/list")
    public TableDataInfo list(FusionCollectionTask task)
    {
        startPage();
        List<FusionCollectionTask> list = taskService.selectTaskList(task);
        return getDataTable(list);
    }

    @RequiresPermissions("fusion:task:query")
    @GetMapping("/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        return success(taskService.selectTaskById(taskId));
    }

    @RequiresPermissions("fusion:task:query")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(taskService.selectTaskSummary());
    }

    @Log(title = "数据采集任务", businessType = BusinessType.EXPORT)
    @RequiresPermissions("fusion:task:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FusionCollectionTask task)
    {
        List<FusionCollectionTask> list = taskService.selectTaskList(task);
        ExcelUtil<FusionCollectionTask> util = new ExcelUtil<FusionCollectionTask>(FusionCollectionTask.class);
        util.exportExcel(response, list, "数据采集任务");
    }

    @RequiresPermissions("fusion:task:add")
    @Log(title = "数据采集任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FusionCollectionTask task)
    {
        task.setCreateBy(SecurityUtils.getUsername());
        return toAjax(taskService.insertTask(task));
    }

    @RequiresPermissions("fusion:task:edit")
    @Log(title = "数据采集任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FusionCollectionTask task)
    {
        task.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(taskService.updateTask(task));
    }

    @RequiresPermissions("fusion:task:edit")
    @Log(title = "数据采集任务状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{taskId}/status/{status}")
    public AjaxResult changeStatus(@PathVariable Long taskId, @PathVariable String status)
    {
        return toAjax(taskService.updateTaskStatus(taskId, status, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("fusion:task:remove")
    @Log(title = "数据采集任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(taskService.deleteTaskByIds(taskIds));
    }
}
