package com.ruoyi.nocontact.report.service;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.report.domain.ReportGenerationTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportTaskCriteriaSupportTest
{
    @Test
    void normalizeResolvesQuarterAndProvinceScope()
    {
        ReportGenerationTask task = new ReportGenerationTask();
        task.setGenerateMode("manual");
        task.setReportPeriod("2026Q2");
        task.setReportScope("全省");

        ReportTaskCriteriaSupport.normalize(task);

        assertEquals("manual", task.getGenerateMode());
        assertEquals("quarter", task.getReportPeriodType());
        assertEquals("province", task.getScopeType());
        assertEquals("全省", task.getReportScopeLabel());
    }

    @Test
    void normalizeResolvesRegionAndIndicatorScopes()
    {
        ReportGenerationTask regionTask = new ReportGenerationTask();
        regionTask.setReportPeriod("2026-06");
        regionTask.setReportScope("region:长沙市");
        ReportTaskCriteriaSupport.normalize(regionTask);
        assertEquals("manual", regionTask.getGenerateMode());
        assertEquals("month", regionTask.getReportPeriodType());
        assertEquals("region", regionTask.getScopeType());
        assertEquals("长沙市", regionTask.getScopeRegionName());

        ReportGenerationTask indicatorTask = new ReportGenerationTask();
        indicatorTask.setReportPeriod("2026");
        indicatorTask.setReportScope("indicator:1001");
        ReportTaskCriteriaSupport.normalize(indicatorTask);
        assertEquals("year", indicatorTask.getReportPeriodType());
        assertEquals("indicator", indicatorTask.getScopeType());
        assertEquals(Long.valueOf(1001L), indicatorTask.getScopeIndicatorId());
    }

    @Test
    void normalizeRejectsScheduledMode()
    {
        ReportGenerationTask task = new ReportGenerationTask();
        task.setGenerateMode("scheduled");
        task.setReportPeriod("2026Q2");
        task.setReportScope("全省");

        ServiceException exception = assertThrows(ServiceException.class, () -> ReportTaskCriteriaSupport.normalize(task));

        assertEquals("当前仅支持手动生成", exception.getMessage());
    }

    @Test
    void normalizeRejectsUnsupportedScope()
    {
        ReportGenerationTask task = new ReportGenerationTask();
        task.setReportPeriod("2026Q2");
        task.setReportScope("指定市州");

        ServiceException exception = assertThrows(ServiceException.class, () -> ReportTaskCriteriaSupport.normalize(task));

        assertEquals("报告范围格式不支持", exception.getMessage());
    }

    @Test
    void normalizeForGenerationAcceptsLegacyScope()
    {
        ReportGenerationTask task = new ReportGenerationTask();
        task.setReportPeriod("2026Q2");
        task.setReportScope("营商专班重点监测");

        ReportTaskCriteriaSupport.normalizeForGeneration(task);

        assertEquals("quarter", task.getReportPeriodType());
        assertEquals("legacy", task.getScopeType());
        assertEquals("营商专班重点监测", task.getReportScopeLabel());
    }
}
