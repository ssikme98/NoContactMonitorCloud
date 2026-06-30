package com.ruoyi.nocontact.survey.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.survey.domain.SurveyQuestion;
import com.ruoyi.nocontact.survey.domain.SurveyQuestionnaire;
import com.ruoyi.nocontact.survey.mapper.SurveyQuestionnaireMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyQuestionnaireServiceImplTest
{
    private SurveyQuestionnaireServiceImpl service;

    @Mock
    private SurveyQuestionnaireMapper questionnaireMapper;

    @BeforeEach
    void setUp()
    {
        service = new SurveyQuestionnaireServiceImpl();
        ReflectionTestUtils.setField(service, "questionnaireMapper", questionnaireMapper);
    }

    @Test
    void updateCollectingQuestionnaireCreatesNewDraftVersion()
    {
        SurveyQuestionnaire collecting = questionnaire(9L, "3", 2, null);
        SurveyQuestionnaire draftResult = questionnaire(101L, "0", 3, 9L);
        draftResult.setDescription("新版说明");
        when(questionnaireMapper.selectQuestionnaireById(9L)).thenReturn(collecting);
        when(questionnaireMapper.selectMaxVersionNo(9L)).thenReturn(2);
        doAnswer(invocation -> {
            SurveyQuestionnaire inserted = invocation.getArgument(0);
            inserted.setQuestionnaireId(101L);
            return 1;
        }).when(questionnaireMapper).insertQuestionnaire(any(SurveyQuestionnaire.class));
        when(questionnaireMapper.selectQuestionnaireById(101L)).thenReturn(draftResult);
        when(questionnaireMapper.selectQuestionsByQuestionnaireId(any(Long.class))).thenReturn(Collections.emptyList());

        SurveyQuestionnaire input = questionnaire(9L, "3", 2, null);
        input.setDescription("新版说明");
        SurveyQuestionnaire result = service.updateQuestionnaire(input);

        assertEquals(101L, result.getQuestionnaireId());
        assertEquals("0", result.getStatus());
        assertEquals(Integer.valueOf(3), result.getVersionNo());
        assertEquals(9L, result.getSourceQuestionnaireId());

        ArgumentCaptor<SurveyQuestionnaire> captor = ArgumentCaptor.forClass(SurveyQuestionnaire.class);
        verify(questionnaireMapper).insertQuestionnaire(captor.capture());
        assertEquals("0", captor.getValue().getStatus());
        assertEquals(Integer.valueOf(3), captor.getValue().getVersionNo());
        assertEquals(9L, captor.getValue().getSourceQuestionnaireId());
    }

    @Test
    void endQuestionnaireAcceptsCollectingStatus()
    {
        when(questionnaireMapper.selectQuestionnaireById(9L)).thenReturn(questionnaire(9L, "3", 2, null));
        when(questionnaireMapper.updateQuestionnaireStatus(any(SurveyQuestionnaire.class))).thenReturn(1);

        int rows = service.endQuestionnaire(9L, "admin");

        assertEquals(1, rows);
        ArgumentCaptor<SurveyQuestionnaire> captor = ArgumentCaptor.forClass(SurveyQuestionnaire.class);
        verify(questionnaireMapper).updateQuestionnaireStatus(captor.capture());
        assertEquals("2", captor.getValue().getStatus());
        assertEquals("admin", captor.getValue().getUpdateBy());
    }

    @Test
    void insertQuestionnaireRejectsLikertQuestionType()
    {
        SurveyQuestionnaire questionnaire = questionnaire(9L, "0", 1, null);
        SurveyQuestion question = new SurveyQuestion();
        question.setQuestionTitle("满意度");
        question.setQuestionType("likert");
        question.setRequiredFlag("1");
        questionnaire.setQuestions(Collections.singletonList(question));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertQuestionnaire(questionnaire));

        assertEquals("不支持的题型：likert", exception.getMessage());
    }

    @Test
    void insertQuestionnaireClearsDimensionForNonMatrixQuestions()
    {
        when(questionnaireMapper.insertQuestionnaire(any(SurveyQuestionnaire.class))).thenAnswer(invocation -> {
            SurveyQuestionnaire questionnaire = invocation.getArgument(0);
            questionnaire.setQuestionnaireId(9L);
            return 1;
        });
        when(questionnaireMapper.insertQuestion(any(SurveyQuestion.class))).thenAnswer(invocation -> {
            SurveyQuestion question = invocation.getArgument(0);
            question.setQuestionId(question.getOrderNum().longValue());
            return 1;
        });

        SurveyQuestionnaire questionnaire = questionnaire(9L, "0", 1, null);
        SurveyQuestion single = new SurveyQuestion();
        single.setQuestionTitle("单选题");
        single.setQuestionType("single");
        single.setRequiredFlag("1");
        single.setDimension("旧维度");
        SurveyQuestion matrix = new SurveyQuestion();
        matrix.setQuestionTitle("矩阵评分题");
        matrix.setQuestionType("matrix_score");
        matrix.setRequiredFlag("1");
        matrix.setDimension("保留维度");
        matrix.setScoreMax(5);
        questionnaire.setQuestions(Arrays.asList(single, matrix));

        service.insertQuestionnaire(questionnaire);

        ArgumentCaptor<SurveyQuestion> captor = ArgumentCaptor.forClass(SurveyQuestion.class);
        verify(questionnaireMapper, times(2)).insertQuestion(captor.capture());
        assertEquals(null, captor.getAllValues().get(0).getDimension());
        assertEquals("保留维度", captor.getAllValues().get(1).getDimension());
    }

    private SurveyQuestionnaire questionnaire(Long id, String status, Integer versionNo, Long sourceId)
    {
        SurveyQuestionnaire questionnaire = new SurveyQuestionnaire();
        questionnaire.setQuestionnaireId(id);
        questionnaire.setQuestionnaireName("营商环境满意度问卷");
        questionnaire.setStatus(status);
        questionnaire.setVersionNo(versionNo);
        questionnaire.setSourceQuestionnaireId(sourceId);
        questionnaire.setQuestions(Collections.emptyList());
        return questionnaire;
    }
}
