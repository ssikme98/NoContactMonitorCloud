package com.ruoyi.survey.service.impl;

import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.bean.BeanValidators;
import com.ruoyi.survey.domain.SurveyEnterprise;
import com.ruoyi.survey.domain.SurveyEnterpriseGroupRel;
import com.ruoyi.survey.mapper.SurveyEnterpriseMapper;
import com.ruoyi.survey.service.ISurveyEnterpriseService;
import java.util.ArrayList;
import java.util.List;
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
}
