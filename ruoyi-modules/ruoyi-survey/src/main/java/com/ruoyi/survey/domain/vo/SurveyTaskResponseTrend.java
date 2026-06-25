package com.ruoyi.survey.domain.vo;

import java.io.Serializable;

/**
 * 调研任务回收趋势
 *
 * @author ruoyi
 */
public class SurveyTaskResponseTrend implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String submitDate;

    private Long submittedCount;

    public String getSubmitDate()
    {
        return submitDate;
    }

    public void setSubmitDate(String submitDate)
    {
        this.submitDate = submitDate;
    }

    public Long getSubmittedCount()
    {
        return submittedCount;
    }

    public void setSubmittedCount(Long submittedCount)
    {
        this.submittedCount = submittedCount;
    }
}
