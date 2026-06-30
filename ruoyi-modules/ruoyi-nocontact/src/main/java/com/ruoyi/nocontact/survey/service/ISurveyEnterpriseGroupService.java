package com.ruoyi.nocontact.survey.service;

import com.ruoyi.nocontact.survey.domain.SurveyEnterpriseGroup;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTreeSelect;
import java.util.List;

/**
 * 企业分组Service接口
 *
 * @author ruoyi
 */
public interface ISurveyEnterpriseGroupService
{
    public List<SurveyEnterpriseGroup> selectGroupList(SurveyEnterpriseGroup group);

    public List<SurveyTreeSelect> selectGroupTreeList(SurveyEnterpriseGroup group);

    public SurveyEnterpriseGroup selectGroupById(Long groupId);

    public boolean checkGroupNameUnique(SurveyEnterpriseGroup group);

    public boolean hasChildByGroupId(Long groupId);

    public boolean checkGroupExistEnterprise(Long groupId);

    public int insertGroup(SurveyEnterpriseGroup group);

    public int updateGroup(SurveyEnterpriseGroup group);

    public int deleteGroupById(Long groupId);
}
