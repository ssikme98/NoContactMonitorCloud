package com.ruoyi.survey.domain;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷题目选项对象 survey_question_option
 *
 * @author ruoyi
 */
public class SurveyQuestionOption implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 选项ID */
    private Long optionId;

    /** 题目ID */
    private Long questionId;

    /** 选项类型（option普通选项 row矩阵行 column矩阵列） */
    private String optionType;

    /** 选项标签 */
    private String optionLabel;

    /** 选项值 */
    private String optionValue;

    /** 分值 */
    private Integer scoreValue;

    /** 排序 */
    private Integer orderNum;

    public Long getOptionId()
    {
        return optionId;
    }

    public void setOptionId(Long optionId)
    {
        this.optionId = optionId;
    }

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public String getOptionType()
    {
        return optionType;
    }

    public void setOptionType(String optionType)
    {
        this.optionType = optionType;
    }

    @NotBlank(message = "选项标签不能为空")
    @Size(max = 200, message = "选项标签长度不能超过200个字符")
    public String getOptionLabel()
    {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel)
    {
        this.optionLabel = optionLabel;
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

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("optionId", getOptionId())
                .append("questionId", getQuestionId())
                .append("optionType", getOptionType())
                .append("optionLabel", getOptionLabel())
                .append("optionValue", getOptionValue())
                .append("scoreValue", getScoreValue())
                .append("orderNum", getOrderNum())
                .toString();
    }
}
