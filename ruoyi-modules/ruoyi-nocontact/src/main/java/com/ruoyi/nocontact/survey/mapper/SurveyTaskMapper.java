package com.ruoyi.nocontact.survey.mapper;

import com.ruoyi.nocontact.survey.domain.SurveyEnterprise;
import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSample;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSendRecord;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskRegionStat;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskResponseTrend;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskTrackingDetail;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskTrackingSummary;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 调研任务Mapper接口
 *
 * @author ruoyi
 */
public interface SurveyTaskMapper
{
    public List<SurveyTask> selectTaskList(SurveyTask task);

    public SurveyTask selectTaskById(Long taskId);

    public List<SurveyTaskSample> selectSamplesByTaskId(Long taskId);

    public List<SurveyTaskSendRecord> selectSendRecordsByTaskId(Long taskId);

    public SurveyTaskSample selectSampleById(Long sampleId);

    public List<SurveyEnterprise> selectEnterprisePool(SurveyTask task);

    public int countSendRecordsByTaskId(Long taskId);

    public SurveyTaskTrackingSummary selectTrackingSummary(Long taskId);

    public List<SurveyTaskRegionStat> selectTrackingRegionStats(Long taskId);

    public List<SurveyTaskTrackingDetail> selectTrackingDetailList(SurveyTaskTrackingDetail detail);

    public List<SurveyTaskResponseTrend> selectTrackingTrend(Long taskId);

    public int insertTask(SurveyTask task);

    public int updateTaskStatus(SurveyTask task);

    public int deleteTaskByIds(Long[] taskIds);

    public int insertTaskSample(SurveyTaskSample sample);

    public int batchSendRecord(@Param("list") List<SurveyTaskSendRecord> records);

    public int deleteSendRecordsByTaskId(Long taskId);

    public int updateSamplesSent(Long taskId);
}
