package com.ruoyi.survey.domain.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 题目答案分布
 *
 * @author ruoyi
 */
public class SurveyQuestionDistribution implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long questionId;

    private String questionTitle;

    private String questionType;

    private List<SurveyDistributionItem> items;

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public String getQuestionTitle()
    {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle)
    {
        this.questionTitle = questionTitle;
    }

    public String getQuestionType()
    {
        return questionType;
    }

    public void setQuestionType(String questionType)
    {
        this.questionType = questionType;
    }

    public List<SurveyDistributionItem> getItems()
    {
        return items;
    }

    public void setItems(List<SurveyDistributionItem> items)
    {
        this.items = items;
    }
}
