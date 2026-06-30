package com.ruoyi.nocontact.survey.domain.vo;

import java.io.Serializable;

/**
 * 题目答案分布项
 *
 * @author ruoyi
 */
public class SurveyDistributionItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String itemLabel;

    private String itemValue;

    private Long count;

    private Double percent;

    public String getItemLabel()
    {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel)
    {
        this.itemLabel = itemLabel;
    }

    public String getItemValue()
    {
        return itemValue;
    }

    public void setItemValue(String itemValue)
    {
        this.itemValue = itemValue;
    }

    public Long getCount()
    {
        return count;
    }

    public void setCount(Long count)
    {
        this.count = count;
    }

    public Double getPercent()
    {
        return percent;
    }

    public void setPercent(Double percent)
    {
        this.percent = percent;
    }
}
