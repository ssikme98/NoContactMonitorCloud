package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 数据采集任务对象 nc_fusion_collection_task
 *
 * @author ruoyi
 */
public class FusionCollectionTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;

    @Excel(name = "任务名称")
    private String taskName;

    @Excel(name = "任务类型")
    private String taskType;

    @Excel(name = "任务状态")
    private String taskStatus;

    @Excel(name = "目标层级")
    private String targetLevel;

    private Long indicatorId;

    @Excel(name = "监测项")
    private String indicatorName;

    @Excel(name = "数据靶点")
    private String dataTargets;

    @Excel(name = "数据源类型")
    private String sourceType;

    @Excel(name = "填报周期")
    private String fillCycle;

    @Excel(name = "调度方式")
    private String scheduleMode;

    @Excel(name = "Cron表达式")
    private String cronExpression;

    @Excel(name = "审核开关")
    private String auditEnabled;

    @Excel(name = "负责人")
    private String ownerName;

    @Excel(name = "数据量")
    private Long dataCount;

    @Excel(name = "最后执行时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastRunTime;

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

    public String getTaskType()
    {
        return taskType;
    }

    public void setTaskType(String taskType)
    {
        this.taskType = taskType;
    }

    public String getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public String getTargetLevel()
    {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel)
    {
        this.targetLevel = targetLevel;
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

    public String getDataTargets()
    {
        return dataTargets;
    }

    public void setDataTargets(String dataTargets)
    {
        this.dataTargets = dataTargets;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getFillCycle()
    {
        return fillCycle;
    }

    public void setFillCycle(String fillCycle)
    {
        this.fillCycle = fillCycle;
    }

    public String getScheduleMode()
    {
        return scheduleMode;
    }

    public void setScheduleMode(String scheduleMode)
    {
        this.scheduleMode = scheduleMode;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public String getAuditEnabled()
    {
        return auditEnabled;
    }

    public void setAuditEnabled(String auditEnabled)
    {
        this.auditEnabled = auditEnabled;
    }

    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName(String ownerName)
    {
        this.ownerName = ownerName;
    }

    public Long getDataCount()
    {
        return dataCount;
    }

    public void setDataCount(Long dataCount)
    {
        this.dataCount = dataCount;
    }

    public Date getLastRunTime()
    {
        return lastRunTime;
    }

    public void setLastRunTime(Date lastRunTime)
    {
        this.lastRunTime = lastRunTime;
    }
}
