package com.ruoyi.survey.domain;

import java.util.Date;

/**
 * 企业与分组关联对象 survey_enterprise_group_rel
 *
 * @author ruoyi
 */
public class SurveyEnterpriseGroupRel
{
    private Long enterpriseId;

    private Long groupId;

    private String createBy;

    private Date createTime;

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
