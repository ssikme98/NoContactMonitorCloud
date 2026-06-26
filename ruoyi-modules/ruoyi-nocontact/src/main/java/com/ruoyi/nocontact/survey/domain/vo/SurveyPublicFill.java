package com.ruoyi.nocontact.survey.domain.vo;

import com.ruoyi.nocontact.survey.domain.SurveyQuestionnaire;
import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSample;

/**
 * 公开填报页数据
 *
 * @author ruoyi
 */
public class SurveyPublicFill
{
    /** token是否有效 */
    private Boolean valid;

    /** 是否已提交 */
    private Boolean submitted;

    /** 状态消息 */
    private String message;

    /** 任务信息 */
    private SurveyTask task;

    /** 样本信息 */
    private SurveyTaskSample sample;

    /** 问卷信息 */
    private SurveyQuestionnaire questionnaire;

    public Boolean getValid()
    {
        return valid;
    }

    public void setValid(Boolean valid)
    {
        this.valid = valid;
    }

    public Boolean getSubmitted()
    {
        return submitted;
    }

    public void setSubmitted(Boolean submitted)
    {
        this.submitted = submitted;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public SurveyTask getTask()
    {
        return task;
    }

    public void setTask(SurveyTask task)
    {
        this.task = task;
    }

    public SurveyTaskSample getSample()
    {
        return sample;
    }

    public void setSample(SurveyTaskSample sample)
    {
        this.sample = sample;
    }

    public SurveyQuestionnaire getQuestionnaire()
    {
        return questionnaire;
    }

    public void setQuestionnaire(SurveyQuestionnaire questionnaire)
    {
        this.questionnaire = questionnaire;
    }
}
