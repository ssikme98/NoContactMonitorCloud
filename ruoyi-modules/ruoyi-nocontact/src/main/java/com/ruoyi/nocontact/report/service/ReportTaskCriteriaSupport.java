package com.ruoyi.nocontact.report.service;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.report.domain.ReportGenerationTask;

public final class ReportTaskCriteriaSupport
{
    private ReportTaskCriteriaSupport()
    {
    }

    public static void normalize(ReportGenerationTask task)
    {
        normalize(task, false);
    }

    public static void normalizeForGeneration(ReportGenerationTask task)
    {
        normalize(task, true);
    }

    private static void normalize(ReportGenerationTask task, boolean allowLegacyScope)
    {
        if (task == null)
        {
            throw new ServiceException("报告任务不能为空");
        }
        task.setGenerateMode(normalizeGenerateMode(task.getGenerateMode()));
        task.setReportPeriod(normalizePeriod(task.getReportPeriod(), task));
        normalizeScope(task, allowLegacyScope);
    }

    private static String normalizeGenerateMode(String generateMode)
    {
        String value = StringUtils.trimToEmpty(generateMode);
        if (StringUtils.isBlank(value))
        {
            return "manual";
        }
        if ("manual".equals(value))
        {
            return value;
        }
        throw new ServiceException("当前仅支持手动生成");
    }

    private static String normalizePeriod(String reportPeriod, ReportGenerationTask task)
    {
        String value = StringUtils.trimToEmpty(reportPeriod);
        if (StringUtils.isBlank(value))
        {
            throw new ServiceException("报告周期不能为空");
        }
        if (value.matches("\\d{4}"))
        {
            task.setReportPeriodType("year");
            return value;
        }
        if (value.matches("\\d{4}Q[1-4]"))
        {
            task.setReportPeriodType("quarter");
            return value;
        }
        if (value.matches("\\d{4}-(0[1-9]|1[0-2])"))
        {
            task.setReportPeriodType("month");
            return value;
        }
        throw new ServiceException("报告周期格式不支持");
    }

    private static void normalizeScope(ReportGenerationTask task, boolean allowLegacyScope)
    {
        String scope = StringUtils.trimToEmpty(task.getReportScope());
        if (StringUtils.isBlank(scope))
        {
            throw new ServiceException("报告范围不能为空");
        }
        task.setReportScopeLabel(scope);
        task.setScopeType(null);
        task.setScopeValue(null);
        task.setScopeRegionName(null);
        task.setScopeIndicatorId(null);
        if ("province".equals(scope) || "全省".equals(scope))
        {
            task.setScopeType("province");
            task.setScopeValue("province");
            task.setReportScopeLabel("全省");
            return;
        }
        if (scope.startsWith("region:"))
        {
            String regionName = StringUtils.trimToEmpty(scope.substring("region:".length()));
            if (StringUtils.isBlank(regionName))
            {
                throw new ServiceException("指定地区不能为空");
            }
            task.setScopeType("region");
            task.setScopeValue(regionName);
            task.setScopeRegionName(regionName);
            task.setReportScopeLabel(regionName);
            return;
        }
        if (scope.startsWith("indicator:"))
        {
            String indicatorId = StringUtils.trimToEmpty(scope.substring("indicator:".length()));
            if (!StringUtils.isNumeric(indicatorId))
            {
                throw new ServiceException("指定指标不能为空");
            }
            task.setScopeType("indicator");
            task.setScopeValue(indicatorId);
            task.setScopeIndicatorId(Long.valueOf(indicatorId));
            task.setReportScopeLabel("指标#" + indicatorId);
            return;
        }
        if (looksLikeRegionName(scope))
        {
            task.setScopeType("region");
            task.setScopeValue(scope);
            task.setScopeRegionName(scope);
            task.setReportScopeLabel(scope);
            return;
        }
        if (allowLegacyScope)
        {
            task.setScopeType("legacy");
            task.setScopeValue(scope);
            task.setReportScopeLabel(scope);
            return;
        }
        throw new ServiceException("报告范围格式不支持");
    }

    private static boolean looksLikeRegionName(String scope)
    {
        return !scope.startsWith("指定")
                && (scope.endsWith("市") || scope.endsWith("州") || scope.endsWith("区") || scope.endsWith("县")
                || scope.endsWith("旗") || scope.endsWith("盟"));
    }
}
