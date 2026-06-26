package com.ruoyi.nocontact.survey.domain;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 调研任务发送记录对象 survey_task_send_record
 *
 * @author ruoyi
 */
public class SurveyTaskSendRecord implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 发送记录ID */
    private Long recordId;

    /** 任务ID */
    private Long taskId;

    /** 样本ID */
    private Long sampleId;

    /** 企业ID */
    private Long enterpriseId;

    /** 发送渠道（sms短信 site站内信） */
    private String channel;

    /** 接收人 */
    private String receiver;

    /** 发送内容 */
    private String content;

    /** 发送状态（0已生成） */
    private String sendStatus;

    /** 创建时间 */
    private Date createTime;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
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

    public String getChannel()
    {
        return channel;
    }

    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getReceiver()
    {
        return receiver;
    }

    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getSendStatus()
    {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus)
    {
        this.sendStatus = sendStatus;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("recordId", getRecordId())
                .append("taskId", getTaskId())
                .append("sampleId", getSampleId())
                .append("enterpriseId", getEnterpriseId())
                .append("channel", getChannel())
                .append("receiver", getReceiver())
                .append("content", getContent())
                .append("sendStatus", getSendStatus())
                .append("createTime", getCreateTime())
                .toString();
    }
}
