package com.ruoyi.survey.controller;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.survey.domain.SurveyEnterpriseGroup;
import com.ruoyi.survey.service.ISurveyEnterpriseGroupService;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业分组Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/enterprise/group")
public class SurveyEnterpriseGroupController extends BaseController
{
    @Autowired
    private ISurveyEnterpriseGroupService groupService;

    @RequiresPermissions("survey:enterprise:group:list")
    @GetMapping("/list")
    public AjaxResult list(SurveyEnterpriseGroup group)
    {
        List<SurveyEnterpriseGroup> groups = groupService.selectGroupList(group);
        return success(groups);
    }

    @RequiresPermissions("survey:enterprise:group:list")
    @GetMapping("/treeselect")
    public AjaxResult treeselect(SurveyEnterpriseGroup group)
    {
        return success(groupService.selectGroupTreeList(group));
    }

    @RequiresPermissions("survey:enterprise:group:list")
    @GetMapping("/list/exclude/{groupId}")
    public AjaxResult excludeChild(@PathVariable Long groupId)
    {
        List<SurveyEnterpriseGroup> groups = groupService.selectGroupList(new SurveyEnterpriseGroup());
        groups.removeIf(g -> g.getGroupId().longValue() == groupId.longValue() || ArrayUtils.contains(g.getAncestors().split(","), groupId + ""));
        return success(groups);
    }

    @RequiresPermissions("survey:enterprise:group:query")
    @GetMapping(value = "/{groupId}")
    public AjaxResult getInfo(@PathVariable Long groupId)
    {
        return success(groupService.selectGroupById(groupId));
    }

    @RequiresPermissions("survey:enterprise:group:add")
    @Log(title = "企业分组", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SurveyEnterpriseGroup group)
    {
        if (!groupService.checkGroupNameUnique(group))
        {
            return error("新增分组'" + group.getGroupName() + "'失败，分组名称已存在");
        }
        group.setCreateBy(SecurityUtils.getUsername());
        return toAjax(groupService.insertGroup(group));
    }

    @RequiresPermissions("survey:enterprise:group:edit")
    @Log(title = "企业分组", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SurveyEnterpriseGroup group)
    {
        if (!groupService.checkGroupNameUnique(group))
        {
            return error("修改分组'" + group.getGroupName() + "'失败，分组名称已存在");
        }
        else if (group.getParentId().longValue() == group.getGroupId().longValue())
        {
            return error("修改分组'" + group.getGroupName() + "'失败，上级分组不能是自己");
        }
        group.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(groupService.updateGroup(group));
    }

    @RequiresPermissions("survey:enterprise:group:remove")
    @Log(title = "企业分组", businessType = BusinessType.DELETE)
    @DeleteMapping("/{groupId}")
    public AjaxResult remove(@PathVariable Long groupId)
    {
        return toAjax(groupService.deleteGroupById(groupId));
    }
}
