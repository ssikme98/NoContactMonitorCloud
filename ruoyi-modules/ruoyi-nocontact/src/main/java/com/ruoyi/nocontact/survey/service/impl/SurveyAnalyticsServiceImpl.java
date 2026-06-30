package com.ruoyi.nocontact.survey.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.survey.domain.SurveyQuestion;
import com.ruoyi.nocontact.survey.domain.SurveyQuestionOption;
import com.ruoyi.nocontact.survey.domain.SurveyQuestionnaire;
import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.domain.vo.SurveyDistributionItem;
import com.ruoyi.nocontact.survey.domain.vo.SurveyQuestionDistribution;
import com.ruoyi.nocontact.survey.domain.vo.SurveyResponseAnswerRow;
import com.ruoyi.nocontact.survey.domain.vo.SurveySatisfactionAnalytics;
import com.ruoyi.nocontact.survey.domain.vo.SurveySatisfactionStat;
import com.ruoyi.nocontact.survey.mapper.SurveyResponseMapper;
import com.ruoyi.nocontact.survey.mapper.SurveyTaskMapper;
import com.ruoyi.nocontact.survey.service.ISurveyAnalyticsService;
import com.ruoyi.nocontact.survey.service.ISurveyQuestionnaireService;
import com.ruoyi.nocontact.survey.service.ISurveyTaskService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 满意度统计分析Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SurveyAnalyticsServiceImpl implements ISurveyAnalyticsService
{
    private static final Logger log = LoggerFactory.getLogger(SurveyAnalyticsServiceImpl.class);

    private static final String QUESTION_TYPE_SINGLE = "single";
    private static final String QUESTION_TYPE_MULTIPLE = "multiple";
    private static final String QUESTION_TYPE_TEXT = "text";
    private static final String QUESTION_TYPE_SCORE = "score";
    private static final String QUESTION_TYPE_MATRIX_SCORE = "matrix_score";
    private static final String QUESTION_TYPE_LIKERT = "likert";

    private static final String OPTION_TYPE_OPTION = "option";
    private static final String OPTION_TYPE_COLUMN = "column";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SurveyTaskMapper taskMapper;

    @Autowired
    private ISurveyTaskService surveyTaskService;

    @Autowired
    private SurveyResponseMapper responseMapper;

    @Autowired
    private ISurveyQuestionnaireService questionnaireService;

    @Override
    public SurveySatisfactionAnalytics selectSatisfactionAnalytics(Long taskId)
    {
        SurveyTask task = surveyTaskService.selectTaskById(taskId);
        if (task == null)
        {
            throw new ServiceException("调研任务不存在");
        }
        SurveyQuestionnaire questionnaire = questionnaireService.selectQuestionnaireById(task.getQuestionnaireId());
        if (questionnaire == null)
        {
            throw new ServiceException("问卷不存在");
        }
        List<SurveyResponseAnswerRow> rows = responseMapper.selectAnswerRowsByTaskId(taskId);
        Set<Long> responseIds = rows.stream().map(SurveyResponseAnswerRow::getResponseId).collect(Collectors.toSet());
        Map<Long, SurveyQuestion> questionMap = questionnaire.getQuestions().stream()
                .collect(Collectors.toMap(SurveyQuestion::getQuestionId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<ScorePoint> points = new ArrayList<ScorePoint>();
        for (SurveyResponseAnswerRow row : rows)
        {
            SurveyQuestion question = questionMap.get(row.getQuestionId());
            if (question == null)
            {
                continue;
            }
            for (Double score : scoreAnswer(question, row))
            {
                points.add(new ScorePoint(row, dimensionName(question), score));
            }
        }

        SurveySatisfactionAnalytics analytics = new SurveySatisfactionAnalytics();
        analytics.setTaskId(taskId);
        analytics.setResponseCount(Long.valueOf(responseIds.size()));
        analytics.setOverallScore(average(points));
        analytics.setRegionStats(groupStats(points, item -> defaultName(item.regionName, "未填城市")));
        analytics.setIndustryStats(groupStats(points, item -> defaultName(item.industryCategory, "未填行业")));
        analytics.setDimensionStats(groupStats(points, item -> item.dimension));
        analytics.setScaleStats(groupStats(points, item -> defaultName(item.enterpriseScale, "未填规模")));
        analytics.setQuestionDistributions(distributions(questionnaire, rows, responseIds.size()));
        return analytics;
    }

    @Override
    public byte[] buildSatisfactionReport(Long taskId) throws IOException
    {
        SurveySatisfactionAnalytics analytics = selectSatisfactionAnalytics(taskId);
        XWPFDocument document = new XWPFDocument();
        addTitle(document, "满意度统计分析报告");
        addParagraph(document, "调研任务ID：" + taskId);
        addParagraph(document, "有效答卷数：" + analytics.getResponseCount());
        addParagraph(document, "总体满意度：" + formatScore(analytics.getOverallScore()));
        addStatsTable(document, "题目维度统计", analytics.getDimensionStats());
        addStatsTable(document, "城市统计", analytics.getRegionStats());
        addStatsTable(document, "行业统计", analytics.getIndustryStats());
        addStatsTable(document, "企业规模统计", analytics.getScaleStats());
        addDistributionTables(document, analytics.getQuestionDistributions());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        document.write(output);
        document.close();
        return output.toByteArray();
    }

    private List<Double> scoreAnswer(SurveyQuestion question, SurveyResponseAnswerRow row)
    {
        List<Double> scores = new ArrayList<Double>();
        if (QUESTION_TYPE_SCORE.equals(question.getQuestionType()))
        {
            if (row.getScoreValue() != null)
            {
                scores.add(toScore(row.getScoreValue(), scoreMax(question)));
            }
        }
        else if (QUESTION_TYPE_LIKERT.equals(question.getQuestionType()))
        {
            SurveyQuestionOption option = optionByValue(question, OPTION_TYPE_OPTION, row.getOptionValue());
            int max = maxOptionScore(question, OPTION_TYPE_OPTION);
            if (option != null && option.getScoreValue() != null && max > 0)
            {
                scores.add(toScore(option.getScoreValue(), max));
            }
        }
        else if (QUESTION_TYPE_MATRIX_SCORE.equals(question.getQuestionType()) && StringUtils.isNotBlank(row.getAnswerText()))
        {
            int max = scoreMax(question);
            Map<String, String> answerMap = parseMatrix(row.getAnswerText());
            for (String columnValue : answerMap.values())
            {
                SurveyQuestionOption option = optionByValue(question, OPTION_TYPE_COLUMN, columnValue);
                if (option != null && option.getScoreValue() != null)
                {
                    scores.add(toScore(option.getScoreValue(), max));
                }
            }
        }
        return scores;
    }

    private List<SurveyQuestionDistribution> distributions(SurveyQuestionnaire questionnaire, List<SurveyResponseAnswerRow> rows, int responseCount)
    {
        Map<Long, List<SurveyResponseAnswerRow>> rowMap = rows.stream().collect(Collectors.groupingBy(SurveyResponseAnswerRow::getQuestionId));
        List<SurveyQuestionDistribution> distributions = new ArrayList<SurveyQuestionDistribution>();
        for (SurveyQuestion question : questionnaire.getQuestions())
        {
            if (!QUESTION_TYPE_SINGLE.equals(question.getQuestionType())
                    && !QUESTION_TYPE_MULTIPLE.equals(question.getQuestionType())
                    && !QUESTION_TYPE_TEXT.equals(question.getQuestionType()))
            {
                continue;
            }
            SurveyQuestionDistribution distribution = new SurveyQuestionDistribution();
            distribution.setQuestionId(question.getQuestionId());
            distribution.setQuestionTitle(question.getQuestionTitle());
            distribution.setQuestionType(question.getQuestionType());
            distribution.setItems(distributionItems(question, rowMap.get(question.getQuestionId()), responseCount));
            distributions.add(distribution);
        }
        return distributions;
    }

    private List<SurveyDistributionItem> distributionItems(SurveyQuestion question, List<SurveyResponseAnswerRow> rows, int responseCount)
    {
        List<SurveyResponseAnswerRow> answerRows = rows == null ? new ArrayList<SurveyResponseAnswerRow>() : rows;
        if (QUESTION_TYPE_TEXT.equals(question.getQuestionType()))
        {
            long filled = answerRows.stream().filter(item -> StringUtils.isNotBlank(item.getAnswerText())).count();
            List<SurveyDistributionItem> items = new ArrayList<SurveyDistributionItem>();
            items.add(distributionItem("已填写", "filled", filled, responseCount));
            items.add(distributionItem("未填写", "blank", Math.max(0, responseCount - filled), responseCount));
            return items;
        }

        Map<String, Long> counts = new HashMap<String, Long>();
        for (SurveyResponseAnswerRow row : answerRows)
        {
            if (QUESTION_TYPE_MULTIPLE.equals(question.getQuestionType()))
            {
                if (StringUtils.isBlank(row.getAnswerText()))
                {
                    continue;
                }
                for (String value : row.getAnswerText().split(","))
                {
                    addCount(counts, value);
                }
            }
            else if (StringUtils.isNotBlank(row.getOptionValue()))
            {
                addCount(counts, row.getOptionValue());
            }
        }
        List<SurveyDistributionItem> items = new ArrayList<SurveyDistributionItem>();
        for (SurveyQuestionOption option : optionRows(question, OPTION_TYPE_OPTION))
        {
            items.add(distributionItem(option.getOptionLabel(), option.getOptionValue(), counts.getOrDefault(option.getOptionValue(), 0L), responseCount));
        }
        return items;
    }

    private List<SurveySatisfactionStat> groupStats(List<ScorePoint> points, Function<ScorePoint, String> keyFunction)
    {
        Map<String, StatAccumulator> rows = new LinkedHashMap<String, StatAccumulator>();
        for (ScorePoint point : points)
        {
            String key = keyFunction.apply(point);
            StatAccumulator stat = rows.get(key);
            if (stat == null)
            {
                stat = new StatAccumulator(key);
                rows.put(key, stat);
            }
            stat.add(point);
        }
        List<SurveySatisfactionStat> stats = new ArrayList<SurveySatisfactionStat>();
        for (StatAccumulator stat : rows.values())
        {
            SurveySatisfactionStat item = new SurveySatisfactionStat();
            item.setStatName(stat.name);
            item.setSampleCount(Long.valueOf(stat.responseIds.size()));
            item.setScore(round(stat.count == 0 ? 0 : stat.total / stat.count));
            stats.add(item);
        }
        return stats;
    }

    private Double average(List<ScorePoint> points)
    {
        if (points.isEmpty())
        {
            return 0D;
        }
        double total = 0D;
        for (ScorePoint point : points)
        {
            total += point.score;
        }
        return round(total / points.size());
    }

    private void addCount(Map<String, Long> counts, String value)
    {
        String key = StringUtils.trim(value);
        if (StringUtils.isBlank(key))
        {
            return;
        }
        counts.put(key, counts.getOrDefault(key, 0L) + 1);
    }

    private SurveyDistributionItem distributionItem(String label, String value, long count, int responseCount)
    {
        SurveyDistributionItem item = new SurveyDistributionItem();
        item.setItemLabel(label);
        item.setItemValue(value);
        item.setCount(count);
        item.setPercent(responseCount == 0 ? 0D : round(count * 100D / responseCount));
        return item;
    }

    private List<SurveyQuestionOption> optionRows(SurveyQuestion question, String optionType)
    {
        List<SurveyQuestionOption> rows = new ArrayList<SurveyQuestionOption>();
        if (StringUtils.isEmpty(question.getOptions()))
        {
            return rows;
        }
        for (SurveyQuestionOption option : question.getOptions())
        {
            if (optionType.equals(option.getOptionType()))
            {
                rows.add(option);
            }
        }
        return rows;
    }

    private SurveyQuestionOption optionByValue(SurveyQuestion question, String optionType, String optionValue)
    {
        for (SurveyQuestionOption option : optionRows(question, optionType))
        {
            if (StringUtils.equals(option.getOptionValue(), optionValue))
            {
                return option;
            }
        }
        return null;
    }

    private int maxOptionScore(SurveyQuestion question, String optionType)
    {
        int max = 0;
        for (SurveyQuestionOption option : optionRows(question, optionType))
        {
            if (option.getScoreValue() != null && option.getScoreValue() > max)
            {
                max = option.getScoreValue();
            }
        }
        return max;
    }

    private int scoreMax(SurveyQuestion question)
    {
        return question.getScoreMax() == null || question.getScoreMax() <= 0 ? Math.max(1, maxOptionScore(question, OPTION_TYPE_COLUMN)) : question.getScoreMax();
    }

    private Double toScore(Integer value, int max)
    {
        return max <= 0 ? 0D : round(value * 100D / max);
    }

    private Map<String, String> parseMatrix(String answerText)
    {
        try
        {
            return objectMapper.readValue(answerText, new TypeReference<Map<String, String>>() {});
        }
        catch (IOException e)
        {
            log.error("Failed to parse matrix answer json", e);
            return new HashMap<String, String>();
        }
    }

    private String dimensionName(SurveyQuestion question)
    {
        return defaultName(question.getDimension(), question.getQuestionTitle());
    }

    private String defaultName(String value, String defaultValue)
    {
        return StringUtils.defaultIfBlank(value, defaultValue);
    }

    private Double round(double value)
    {
        return Math.round(value * 100D) / 100D;
    }

    private void addTitle(XWPFDocument document, String text)
    {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(18);
        run.setFontFamily("Microsoft YaHei");
        run.setText(text);
    }

    private void addParagraph(XWPFDocument document, String text)
    {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Microsoft YaHei");
        run.setText(text);
    }

    private void addStatsTable(XWPFDocument document, String title, List<SurveySatisfactionStat> stats)
    {
        addParagraph(document, title);
        XWPFTable table = document.createTable(Math.max(1, stats.size()) + 1, 3);
        setCell(table.getRow(0), 0, "名称");
        setCell(table.getRow(0), 1, "样本数");
        setCell(table.getRow(0), 2, "满意度");
        for (int i = 0; i < stats.size(); i++)
        {
            SurveySatisfactionStat stat = stats.get(i);
            XWPFTableRow row = table.getRow(i + 1);
            setCell(row, 0, stat.getStatName());
            setCell(row, 1, String.valueOf(stat.getSampleCount()));
            setCell(row, 2, formatScore(stat.getScore()));
        }
    }

    private void addDistributionTables(XWPFDocument document, List<SurveyQuestionDistribution> distributions)
    {
        for (SurveyQuestionDistribution distribution : distributions)
        {
            addParagraph(document, "分布统计：" + distribution.getQuestionTitle());
            XWPFTable table = document.createTable(distribution.getItems().size() + 1, 3);
            setCell(table.getRow(0), 0, "选项");
            setCell(table.getRow(0), 1, "数量");
            setCell(table.getRow(0), 2, "占比");
            for (int i = 0; i < distribution.getItems().size(); i++)
            {
                SurveyDistributionItem item = distribution.getItems().get(i);
                XWPFTableRow row = table.getRow(i + 1);
                setCell(row, 0, item.getItemLabel());
                setCell(row, 1, String.valueOf(item.getCount()));
                setCell(row, 2, formatScore(item.getPercent()));
            }
        }
    }

    private void setCell(XWPFTableRow row, int index, String text)
    {
        row.getCell(index).setText(text);
    }

    private String formatScore(Double value)
    {
        return String.format(java.util.Locale.ROOT, "%.2f", value == null ? 0D : value);
    }

    private static class ScorePoint
    {
        private final Long responseId;
        private final String regionName;
        private final String industryCategory;
        private final String enterpriseScale;
        private final String dimension;
        private final Double score;

        private ScorePoint(SurveyResponseAnswerRow row, String dimension, Double score)
        {
            this.responseId = row.getResponseId();
            this.regionName = row.getRegionName();
            this.industryCategory = row.getIndustryCategory();
            this.enterpriseScale = row.getEnterpriseScale();
            this.dimension = dimension;
            this.score = score;
        }
    }

    private static class StatAccumulator
    {
        private final String name;
        private final Set<Long> responseIds = new HashSet<Long>();
        private double total;
        private int count;

        private StatAccumulator(String name)
        {
            this.name = name;
        }

        private void add(ScorePoint point)
        {
            responseIds.add(point.responseId);
            total += point.score;
            count++;
        }
    }
}
