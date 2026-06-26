package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import com.ruoyi.nocontact.fusion.mapper.FusionIndicatorMapper;
import com.ruoyi.nocontact.fusion.service.IFusionIndicatorService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 营商监测指标Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class FusionIndicatorServiceImpl implements IFusionIndicatorService
{
    @Autowired
    private FusionIndicatorMapper indicatorMapper;

    @Override
    public List<FusionIndicator> selectIndicatorList(FusionIndicator indicator)
    {
        return indicatorMapper.selectIndicatorList(indicator);
    }

    @Override
    public FusionIndicator selectIndicatorById(Long indicatorId)
    {
        return indicatorMapper.selectIndicatorById(indicatorId);
    }

    @Override
    public int insertIndicator(FusionIndicator indicator)
    {
        indicator.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(indicator.getStatus()))
        {
            indicator.setStatus("0");
        }
        return indicatorMapper.insertIndicator(indicator);
    }

    @Override
    public int updateIndicator(FusionIndicator indicator)
    {
        indicator.setUpdateTime(DateUtils.getNowDate());
        return indicatorMapper.updateIndicator(indicator);
    }

    @Override
    public int deleteIndicatorByIds(Long[] indicatorIds)
    {
        return indicatorMapper.deleteIndicatorByIds(indicatorIds);
    }
}
