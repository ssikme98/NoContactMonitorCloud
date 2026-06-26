package com.ruoyi.nocontact.survey.service;

import com.ruoyi.nocontact.survey.domain.SurveyQuestionnaire;
import java.util.List;

/**
 * 问卷Service接口
 *
 * @author ruoyi
 */
public interface ISurveyQuestionnaireService
{
    public List<SurveyQuestionnaire> selectQuestionnaireList(SurveyQuestionnaire questionnaire);

    public SurveyQuestionnaire selectQuestionnaireById(Long questionnaireId);

    public int insertQuestionnaire(SurveyQuestionnaire questionnaire);

    public SurveyQuestionnaire updateQuestionnaire(SurveyQuestionnaire questionnaire);

    public int deleteQuestionnaireByIds(Long[] questionnaireIds);

    public SurveyQuestionnaire createDraftFromPublished(Long questionnaireId, String operName);

    public int publishQuestionnaire(Long questionnaireId, String operName);

    public int endQuestionnaire(Long questionnaireId, String operName);
}
