package com.ruoyi.nocontact.report.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.report.domain.ReportGenerationTask;
import com.ruoyi.nocontact.report.domain.ReportGenerationSnapshot;
import com.ruoyi.nocontact.report.domain.ReportTemplate;
import com.ruoyi.nocontact.report.mapper.ReportGenerationMapper;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportGenerationServiceImplTest
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ReportGenerationServiceImpl service;

    @Mock
    private ReportGenerationMapper reportMapper;

    @Mock
    private IBusinessMessageService businessMessageService;

    @TempDir
    private Path outputDir;

    @BeforeEach
    void setUp()
    {
        service = new ReportGenerationServiceImpl();
        ReflectionTestUtils.setField(service, "reportMapper", reportMapper);
        ReflectionTestUtils.setField(service, "businessMessageService", businessMessageService);
        ReflectionTestUtils.setField(service, "outputDir", outputDir.toString());
    }

    @Test
    void generateCreatesSnapshotAndWordExcelFiles() throws Exception
    {
        when(reportMapper.selectTaskById(77L)).thenReturn(task());
        when(reportMapper.selectTemplateById(6L)).thenReturn(template());
        when(reportMapper.countIndicators(any(ReportGenerationTask.class))).thenReturn(8L);
        when(reportMapper.countCollectionBatches(any(ReportGenerationTask.class))).thenReturn(3L);
        when(reportMapper.countCollectionItems(any(ReportGenerationTask.class))).thenReturn(23L);
        when(reportMapper.countWarningMessages(any(ReportGenerationTask.class))).thenReturn(5L);
        when(reportMapper.countRectificationIssues(any(ReportGenerationTask.class))).thenReturn(2L);
        when(reportMapper.countSurveySamples(any(ReportGenerationTask.class))).thenReturn(20L);
        when(reportMapper.countSurveyResponses(any(ReportGenerationTask.class))).thenReturn(13L);

        ReportGenerationTask result = service.generate(77L, "admin");

        assertEquals("completed", result.getTaskStatus());
        assertEquals("admin", result.getGeneratedBy());
        assertEquals(Integer.valueOf(3), result.getTemplateVersion());
        assertTrue(result.getGeneratedWordFileName().endsWith(".docx"));
        assertTrue(result.getGeneratedExcelFileName().endsWith(".xlsx"));
        assertTrue(Files.isRegularFile(outputDir.resolve(result.getGeneratedWordFileName())));
        assertTrue(Files.isRegularFile(outputDir.resolve(result.getGeneratedExcelFileName())));
        assertTrue(result.getSnapshotContent().contains("\"reportPeriod\":\"2026Q2\""));
        assertTrue(result.getSnapshotContent().contains("\"templateVersion\":3"));
        assertTrue(result.getSnapshotContent().contains("\"sections\":\"指标概览,数据采集,预警整改,问卷满意度\""));
        assertTrue(result.getSnapshotContent().contains("\"collectionItemCount\":23"));
        assertTrue(result.getSnapshotContent().contains("\"surveySampleCount\":20"));
        assertTrue(result.getSnapshotContent().contains("\"surveyResponseCount\":13"));
        assertTrue(result.getSnapshotContent().contains("\"surveyResponseRate\":65.0"));
        assertEquals("全省", OBJECT_MAPPER.readTree(result.getSnapshotContent()).get("reportScope").asText());
        assertTrue(zipEntryContent(outputDir.resolve(result.getGeneratedWordFileName()), "word/document.xml").contains("报告范围：全省"));
        assertTrue(zipEntryContent(outputDir.resolve(result.getGeneratedWordFileName()), "word/document.xml").contains("指标概览："));
        assertTrue(zipEntryContent(outputDir.resolve(result.getGeneratedWordFileName()), "word/document.xml").contains("问卷满意度：已发卷样本20个，有效答卷13份，回收率65.0%。"));
        assertTrue(zipEntryContent(outputDir.resolve(result.getGeneratedExcelFileName()), "xl/worksheets/sheet1.xml").contains("<t>报告范围</t></is></c><c r=\"B3\" t=\"inlineStr\"><is><t>全省</t>"));
        assertTrue(zipEntryContent(outputDir.resolve(result.getGeneratedExcelFileName()), "xl/worksheets/sheet1.xml").contains("<t>章节-预警整改</t>"));
        assertTrue(zipEntryContent(outputDir.resolve(result.getGeneratedExcelFileName()), "xl/worksheets/sheet1.xml").contains("<t>章节-问卷满意度</t>"));

        ArgumentCaptor<ReportGenerationTask> taskCaptor = ArgumentCaptor.forClass(ReportGenerationTask.class);
        verify(reportMapper).updateTask(taskCaptor.capture());
        assertTrue(taskCaptor.getValue().getGeneratedWordFileName().matches("营商环境季度报告-77-v3-\\d{17}\\.docx"));
        assertTrue(taskCaptor.getValue().getGeneratedExcelFileName().matches("营商环境季度报告-77-v3-\\d{17}\\.xlsx"));
        assertEquals("quarter", taskCaptor.getValue().getReportPeriodType());
        assertEquals("province", taskCaptor.getValue().getScopeType());

        ArgumentCaptor<ReportGenerationTask> countTaskCaptor = ArgumentCaptor.forClass(ReportGenerationTask.class);
        verify(reportMapper).countWarningMessages(countTaskCaptor.capture());
        assertEquals("2026Q2", countTaskCaptor.getValue().getReportPeriod());
        assertEquals("quarter", countTaskCaptor.getValue().getReportPeriodType());
        assertEquals("province", countTaskCaptor.getValue().getScopeType());

        ArgumentCaptor<ReportGenerationSnapshot> snapshotCaptor = ArgumentCaptor.forClass(ReportGenerationSnapshot.class);
        verify(reportMapper).insertSnapshot(snapshotCaptor.capture());
        assertEquals("全省", snapshotCaptor.getValue().getReportScope());
        assertEquals("admin", snapshotCaptor.getValue().getGeneratedBy());
    }

    @Test
    void generateAcceptsLegacyReportScope() throws Exception
    {
        ReportGenerationTask legacyTask = task();
        legacyTask.setReportScope("营商专班重点监测");
        when(reportMapper.selectTaskById(77L)).thenReturn(legacyTask);
        when(reportMapper.selectTemplateById(6L)).thenReturn(template());
        when(reportMapper.countIndicators(any(ReportGenerationTask.class))).thenReturn(2L);
        when(reportMapper.countCollectionBatches(any(ReportGenerationTask.class))).thenReturn(2L);
        when(reportMapper.countCollectionItems(any(ReportGenerationTask.class))).thenReturn(2L);
        when(reportMapper.countWarningMessages(any(ReportGenerationTask.class))).thenReturn(2L);
        when(reportMapper.countRectificationIssues(any(ReportGenerationTask.class))).thenReturn(2L);
        when(reportMapper.countSurveySamples(any(ReportGenerationTask.class))).thenReturn(4L);
        when(reportMapper.countSurveyResponses(any(ReportGenerationTask.class))).thenReturn(2L);

        ReportGenerationTask result = service.generate(77L, "admin");

        assertTrue(result.getSnapshotContent().contains("\"reportScope\":\"营商专班重点监测\""));
    }

    @Test
    void generateArchivesHistoryForRepeatedTaskRuns() throws Exception
    {
        ReportGenerationTask completedTask = task();
        completedTask.setTaskStatus("completed");
        completedTask.setGeneratedWordFileName("archived-old.docx");
        completedTask.setGeneratedExcelFileName("archived-old.xlsx");
        when(reportMapper.selectTaskById(77L)).thenReturn(completedTask);
        when(reportMapper.selectTemplateById(6L)).thenReturn(template());
        when(reportMapper.countIndicators(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countCollectionBatches(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countCollectionItems(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countWarningMessages(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countRectificationIssues(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countSurveySamples(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countSurveyResponses(any(ReportGenerationTask.class))).thenReturn(1L);

        ReportGenerationTask result = service.generate(77L, "admin");

        assertTrue(result.getGeneratedWordFileName().endsWith(".docx"));
        assertTrue(!"archived-old.docx".equals(result.getGeneratedWordFileName()));
        verify(reportMapper).insertSnapshot(any(ReportGenerationSnapshot.class));
    }

    @Test
    void generateDeletesFilesWhenTaskUpdateFails()
    {
        when(reportMapper.selectTaskById(77L)).thenReturn(task());
        when(reportMapper.selectTemplateById(6L)).thenReturn(template());
        when(reportMapper.countIndicators(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countCollectionBatches(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countCollectionItems(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countWarningMessages(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countRectificationIssues(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countSurveySamples(any(ReportGenerationTask.class))).thenReturn(1L);
        when(reportMapper.countSurveyResponses(any(ReportGenerationTask.class))).thenReturn(1L);
        doThrow(new ServiceException("db update failed")).when(reportMapper).updateTask(any(ReportGenerationTask.class));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.generate(77L, "admin"));

        assertEquals("db update failed", exception.getMessage());
        try (Stream<Path> files = Files.list(outputDir))
        {
            assertEquals(0L, files.count());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private ReportGenerationTask task()
    {
        ReportGenerationTask task = new ReportGenerationTask();
        task.setTaskId(77L);
        task.setTaskName("营商环境季度报告");
        task.setTemplateId(6L);
        task.setTemplateName("季度综合模板");
        task.setReportPeriod("2026Q2");
        task.setReportScope("全省");
        task.setGenerateMode("manual");
        task.setTaskStatus("pending");
        return task;
    }

    private ReportTemplate template()
    {
        ReportTemplate template = new ReportTemplate();
        template.setTemplateId(6L);
        template.setTemplateName("季度综合模板");
        template.setSections("指标概览,数据采集,预警整改,问卷满意度");
        template.setVersionNo(3);
        return template;
    }

    private String zipEntryContent(Path file, String entryName) throws Exception
    {
        try (ZipFile zipFile = new ZipFile(file.toFile()))
        {
            ZipEntry entry = zipFile.getEntry(entryName);
            assertTrue(entry != null, "missing zip entry " + entryName);
            try (InputStream inputStream = zipFile.getInputStream(entry))
            {
                byte[] buffer = new byte[1024];
                int read;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while ((read = inputStream.read(buffer)) != -1)
                {
                    outputStream.write(buffer, 0, read);
                }
                byte[] bytes = outputStream.toByteArray();
                return new String(bytes, "UTF-8");
            }
        }
    }
}
