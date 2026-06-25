package com.ruoyi.survey.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import com.ruoyi.survey.domain.SurveyQuestion;
import com.ruoyi.survey.domain.SurveyQuestionOption;
import com.ruoyi.survey.domain.SurveyQuestionnaire;
import com.ruoyi.survey.domain.SurveyResponse;
import com.ruoyi.survey.domain.SurveyResponseAnswer;
import com.ruoyi.survey.domain.SurveyTask;
import com.ruoyi.survey.domain.SurveyTaskSample;
import com.ruoyi.survey.domain.vo.SurveyPublicFill;
import com.ruoyi.survey.mapper.SurveyResponseMapper;
import com.ruoyi.survey.mapper.SurveyTaskMapper;
import com.ruoyi.survey.service.ISurveyQuestionnaireService;
import com.ruoyi.survey.service.ISurveyResponseService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 问卷答卷Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SurveyResponseServiceImpl implements ISurveyResponseService
{
    private static final String TASK_STATUS_DISPATCHED = "2";
    private static final String TASK_STATUS_ENDED = "3";
    private static final String SAMPLE_STATUS_COMPLETED = "2";

    private static final String QUESTION_TYPE_SINGLE = "single";
    private static final String QUESTION_TYPE_MULTIPLE = "multiple";
    private static final String QUESTION_TYPE_TEXT = "text";
    private static final String QUESTION_TYPE_SCORE = "score";
    private static final String QUESTION_TYPE_MATRIX_SCORE = "matrix_score";
    private static final String QUESTION_TYPE_LIKERT = "likert";

    @Autowired
    private SurveyResponseMapper responseMapper;

    @Autowired
    private SurveyTaskMapper taskMapper;

    @Autowired
    private ISurveyQuestionnaireService questionnaireService;

    @Override
    public SurveyPublicFill getFill(String token)
    {
        SurveyPublicFill fill = new SurveyPublicFill();
        SurveyTaskSample sample = responseMapper.selectSampleByToken(token);
        if (sample == null)
        {
            return invalid(fill, "token无效");
        }
        SurveyTask task = taskMapper.selectTaskById(sample.getTaskId());
        if (task == null)
        {
            return invalid(fill, "调研任务不存在");
        }
        fill.setSample(sample);
        fill.setTask(task);
        if (TASK_STATUS_ENDED.equals(task.getStatus()) || !TASK_STATUS_DISPATCHED.equals(task.getStatus()))
        {
            return invalid(fill, "调研任务未发卷或已结束，token已失效");
        }
        if (sample.getTokenExpireTime() == null || sample.getTokenExpireTime().before(new Date()))
        {
            return invalid(fill, "token已失效");
        }
        SurveyQuestionnaire questionnaire = questionnaireService.selectQuestionnaireById(task.getQuestionnaireId());
        fill.setQuestionnaire(questionnaire);
        SurveyResponse exists = responseMapper.selectResponseBySampleId(sample.getSampleId());
        fill.setValid(true);
        if (exists != null || SAMPLE_STATUS_COMPLETED.equals(sample.getStatus()))
        {
            fill.setSubmitted(true);
            fill.setMessage("已提交");
        }
        else
        {
            fill.setSubmitted(false);
            fill.setMessage("可填写");
        }
        return fill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitResponse(String token, SurveyResponse response, String clientIp)
    {
        SurveyPublicFill fill = getFill(token);
        if (!Boolean.TRUE.equals(fill.getValid()))
        {
            throw new ServiceException(fill.getMessage());
        }
        if (Boolean.TRUE.equals(fill.getSubmitted()))
        {
            throw new ServiceException("已提交，不允许重复提交");
        }
        SurveyQuestionnaire questionnaire = fill.getQuestionnaire();
        validateRequiredAnswers(questionnaire, response.getAnswers());
        SurveyTaskSample sample = fill.getSample();
        SurveyTask task = fill.getTask();
        response.setTaskId(task.getTaskId());
        response.setSampleId(sample.getSampleId());
        response.setEnterpriseId(sample.getEnterpriseId());
        response.setQuestionnaireId(task.getQuestionnaireId());
        response.setClientIp(clientIp);
        response.setSubmitTime(DateUtils.getNowDate());
        response.setStatus("0");
        int rows;
        try
        {
            rows = responseMapper.insertResponse(response);
        }
        catch (DuplicateKeyException e)
        {
            throw new ServiceException("已提交，不允许重复提交");
        }
        List<SurveyResponseAnswer> answers = normalizeAnswers(questionnaire, response);
        if (!answers.isEmpty())
        {
            responseMapper.batchResponseAnswer(answers);
        }
        responseMapper.updateSampleCompleted(sample.getSampleId());
        return rows;
    }

    private SurveyPublicFill invalid(SurveyPublicFill fill, String message)
    {
        fill.setValid(false);
        fill.setSubmitted(false);
        fill.setMessage(message);
        return fill;
    }

    private void validateRequiredAnswers(SurveyQuestionnaire questionnaire, List<SurveyResponseAnswer> answers)
    {
        if (questionnaire == null || StringUtils.isEmpty(questionnaire.getQuestions()))
        {
            throw new ServiceException("问卷不存在或未配置题目");
        }
        Map<Long, SurveyResponseAnswer> answerMap = new HashMap<Long, SurveyResponseAnswer>();
        if (answers != null)
        {
            for (SurveyResponseAnswer answer : answers)
            {
                if (answer.getQuestionId() != null)
                {
                    answerMap.put(answer.getQuestionId(), answer);
                }
            }
        }
        for (SurveyQuestion question : questionnaire.getQuestions())
        {
            SurveyResponseAnswer answer = answerMap.get(question.getQuestionId());
            if ("1".equals(question.getRequiredFlag()) && !hasAnswer(question, answer))
            {
                throw new ServiceException("题目“" + question.getQuestionTitle() + "”为必填");
            }
            if (answer != null)
            {
                validateAnswerFormat(question, answer);
            }
        }
    }

    private boolean hasAnswer(SurveyQuestion question, SurveyResponseAnswer answer)
    {
        if (answer == null)
        {
            return false;
        }
        if (QUESTION_TYPE_SINGLE.equals(question.getQuestionType()) || QUESTION_TYPE_LIKERT.equals(question.getQuestionType()))
        {
            return StringUtils.isNotBlank(answer.getOptionValue());
        }
        if (QUESTION_TYPE_MULTIPLE.equals(question.getQuestionType()) || QUESTION_TYPE_TEXT.equals(question.getQuestionType()) || QUESTION_TYPE_MATRIX_SCORE.equals(question.getQuestionType()))
        {
            return StringUtils.isNotBlank(answer.getAnswerText());
        }
        if (QUESTION_TYPE_SCORE.equals(question.getQuestionType()))
        {
            return answer.getScoreValue() != null;
        }
        return false;
    }

    private void validateAnswerFormat(SurveyQuestion question, SurveyResponseAnswer answer)
    {
        if (QUESTION_TYPE_SINGLE.equals(question.getQuestionType()) || QUESTION_TYPE_LIKERT.equals(question.getQuestionType()))
        {
            if (StringUtils.isNotBlank(answer.getOptionValue()) && !optionValues(question).contains(answer.getOptionValue()))
            {
                throw new ServiceException("题目“" + question.getQuestionTitle() + "”选项无效");
            }
        }
        else if (QUESTION_TYPE_MULTIPLE.equals(question.getQuestionType()) && StringUtils.isNotBlank(answer.getAnswerText()))
        {
            Set<String> allowed = optionValues(question);
            List<String> values = Arrays.asList(answer.getAnswerText().split(","));
            for (String v : values)
            {
                String value = v.trim();
                if (StringUtils.isBlank(value))
                {
                    continue;
                }
                if (!allowed.contains(value))
                {
                    throw new ServiceException("题目“" + question.getQuestionTitle() + "”选项无效");
                }
            }
        }
        else if (QUESTION_TYPE_SCORE.equals(question.getQuestionType()) && answer.getScoreValue() != null)
        {
            int max = question.getScoreMax() == null || question.getScoreMax() <= 0 ? 5 : question.getScoreMax();
            if (answer.getScoreValue() < 1 || answer.getScoreValue() > max)
            {
                throw new ServiceException("题目“" + question.getQuestionTitle() + "”评分超出范围");
            }
        }
    }

    private Set<String> optionValues(SurveyQuestion question)
    {
        if (StringUtils.isEmpty(question.getOptions()))
        {
            return new HashSet<String>();
        }
        return question.getOptions().stream().map(SurveyQuestionOption::getOptionValue).collect(Collectors.toSet());
    }

    private List<SurveyResponseAnswer> normalizeAnswers(SurveyQuestionnaire questionnaire, SurveyResponse response)
    {
        Map<Long, SurveyQuestion> questionMap = questionnaire.getQuestions().stream().collect(Collectors.toMap(SurveyQuestion::getQuestionId, item -> item));
        List<SurveyResponseAnswer> rows = new ArrayList<SurveyResponseAnswer>();
        if (StringUtils.isEmpty(response.getAnswers()))
        {
            return rows;
        }
        for (SurveyResponseAnswer answer : response.getAnswers())
        {
            SurveyQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null || !hasAnswer(question, answer))
            {
                continue;
            }
            answer.setResponseId(response.getResponseId());
            answer.setQuestionType(question.getQuestionType());
            rows.add(answer);
        }
        return rows;
    }
}
