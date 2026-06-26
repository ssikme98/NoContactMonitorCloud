package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionTask;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionTaskMapper;
import com.ruoyi.nocontact.fusion.service.IFusionCollectionTaskService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据采集任务Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class FusionCollectionTaskServiceImpl implements IFusionCollectionTaskService
{
    @Autowired
    private FusionCollectionTaskMapper taskMapper;

    @Override
    public List<FusionCollectionTask> selectTaskList(FusionCollectionTask task)
    {
        return taskMapper.selectTaskList(task);
    }

    @Override
    public FusionCollectionTask selectTaskById(Long taskId)
    {
        return taskMapper.selectTaskById(taskId);
    }

    @Override
    public int insertTask(FusionCollectionTask task)
    {
        task.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(task.getTaskStatus()))
        {
            task.setTaskStatus("draft");
        }
        if (StringUtils.isBlank(task.getAuditEnabled()))
        {
            task.setAuditEnabled("1");
        }
        return taskMapper.insertTask(task);
    }

    @Override
    public int updateTask(FusionCollectionTask task)
    {
        task.setUpdateTime(DateUtils.getNowDate());
        return taskMapper.updateTask(task);
    }

    @Override
    public int updateTaskStatus(Long taskId, String status, String operName)
    {
        FusionCollectionTask task = new FusionCollectionTask();
        task.setTaskId(taskId);
        task.setTaskStatus(status);
        task.setUpdateBy(operName);
        task.setUpdateTime(DateUtils.getNowDate());
        if ("running".equals(status) || "done".equals(status) || "error".equals(status))
        {
            task.setLastRunTime(DateUtils.getNowDate());
        }
        return taskMapper.updateTaskStatus(task);
    }

    @Override
    public int deleteTaskByIds(Long[] taskIds)
    {
        return taskMapper.deleteTaskByIds(taskIds);
    }

    @Override
    public Map<String, Object> selectTaskSummary()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("statusStats", taskMapper.selectTaskStatusStats());
        data.put("typeStats", taskMapper.selectTaskTypeStats());
        return data;
    }
}
