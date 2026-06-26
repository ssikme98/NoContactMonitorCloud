package com.ruoyi.nocontact.survey.mapper;

import com.ruoyi.nocontact.survey.domain.SurveyEnterprise;
import com.ruoyi.nocontact.survey.domain.SurveyEnterpriseGroupRel;
import java.util.List;

/**
 * 企业库Mapper接口
 *
 * @author ruoyi
 */
public interface SurveyEnterpriseMapper
{
    public List<SurveyEnterprise> selectEnterpriseList(SurveyEnterprise enterprise);

    public SurveyEnterprise selectEnterpriseById(Long enterpriseId);

    public SurveyEnterprise selectEnterpriseByCreditCode(String creditCode);

    public Long[] selectGroupIdsByEnterpriseId(Long enterpriseId);

    public int insertEnterprise(SurveyEnterprise enterprise);

    public int updateEnterprise(SurveyEnterprise enterprise);

    public int deleteEnterpriseByIds(Long[] enterpriseIds);

    public int deleteEnterpriseGroupByEnterpriseId(Long enterpriseId);

    public int deleteEnterpriseGroupByEnterpriseIds(Long[] enterpriseIds);

    public int batchEnterpriseGroup(List<SurveyEnterpriseGroupRel> list);
}
