package com.ruoyi.nocontact.survey.mapper;

import com.ruoyi.nocontact.survey.domain.SurveyEnterpriseGroup;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 企业分组Mapper接口
 *
 * @author ruoyi
 */
public interface SurveyEnterpriseGroupMapper
{
    public List<SurveyEnterpriseGroup> selectGroupList(SurveyEnterpriseGroup group);

    public SurveyEnterpriseGroup selectGroupById(Long groupId);

    public List<SurveyEnterpriseGroup> selectChildrenGroupById(Long groupId);

    public SurveyEnterpriseGroup checkGroupNameUnique(@Param("groupName") String groupName, @Param("parentId") Long parentId);

    public int hasChildByGroupId(Long groupId);

    public int checkGroupExistEnterprise(Long groupId);

    public int insertGroup(SurveyEnterpriseGroup group);

    public int updateGroup(SurveyEnterpriseGroup group);

    public int updateGroupChildren(@Param("groups") List<SurveyEnterpriseGroup> groups);

    public int deleteGroupById(Long groupId);
}
