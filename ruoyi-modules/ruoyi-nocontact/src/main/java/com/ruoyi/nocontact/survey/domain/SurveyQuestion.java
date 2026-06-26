package com.ruoyi.nocontact.survey.domain;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷题目对象 survey_question
 *
 * @author ruoyi
 */
public class SurveyQuestion implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long questionId;

    /** 问卷ID */
    private Long questionnaireId;

    /** 题目标题 */
    private String questionTitle;

    /** 题型 */
    private String questionType;

    /** 是否必填（0否 1是） */
    private String requiredFlag;

    /** 维度 */
    private String dimension;

    /** 评分上限 */
    private Integer scoreMax;

    /** 排序 */
    private Integer orderNum;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 选项列表 */
    @Valid
    private List<SurveyQuestionOption> options;

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public Long getQuestionnaireId()
    {
        return questionnaireId;
    }

    public void setQuestionnaireId(Long questionnaireId)
    {
        this.questionnaireId = questionnaireId;
    }

    @NotBlank(message = "题目标题不能为空")
    @Size(max = 300, message = "题目标题长度不能超过300个字符")
    public String getQuestionTitle()
    {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle)
    {
        this.questionTitle = questionTitle;
    }

    @NotBlank(message = "题型不能为空")
    public String getQuestionType()
    {
        return questionType;
    }

    public void setQuestionType(String questionType)
    {
        this.questionType = questionType;
    }

    public String getRequiredFlag()
    {
        return requiredFlag;
    }

    public void setRequiredFlag(String requiredFlag)
    {
        this.requiredFlag = requiredFlag;
    }

    public String getDimension()
    {
        return dimension;
    }

    public void setDimension(String dimension)
    {
        this.dimension = dimension;
    }

    public Integer getScoreMax()
    {
        return scoreMax;
    }

    public void setScoreMax(Integer scoreMax)
    {
        this.scoreMax = scoreMax;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<SurveyQuestionOption> getOptions()
    {
        return options;
    }

    public void setOptions(List<SurveyQuestionOption> options)
    {
        this.options = options;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("questionId", getQuestionId())
                .append("questionnaireId", getQuestionnaireId())
                .append("questionTitle", getQuestionTitle())
                .append("questionType", getQuestionType())
                .append("requiredFlag", getRequiredFlag())
                .append("dimension", getDimension())
                .append("scoreMax", getScoreMax())
                .append("orderNum", getOrderNum())
                .append("delFlag", getDelFlag())
                .append("options", getOptions())
                .toString();
    }
}
