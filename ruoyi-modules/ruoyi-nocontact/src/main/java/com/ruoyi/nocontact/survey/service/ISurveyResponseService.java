package com.ruoyi.nocontact.survey.service;

import com.ruoyi.nocontact.survey.domain.SurveyResponse;
import com.ruoyi.nocontact.survey.domain.vo.SurveyPublicFill;

/**
 * 问卷答卷Service接口
 *
 * @author ruoyi
 */
public interface ISurveyResponseService
{
    public SurveyPublicFill getFill(String token);

    public int submitResponse(String token, SurveyResponse response, String clientIp);
}
