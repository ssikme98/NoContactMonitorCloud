package com.ruoyi.survey.service;

import com.ruoyi.survey.domain.vo.SurveySatisfactionAnalytics;
import java.io.IOException;

/**
 * 满意度统计分析Service接口
 *
 * @author ruoyi
 */
public interface ISurveyAnalyticsService
{
    public SurveySatisfactionAnalytics selectSatisfactionAnalytics(Long taskId);

    public byte[] buildSatisfactionReport(Long taskId) throws IOException;
}
