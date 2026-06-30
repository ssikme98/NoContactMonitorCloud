package com.ruoyi.nocontact.report.mapper;

import com.ruoyi.nocontact.report.domain.ReportGenerationTask;
import com.ruoyi.nocontact.report.domain.ReportGenerationSnapshot;
import com.ruoyi.nocontact.report.domain.ReportTemplate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReportGenerationMapper
{
    List<ReportTemplate> selectTemplateList(ReportTemplate template);

    ReportTemplate selectTemplateById(@Param("templateId") Long templateId);

    int insertTemplate(ReportTemplate template);

    int updateTemplate(ReportTemplate template);

    int deleteTemplateByIds(Long[] templateIds);

    List<ReportGenerationTask> selectTaskList(ReportGenerationTask task);

    ReportGenerationTask selectTaskById(@Param("taskId") Long taskId);

    int insertTask(ReportGenerationTask task);

    int updateTask(ReportGenerationTask task);

    List<ReportGenerationSnapshot> selectSnapshotList(@Param("taskId") Long taskId);

    ReportGenerationSnapshot selectSnapshotById(@Param("snapshotId") Long snapshotId);

    int insertSnapshot(ReportGenerationSnapshot snapshot);

    Long countIndicators(ReportGenerationTask task);

    Long countCollectionBatches(ReportGenerationTask task);

    Long countCollectionItems(ReportGenerationTask task);

    Long countWarningMessages(ReportGenerationTask task);

    Long countRectificationIssues(ReportGenerationTask task);

    Long countSurveySamples(ReportGenerationTask task);

    Long countSurveyResponses(ReportGenerationTask task);
}
