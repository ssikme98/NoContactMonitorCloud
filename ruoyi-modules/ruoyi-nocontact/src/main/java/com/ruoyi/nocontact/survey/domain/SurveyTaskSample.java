package com.ruoyi.nocontact.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 调研任务样本对象 survey_task_sample
 *
 * @author ruoyi
 */
public class SurveyTaskSample implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 样本ID */
    private Long sampleId;

    /** 任务ID */
    private Long taskId;

    /** 企业ID */
    private Long enterpriseId;

    /** 企业名称 */
    private String enterpriseName;

    /** 统一社会信用代码 */
    private String creditCode;

    /** 地区 */
    private String regionName;

    /** 行业 */
    private String industryCategory;

    /** 企业规模 */
    private String enterpriseScale;

    /** 联系电话 */
    private String contactPhone;

    /** 填报token */
    private String token;

    /** token失效时间 */
    private Date tokenExpireTime;

    /** 二维码内容 */
    private String qrContent;

    /** 状态（0待发送 1已发送 2已填报 3已失效） */
    private String status;

    /** 创建时间 */
    private Date createTime;

    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
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

    public String getEnterpriseName()
    {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName)
    {
        this.enterpriseName = enterpriseName;
    }

    @JsonIgnore
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

    @JsonIgnore
    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Date getTokenExpireTime()
    {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(Date tokenExpireTime)
    {
        this.tokenExpireTime = tokenExpireTime;
    }

    public String getQrContent()
    {
        return qrContent;
    }

    public void setQrContent(String qrContent)
    {
        this.qrContent = qrContent;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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
                .append("sampleId", getSampleId())
                .append("taskId", getTaskId())
                .append("enterpriseId", getEnterpriseId())
                .append("enterpriseName", getEnterpriseName())
                .append("creditCode", getCreditCode())
                .append("regionName", getRegionName())
                .append("industryCategory", getIndustryCategory())
                .append("enterpriseScale", getEnterpriseScale())
                .append("contactPhone", getContactPhone())
                .append("token", getToken())
                .append("tokenExpireTime", getTokenExpireTime())
                .append("qrContent", getQrContent())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .toString();
    }
}
