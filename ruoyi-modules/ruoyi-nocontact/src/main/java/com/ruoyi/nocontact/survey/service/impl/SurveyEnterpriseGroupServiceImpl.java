package com.ruoyi.nocontact.survey.service.impl;

import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.survey.domain.SurveyEnterpriseGroup;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTreeSelect;
import com.ruoyi.nocontact.survey.mapper.SurveyEnterpriseGroupMapper;
import com.ruoyi.nocontact.survey.service.ISurveyEnterpriseGroupService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 企业分组Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SurveyEnterpriseGroupServiceImpl implements ISurveyEnterpriseGroupService
{
    @Autowired
    private SurveyEnterpriseGroupMapper groupMapper;

    @Override
    public List<SurveyEnterpriseGroup> selectGroupList(SurveyEnterpriseGroup group)
    {
        return groupMapper.selectGroupList(group);
    }

    @Override
    public List<SurveyTreeSelect> selectGroupTreeList(SurveyEnterpriseGroup group)
    {
        List<SurveyEnterpriseGroup> groups = groupMapper.selectGroupList(group);
        return buildGroupTreeSelect(groups);
    }

    @Override
    public SurveyEnterpriseGroup selectGroupById(Long groupId)
    {
        return groupMapper.selectGroupById(groupId);
    }

    @Override
    public boolean checkGroupNameUnique(SurveyEnterpriseGroup group)
    {
        Long groupId = group.getGroupId() == null ? -1L : group.getGroupId();
        SurveyEnterpriseGroup info = groupMapper.checkGroupNameUnique(group.getGroupName(), group.getParentId());
        return info == null || info.getGroupId().longValue() == groupId.longValue();
    }

    @Override
    public boolean hasChildByGroupId(Long groupId)
    {
        return groupMapper.hasChildByGroupId(groupId) > 0;
    }

    @Override
    public boolean checkGroupExistEnterprise(Long groupId)
    {
        return groupMapper.checkGroupExistEnterprise(groupId) > 0;
    }

    @Override
    public int insertGroup(SurveyEnterpriseGroup group)
    {
        if (group.getParentId() == null)
        {
            group.setParentId(0L);
        }
        if (group.getOrderNum() == null)
        {
            group.setOrderNum(0);
        }
        if (StringUtils.isEmpty(group.getStatus()))
        {
            group.setStatus(UserConstants.NORMAL);
        }
        SurveyEnterpriseGroup parent = group.getParentId() == 0L ? null : groupMapper.selectGroupById(group.getParentId());
        group.setAncestors(parent == null ? "0" : parent.getAncestors() + "," + group.getParentId());
        group.setCreateTime(DateUtils.getNowDate());
        return groupMapper.insertGroup(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateGroup(SurveyEnterpriseGroup group)
    {
        SurveyEnterpriseGroup newParent = group.getParentId() == null || group.getParentId() == 0L ? null : groupMapper.selectGroupById(group.getParentId());
        SurveyEnterpriseGroup oldGroup = groupMapper.selectGroupById(group.getGroupId());
        if (StringUtils.isNotNull(oldGroup))
        {
            String newAncestors = newParent == null ? "0" : newParent.getAncestors() + "," + newParent.getGroupId();
            String oldAncestors = oldGroup.getAncestors();
            group.setAncestors(newAncestors);
            updateGroupChildren(group.getGroupId(), newAncestors, oldAncestors);
        }
        group.setUpdateTime(DateUtils.getNowDate());
        return groupMapper.updateGroup(group);
    }

    @Override
    public int deleteGroupById(Long groupId)
    {
        if (hasChildByGroupId(groupId))
        {
            throw new ServiceException("存在下级分组，不允许删除");
        }
        if (checkGroupExistEnterprise(groupId))
        {
            throw new ServiceException("分组下存在企业，不允许删除");
        }
        return groupMapper.deleteGroupById(groupId);
    }

    private List<SurveyTreeSelect> buildGroupTreeSelect(List<SurveyEnterpriseGroup> groups)
    {
        List<SurveyEnterpriseGroup> groupTrees = buildGroupTree(groups);
        return groupTrees.stream().map(SurveyTreeSelect::new).collect(Collectors.toList());
    }

    private List<SurveyEnterpriseGroup> buildGroupTree(List<SurveyEnterpriseGroup> groups)
    {
        List<SurveyEnterpriseGroup> returnList = new ArrayList<SurveyEnterpriseGroup>();
        List<Long> tempList = groups.stream().map(SurveyEnterpriseGroup::getGroupId).collect(Collectors.toList());
        for (SurveyEnterpriseGroup group : groups)
        {
            if (!tempList.contains(group.getParentId()))
            {
                recursionFn(groups, group);
                returnList.add(group);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = groups;
        }
        return returnList;
    }

    private void recursionFn(List<SurveyEnterpriseGroup> list, SurveyEnterpriseGroup node)
    {
        List<SurveyEnterpriseGroup> childList = getChildList(list, node);
        node.setChildren(childList);
        for (SurveyEnterpriseGroup child : childList)
        {
            if (hasChild(list, child))
            {
                recursionFn(list, child);
            }
        }
    }

    private List<SurveyEnterpriseGroup> getChildList(List<SurveyEnterpriseGroup> list, SurveyEnterpriseGroup node)
    {
        List<SurveyEnterpriseGroup> childList = new ArrayList<SurveyEnterpriseGroup>();
        Iterator<SurveyEnterpriseGroup> it = list.iterator();
        while (it.hasNext())
        {
            SurveyEnterpriseGroup group = it.next();
            if (StringUtils.isNotNull(group.getParentId()) && group.getParentId().longValue() == node.getGroupId().longValue())
            {
                childList.add(group);
            }
        }
        return childList;
    }

    private boolean hasChild(List<SurveyEnterpriseGroup> list, SurveyEnterpriseGroup node)
    {
        return getChildList(list, node).size() > 0;
    }

    private void updateGroupChildren(Long groupId, String newAncestors, String oldAncestors)
    {
        List<SurveyEnterpriseGroup> children = groupMapper.selectChildrenGroupById(groupId);
        for (SurveyEnterpriseGroup child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (!children.isEmpty())
        {
            groupMapper.updateGroupChildren(children);
        }
    }
}
