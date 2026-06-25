package com.ruoyi.survey.domain.vo;

import java.io.Serializable;

/**
 * 满意度统计答题明细行
 *
 * @author ruoyi
 */
public class SurveyResponseAnswerRow implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long responseId;

    private Long taskId;

    private Long enterpriseId;

    private String regionName;

    private String industryCategory;

    private String enterpriseScale;

    private Long questionId;

    private String questionType;

    private String answerText;

    private String optionValue;

    private Integer scoreValue;

    public Long getResponseId()
    {
        return responseId;
    }

    public void setResponseId(Long responseId)
    {
        this.responseId = responseId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    public String getIndustryCategory()
    {
        return industryCategory;
    }

    public void setIndustryCategory(String industryCategory)
    {
        this.industryCategory = industryCategory;
    }

    public String getEnterpriseScale()
    {
        return enterpriseScale;
    }

    public void setEnterpriseScale(String enterpriseScale)
    {
        this.enterpriseScale = enterpriseScale;
    }

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public String getQuestionType()
    {
        return questionType;
    }

    public void setQuestionType(String questionType)
    {
        this.questionType = questionType;
    }

    public String getAnswerText()
    {
        return answerText;
    }

    public void setAnswerText(String answerText)
    {
        this.answerText = answerText;
    }

    public String getOptionValue()
    {
        return optionValue;
    }

    public void setOptionValue(String optionValue)
    {
        this.optionValue = optionValue;
    }

    public Integer getScoreValue()
    {
        return scoreValue;
    }

    public void setScoreValue(Integer scoreValue)
    {
        this.scoreValue = scoreValue;
    }
}
