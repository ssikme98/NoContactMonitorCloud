package com.ruoyi.nocontact.report.controller;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.report.domain.ReportGenerationTask;
import com.ruoyi.nocontact.report.domain.ReportTemplate;
import com.ruoyi.nocontact.report.mapper.ReportGenerationMapper;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
public class ReportGenerationController extends BaseController
{
    @Autowired
    private ReportGenerationMapper reportMapper;

    @RequiresPermissions("nocontact:report:template:list")
    @GetMapping("/template/list")
    public TableDataInfo templateList(ReportTemplate template)
    {
        startPage();
        return getDataTable(reportMapper.selectTemplateList(template));
    }

    @RequiresPermissions("nocontact:report:template:query")
    @GetMapping("/template/{templateId}")
    public AjaxResult templateInfo(@PathVariable Long templateId)
    {
        return success(reportMapper.selectTemplateById(templateId));
    }

    @RequiresPermissions("nocontact:report:template:add")
    @Log(title = "报告模板", businessType = BusinessType.INSERT)
    @PostMapping("/template")
    public AjaxResult addTemplate(@Valid @RequestBody ReportTemplate template)
    {
        template.setCreateBy(SecurityUtils.getUsername());
        template.setCreateTime(DateUtils.getNowDate());
        if (template.getVersionNo() == null)
        {
            template.setVersionNo(1);
        }
        if (StringUtils.isBlank(template.getStatus()))
        {
            template.setStatus("0");
        }
        return toAjax(reportMapper.insertTemplate(template));
    }

    @RequiresPermissions("nocontact:report:template:edit")
    @Log(title = "报告模板", businessType = BusinessType.UPDATE)
    @PutMapping("/template")
    public AjaxResult editTemplate(@Valid @RequestBody ReportTemplate template)
    {
        template.setUpdateBy(SecurityUtils.getUsername());
        template.setUpdateTime(DateUtils.getNowDate());
        return toAjax(reportMapper.updateTemplate(template));
    }

    @RequiresPermissions("nocontact:report:template:remove")
    @Log(title = "报告模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/template/{templateIds}")
    public AjaxResult removeTemplate(@PathVariable Long[] templateIds)
    {
        return toAjax(reportMapper.deleteTemplateByIds(templateIds));
    }

    @RequiresPermissions("nocontact:report:task:list")
    @GetMapping("/task/list")
    public TableDataInfo taskList(ReportGenerationTask task)
    {
        startPage();
        return getDataTable(reportMapper.selectTaskList(task));
    }

    @RequiresPermissions("nocontact:report:task:query")
    @GetMapping("/task/{taskId}")
    public AjaxResult taskInfo(@PathVariable Long taskId)
    {
        return success(reportMapper.selectTaskById(taskId));
    }

    @RequiresPermissions("nocontact:report:task:add")
    @Log(title = "报告生成任务", businessType = BusinessType.INSERT)
    @PostMapping("/task")
    public AjaxResult addTask(@Valid @RequestBody ReportGenerationTask task)
    {
        task.setCreateBy(SecurityUtils.getUsername());
        task.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(task.getTaskStatus()))
        {
            task.setTaskStatus("pending");
        }
        return toAjax(reportMapper.insertTask(task));
    }

    @RequiresPermissions("nocontact:report:task:edit")
    @Log(title = "报告生成", businessType = BusinessType.UPDATE)
    @PutMapping("/task/{taskId}/generate")
    public AjaxResult generate(@PathVariable Long taskId)
    {
        ReportGenerationTask task = reportMapper.selectTaskById(taskId);
        if (task == null)
        {
            throw new ServiceException("报告任务不存在");
        }
        task.setTaskStatus("completed");
        task.setGeneratedTime(DateUtils.getNowDate());
        task.setGeneratedFileName(task.getTaskName() + ".docx");
        task.setSnapshotContent("报告范围：" + safe(task.getReportScope()) + "；周期：" + safe(task.getReportPeriod()) + "；模板：" + safe(task.getTemplateName()));
        task.setUpdateBy(SecurityUtils.getUsername());
        task.setUpdateTime(DateUtils.getNowDate());
        reportMapper.updateTask(task);
        return success(task);
    }

    private String safe(String value)
    {
        return value == null ? "" : value;
    }
}
