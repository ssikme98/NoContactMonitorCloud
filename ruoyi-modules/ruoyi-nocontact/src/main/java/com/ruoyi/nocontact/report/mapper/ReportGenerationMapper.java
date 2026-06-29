package com.ruoyi.nocontact.report.mapper;

import com.ruoyi.nocontact.report.domain.ReportGenerationTask;
import com.ruoyi.nocontact.report.domain.ReportTemplate;
import java.util.List;

public interface ReportGenerationMapper
{
    List<ReportTemplate> selectTemplateList(ReportTemplate template);

    ReportTemplate selectTemplateById(Long templateId);

    int insertTemplate(ReportTemplate template);

    int updateTemplate(ReportTemplate template);

    int deleteTemplateByIds(Long[] templateIds);

    List<ReportGenerationTask> selectTaskList(ReportGenerationTask task);

    ReportGenerationTask selectTaskById(Long taskId);

    int insertTask(ReportGenerationTask task);

    int updateTask(ReportGenerationTask task);
}
