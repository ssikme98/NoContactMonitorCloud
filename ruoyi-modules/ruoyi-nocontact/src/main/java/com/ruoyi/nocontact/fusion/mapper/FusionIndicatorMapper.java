package com.ruoyi.nocontact.fusion.mapper;

import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import java.util.List;

/**
 * 营商监测指标Mapper接口
 *
 * @author ruoyi
 */
public interface FusionIndicatorMapper
{
    public List<FusionIndicator> selectIndicatorList(FusionIndicator indicator);

    public FusionIndicator selectIndicatorById(Long indicatorId);

    public FusionIndicator selectIndicatorByCode(String indicatorCode);

    public FusionIndicator selectEnabledIndicatorByCode(String indicatorCode);

    public Integer selectMaxVersionNoByCode(String indicatorCode);

    public int countEnabledIndicatorByCode(FusionIndicator indicator);

    public int insertIndicator(FusionIndicator indicator);

    public int updateIndicator(FusionIndicator indicator);

    public int deleteIndicatorByIds(Long[] indicatorIds);
}
