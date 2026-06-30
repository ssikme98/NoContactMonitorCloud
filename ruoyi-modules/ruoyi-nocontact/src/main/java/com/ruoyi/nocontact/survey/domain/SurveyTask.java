package com.ruoyi.nocontact.survey.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 调研任务对象 survey_task
 *
 * @author ruoyi
 */
public class SurveyTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务名称 */
    private String taskName;

    /** 问卷ID */
    private Long questionnaireId;

    /** 问卷名称 */
    private String questionnaireName;

    /** 样本来源（all全部 group分组 enterprise指定企业） */
    private String sampleSource;

    /** 抽样方式（random随机 stratified分层 specified指定） */
    private String samplingMethod;

    /** 样本数量 */
    private Integer sampleSize;

    /** 企业分组ID */
    private Long groupId;

    /** 抽样批次号 */
    private String samplingBatchNo;

    /** 抽样批次时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date samplingBatchTime;

    /** 抽样筛选快照 */
    private String samplingFilterSnapshot;

    /** token有效小时数 */
    private Integer tokenExpireHours;

    /** 状态（0草稿 1已抽样 2已发卷 3已结束） */
    private String status;

    /** 并发更新时的期望状态 */
    private String expectedStatus;

    /** 发卷时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dispatchTime;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 指定企业ID数组 */
    private Long[] enterpriseIds;

    /** 发送渠道文本 */
    private String sendChannelsText;

    /** 发送渠道数组 */
    private String[] sendChannels;

    /** 样本列表 */
    private List<SurveyTaskSample> samples;

    /** 发送记录列表 */
    private List<SurveyTaskSendRecord> sendRecords;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 120, message = "任务名称长度不能超过120个字符")
    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    @NotNull(message = "问卷不能为空")
    public Long getQuestionnaireId()
    {
        return questionnaireId;
    }

    public void setQuestionnaireId(Long questionnaireId)
    {
        this.questionnaireId = questionnaireId;
    }

    public String getQuestionnaireName()
    {
        return questionnaireName;
    }

    public void setQuestionnaireName(String questionnaireName)
    {
        this.questionnaireName = questionnaireName;
    }

    public String getSampleSource()
    {
        return sampleSource;
    }

    public void setSampleSource(String sampleSource)
    {
        this.sampleSource = sampleSource;
    }

    public String getSamplingMethod()
    {
        return samplingMethod;
    }

    public void setSamplingMethod(String samplingMethod)
    {
        this.samplingMethod = samplingMethod;
    }

    public Integer getSampleSize()
    {
        return sampleSize;
    }

    public void setSampleSize(Integer sampleSize)
    {
        this.sampleSize = sampleSize;
    }

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public String getSamplingBatchNo()
    {
        return samplingBatchNo;
    }

    public void setSamplingBatchNo(String samplingBatchNo)
    {
        this.samplingBatchNo = samplingBatchNo;
    }

    public Date getSamplingBatchTime()
    {
        return samplingBatchTime;
    }

    public void setSamplingBatchTime(Date samplingBatchTime)
    {
        this.samplingBatchTime = samplingBatchTime;
    }

    public String getSamplingFilterSnapshot()
    {
        return samplingFilterSnapshot;
    }

    public void setSamplingFilterSnapshot(String samplingFilterSnapshot)
    {
        this.samplingFilterSnapshot = samplingFilterSnapshot;
    }

    public Integer getTokenExpireHours()
    {
        return tokenExpireHours;
    }

    public void setTokenExpireHours(Integer tokenExpireHours)
    {
        this.tokenExpireHours = tokenExpireHours;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getExpectedStatus()
    {
        return expectedStatus;
    }

    public void setExpectedStatus(String expectedStatus)
    {
        this.expectedStatus = expectedStatus;
    }

    public Date getDispatchTime()
    {
        return dispatchTime;
    }

    public void setDispatchTime(Date dispatchTime)
    {
        this.dispatchTime = dispatchTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public Long[] getEnterpriseIds()
    {
        return enterpriseIds;
    }

    public void setEnterpriseIds(Long[] enterpriseIds)
    {
        this.enterpriseIds = enterpriseIds;
    }

    public String getSendChannelsText()
    {
        return sendChannelsText;
    }

    public void setSendChannelsText(String sendChannelsText)
    {
        this.sendChannelsText = sendChannelsText;
    }

    public String[] getSendChannels()
    {
        return sendChannels;
    }

    public void setSendChannels(String[] sendChannels)
    {
        this.sendChannels = sendChannels;
    }

    public List<SurveyTaskSample> getSamples()
    {
        return samples;
    }

    public void setSamples(List<SurveyTaskSample> samples)
    {
        this.samples = samples;
    }

    public List<SurveyTaskSendRecord> getSendRecords()
    {
        return sendRecords;
    }

    public void setSendRecords(List<SurveyTaskSendRecord> sendRecords)
    {
        this.sendRecords = sendRecords;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("taskId", getTaskId())
                .append("taskName", getTaskName())
                .append("questionnaireId", getQuestionnaireId())
                .append("questionnaireName", getQuestionnaireName())
                .append("sampleSource", getSampleSource())
                .append("samplingMethod", getSamplingMethod())
                .append("sampleSize", getSampleSize())
                .append("groupId", getGroupId())
                .append("samplingBatchNo", getSamplingBatchNo())
                .append("samplingBatchTime", getSamplingBatchTime())
                .append("samplingFilterSnapshot", getSamplingFilterSnapshot())
                .append("tokenExpireHours", getTokenExpireHours())
                .append("status", getStatus())
                .append("dispatchTime", getDispatchTime())
                .append("delFlag", getDelFlag())
                .append("enterpriseIds", getEnterpriseIds())
                .append("sendChannelsText", getSendChannelsText())
                .append("sendChannels", getSendChannels())
                .append("samples", getSamples())
                .append("sendRecords", getSendRecords())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
