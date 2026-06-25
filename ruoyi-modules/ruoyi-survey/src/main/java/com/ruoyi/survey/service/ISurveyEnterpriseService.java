package com.ruoyi.survey.service;

import com.ruoyi.survey.domain.SurveyEnterprise;
import java.util.List;

/**
 * 企业库Service接口
 *
 * @author ruoyi
 */
public interface ISurveyEnterpriseService
{
    public List<SurveyEnterprise> selectEnterpriseList(SurveyEnterprise enterprise);

    public SurveyEnterprise selectEnterpriseById(Long enterpriseId);

    public boolean checkCreditCodeUnique(SurveyEnterprise enterprise);

    public int insertEnterprise(SurveyEnterprise enterprise);

    public int updateEnterprise(SurveyEnterprise enterprise);

    public int deleteEnterpriseByIds(Long[] enterpriseIds);

    public String importEnterprise(List<SurveyEnterprise> enterpriseList, Boolean updateSupport, String operName);
}
