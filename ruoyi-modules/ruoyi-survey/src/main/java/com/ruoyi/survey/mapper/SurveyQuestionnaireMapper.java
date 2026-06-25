package com.ruoyi.survey.mapper;

import com.ruoyi.survey.domain.SurveyQuestion;
import com.ruoyi.survey.domain.SurveyQuestionOption;
import com.ruoyi.survey.domain.SurveyQuestionnaire;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 问卷Mapper接口
 *
 * @author ruoyi
 */
public interface SurveyQuestionnaireMapper
{
    public List<SurveyQuestionnaire> selectQuestionnaireList(SurveyQuestionnaire questionnaire);

    public SurveyQuestionnaire selectQuestionnaireById(Long questionnaireId);

    public List<SurveyQuestion> selectQuestionsByQuestionnaireId(Long questionnaireId);

    public List<SurveyQuestionOption> selectOptionsByQuestionnaireId(Long questionnaireId);

    public Integer selectMaxVersionNo(Long rootQuestionnaireId);

    public int insertQuestionnaire(SurveyQuestionnaire questionnaire);

    public int updateQuestionnaire(SurveyQuestionnaire questionnaire);

    public int updateQuestionnaireStatus(SurveyQuestionnaire questionnaire);

    public int deleteQuestionnaireByIds(Long[] questionnaireIds);

    public int insertQuestion(SurveyQuestion question);

    public int batchQuestionOption(@Param("list") List<SurveyQuestionOption> options);

    public int deleteQuestionsByQuestionnaireId(Long questionnaireId);

    public int deleteOptionsByQuestionnaireId(Long questionnaireId);
}
