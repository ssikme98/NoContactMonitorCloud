package com.ruoyi.survey.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 问卷答卷对象 survey_response
 *
 * @author ruoyi
 */
public class SurveyResponse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 答卷ID */
    private Long responseId;

    /** 任务ID */
    private Long taskId;

    /** 样本ID */
    private Long sampleId;

    /** 企业ID */
    private Long enterpriseId;

    /** 问卷ID */
    private Long questionnaireId;

    /** 提交时间 */
    private Date submitTime;

    /** 客户端IP */
    private String clientIp;

    /** 状态（0有效） */
    private String status;

    /** 答案列表 */
    private List<SurveyResponseAnswer> answers;

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

    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getQuestionnaireId()
    {
        return questionnaireId;
    }

    public void setQuestionnaireId(Long questionnaireId)
    {
        this.questionnaireId = questionnaireId;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public String getClientIp()
    {
        return clientIp;
    }

    public void setClientIp(String clientIp)
    {
        this.clientIp = clientIp;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<SurveyResponseAnswer> getAnswers()
    {
        return answers;
    }

    public void setAnswers(List<SurveyResponseAnswer> answers)
    {
        this.answers = answers;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("responseId", getResponseId())
                .append("taskId", getTaskId())
                .append("sampleId", getSampleId())
                .append("enterpriseId", getEnterpriseId())
                .append("questionnaireId", getQuestionnaireId())
                .append("submitTime", getSubmitTime())
                .append("clientIp", getClientIp())
                .append("status", getStatus())
                .append("answers", getAnswers())
                .toString();
    }
}
