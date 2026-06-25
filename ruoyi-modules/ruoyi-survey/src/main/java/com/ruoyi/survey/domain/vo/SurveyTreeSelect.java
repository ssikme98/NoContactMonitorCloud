package com.ruoyi.survey.domain.vo;

import com.ruoyi.survey.domain.SurveyEnterpriseGroup;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 问卷模块树节点
 *
 * @author ruoyi
 */
public class SurveyTreeSelect implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private String label;

    private Boolean disabled;

    private List<SurveyTreeSelect> children;

    public SurveyTreeSelect()
    {
    }

    public SurveyTreeSelect(SurveyEnterpriseGroup group)
    {
        this.id = group.getGroupId();
        this.label = group.getGroupName();
        this.disabled = "1".equals(group.getStatus());
        if (group.getChildren() != null)
        {
            this.children = group.getChildren().stream().map(SurveyTreeSelect::new).collect(Collectors.toList());
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Boolean getDisabled()
    {
        return disabled;
    }

    public void setDisabled(Boolean disabled)
    {
        this.disabled = disabled;
    }

    public List<SurveyTreeSelect> getChildren()
    {
        return children;
    }

    public void setChildren(List<SurveyTreeSelect> children)
    {
        this.children = children;
    }
}
