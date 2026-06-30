package com.ruoyi.nocontact.report.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.support.domain.BusinessMessage;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import com.ruoyi.nocontact.support.service.impl.BusinessMessageServiceImpl;
import com.ruoyi.nocontact.report.domain.ReportGenerationTask;
import com.ruoyi.nocontact.report.domain.ReportGenerationSnapshot;
import com.ruoyi.nocontact.report.domain.ReportTemplate;
import com.ruoyi.nocontact.report.mapper.ReportGenerationMapper;
import com.ruoyi.nocontact.report.service.IReportGenerationService;
import com.ruoyi.nocontact.report.service.ReportTaskCriteriaSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportGenerationServiceImpl implements IReportGenerationService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    @Autowired
    private ReportGenerationMapper reportMapper;

    @Autowired
    private IBusinessMessageService businessMessageService;

    @Value("${nocontact.report.output-dir:}")
    private String outputDir;

    @Value("${file.path:${user.home}/uploadPath}")
    private String fileRoot;

    @Override
    public List<ReportTemplate> selectTemplateList(ReportTemplate template)
    {
        return reportMapper.selectTemplateList(template);
    }

    @Override
    public ReportTemplate selectTemplateById(Long templateId)
    {
        return reportMapper.selectTemplateById(templateId);
    }

    @Override
    public int insertTemplate(ReportTemplate template)
    {
        return reportMapper.insertTemplate(template);
    }

    @Override
    public int updateTemplate(ReportTemplate template)
    {
        return reportMapper.updateTemplate(template);
    }

    @Override
    public int deleteTemplateByIds(Long[] templateIds)
    {
        return reportMapper.deleteTemplateByIds(templateIds);
    }

    @Override
    public List<ReportGenerationTask> selectTaskList(ReportGenerationTask task)
    {
        return reportMapper.selectTaskList(task);
    }

    @Override
    public ReportGenerationTask selectTaskById(Long taskId)
    {
        return reportMapper.selectTaskById(taskId);
    }

    @Override
    public int insertTask(ReportGenerationTask task)
    {
        return reportMapper.insertTask(task);
    }

    @Override
    public List<ReportGenerationSnapshot> selectSnapshotList(Long taskId)
    {
        return reportMapper.selectSnapshotList(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportGenerationTask generate(Long taskId, String operName)
    {
        ReportGenerationTask task = requireTask(taskId);
        ReportTaskCriteriaSupport.normalizeForGeneration(task);
        ReportTemplate template = task.getTemplateId() == null ? null : reportMapper.selectTemplateById(task.getTemplateId());
        Integer templateVersion = template == null || template.getVersionNo() == null ? 1 : template.getVersionNo();
        String templateName = template == null ? task.getTemplateName() : template.getTemplateName();
        String timestamp = DateUtils.dateTimeNow("yyyyMMddHHmmssSSS");
        String baseName = buildSnapshotBaseName(task, templateVersion, timestamp);
        String wordFileName = baseName + ".docx";
        String excelFileName = baseName + ".xlsx";
        Path wordPath = rootDir().resolve(wordFileName);
        Path excelPath = rootDir().resolve(excelFileName);

        ReportSnapshot snapshot = loadSnapshot(task, template, templateName, templateVersion);
        try
        {
            Files.createDirectories(wordPath.getParent());
            writeWord(wordPath, task, snapshot);
            writeExcel(excelPath, task, snapshot);
        }
        catch (IOException e)
        {
            throw new ServiceException("报告文件生成失败：" + e.getMessage());
        }

        try
        {
            ReportGenerationSnapshot archive = buildSnapshotArchive(task, snapshot, templateName, templateVersion, wordFileName, excelFileName, operName);
            reportMapper.insertSnapshot(archive);

            task.setTaskStatus("completed");
            task.setGeneratedWordFileName(wordFileName);
            task.setGeneratedExcelFileName(excelFileName);
            task.setGeneratedFileName(wordFileName);
            task.setTemplateName(templateName);
            task.setTemplateVersion(templateVersion);
            task.setGeneratedBy(operName);
            task.setGeneratedTime(archive.getGeneratedTime());
            task.setSnapshotContent(archive.getSnapshotContent());
            task.setUpdateBy(operName);
            task.setUpdateTime(archive.getGeneratedTime());
            reportMapper.updateTask(task);
            createReportMessage(task, operName);
            return task;
        }
        catch (RuntimeException e)
        {
            cleanupGeneratedFiles(e, wordPath, excelPath);
            throw e;
        }
    }

    @Override
    public Path resolveGeneratedFile(Long taskId, String fileType)
    {
        ReportGenerationTask task = requireTask(taskId);
        return resolveFile(task.getGeneratedWordFileName(), task.getGeneratedExcelFileName(), fileType);
    }

    @Override
    public Path resolveSnapshotGeneratedFile(Long snapshotId, String fileType)
    {
        ReportGenerationSnapshot snapshot = reportMapper.selectSnapshotById(snapshotId);
        if (snapshot == null)
        {
            throw new ServiceException("报告快照不存在");
        }
        return resolveFile(snapshot.getGeneratedWordFileName(), snapshot.getGeneratedExcelFileName(), fileType);
    }

    private ReportGenerationTask requireTask(Long taskId)
    {
        ReportGenerationTask task = reportMapper.selectTaskById(taskId);
        if (task == null)
        {
            throw new ServiceException("报告任务不存在");
        }
        return task;
    }

    private Path rootDir()
    {
        if (StringUtils.isNotBlank(outputDir))
        {
            return Paths.get(outputDir).toAbsolutePath().normalize();
        }
        return Paths.get(fileRoot, "nocontact-report").toAbsolutePath().normalize();
    }

    private void createReportMessage(ReportGenerationTask task, String operName)
    {
        BusinessMessage message = new BusinessMessage();
        message.setMessageType(BusinessMessageServiceImpl.REPORT_GENERATED);
        message.setTitle("报告生成完成");
        message.setContent("报告任务“" + task.getTaskName() + "”已生成 Word 和 Excel 文件");
        message.setBusinessType("report");
        message.setBusinessId(task.getTaskId());
        message.setJumpTarget("/nocontact/report/task?taskId=" + task.getTaskId());
        message.setReceiverUserName(operName);
        message.setCreateBy(operName);
        businessMessageService.createMessage(message);
    }

    private ReportSnapshot loadSnapshot(ReportGenerationTask task, ReportTemplate template, String templateName, Integer templateVersion)
    {
        ReportSnapshot snapshot = new ReportSnapshot();
        snapshot.taskId = task.getTaskId();
        snapshot.taskName = task.getTaskName();
        snapshot.templateName = templateName;
        snapshot.reportPeriod = task.getReportPeriod();
        snapshot.reportScope = StringUtils.defaultIfBlank(task.getReportScopeLabel(), task.getReportScope());
        snapshot.templateVersion = templateVersion;
        snapshot.sections = template == null ? "" : safe(template.getSections());
        snapshot.dataScope = template == null ? "" : safe(template.getDataScope());
        snapshot.indicatorCount = count(reportMapper.countIndicators(task));
        snapshot.collectionBatchCount = count(reportMapper.countCollectionBatches(task));
        snapshot.collectionItemCount = count(reportMapper.countCollectionItems(task));
        snapshot.warningMessageCount = count(reportMapper.countWarningMessages(task));
        snapshot.rectificationIssueCount = count(reportMapper.countRectificationIssues(task));
        snapshot.surveySampleCount = count(reportMapper.countSurveySamples(task));
        snapshot.surveyResponseCount = count(reportMapper.countSurveyResponses(task));
        snapshot.surveyResponseRate = snapshot.surveySampleCount == 0 ? 0D
                : round(snapshot.surveyResponseCount * 100D / snapshot.surveySampleCount);
        return snapshot;
    }

    private long count(Long value)
    {
        return value == null ? 0L : value;
    }

    private void cleanupGeneratedFiles(RuntimeException original, Path... files)
    {
        for (Path file : files)
        {
            try
            {
                Files.deleteIfExists(file);
            }
            catch (IOException ex)
            {
                original.addSuppressed(ex);
            }
        }
    }

    private String safeFileBaseName(String value)
    {
        String name = StringUtils.defaultIfBlank(value, "营商环境监测报告").trim();
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String buildSnapshotBaseName(ReportGenerationTask task, Integer templateVersion, String timestamp)
    {
        return safeFileBaseName(task.getTaskName()) + "-" + task.getTaskId() + "-v" + templateVersion + "-" + timestamp;
    }

    private void writeWord(Path path, ReportGenerationTask task, ReportSnapshot snapshot) throws IOException
    {
        try (OutputStream output = Files.newOutputStream(path); ZipOutputStream zip = new ZipOutputStream(output))
        {
            entry(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
                    + "</Types>");
            entry(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
                    + "</Relationships>");
            entry(zip, "word/document.xml", wordDocument(task, snapshot));
        }
    }

    private void writeExcel(Path path, ReportGenerationTask task, ReportSnapshot snapshot) throws IOException
    {
        try (OutputStream output = Files.newOutputStream(path); ZipOutputStream zip = new ZipOutputStream(output))
        {
            entry(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "</Types>");
            entry(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            entry(zip, "xl/_rels/workbook.xml.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "</Relationships>");
            entry(zip, "xl/workbook.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"报告快照\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            entry(zip, "xl/worksheets/sheet1.xml", sheetDocument(task, snapshot));
        }
    }

    private String wordDocument(ReportGenerationTask task, ReportSnapshot snapshot)
    {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>");
        xml.append(paragraph(task.getTaskName()));
        xml.append(paragraph("报告周期：" + safe(task.getReportPeriod())));
        xml.append(paragraph("报告范围：" + safe(snapshot.reportScope)));
        xml.append(paragraph("模板版本：" + snapshot.templateVersion));
        xml.append(paragraph("模板范围：" + safe(snapshot.dataScope)));
        for (ReportSectionSummary section : buildSectionSummaries(snapshot))
        {
            xml.append(paragraph(section.title + "：" + section.summary));
        }
        xml.append("<w:sectPr/></w:body></w:document>");
        return xml.toString();
    }

    private String paragraph(String text)
    {
        return "<w:p><w:r><w:t>" + xml(text) + "</w:t></w:r></w:p>";
    }

    private String sheetDocument(ReportGenerationTask task, ReportSnapshot snapshot)
    {
        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] {"任务名称", task.getTaskName()});
        rows.add(new String[] {"报告周期", task.getReportPeriod()});
        rows.add(new String[] {"报告范围", snapshot.reportScope});
        rows.add(new String[] {"模板版本", String.valueOf(snapshot.templateVersion)});
        rows.add(new String[] {"模板范围", snapshot.dataScope});
        for (ReportSectionSummary section : buildSectionSummaries(snapshot))
        {
            rows.add(new String[] {"章节-" + section.title, section.summary});
        }
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
        for (int i = 0; i < rows.size(); i++)
        {
            int rowNum = i + 1;
            xml.append("<row r=\"").append(rowNum).append("\">");
            xml.append(cell("A", rowNum, rows.get(i)[0]));
            xml.append(cell("B", rowNum, rows.get(i)[1]));
            xml.append("</row>");
        }
        xml.append("</sheetData></worksheet>");
        return xml.toString();
    }

    private List<ReportSectionSummary> buildSectionSummaries(ReportSnapshot snapshot)
    {
        List<ReportSectionSummary> sections = new ArrayList<ReportSectionSummary>();
        for (String sectionName : sectionNames(snapshot.sections))
        {
            sections.add(new ReportSectionSummary(sectionName, sectionSummary(sectionName, snapshot)));
        }
        if (sections.isEmpty())
        {
            sections.add(new ReportSectionSummary("概述", sectionSummary("概述", snapshot)));
            sections.add(new ReportSectionSummary("监测结果", sectionSummary("监测结果", snapshot)));
            sections.add(new ReportSectionSummary("问卷满意度", sectionSummary("问卷满意度", snapshot)));
        }
        return sections;
    }

    private List<String> sectionNames(String sections)
    {
        List<String> values = new ArrayList<String>();
        String[] parts = safe(sections).split("[,，]");
        for (String part : parts)
        {
            String value = StringUtils.trimToEmpty(part);
            if (StringUtils.isNotBlank(value))
            {
                values.add(value);
            }
        }
        return values;
    }

    private String sectionSummary(String sectionName, ReportSnapshot snapshot)
    {
        String name = StringUtils.trimToEmpty(sectionName);
        if ("概述".equals(name) || "总体评价".equals(name))
        {
            return "模板“" + safe(snapshot.templateName) + "”覆盖" + safe(snapshot.dataScope) + "，本次统计周期为"
                    + safe(snapshot.reportPeriod) + "。";
        }
        if ("监测结果".equals(name) || "指标概览".equals(name) || "指标分析".equals(name))
        {
            return "纳入指标" + snapshot.indicatorCount + "项，采集批次" + snapshot.collectionBatchCount + "个，采集明细"
                    + snapshot.collectionItemCount + "条。";
        }
        if ("数据采集".equals(name))
        {
            return "采集批次" + snapshot.collectionBatchCount + "个，采集明细" + snapshot.collectionItemCount + "条。";
        }
        if ("预警分析".equals(name) || "预警整改".equals(name))
        {
            return "预警消息" + snapshot.warningMessageCount + "条，整改问题" + snapshot.rectificationIssueCount + "个。";
        }
        if ("整改建议".equals(name) || "问题与建议".equals(name))
        {
            return "当前快照纳入整改问题" + snapshot.rectificationIssueCount + "个，可结合预警消息"
                    + snapshot.warningMessageCount + "条继续跟踪。";
        }
        if ("问卷满意度".equals(name) || "问卷统计".equals(name))
        {
            return "已发卷样本" + snapshot.surveySampleCount + "个，有效答卷" + snapshot.surveyResponseCount
                    + "份，回收率" + snapshot.surveyResponseRate + "%。";
        }
        return "本章节已纳入当前报告快照归档。";
    }

    private double round(double value)
    {
        return Math.round(value * 100D) / 100D;
    }

    private String cell(String column, int rowNum, String value)
    {
        return "<c r=\"" + column + rowNum + "\" t=\"inlineStr\"><is><t>" + xml(value) + "</t></is></c>";
    }

    private void entry(ZipOutputStream zip, String name, String content) throws IOException
    {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String safe(String value)
    {
        return value == null ? "" : value;
    }

    private String xml(String value)
    {
        return safe(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private Path resolveFile(String wordFileName, String excelFileName, String fileType)
    {
        String fileName;
        if ("word".equals(fileType))
        {
            fileName = wordFileName;
        }
        else if ("excel".equals(fileType))
        {
            fileName = excelFileName;
        }
        else
        {
            throw new ServiceException("报告类型不支持");
        }
        if (StringUtils.isBlank(fileName))
        {
            throw new ServiceException("报告文件尚未生成");
        }
        Path root = rootDir();
        Path file = root.resolve(fileName).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file))
        {
            throw new ServiceException("报告文件不存在");
        }
        return file;
    }

    private ReportGenerationSnapshot buildSnapshotArchive(ReportGenerationTask task, ReportSnapshot snapshot, String templateName,
                                                          Integer templateVersion, String wordFileName, String excelFileName, String operName)
    {
        ReportGenerationSnapshot archive = new ReportGenerationSnapshot();
        archive.setTaskId(task.getTaskId());
        archive.setTaskName(task.getTaskName());
        archive.setTemplateName(templateName);
        archive.setReportPeriod(task.getReportPeriod());
        archive.setReportScope(snapshot.reportScope);
        archive.setGeneratedWordFileName(wordFileName);
        archive.setGeneratedExcelFileName(excelFileName);
        archive.setTemplateVersion(templateVersion);
        archive.setGeneratedBy(operName);
        archive.setGeneratedTime(DateUtils.getNowDate());
        archive.setSnapshotContent(snapshot.toJson());
        archive.setCreateBy(operName);
        archive.setCreateTime(archive.getGeneratedTime());
        return archive;
    }

    private static class ReportSnapshot
    {
        private Long taskId;
        private String taskName;
        private String templateName;
        private String reportPeriod;
        private String reportScope;
        private Integer templateVersion;
        private String sections;
        private String dataScope;
        private long indicatorCount;
        private long collectionBatchCount;
        private long collectionItemCount;
        private long warningMessageCount;
        private long rectificationIssueCount;
        private long surveySampleCount;
        private long surveyResponseCount;
        private double surveyResponseRate;

        private String toJson()
        {
            try
            {
                return OBJECT_MAPPER.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                throw new IllegalStateException("报告快照序列化失败", e);
            }
        }
    }

    private static class ReportSectionSummary
    {
        private final String title;
        private final String summary;

        private ReportSectionSummary(String title, String summary)
        {
            this.title = title;
            this.summary = summary;
        }
    }
}
