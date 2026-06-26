package com.ruoyi.nocontact.fusion.service;

import com.ruoyi.nocontact.fusion.domain.FusionCollectionTask;
import java.util.List;
import java.util.Map;

/**
 * 数据采集任务Service接口
 *
 * @author ruoyi
 */
public interface IFusionCollectionTaskService
{
    public List<FusionCollectionTask> selectTaskList(FusionCollectionTask task);

    public FusionCollectionTask selectTaskById(Long taskId);

    public int insertTask(FusionCollectionTask task);

    public int updateTask(FusionCollectionTask task);

    public int updateTaskStatus(Long taskId, String status, String operName);

    public int deleteTaskByIds(Long[] taskIds);

    public Map<String, Object> selectTaskSummary();
}
