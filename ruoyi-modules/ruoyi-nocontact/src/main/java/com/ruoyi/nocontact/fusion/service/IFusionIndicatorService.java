package com.ruoyi.nocontact.fusion.service;

import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import java.util.List;

/**
 * 营商监测指标Service接口
 *
 * @author ruoyi
 */
public interface IFusionIndicatorService
{
    public List<FusionIndicator> selectIndicatorList(FusionIndicator indicator);

    public FusionIndicator selectIndicatorById(Long indicatorId);

    public int insertIndicator(FusionIndicator indicator);

    public int updateIndicator(FusionIndicator indicator);

    public int deleteIndicatorByIds(Long[] indicatorIds);
}
