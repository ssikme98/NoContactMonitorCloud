package com.ruoyi.nocontact.fusion.domain;

import com.ruoyi.common.core.web.domain.BaseEntity;
import javax.validation.constraints.NotBlank;

/**
 * 指标标签分类对象 nc_indicator_tag
 */
public class FusionIndicatorTag extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long tagId;
    private String tagName;
    private String tagColor;
    private String categoryName;
    private Integer orderNum;
    private Long relatedCount;
    private String status;

    public Long getTagId()
    {
        return tagId;
    }

    public void setTagId(Long tagId)
    {
        this.tagId = tagId;
    }

    @NotBlank(message = "标签名称不能为空")
    public String getTagName()
    {
        return tagName;
    }

    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }

    public String getTagColor()
    {
        return tagColor;
    }

    public void setTagColor(String tagColor)
    {
        this.tagColor = tagColor;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public Long getRelatedCount()
    {
        return relatedCount;
    }

    public void setRelatedCount(Long relatedCount)
    {
        this.relatedCount = relatedCount;
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
