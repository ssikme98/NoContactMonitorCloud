package com.ruoyi.nocontact.survey.mapper;

import com.ruoyi.nocontact.survey.domain.SurveyResponse;
import com.ruoyi.nocontact.survey.domain.SurveyResponseAnswer;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSample;
import com.ruoyi.nocontact.survey.domain.vo.SurveyResponseAnswerRow;
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

    public int updateSendRecordSubmitted(@Param("sampleId") Long sampleId, @Param("recoveryTime") java.util.Date recoveryTime);
}
