package com.ruoyi.survey.domain.vo;

import java.io.Serializable;

/**
 * 调研任务填报追踪汇总
 *
 * @author ruoyi
 */
public class SurveyTaskTrackingSummary implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long totalCount;

    private Long submittedCount;

    private Long unsubmittedCount;

    private Double responseRate;

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

    public Long getUnsubmittedCount()
    {
        return unsubmittedCount;
    }

    public void setUnsubmittedCount(Long unsubmittedCount)
    {
        this.unsubmittedCount = unsubmittedCount;
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
