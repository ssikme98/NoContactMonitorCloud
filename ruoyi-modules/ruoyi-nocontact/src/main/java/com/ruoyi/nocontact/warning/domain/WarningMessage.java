package com.ruoyi.nocontact.warning.domain;

import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 预警消息对象 nc_warning_message
 *
 * @author ruoyi
 */
public class WarningMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long messageId;

    private Long ruleId;

    @Excel(name = "规则名称")
    private String ruleName;

    private Long indicatorId;

    @Excel(name = "关联指标")
    private String indicatorName;

    @Excel(name = "预警级别")
    private String warningLevel;

    @Excel(name = "地区")
    private String regionName;

    @Excel(name = "当前值")
    private BigDecimal currentValue;

    @Excel(name = "阈值")
    private BigDecimal thresholdValue;

    @Excel(name = "推送渠道")
    private String pushChannels;

    @Excel(name = "接收人")
    private String receivers;

    @Excel(name = "状态")
    private String messageStatus;

    @Excel(name = "触发时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date triggerTime;

    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    public String getRuleName()
    {
        return ruleName;
    }

    public void setRuleName(String ruleName)
    {
        this.ruleName = ruleName;
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

    public String getPushChannels()
    {
        return pushChannels;
    }

    public void setPushChannels(String pushChannels)
    {
        this.pushChannels = pushChannels;
    }

    public String getReceivers()
    {
        return receivers;
    }

    public void setReceivers(String receivers)
    {
        this.receivers = receivers;
    }

    public String getMessageStatus()
    {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus)
    {
        this.messageStatus = messageStatus;
    }

    public Date getTriggerTime()
    {
        return triggerTime;
    }

    public void setTriggerTime(Date triggerTime)
    {
        this.triggerTime = triggerTime;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }
}
