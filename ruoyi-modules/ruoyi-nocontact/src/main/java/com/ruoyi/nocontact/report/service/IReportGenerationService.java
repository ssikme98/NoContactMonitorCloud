package com.ruoyi.nocontact.report.service;

import com.ruoyi.nocontact.report.domain.ReportGenerationTask;
import com.ruoyi.nocontact.report.domain.ReportGenerationSnapshot;
import com.ruoyi.nocontact.report.domain.ReportTemplate;
import java.nio.file.Path;
import java.util.List;

public interface IReportGenerationService
{
    List<ReportTemplate> selectTemplateList(ReportTemplate template);

    ReportTemplate selectTemplateById(Long templateId);

    int insertTemplate(ReportTemplate template);

    int updateTemplate(ReportTemplate template);

    int deleteTemplateByIds(Long[] templateIds);

    List<ReportGenerationTask> selectTaskList(ReportGenerationTask task);

    ReportGenerationTask selectTaskById(Long taskId);

    int insertTask(ReportGenerationTask task);

    List<ReportGenerationSnapshot> selectSnapshotList(Long taskId);

    ReportGenerationTask generate(Long taskId, String operName);

    Path resolveGeneratedFile(Long taskId, String fileType);

    Path resolveSnapshotGeneratedFile(Long snapshotId, String fileType);
}
