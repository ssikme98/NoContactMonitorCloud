package com.ruoyi.nocontact.survey.domain.vo;

import java.io.Serializable;

/**
 * 满意度聚合统计项
 *
 * @author ruoyi
 */
public class SurveySatisfactionStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String statName;

    private Long sampleCount;

    private Double score;

    public String getStatName()
    {
        return statName;
    }

    public void setStatName(String statName)
    {
        this.statName = statName;
    }

    public Long getSampleCount()
    {
        return sampleCount;
    }

    public void setSampleCount(Long sampleCount)
    {
        this.sampleCount = sampleCount;
    }

    public Double getScore()
    {
        return score;
    }

    public void setScore(Double score)
    {
        this.score = score;
    }
}
