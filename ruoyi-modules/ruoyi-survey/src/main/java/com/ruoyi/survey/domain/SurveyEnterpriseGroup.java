package com.ruoyi.survey.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 企业分组对象 survey_enterprise_group
 *
 * @author ruoyi
 */
public class SurveyEnterpriseGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分组ID */
    private Long groupId;

    /** 父分组ID */
    private Long parentId;

    /** 祖级列表 */
    private String ancestors;

    /** 分组名称 */
    private String groupName;

    /** 显示顺序 */
    private Integer orderNum;

    /** 状态（0正常 1停用） */
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 子分组 */
    private List<SurveyEnterpriseGroup> children;

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    @NotNull(message = "上级分组不能为空")
    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    @NotBlank(message = "分组名称不能为空")
    @Size(max = 80, message = "分组名称长度不能超过80个字符")
    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<SurveyEnterpriseGroup> getChildren()
    {
        return children;
    }

    public void setChildren(List<SurveyEnterpriseGroup> children)
    {
        this.children = children;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("groupId", getGroupId())
                .append("parentId", getParentId())
                .append("ancestors", getAncestors())
                .append("groupName", getGroupName())
                .append("orderNum", getOrderNum())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("children", getChildren())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
