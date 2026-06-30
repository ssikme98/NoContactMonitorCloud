package com.ruoyi.nocontact.survey.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskTrackingDetail;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurveyDomainJsonFormatTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SurveyDomainJsonFormatTest()
    {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        objectMapper.setTimeZone(timeZone);
        formatter.setTimeZone(timeZone);
    }

    @Test
    void questionnaireDatesUseFrontendDateTimeFormat() throws Exception
    {
        SurveyQuestionnaire questionnaire = objectMapper.readValue(
                "{\"questionnaireName\":\"E2E问卷\",\"publishedTime\":\"2026-06-29 19:44:23\",\"endedTime\":\"2026-07-01 08:30:00\"}",
                SurveyQuestionnaire.class);

        assertEquals("2026-06-29 19:44:23", formatter.format(questionnaire.getPublishedTime()));
        assertEquals("2026-07-01 08:30:00", formatter.format(questionnaire.getEndedTime()));
    }

    @Test
    void taskDispatchTimeSerializesWithoutTimezoneShift() throws Exception
    {
        SurveyTask task = new SurveyTask();
        Date dispatchTime = formatter.parse("2026-06-29 19:45:28");
        task.setDispatchTime(dispatchTime);

        String json = objectMapper.writeValueAsString(task);

        assertTrue(json.contains("\"dispatchTime\":\"2026-06-29 19:45:28\""));
    }

    @Test
    void taskDetailDatesSerializeWithoutTimezoneShift() throws Exception
    {
        Date eventTime = formatter.parse("2026-06-29 19:57:02");

        SurveyTaskSample sample = new SurveyTaskSample();
        sample.setCreateTime(eventTime);
        sample.setTokenExpireTime(eventTime);

        SurveyTaskSendRecord sendRecord = new SurveyTaskSendRecord();
        sendRecord.setCreateTime(eventTime);
        sendRecord.setRecoveryTime(eventTime);

        SurveyTaskTrackingDetail detail = new SurveyTaskTrackingDetail();
        detail.setSendTime(eventTime);
        detail.setSubmitTime(eventTime);
        detail.setTokenExpireTime(eventTime);

        assertTrue(objectMapper.writeValueAsString(sample).contains("\"createTime\":\"2026-06-29 19:57:02\""));
        assertTrue(objectMapper.writeValueAsString(sample).contains("\"tokenExpireTime\":\"2026-06-29 19:57:02\""));
        assertTrue(objectMapper.writeValueAsString(sendRecord).contains("\"createTime\":\"2026-06-29 19:57:02\""));
        assertTrue(objectMapper.writeValueAsString(sendRecord).contains("\"recoveryTime\":\"2026-06-29 19:57:02\""));
        assertTrue(objectMapper.writeValueAsString(detail).contains("\"sendTime\":\"2026-06-29 19:57:02\""));
        assertTrue(objectMapper.writeValueAsString(detail).contains("\"submitTime\":\"2026-06-29 19:57:02\""));
        assertTrue(objectMapper.writeValueAsString(detail).contains("\"tokenExpireTime\":\"2026-06-29 19:57:02\""));
    }
}
