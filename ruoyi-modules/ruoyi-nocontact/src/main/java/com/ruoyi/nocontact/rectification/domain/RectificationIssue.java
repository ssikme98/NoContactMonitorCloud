package com.ruoyi.nocontact.rectification.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotBlank;

/**
 * 问题整改台账对象 nc_rectification_issue
 */
public class RectificationIssue extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long issueId;
    private String issueCode;
    private String issueTitle;
    private String sourceType;
    private Long sourceWarningId;
    private Long deptId;
    private String warningLevel;
    private String regionName;
    private String responsibleUnitName;
    private String responsiblePerson;
    private String supervisorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deadline;
    private String issueStatus;
    private String expectedStatus;
    private Long indicatorId;
    private String indicatorName;
    private BigDecimal currentValue;
    private BigDecimal thresholdValue;
    private String issueDescription;
    private String rectificationResult;
    private String reviewOpinion;
    private String attachmentUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dispatchTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archiveTime;
    private List<RectificationLog> logs;

    public Long getIssueId()
    {
        return issueId;
    }

    public void setIssueId(Long issueId)
    {
        this.issueId = issueId;
    }

    public String getIssueCode()
    {
        return issueCode;
    }

    public void setIssueCode(String issueCode)
    {
        this.issueCode = issueCode;
    }

    @NotBlank(message = "问题标题不能为空")
    public String getIssueTitle()
    {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle)
    {
        this.issueTitle = issueTitle;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public Long getSourceWarningId()
    {
        return sourceWarningId;
    }

    public void setSourceWarningId(Long sourceWarningId)
    {
        this.sourceWarningId = sourceWarningId;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public String getWarningLevel()
    {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel)
    {
        this.warningLevel = warningLevel;
    }

    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    public String getResponsibleUnitName()
    {
        return responsibleUnitName;
    }

    public void setResponsibleUnitName(String responsibleUnitName)
    {
        this.responsibleUnitName = responsibleUnitName;
    }

    public String getResponsiblePerson()
    {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson)
    {
        this.responsiblePerson = responsiblePerson;
    }

    public String getSupervisorName()
    {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName)
    {
        this.supervisorName = supervisorName;
    }

    public Date getDeadline()
    {
        return deadline;
    }

    public void setDeadline(Date deadline)
    {
        this.deadline = deadline;
    }

    public String getIssueStatus()
    {
        return issueStatus;
    }

    public void setIssueStatus(String issueStatus)
    {
        this.issueStatus = issueStatus;
    }

    public String getExpectedStatus()
    {
        return expectedStatus;
    }

    public void setExpectedStatus(String expectedStatus)
    {
        this.expectedStatus = expectedStatus;
    }

    public Long getIndicatorId()
    {
        return indicatorId;
    }

    public void setIndicatorId(Long indicatorId)
    {
        this.indicatorId = indicatorId;
    }

    public String getIndicatorName()
    {
        return indicatorName;
    }

    public void setIndicatorName(String indicatorName)
    {
        this.indicatorName = indicatorName;
    }

    public BigDecimal getCurrentValue()
    {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue)
    {
        this.currentValue = currentValue;
    }

    public BigDecimal getThresholdValue()
    {
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue)
    {
        this.thresholdValue = thresholdValue;
    }

    public String getIssueDescription()
    {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription)
    {
        this.issueDescription = issueDescription;
    }

    public String getRectificationResult()
    {
        return rectificationResult;
    }

    public void setRectificationResult(String rectificationResult)
    {
        this.rectificationResult = rectificationResult;
    }

    public String getReviewOpinion()
    {
        return reviewOpinion;
    }

    public void setReviewOpinion(String reviewOpinion)
    {
        this.reviewOpinion = reviewOpinion;
    }

    public String getAttachmentUrl()
    {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl)
    {
        this.attachmentUrl = attachmentUrl;
    }

    public Date getDispatchTime()
    {
        return dispatchTime;
    }

    public void setDispatchTime(Date dispatchTime)
    {
        this.dispatchTime = dispatchTime;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public Date getReviewTime()
    {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime)
    {
        this.reviewTime = reviewTime;
    }

    public Date getArchiveTime()
    {
        return archiveTime;
    }

    public void setArchiveTime(Date archiveTime)
    {
        this.archiveTime = archiveTime;
    }

    public List<RectificationLog> getLogs()
    {
        return logs;
    }

    public void setLogs(List<RectificationLog> logs)
    {
        this.logs = logs;
    }
}
