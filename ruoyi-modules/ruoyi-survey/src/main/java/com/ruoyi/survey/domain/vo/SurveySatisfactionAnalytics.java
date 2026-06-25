package com.ruoyi.survey.domain.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 满意度统计分析
 *
 * @author ruoyi
 */
public class SurveySatisfactionAnalytics implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long taskId;

    private Long responseCount;

    private Double overallScore;

    private List<SurveySatisfactionStat> regionStats;

    private List<SurveySatisfactionStat> industryStats;

    private List<SurveySatisfactionStat> dimensionStats;

    private List<SurveySatisfactionStat> scaleStats;

    private List<SurveyQuestionDistribution> questionDistributions;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getResponseCount()
    {
        return responseCount;
    }

    public void setResponseCount(Long responseCount)
    {
        this.responseCount = responseCount;
    }

    public Double getOverallScore()
    {
        return overallScore;
    }

    public void setOverallScore(Double overallScore)
    {
        this.overallScore = overallScore;
    }

    public List<SurveySatisfactionStat> getRegionStats()
    {
        return regionStats;
    }

    public void setRegionStats(List<SurveySatisfactionStat> regionStats)
    {
        this.regionStats = regionStats;
    }

    public List<SurveySatisfactionStat> getIndustryStats()
    {
        return industryStats;
    }

    public void setIndustryStats(List<SurveySatisfactionStat> industryStats)
    {
        this.industryStats = industryStats;
    }

    public List<SurveySatisfactionStat> getDimensionStats()
    {
        return dimensionStats;
    }

    public void setDimensionStats(List<SurveySatisfactionStat> dimensionStats)
    {
        this.dimensionStats = dimensionStats;
    }

    public List<SurveySatisfactionStat> getScaleStats()
    {
        return scaleStats;
    }

    public void setScaleStats(List<SurveySatisfactionStat> scaleStats)
    {
        this.scaleStats = scaleStats;
    }

    public List<SurveyQuestionDistribution> getQuestionDistributions()
    {
        return questionDistributions;
    }

    public void setQuestionDistributions(List<SurveyQuestionDistribution> questionDistributions)
    {
        this.questionDistributions = questionDistributions;
    }
}
