package com.ruoyi.nocontact.survey.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.survey.domain.SurveyQuestion;
import com.ruoyi.nocontact.survey.domain.SurveyQuestionOption;
import com.ruoyi.nocontact.survey.domain.SurveyQuestionnaire;
import com.ruoyi.nocontact.survey.mapper.SurveyQuestionnaireMapper;
import com.ruoyi.nocontact.survey.service.ISurveyQuestionnaireService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 问卷Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SurveyQuestionnaireServiceImpl implements ISurveyQuestionnaireService
{
    private static final String STATUS_DRAFT = "0";
    private static final String STATUS_PUBLISHED = "1";
    private static final String STATUS_ENDED = "2";
    private static final String STATUS_COLLECTING = "3";

    private static final String QUESTION_TYPE_SINGLE = "single";
    private static final String QUESTION_TYPE_MULTIPLE = "multiple";
    private static final String QUESTION_TYPE_TEXT = "text";
    private static final String QUESTION_TYPE_SCORE = "score";
    private static final String QUESTION_TYPE_MATRIX_SCORE = "matrix_score";
    private static final String QUESTION_TYPE_LIKERT = "likert";

    private static final String OPTION_TYPE_OPTION = "option";

    private static final Set<String> QUESTION_TYPES = new HashSet<String>(Arrays.asList(
            QUESTION_TYPE_SINGLE,
            QUESTION_TYPE_MULTIPLE,
            QUESTION_TYPE_TEXT,
            QUESTION_TYPE_SCORE,
            QUESTION_TYPE_MATRIX_SCORE
    ));

    @Autowired
    private SurveyQuestionnaireMapper questionnaireMapper;

    @Override
    public List<SurveyQuestionnaire> selectQuestionnaireList(SurveyQuestionnaire questionnaire)
    {
        return questionnaireMapper.selectQuestionnaireList(questionnaire);
    }

    @Override
    public SurveyQuestionnaire selectQuestionnaireById(Long questionnaireId)
    {
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectQuestionnaireById(questionnaireId);
        fillQuestions(questionnaire);
        return questionnaire;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertQuestionnaire(SurveyQuestionnaire questionnaire)
    {
        prepareNewDraft(questionnaire, questionnaire.getCreateBy(), 1, null);
        int rows = questionnaireMapper.insertQuestionnaire(questionnaire);
        saveQuestions(questionnaire);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyQuestionnaire updateQuestionnaire(SurveyQuestionnaire questionnaire)
    {
        SurveyQuestionnaire exists = questionnaireMapper.selectQuestionnaireById(questionnaire.getQuestionnaireId());
        if (exists == null)
        {
            throw new ServiceException("问卷不存在");
        }
        if (STATUS_ENDED.equals(exists.getStatus()))
        {
            throw new ServiceException("已结束问卷不允许修改");
        }
        if (isImmutableQuestionnaire(exists.getStatus()))
        {
            return createDraftFromSubmitted(questionnaire, exists, questionnaire.getUpdateBy());
        }
        validateQuestionnaire(questionnaire);
        questionnaire.setStatus(STATUS_DRAFT);
        questionnaire.setVersionNo(exists.getVersionNo());
        questionnaire.setSourceQuestionnaireId(exists.getSourceQuestionnaireId());
        questionnaire.setUpdateTime(DateUtils.getNowDate());
        questionnaireMapper.updateQuestionnaire(questionnaire);
        questionnaireMapper.deleteOptionsByQuestionnaireId(questionnaire.getQuestionnaireId());
        questionnaireMapper.deleteQuestionsByQuestionnaireId(questionnaire.getQuestionnaireId());
        saveQuestions(questionnaire);
        return selectQuestionnaireById(questionnaire.getQuestionnaireId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteQuestionnaireByIds(Long[] questionnaireIds)
    {
        return questionnaireMapper.deleteQuestionnaireByIds(questionnaireIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyQuestionnaire createDraftFromPublished(Long questionnaireId, String operName)
    {
        SurveyQuestionnaire published = selectQuestionnaireById(questionnaireId);
        if (published == null)
        {
            throw new ServiceException("问卷不存在");
        }
        if (!STATUS_PUBLISHED.equals(published.getStatus()) && !STATUS_COLLECTING.equals(published.getStatus()))
        {
            throw new ServiceException("只有已发布或收集中问卷才能创建新版草稿");
        }
        SurveyQuestionnaire draft = cloneAsDraft(published, operName);
        questionnaireMapper.insertQuestionnaire(draft);
        saveQuestions(draft);
        return selectQuestionnaireById(draft.getQuestionnaireId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishQuestionnaire(Long questionnaireId, String operName)
    {
        SurveyQuestionnaire questionnaire = selectQuestionnaireById(questionnaireId);
        if (questionnaire == null)
        {
            throw new ServiceException("问卷不存在");
        }
        if (!STATUS_DRAFT.equals(questionnaire.getStatus()))
        {
            throw new ServiceException("只有草稿问卷允许发布");
        }
        if (StringUtils.isEmpty(questionnaire.getQuestions()))
        {
            throw new ServiceException("问卷至少需要配置一道题目");
        }
        SurveyQuestionnaire update = new SurveyQuestionnaire();
        update.setQuestionnaireId(questionnaireId);
        update.setStatus(STATUS_PUBLISHED);
        update.setPublishedTime(DateUtils.getNowDate());
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        return questionnaireMapper.updateQuestionnaireStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int endQuestionnaire(Long questionnaireId, String operName)
    {
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectQuestionnaireById(questionnaireId);
        if (questionnaire == null)
        {
            throw new ServiceException("问卷不存在");
        }
        if (!STATUS_PUBLISHED.equals(questionnaire.getStatus()) && !STATUS_COLLECTING.equals(questionnaire.getStatus()))
        {
            throw new ServiceException("只有已发布或收集中问卷允许结束");
        }
        SurveyQuestionnaire update = new SurveyQuestionnaire();
        update.setQuestionnaireId(questionnaireId);
        update.setStatus(STATUS_ENDED);
        update.setEndedTime(DateUtils.getNowDate());
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        return questionnaireMapper.updateQuestionnaireStatus(update);
    }

    private SurveyQuestionnaire createDraftFromSubmitted(SurveyQuestionnaire submitted, SurveyQuestionnaire exists, String operName)
    {
        SurveyQuestionnaire draft = new SurveyQuestionnaire();
        draft.setQuestionnaireName(submitted.getQuestionnaireName());
        draft.setDescription(submitted.getDescription());
        draft.setQuestions(submitted.getQuestions());
        Long rootId = rootQuestionnaireId(exists);
        Integer versionNo = nextVersionNo(rootId);
        prepareNewDraft(draft, operName, versionNo, rootId);
        questionnaireMapper.insertQuestionnaire(draft);
        saveQuestions(draft);
        return selectQuestionnaireById(draft.getQuestionnaireId());
    }

    private SurveyQuestionnaire cloneAsDraft(SurveyQuestionnaire published, String operName)
    {
        SurveyQuestionnaire draft = new SurveyQuestionnaire();
        draft.setQuestionnaireName(published.getQuestionnaireName());
        draft.setDescription(published.getDescription());
        draft.setQuestions(cloneQuestions(published.getQuestions()));
        Long rootId = rootQuestionnaireId(published);
        Integer versionNo = nextVersionNo(rootId);
        prepareNewDraft(draft, operName, versionNo, rootId);
        return draft;
    }

    private void prepareNewDraft(SurveyQuestionnaire questionnaire, String operName, Integer versionNo, Long sourceQuestionnaireId)
    {
        validateQuestionnaire(questionnaire);
        questionnaire.setStatus(STATUS_DRAFT);
        questionnaire.setVersionNo(versionNo == null ? 1 : versionNo);
        questionnaire.setSourceQuestionnaireId(sourceQuestionnaireId);
        questionnaire.setCreateBy(operName);
        questionnaire.setCreateTime(DateUtils.getNowDate());
    }

    private boolean isImmutableQuestionnaire(String status)
    {
        return STATUS_PUBLISHED.equals(status) || STATUS_COLLECTING.equals(status);
    }

    private void validateQuestionnaire(SurveyQuestionnaire questionnaire)
    {
        if (StringUtils.isBlank(questionnaire.getQuestionnaireName()))
        {
            throw new ServiceException("问卷名称不能为空");
        }
        List<SurveyQuestion> questions = questionnaire.getQuestions();
        if (StringUtils.isEmpty(questions))
        {
            return;
        }
        for (SurveyQuestion question : questions)
        {
            if (StringUtils.isBlank(question.getQuestionTitle()))
            {
                throw new ServiceException("题目标题不能为空");
            }
            if (!QUESTION_TYPES.contains(question.getQuestionType()))
            {
                throw new ServiceException("不支持的题型：" + question.getQuestionType());
            }
            if (!QUESTION_TYPE_MATRIX_SCORE.equals(question.getQuestionType()))
            {
                question.setDimension(null);
            }
            if ((QUESTION_TYPE_SCORE.equals(question.getQuestionType()) || QUESTION_TYPE_MATRIX_SCORE.equals(question.getQuestionType()))
                    && (question.getScoreMax() == null || question.getScoreMax() <= 0))
            {
                question.setScoreMax(5);
            }
        }
    }

    private void saveQuestions(SurveyQuestionnaire questionnaire)
    {
        List<SurveyQuestion> questions = questionnaire.getQuestions();
        if (StringUtils.isEmpty(questions))
        {
            return;
        }
        for (int i = 0; i < questions.size(); i++)
        {
            SurveyQuestion question = questions.get(i);
            question.setQuestionId(null);
            question.setQuestionnaireId(questionnaire.getQuestionnaireId());
            question.setOrderNum(question.getOrderNum() == null ? i + 1 : question.getOrderNum());
            question.setRequiredFlag(StringUtils.defaultIfBlank(question.getRequiredFlag(), "0"));
            question.setScoreMax(question.getScoreMax() == null ? 0 : question.getScoreMax());
            questionnaireMapper.insertQuestion(question);
            saveOptions(question);
        }
    }

    private void saveOptions(SurveyQuestion question)
    {
        List<SurveyQuestionOption> options = question.getOptions();
        if (StringUtils.isEmpty(options))
        {
            return;
        }
        List<SurveyQuestionOption> rows = new ArrayList<SurveyQuestionOption>();
        for (int i = 0; i < options.size(); i++)
        {
            SurveyQuestionOption option = options.get(i);
            if (StringUtils.isBlank(option.getOptionLabel()))
            {
                continue;
            }
            option.setOptionId(null);
            option.setQuestionId(question.getQuestionId());
            option.setOptionType(StringUtils.defaultIfBlank(option.getOptionType(), OPTION_TYPE_OPTION));
            option.setOptionValue(StringUtils.defaultIfBlank(option.getOptionValue(), String.valueOf(i + 1)));
            option.setOrderNum(option.getOrderNum() == null ? i + 1 : option.getOrderNum());
            rows.add(option);
        }
        if (!rows.isEmpty())
        {
            questionnaireMapper.batchQuestionOption(rows);
        }
    }

    private void fillQuestions(SurveyQuestionnaire questionnaire)
    {
        if (questionnaire == null)
        {
            return;
        }
        List<SurveyQuestion> questions = questionnaireMapper.selectQuestionsByQuestionnaireId(questionnaire.getQuestionnaireId());
        if (StringUtils.isEmpty(questions))
        {
            questionnaire.setQuestions(new ArrayList<SurveyQuestion>());
            return;
        }
        List<SurveyQuestionOption> options = questionnaireMapper.selectOptionsByQuestionnaireId(questionnaire.getQuestionnaireId());
        Map<Long, List<SurveyQuestionOption>> optionMap = options.stream().collect(Collectors.groupingBy(SurveyQuestionOption::getQuestionId));
        for (SurveyQuestion question : questions)
        {
            question.setOptions(optionMap.getOrDefault(question.getQuestionId(), new ArrayList<SurveyQuestionOption>()));
        }
        questionnaire.setQuestions(questions);
    }

    private List<SurveyQuestion> cloneQuestions(List<SurveyQuestion> questions)
    {
        List<SurveyQuestion> rows = new ArrayList<SurveyQuestion>();
        if (StringUtils.isEmpty(questions))
        {
            return rows;
        }
        for (SurveyQuestion source : questions)
        {
            SurveyQuestion target = new SurveyQuestion();
            target.setQuestionTitle(source.getQuestionTitle());
            target.setQuestionType(source.getQuestionType());
            target.setRequiredFlag(source.getRequiredFlag());
            target.setDimension(source.getDimension());
            target.setScoreMax(source.getScoreMax());
            target.setOrderNum(source.getOrderNum());
            target.setOptions(cloneOptions(source.getOptions()));
            rows.add(target);
        }
        return rows;
    }

    private List<SurveyQuestionOption> cloneOptions(List<SurveyQuestionOption> options)
    {
        List<SurveyQuestionOption> rows = new ArrayList<SurveyQuestionOption>();
        if (StringUtils.isEmpty(options))
        {
            return rows;
        }
        for (SurveyQuestionOption source : options)
        {
            SurveyQuestionOption target = new SurveyQuestionOption();
            target.setOptionType(source.getOptionType());
            target.setOptionLabel(source.getOptionLabel());
            target.setOptionValue(source.getOptionValue());
            target.setScoreValue(source.getScoreValue());
            target.setOrderNum(source.getOrderNum());
            rows.add(target);
        }
        return rows;
    }

    private Long rootQuestionnaireId(SurveyQuestionnaire questionnaire)
    {
        return questionnaire.getSourceQuestionnaireId() == null ? questionnaire.getQuestionnaireId() : questionnaire.getSourceQuestionnaireId();
    }

    private Integer nextVersionNo(Long rootQuestionnaireId)
    {
        Integer versionNo = questionnaireMapper.selectMaxVersionNo(rootQuestionnaireId);
        return versionNo == null ? 1 : versionNo + 1;
    }
}
