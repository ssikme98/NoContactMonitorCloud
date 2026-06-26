package com.ruoyi.nocontact.warning.domain;

import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 预警规则对象 nc_warning_rule
 *
 * @author ruoyi
 */
public class WarningRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long ruleId;

    @Excel(name = "规则名称")
    private String ruleName;

    private Long indicatorId;

    @Excel(name = "关联指标")
    private String indicatorName;

    @Excel(name = "预警级别")
    private String warningLevel;

    @Excel(name = "阈值类型")
    private String thresholdType;

    @Excel(name = "阈值")
    private BigDecimal thresholdValue;

    @Excel(name = "阈值上限")
    private BigDecimal thresholdValueMax;

    @Excel(name = "触发条件")
    private String triggerCondition;

    @Excel(name = "触发频率")
    private String triggerFrequency;

    @Excel(name = "推送渠道")
    private String pushChannels;

    @Excel(name = "推送对象")
    private String pushTargets;

    private String contentTemplate;

    private String effectiveMode;

    private Date effectiveTime;

    @Excel(name = "状态")
    private String status;

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 120, message = "规则名称长度不能超过120个字符")
    public String getRuleName()
    {
        return ruleName;
    }

    public void setRuleName(String ruleName)
    {
        this.ruleName = ruleName;
    }

    @NotNull(message = "关联指标不能为空")
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

    public String getThresholdType()
    {
        return thresholdType;
    }

    public void setThresholdType(String thresholdType)
    {
        this.thresholdType = thresholdType;
    }

    public BigDecimal getThresholdValue()
    {
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue)
    {
        this.thresholdValue = thresholdValue;
    }

    public BigDecimal getThresholdValueMax()
    {
        return thresholdValueMax;
    }

    public void setThresholdValueMax(BigDecimal thresholdValueMax)
    {
        this.thresholdValueMax = thresholdValueMax;
    }

    public String getTriggerCondition()
    {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition)
    {
        this.triggerCondition = triggerCondition;
    }

    public String getTriggerFrequency()
    {
        return triggerFrequency;
    }

    public void setTriggerFrequency(String triggerFrequency)
    {
        this.triggerFrequency = triggerFrequency;
    }

    public String getPushChannels()
    {
        return pushChannels;
    }

    public void setPushChannels(String pushChannels)
    {
        this.pushChannels = pushChannels;
    }

    public String getPushTargets()
    {
        return pushTargets;
    }

    public void setPushTargets(String pushTargets)
    {
        this.pushTargets = pushTargets;
    }

    public String getContentTemplate()
    {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate)
    {
        this.contentTemplate = contentTemplate;
    }

    public String getEffectiveMode()
    {
        return effectiveMode;
    }

    public void setEffectiveMode(String effectiveMode)
    {
        this.effectiveMode = effectiveMode;
    }

    public Date getEffectiveTime()
    {
        return effectiveTime;
    }

    public void setEffectiveTime(Date effectiveTime)
    {
        this.effectiveTime = effectiveTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
