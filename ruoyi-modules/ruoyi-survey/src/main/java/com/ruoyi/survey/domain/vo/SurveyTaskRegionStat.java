package com.ruoyi.survey.domain.vo;

import java.io.Serializable;

/**
 * 调研任务地区回收统计
 *
 * @author ruoyi
 */
public class SurveyTaskRegionStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String regionName;

    private Long totalCount;

    private Long submittedCount;

    private Double responseRate;

    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    public Long getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Long totalCount)
    {
        this.totalCount = totalCount;
    }

    public Long getSubmittedCount()
    {
        return submittedCount;
    }

    public void setSubmittedCount(Long submittedCount)
    {
        this.submittedCount = submittedCount;
    }

    public Double getResponseRate()
    {
        return responseRate;
    }

    public void setResponseRate(Double responseRate)
    {
        this.responseRate = responseRate;
    }
}
