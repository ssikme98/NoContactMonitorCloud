package com.ruoyi.nocontact.survey.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSample;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskTrackingDetail;
import com.ruoyi.nocontact.survey.service.ISurveyAnalyticsService;
import com.ruoyi.nocontact.survey.service.ISurveyTaskService;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调研任务Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/task")
public class SurveyTaskController extends BaseController
{
    @Autowired
    private ISurveyTaskService taskService;

    @Autowired
    private ISurveyAnalyticsService analyticsService;

    @RequiresPermissions("survey:task:list")
    @GetMapping("/list")
    public TableDataInfo list(SurveyTask task)
    {
        startPage();
        List<SurveyTask> list = taskService.selectTaskList(task);
        return getDataTable(list);
    }

    @RequiresPermissions("survey:task:query")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        return success(taskService.selectTaskById(taskId));
    }

    @RequiresPermissions("survey:task:query")
    @GetMapping("/{taskId}/track/summary")
    public AjaxResult trackSummary(@PathVariable Long taskId)
    {
        return success(taskService.selectTrackingSummary(taskId));
    }

    @RequiresPermissions("survey:task:query")
    @GetMapping("/{taskId}/track/regions")
    public AjaxResult trackRegions(@PathVariable Long taskId)
    {
        return success(taskService.selectTrackingRegionStats(taskId));
    }

    @RequiresPermissions("survey:task:query")
    @GetMapping("/{taskId}/track/details")
    public TableDataInfo trackDetails(@PathVariable Long taskId, SurveyTaskTrackingDetail detail)
    {
        detail.setTaskId(taskId);
        startPage();
        List<SurveyTaskTrackingDetail> list = taskService.selectTrackingDetailList(detail);
        return getDataTable(list);
    }

    @RequiresPermissions("survey:task:query")
    @GetMapping("/{taskId}/track/trend")
    public AjaxResult trackTrend(@PathVariable Long taskId)
    {
        return success(taskService.selectTrackingTrend(taskId));
    }

    @RequiresPermissions("survey:task:query")
    @GetMapping("/{taskId}/analytics/satisfaction")
    public AjaxResult satisfactionAnalytics(@PathVariable Long taskId)
    {
        return success(analyticsService.selectSatisfactionAnalytics(taskId));
    }

    @RequiresPermissions("survey:task:query")
    @GetMapping("/{taskId}/analytics/report")
    public void satisfactionReport(@PathVariable Long taskId, HttpServletResponse response) throws IOException
    {
        byte[] data = analyticsService.buildSatisfactionReport(taskId);
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=survey-satisfaction-" + taskId + ".docx");
        response.getOutputStream().write(data);
    }

    @RequiresPermissions("survey:task:add")
    @Log(title = "调研任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SurveyTask task)
    {
        task.setCreateBy(SecurityUtils.getUsername());
        return toAjax(taskService.insertTask(task));
    }

    @RequiresPermissions("survey:task:dispatch")
    @Log(title = "调研任务发卷", businessType = BusinessType.UPDATE)
    @PostMapping("/{taskId}/dispatch")
    public AjaxResult dispatch(@PathVariable Long taskId)
    {
        return toAjax(taskService.dispatchTask(taskId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("survey:task:query")
    @GetMapping("/sample/{sampleId}/qrcode")
    public void qrCode(@PathVariable Long sampleId, HttpServletResponse response) throws IOException, WriterException
    {
        SurveyTaskSample sample = taskService.selectSampleById(sampleId);
        if (sample == null)
        {
            throw new ServiceException("任务样本不存在");
        }
        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=survey-sample-" + sampleId + ".png");
        BitMatrix bitMatrix = new QRCodeWriter().encode(sample.getQrContent(), BarcodeFormat.QR_CODE, 260, 260);
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", response.getOutputStream());
    }

    @RequiresPermissions("survey:task:remove")
    @Log(title = "调研任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(taskService.deleteTaskByIds(taskIds));
    }
}
