package com.ruoyi.survey.mapper;

import com.ruoyi.survey.domain.SurveyResponse;
import com.ruoyi.survey.domain.SurveyResponseAnswer;
import com.ruoyi.survey.domain.SurveyTaskSample;
import com.ruoyi.survey.domain.vo.SurveyResponseAnswerRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 问卷答卷Mapper接口
 *
 * @author ruoyi
 */
public interface SurveyResponseMapper
{
    public SurveyTaskSample selectSampleByToken(String token);

    public SurveyResponse selectResponseBySampleId(Long sampleId);

    public List<SurveyResponseAnswerRow> selectAnswerRowsByTaskId(Long taskId);

    public int insertResponse(SurveyResponse response);

    public int batchResponseAnswer(@Param("list") List<SurveyResponseAnswer> answers);

    public int updateSampleCompleted(Long sampleId);
}
