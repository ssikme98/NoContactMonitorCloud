package com.ruoyi.nocontact.survey.service.impl;

import com.ruoyi.nocontact.survey.domain.SurveyQuestion;
import com.ruoyi.nocontact.survey.domain.SurveyQuestionnaire;
import com.ruoyi.nocontact.survey.domain.SurveyResponse;
import com.ruoyi.nocontact.survey.domain.SurveyResponseAnswer;
import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSample;
import com.ruoyi.nocontact.survey.domain.vo.SurveyPublicFill;
import com.ruoyi.nocontact.survey.mapper.SurveyResponseMapper;
import com.ruoyi.nocontact.survey.mapper.SurveyTaskMapper;
import com.ruoyi.nocontact.survey.service.ISurveyQuestionnaireService;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyResponseServiceImplTest
{
    private SurveyResponseServiceImpl service;

    @Mock
    private SurveyResponseMapper responseMapper;

    @Mock
    private SurveyTaskMapper taskMapper;

    @Mock
    private ISurveyQuestionnaireService questionnaireService;

    @BeforeEach
    void setUp()
    {
        service = new SurveyResponseServiceImpl();
        ReflectionTestUtils.setField(service, "responseMapper", responseMapper);
        ReflectionTestUtils.setField(service, "taskMapper", taskMapper);
        ReflectionTestUtils.setField(service, "questionnaireService", questionnaireService);
    }

    @Test
    void submitResponseUpdatesSendRecordFillStatusAndRecoveryTime()
    {
        SurveyTaskSample sample = new SurveyTaskSample();
        sample.setSampleId(10L);
        sample.setTaskId(20L);
        sample.setEnterpriseId(30L);
        sample.setToken("sample-token");
        sample.setStatus("1");
        sample.setTokenExpireTime(new Date(System.currentTimeMillis() + 3600_000));

        SurveyTask task = new SurveyTask();
        task.setTaskId(20L);
        task.setQuestionnaireId(40L);
        task.setStatus("2");

        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionId(50L);
        question.setQuestionTitle("整体满意度");
        question.setQuestionType("text");
        question.setRequiredFlag("1");

        SurveyQuestionnaire questionnaire = new SurveyQuestionnaire();
        questionnaire.setQuestionnaireId(40L);
        questionnaire.setQuestions(Collections.singletonList(question));

        when(responseMapper.selectSampleByToken("sample-token")).thenReturn(sample);
        when(taskMapper.selectTaskById(20L)).thenReturn(task);
        when(questionnaireService.selectQuestionnaireById(40L)).thenReturn(questionnaire);
        when(responseMapper.selectResponseBySampleId(10L)).thenReturn(null);
        when(responseMapper.insertResponse(any(SurveyResponse.class))).thenAnswer(invocation -> {
            SurveyResponse inserted = invocation.getArgument(0);
            inserted.setResponseId(60L);
            return 1;
        });

        SurveyResponse response = new SurveyResponse();
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setQuestionId(50L);
        answer.setQuestionType("text");
        answer.setAnswerText("本次服务体验良好");
        response.setAnswers(Collections.singletonList(answer));

        int rows = service.submitResponse("sample-token", response, "127.0.0.1");

        assertEquals(1, rows);

        ArgumentCaptor<SurveyResponse> responseCaptor = ArgumentCaptor.forClass(SurveyResponse.class);
        verify(responseMapper).insertResponse(responseCaptor.capture());
        assertEquals(20L, responseCaptor.getValue().getTaskId());
        assertEquals(10L, responseCaptor.getValue().getSampleId());
        assertNotNull(responseCaptor.getValue().getSubmitTime());

        verify(responseMapper).updateSampleCompleted(10L);
        ArgumentCaptor<Date> recoveryCaptor = ArgumentCaptor.forClass(Date.class);
        verify(responseMapper).updateSendRecordSubmitted(eq(10L), recoveryCaptor.capture());
        assertNotNull(recoveryCaptor.getValue());
    }
}
