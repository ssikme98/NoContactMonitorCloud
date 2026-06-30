package com.ruoyi.nocontact.report.controller;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.report.domain.ReportGenerationTask;
import com.ruoyi.nocontact.report.domain.ReportGenerationSnapshot;
import com.ruoyi.nocontact.report.domain.ReportTemplate;
import com.ruoyi.nocontact.report.service.IReportGenerationService;
import com.ruoyi.nocontact.report.service.ReportTaskCriteriaSupport;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
    private IReportGenerationService reportGenerationService;

    @RequiresPermissions("nocontact:report:template:list")
    @GetMapping("/template/list")
    public TableDataInfo templateList(ReportTemplate template)
    {
        startPage();
        return getDataTable(reportGenerationService.selectTemplateList(template));
    }

    @RequiresPermissions("nocontact:report:template:query")
    @GetMapping("/template/{templateId}")
    public AjaxResult templateInfo(@PathVariable Long templateId)
    {
        return success(reportGenerationService.selectTemplateById(templateId));
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
        return toAjax(reportGenerationService.insertTemplate(template));
    }

    @RequiresPermissions("nocontact:report:template:edit")
    @Log(title = "报告模板", businessType = BusinessType.UPDATE)
    @PutMapping("/template")
    public AjaxResult editTemplate(@Valid @RequestBody ReportTemplate template)
    {
        template.setUpdateBy(SecurityUtils.getUsername());
        template.setUpdateTime(DateUtils.getNowDate());
        return toAjax(reportGenerationService.updateTemplate(template));
    }

    @RequiresPermissions("nocontact:report:template:remove")
    @Log(title = "报告模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/template/{templateIds}")
    public AjaxResult removeTemplate(@PathVariable Long[] templateIds)
    {
        return toAjax(reportGenerationService.deleteTemplateByIds(templateIds));
    }

    @RequiresPermissions("nocontact:report:task:list")
    @GetMapping("/task/list")
    public TableDataInfo taskList(ReportGenerationTask task)
    {
        startPage();
        return getDataTable(reportGenerationService.selectTaskList(task));
    }

    @RequiresPermissions("nocontact:report:task:query")
    @GetMapping("/task/{taskId}")
    public AjaxResult taskInfo(@PathVariable Long taskId)
    {
        return success(reportGenerationService.selectTaskById(taskId));
    }

    @RequiresPermissions("nocontact:report:task:query")
    @GetMapping("/task/{taskId}/snapshot/list")
    public AjaxResult snapshotList(@PathVariable Long taskId)
    {
        List<ReportGenerationSnapshot> snapshots = reportGenerationService.selectSnapshotList(taskId);
        return success(snapshots);
    }

    @RequiresPermissions("nocontact:report:task:add")
    @Log(title = "报告生成任务", businessType = BusinessType.INSERT)
    @PostMapping("/task")
    public AjaxResult addTask(@Valid @RequestBody ReportGenerationTask task)
    {
        ReportTemplate template = requireTemplate(task.getTemplateId());
        ReportTaskCriteriaSupport.normalize(task);
        task.setCreateBy(SecurityUtils.getUsername());
        task.setCreateTime(DateUtils.getNowDate());
        task.setTemplateName(template.getTemplateName());
        if (StringUtils.isBlank(task.getGenerateMode()))
        {
            task.setGenerateMode("manual");
        }
        if (StringUtils.isBlank(task.getTaskStatus()))
        {
            task.setTaskStatus("pending");
        }
        return toAjax(reportGenerationService.insertTask(task));
    }

    @RequiresPermissions("nocontact:report:task:edit")
    @Log(title = "报告生成", businessType = BusinessType.UPDATE)
    @PutMapping("/task/{taskId}/generate")
    public AjaxResult generate(@PathVariable Long taskId)
    {
        return success(reportGenerationService.generate(taskId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:report:task:query")
    @Log(title = "报告下载", businessType = BusinessType.EXPORT)
    @PostMapping("/task/{taskId}/download/{fileType}")
    public void download(@PathVariable Long taskId, @PathVariable String fileType, HttpServletResponse response) throws IOException
    {
        Path file = reportGenerationService.resolveGeneratedFile(taskId, fileType);
        writeDownloadResponse(file, fileType, response);
    }

    @RequiresPermissions("nocontact:report:task:query")
    @Log(title = "报告快照下载", businessType = BusinessType.EXPORT)
    @PostMapping("/task/snapshot/{snapshotId}/download/{fileType}")
    public void downloadSnapshot(@PathVariable Long snapshotId, @PathVariable String fileType, HttpServletResponse response) throws IOException
    {
        Path file = reportGenerationService.resolveSnapshotGeneratedFile(snapshotId, fileType);
        writeDownloadResponse(file, fileType, response);
    }

    private void writeDownloadResponse(Path file, String fileType, HttpServletResponse response) throws IOException
    {
        String fileName = file.getFileName().toString();
        response.setContentType(contentType(fileType));
        FileUtils.setAttachmentResponseHeader(response, fileName);
        FileUtils.writeBytes(file.toString(), response.getOutputStream());
    }

    private String contentType(String fileType)
    {
        if ("word".equals(fileType))
        {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if ("excel".equals(fileType))
        {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        throw new ServiceException("报告类型不支持");
    }

    private ReportTemplate requireTemplate(Long templateId)
    {
        if (templateId == null)
        {
            throw new ServiceException("模板不能为空");
        }
        ReportTemplate template = reportGenerationService.selectTemplateById(templateId);
        if (template == null)
        {
            throw new ServiceException("报告模板不存在");
        }
        return template;
    }
}
