package com.ruoyi.nocontact.survey.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷对象 survey_questionnaire
 *
 * @author ruoyi
 */
public class SurveyQuestionnaire extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 问卷ID */
    private Long questionnaireId;

    /** 问卷名称 */
    private String questionnaireName;

    /** 问卷说明 */
    private String description;

    /** 状态（0草稿 1已发布 2已结束 3收集中） */
    private String status;

    /** 条件更新使用的当前状态 */
    private String currentStatus;

    /** 版本号 */
    private Integer versionNo;

    /** 来源问卷ID */
    private Long sourceQuestionnaireId;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishedTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endedTime;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 题目列表 */
    @Valid
    private List<SurveyQuestion> questions;

    public Long getQuestionnaireId()
    {
        return questionnaireId;
    }

    public void setQuestionnaireId(Long questionnaireId)
    {
        this.questionnaireId = questionnaireId;
    }

    @NotBlank(message = "问卷名称不能为空")
    @Size(max = 120, message = "问卷名称长度不能超过120个字符")
    public String getQuestionnaireName()
    {
        return questionnaireName;
    }

    public void setQuestionnaireName(String questionnaireName)
    {
        this.questionnaireName = questionnaireName;
    }

    @Size(max = 500, message = "问卷说明长度不能超过500个字符")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getCurrentStatus()
    {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus)
    {
        this.currentStatus = currentStatus;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
    }

    public Long getSourceQuestionnaireId()
    {
        return sourceQuestionnaireId;
    }

    public void setSourceQuestionnaireId(Long sourceQuestionnaireId)
    {
        this.sourceQuestionnaireId = sourceQuestionnaireId;
    }

    public Date getPublishedTime()
    {
        return publishedTime;
    }

    public void setPublishedTime(Date publishedTime)
    {
        this.publishedTime = publishedTime;
    }

    public Date getEndedTime()
    {
        return endedTime;
    }

    public void setEndedTime(Date endedTime)
    {
        this.endedTime = endedTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<SurveyQuestion> getQuestions()
    {
        return questions;
    }

    public void setQuestions(List<SurveyQuestion> questions)
    {
        this.questions = questions;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("questionnaireId", getQuestionnaireId())
                .append("questionnaireName", getQuestionnaireName())
                .append("description", getDescription())
                .append("status", getStatus())
                .append("currentStatus", getCurrentStatus())
                .append("versionNo", getVersionNo())
                .append("sourceQuestionnaireId", getSourceQuestionnaireId())
                .append("publishedTime", getPublishedTime())
                .append("endedTime", getEndedTime())
                .append("delFlag", getDelFlag())
                .append("questions", getQuestions())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
