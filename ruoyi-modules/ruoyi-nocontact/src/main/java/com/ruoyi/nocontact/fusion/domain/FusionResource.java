package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import javax.validation.constraints.NotBlank;

/**
 * 数据资源目录对象 nc_fusion_resource_directory
 */
public class FusionResource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long resourceId;
    private Long parentId;
    private String resourceName;
    private String resourceType;
    private String sourceSystem;
    private String updateCycle;
    private String ownerName;
    private Long dataCount;
    private String tags;
    private String permissionScope;
    private String status;

    public Long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(Long resourceId)
    {
        this.resourceId = resourceId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    @NotBlank(message = "资源名称不能为空")
    public String getResourceName()
    {
        return resourceName;
    }

    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
    }

    public String getResourceType()
    {
        return resourceType;
    }

    public void setResourceType(String resourceType)
    {
        this.resourceType = resourceType;
    }

    public String getSourceSystem()
    {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem)
    {
        this.sourceSystem = sourceSystem;
    }

    public String getUpdateCycle()
    {
        return updateCycle;
    }

    public void setUpdateCycle(String updateCycle)
    {
        this.updateCycle = updateCycle;
    }

    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName(String ownerName)
    {
        this.ownerName = ownerName;
    }

    public Long getDataCount()
    {
        return dataCount;
    }

    public void setDataCount(Long dataCount)
    {
        this.dataCount = dataCount;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public String getPermissionScope()
    {
        return permissionScope;
    }

    public void setPermissionScope(String permissionScope)
    {
        this.permissionScope = permissionScope;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
