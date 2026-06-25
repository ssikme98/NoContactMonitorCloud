package com.ruoyi.survey.domain;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷答案对象 survey_response_answer
 *
 * @author ruoyi
 */
public class SurveyResponseAnswer implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 答案ID */
    private Long answerId;

    /** 答卷ID */
    private Long responseId;

    /** 题目ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 文本答案 */
    private String answerText;

    /** 选项值 */
    private String optionValue;

    /** 分值 */
    private Integer scoreValue;

    public Long getAnswerId()
    {
        return answerId;
    }

    public void setAnswerId(Long answerId)
    {
        this.answerId = answerId;
    }

    public Long getResponseId()
    {
        return responseId;
    }

    public void setResponseId(Long responseId)
    {
        this.responseId = responseId;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("answerId", getAnswerId())
                .append("responseId", getResponseId())
                .append("questionId", getQuestionId())
                .append("questionType", getQuestionType())
                .append("answerText", getAnswerText())
                .append("optionValue", getOptionValue())
                .append("scoreValue", getScoreValue())
                .toString();
    }
}
