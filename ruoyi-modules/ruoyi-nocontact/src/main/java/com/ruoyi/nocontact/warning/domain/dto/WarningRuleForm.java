package com.ruoyi.nocontact.warning.domain.dto;

import com.ruoyi.nocontact.warning.domain.WarningRule;
import java.math.BigDecimal;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 预警规则写入表单。
 */
public class WarningRuleForm
{
    private Long ruleId;

    private String ruleName;

    private Long indicatorId;

    private String indicatorName;

    private Long responsibleUnitId;

    private String responsibleUnitName;

    private String regionCode;

    private String regionName;

    private String periodType;

    private String warningLevel;

    private String thresholdType;

    private BigDecimal thresholdValue;

    private BigDecimal thresholdValueMax;

    private String triggerCondition;

    private String triggerFrequency;

    private String pushChannels;

    private String pushTargets;

    private String contentTemplate;

    private String effectiveMode;

    private Date effectiveTime;

    private String status;

    public WarningRule toEntity()
    {
        WarningRule rule = new WarningRule();
        rule.setRuleId(ruleId);
        rule.setRuleName(ruleName);
        rule.setIndicatorId(indicatorId);
        rule.setIndicatorName(indicatorName);
        rule.setResponsibleUnitId(responsibleUnitId);
        rule.setResponsibleUnitName(responsibleUnitName);
        rule.setRegionCode(regionCode);
        rule.setRegionName(regionName);
        rule.setPeriodType(periodType);
        rule.setWarningLevel(warningLevel);
        rule.setThresholdType(thresholdType);
        rule.setThresholdValue(thresholdValue);
        rule.setThresholdValueMax(thresholdValueMax);
        rule.setTriggerCondition(triggerCondition);
        rule.setTriggerFrequency(triggerFrequency);
        rule.setPushChannels(pushChannels);
        rule.setPushTargets(pushTargets);
        rule.setContentTemplate(contentTemplate);
        rule.setEffectiveMode(effectiveMode);
        rule.setEffectiveTime(effectiveTime);
        rule.setStatus(status);
        return rule;
    }

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

    public Long getResponsibleUnitId()
    {
        return responsibleUnitId;
    }

    public void setResponsibleUnitId(Long responsibleUnitId)
    {
        this.responsibleUnitId = responsibleUnitId;
    }

    public String getResponsibleUnitName()
    {
        return responsibleUnitName;
    }

    public void setResponsibleUnitName(String responsibleUnitName)
    {
        this.responsibleUnitName = responsibleUnitName;
    }

    public String getRegionCode()
    {
        return regionCode;
    }

    public void setRegionCode(String regionCode)
    {
        this.regionCode = regionCode;
    }

    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    public String getPeriodType()
    {
        return periodType;
    }

    public void setPeriodType(String periodType)
    {
        this.periodType = periodType;
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
