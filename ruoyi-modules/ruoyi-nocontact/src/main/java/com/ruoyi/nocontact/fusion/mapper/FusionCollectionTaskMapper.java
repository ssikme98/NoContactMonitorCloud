package com.ruoyi.nocontact.fusion.mapper;

import com.ruoyi.nocontact.fusion.domain.FusionCollectionTask;
import java.util.List;
import java.util.Map;

/**
 * 数据采集任务Mapper接口
 *
 * @author ruoyi
 */
public interface FusionCollectionTaskMapper
{
    public List<FusionCollectionTask> selectTaskList(FusionCollectionTask task);

    public int countTaskList(FusionCollectionTask task);

    public FusionCollectionTask selectTaskById(Long taskId);

    public FusionCollectionTask selectTaskByScope(FusionCollectionTask task);

    public int insertTask(FusionCollectionTask task);

    public int updateTask(FusionCollectionTask task);

    public int updateTaskStatus(FusionCollectionTask task);

    public int deleteTaskByIds(Long[] taskIds);

    public List<Map<String, Object>> selectTaskStatusStats(FusionCollectionTask task);

    public List<Map<String, Object>> selectTaskTypeStats(FusionCollectionTask task);
}
