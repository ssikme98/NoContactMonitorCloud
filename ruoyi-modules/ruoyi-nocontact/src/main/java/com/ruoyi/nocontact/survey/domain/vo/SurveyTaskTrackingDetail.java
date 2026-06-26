package com.ruoyi.nocontact.survey.domain.vo;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;

/**
 * 调研任务企业级填报明细
 *
 * @author ruoyi
 */
public class SurveyTaskTrackingDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;

    private Long sampleId;

    private Long enterpriseId;

    private Long responseId;

    private String enterpriseName;

    private String creditCode;

    private String regionName;

    private String industryCategory;

    private String enterpriseScale;

    private String contactPhone;

    private String sampleStatus;

    private String sendStatus;

    private String submitStatus;

    private Date sendTime;

    private Date submitTime;

    private Date tokenExpireTime;

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

    public Long getResponseId()
    {
        return responseId;
    }

    public void setResponseId(Long responseId)
    {
        this.responseId = responseId;
    }

    public String getEnterpriseName()
    {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName)
    {
        this.enterpriseName = enterpriseName;
    }

    public String getCreditCode()
    {
        return creditCode;
    }

    public void setCreditCode(String creditCode)
    {
        this.creditCode = creditCode;
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

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public String getSampleStatus()
    {
        return sampleStatus;
    }

    public void setSampleStatus(String sampleStatus)
    {
        this.sampleStatus = sampleStatus;
    }

    public String getSendStatus()
    {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus)
    {
        this.sendStatus = sendStatus;
    }

    public String getSubmitStatus()
    {
        return submitStatus;
    }

    public void setSubmitStatus(String submitStatus)
    {
        this.submitStatus = submitStatus;
    }

    public Date getSendTime()
    {
        return sendTime;
    }

    public void setSendTime(Date sendTime)
    {
        this.sendTime = sendTime;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public Date getTokenExpireTime()
    {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(Date tokenExpireTime)
    {
        this.tokenExpireTime = tokenExpireTime;
    }
}
