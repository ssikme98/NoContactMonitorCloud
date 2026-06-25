package com.ruoyi.survey.domain;

import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 企业库对象 survey_enterprise
 *
 * @author ruoyi
 */
public class SurveyEnterprise extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 企业ID */
    private Long enterpriseId;

    /** 企业名称 */
    @Excel(name = "企业名称")
    private String enterpriseName;

    /** 统一社会信用代码 */
    @Excel(name = "统一社会信用代码")
    private String creditCode;

    /** 行业分类 */
    @Excel(name = "行业分类")
    private String industryCategory;

    /** 地区编码 */
    private String regionCode;

    /** 地区名称 */
    @Excel(name = "地区")
    private String regionName;

    /** 企业规模 */
    @Excel(name = "企业规模")
    private String enterpriseScale;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactName;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 查询分组ID */
    private Long groupId;

    /** 所属分组ID数组 */
    private Long[] groupIds;

    public Long getEnterpriseId()
    {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId)
    {
        this.enterpriseId = enterpriseId;
    }

    @NotBlank(message = "企业名称不能为空")
    @Size(max = 120, message = "企业名称长度不能超过120个字符")
    public String getEnterpriseName()
    {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName)
    {
        this.enterpriseName = enterpriseName;
    }

    @NotBlank(message = "统一社会信用代码不能为空")
    @Pattern(regexp = "^[0-9A-Z]{18}$", message = "统一社会信用代码必须为18位数字或大写字母")
    public String getCreditCode()
    {
        return creditCode;
    }

    public void setCreditCode(String creditCode)
    {
        this.creditCode = creditCode;
    }

    public String getIndustryCategory()
    {
        return industryCategory;
    }

    public void setIndustryCategory(String industryCategory)
    {
        this.industryCategory = industryCategory;
    }

    public String getRegionCode()
    {
        return regionCode;
    }

    public void setRegionCode(String regionCode)
    {
        this.regionCode = regionCode;
    }

    public String getRegionName()
    {
        return regionName;
    }

    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    public String getEnterpriseScale()
    {
        return enterpriseScale;
    }

    public void setEnterpriseScale(String enterpriseScale)
    {
        this.enterpriseScale = enterpriseScale;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long[] getGroupIds()
    {
        return groupIds;
    }

    public void setGroupIds(Long[] groupIds)
    {
        this.groupIds = groupIds;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("enterpriseId", getEnterpriseId())
                .append("enterpriseName", getEnterpriseName())
                .append("creditCode", getCreditCode())
                .append("industryCategory", getIndustryCategory())
                .append("regionCode", getRegionCode())
                .append("regionName", getRegionName())
                .append("enterpriseScale", getEnterpriseScale())
                .append("contactName", getContactName())
                .append("contactPhone", getContactPhone())
                .append("status", getStatus())
                .append("groupId", getGroupId())
                .append("groupIds", getGroupIds())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
