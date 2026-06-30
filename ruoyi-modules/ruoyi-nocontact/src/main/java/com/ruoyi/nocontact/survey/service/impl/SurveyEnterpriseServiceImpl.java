package com.ruoyi.nocontact.survey.service.impl;

import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.bean.BeanValidators;
import com.ruoyi.nocontact.survey.domain.SurveyEnterprise;
import com.ruoyi.nocontact.survey.domain.SurveyEnterpriseGroupRel;
import com.ruoyi.nocontact.survey.mapper.SurveyEnterpriseMapper;
import com.ruoyi.nocontact.survey.service.ISurveyEnterpriseService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 企业库Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SurveyEnterpriseServiceImpl implements ISurveyEnterpriseService
{
    private static final Logger log = LoggerFactory.getLogger(SurveyEnterpriseServiceImpl.class);

    private static final Map<String, String> HUNAN_CITY_CODES = buildHunanCityCodes();

    @Autowired
    private SurveyEnterpriseMapper enterpriseMapper;

    @Autowired
    protected Validator validator;

    @Override
    public List<SurveyEnterprise> selectEnterpriseList(SurveyEnterprise enterprise)
    {
        return enterpriseMapper.selectEnterpriseList(enterprise);
    }

    @Override
    public SurveyEnterprise selectEnterpriseById(Long enterpriseId)
    {
        SurveyEnterprise enterprise = enterpriseMapper.selectEnterpriseById(enterpriseId);
        if (enterprise != null)
        {
            enterprise.setGroupIds(enterpriseMapper.selectGroupIdsByEnterpriseId(enterpriseId));
        }
        return enterprise;
    }

    @Override
    public boolean checkCreditCodeUnique(SurveyEnterprise enterprise)
    {
        Long enterpriseId = enterprise.getEnterpriseId() == null ? -1L : enterprise.getEnterpriseId();
        SurveyEnterprise info = enterpriseMapper.selectEnterpriseByCreditCode(enterprise.getCreditCode());
        return info == null || info.getEnterpriseId().longValue() == enterpriseId.longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertEnterprise(SurveyEnterprise enterprise)
    {
        normalizeCity(enterprise);
        enterprise.setCreateTime(DateUtils.getNowDate());
        if (enterprise.getStatus() == null || enterprise.getStatus().length() == 0)
        {
            enterprise.setStatus(UserConstants.NORMAL);
        }
        int rows = enterpriseMapper.insertEnterprise(enterprise);
        insertEnterpriseGroupRel(enterprise);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnterprise(SurveyEnterprise enterprise)
    {
        normalizeCity(enterprise);
        enterprise.setUpdateTime(DateUtils.getNowDate());
        enterpriseMapper.deleteEnterpriseGroupByEnterpriseId(enterprise.getEnterpriseId());
        insertEnterpriseGroupRel(enterprise);
        return enterpriseMapper.updateEnterprise(enterprise);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEnterpriseByIds(Long[] enterpriseIds)
    {
        enterpriseMapper.deleteEnterpriseGroupByEnterpriseIds(enterpriseIds);
        return enterpriseMapper.deleteEnterpriseByIds(enterpriseIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importEnterprise(List<SurveyEnterprise> enterpriseList, Boolean updateSupport, String operName)
    {
        if (StringUtils.isNull(enterpriseList) || enterpriseList.size() == 0)
        {
            throw new ServiceException("导入企业数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (int i = 0; i < enterpriseList.size(); i++)
        {
            SurveyEnterprise enterprise = enterpriseList.get(i);
            String rowName = StringUtils.defaultIfBlank(enterprise.getEnterpriseName(), "第" + (i + 2) + "行");
            try
            {
                BeanValidators.validateWithException(validator, enterprise);
                normalizeCity(enterprise);
                SurveyEnterprise exists = enterpriseMapper.selectEnterpriseByCreditCode(enterprise.getCreditCode());
                if (StringUtils.isNull(exists))
                {
                    enterprise.setCreateBy(operName);
                    insertEnterprise(enterprise);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、企业 " + rowName + " 导入成功");
                }
                else if (updateSupport)
                {
                    enterprise.setEnterpriseId(exists.getEnterpriseId());
                    enterprise.setUpdateBy(operName);
                    updateEnterprise(enterprise);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、企业 " + rowName + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、企业 " + rowName + " 统一社会信用代码已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、企业 " + rowName + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
                log.error(msg, e);
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        return successMsg.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnterpriseLocation(SurveyEnterprise enterprise, String operName)
    {
        normalizeCity(enterprise);
        enterprise.setUpdateBy(operName);
        enterprise.setUpdateTime(DateUtils.getNowDate());
        return enterpriseMapper.updateEnterpriseLocation(enterprise);
    }

    private void insertEnterpriseGroupRel(SurveyEnterprise enterprise)
    {
        Long[] groupIds = enterprise.getGroupIds();
        if (StringUtils.isEmpty(groupIds))
        {
            return;
        }
        List<SurveyEnterpriseGroupRel> list = new ArrayList<SurveyEnterpriseGroupRel>();
        for (Long groupId : groupIds)
        {
            if (groupId == null)
            {
                continue;
            }
            SurveyEnterpriseGroupRel rel = new SurveyEnterpriseGroupRel();
            rel.setEnterpriseId(enterprise.getEnterpriseId());
            rel.setGroupId(groupId);
            rel.setCreateBy(StringUtils.defaultIfBlank(enterprise.getUpdateBy(), enterprise.getCreateBy()));
            rel.setCreateTime(DateUtils.getNowDate());
            list.add(rel);
        }
        if (!list.isEmpty())
        {
            enterpriseMapper.batchEnterpriseGroup(list);
        }
    }

    private void normalizeCity(SurveyEnterprise enterprise)
    {
        if (enterprise == null)
        {
            return;
        }
        String cityName = StringUtils.trim(enterprise.getRegionName());
        enterprise.setRegionName(cityName);
        if (StringUtils.isBlank(cityName))
        {
            enterprise.setRegionCode(null);
            return;
        }
        String cityCode = HUNAN_CITY_CODES.get(cityName);
        if (cityCode == null)
        {
            throw new ServiceException("城市必须为湖南省内城市：" + cityName);
        }
        enterprise.setRegionCode(cityCode);
    }

    private static Map<String, String> buildHunanCityCodes()
    {
        Map<String, String> cities = new HashMap<String, String>();
        cities.put("长沙市", "430100");
        cities.put("株洲市", "430200");
        cities.put("湘潭市", "430300");
        cities.put("衡阳市", "430400");
        cities.put("邵阳市", "430500");
        cities.put("岳阳市", "430600");
        cities.put("常德市", "430700");
        cities.put("张家界市", "430800");
        cities.put("益阳市", "430900");
        cities.put("郴州市", "431000");
        cities.put("永州市", "431100");
        cities.put("怀化市", "431200");
        cities.put("娄底市", "431300");
        cities.put("湘西土家族苗族自治州", "433100");
        return Collections.unmodifiableMap(cities);
    }
}
