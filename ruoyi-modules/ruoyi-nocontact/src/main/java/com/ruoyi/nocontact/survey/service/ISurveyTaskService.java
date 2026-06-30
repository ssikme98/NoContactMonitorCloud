package com.ruoyi.nocontact.survey.service;

import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSample;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskRegionStat;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskResponseTrend;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskTrackingDetail;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskTrackingSummary;
import java.util.List;

/**
 * 调研任务Service接口
 *
 * @author ruoyi
 */
public interface ISurveyTaskService
{
    public List<SurveyTask> selectTaskList(SurveyTask task);

    public SurveyTask selectTaskById(Long taskId);

    public SurveyTaskSample selectSampleById(Long sampleId);

    public SurveyTaskTrackingSummary selectTrackingSummary(Long taskId);

    public List<SurveyTaskRegionStat> selectTrackingRegionStats(Long taskId);

    public List<SurveyTaskTrackingDetail> selectTrackingDetailList(SurveyTaskTrackingDetail detail);

    public List<SurveyTaskResponseTrend> selectTrackingTrend(Long taskId);

    public int insertTask(SurveyTask task);

    public int dispatchTask(Long taskId, String operName);

    public int deleteTaskByIds(Long[] taskIds);
}
