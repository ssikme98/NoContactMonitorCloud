package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import com.ruoyi.nocontact.fusion.mapper.FusionIndicatorMapper;
import com.ruoyi.nocontact.fusion.service.IFusionIndicatorService;
import java.util.List;
import java.util.Objects;
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
        normalizeIndicator(indicator);
        assertSingleEnabledVersion(indicator);
        indicator.setCreateTime(DateUtils.getNowDate());
        return indicatorMapper.insertIndicator(indicator);
    }

    @Override
    public int updateIndicator(FusionIndicator indicator)
    {
        FusionIndicator existing = indicatorMapper.selectIndicatorById(indicator.getIndicatorId());
        if (existing == null)
        {
            throw new ServiceException("指标不存在");
        }
        normalizeIndicator(indicator);
        assertSingleEnabledVersion(indicator);
        if ("enabled".equals(existing.getLifecycleStatus()) && hasBusinessChange(existing, indicator))
        {
            throw new ServiceException("已启用指标不能直接修改，请复制草稿后变更");
        }
        indicator.setUpdateTime(DateUtils.getNowDate());
        return indicatorMapper.updateIndicator(indicator);
    }

    @Override
    public FusionIndicator copyIndicatorDraft(Long indicatorId, String operName)
    {
        FusionIndicator source = indicatorMapper.selectIndicatorById(indicatorId);
        if (source == null)
        {
            throw new ServiceException("指标不存在");
        }
        FusionIndicator draft = new FusionIndicator();
        draft.setIndicatorCode(source.getIndicatorCode());
        draft.setExternalCode(source.getExternalCode());
        draft.setSystemName(source.getSystemName());
        draft.setCategoryName(source.getCategoryName());
        Integer maxVersionNo = indicatorMapper.selectMaxVersionNoByCode(source.getIndicatorCode());
        draft.setVersionNo(maxVersionNo == null ? 1 : maxVersionNo + 1);
        draft.setLifecycleStatus("draft");
        draft.setYearName(source.getYearName());
        draft.setFirstLevel(source.getFirstLevel());
        draft.setSecondLevel(source.getSecondLevel());
        draft.setIndicatorName(source.getIndicatorName());
        draft.setPeriodType(source.getPeriodType());
        draft.setDataType(source.getDataType());
        draft.setScoringRule(source.getScoringRule());
        draft.setAlgorithmType(source.getAlgorithmType());
        draft.setAlgorithmParams(source.getAlgorithmParams());
        draft.setResponsibleUnit(source.getResponsibleUnit());
        draft.setRegionCode(source.getRegionCode());
        draft.setRegionName(source.getRegionName());
        draft.setDataSource(source.getDataSource());
        draft.setTagNames(source.getTagNames());
        draft.setSortOrder(source.getSortOrder());
        draft.setStatus("1");
        draft.setRemark(source.getRemark());
        draft.setCreateBy(operName);
        draft.setCreateTime(DateUtils.getNowDate());
        indicatorMapper.insertIndicator(draft);
        return draft;
    }

    @Override
    public int deleteIndicatorByIds(Long[] indicatorIds)
    {
        if (indicatorIds != null)
        {
            for (Long indicatorId : indicatorIds)
            {
                FusionIndicator existing = indicatorMapper.selectIndicatorById(indicatorId);
                if (existing != null && "enabled".equals(existing.getLifecycleStatus()))
                {
                    throw new ServiceException("已启用指标不能删除，请先停用或归档");
                }
            }
        }
        return indicatorMapper.deleteIndicatorByIds(indicatorIds);
    }

    private void normalizeIndicator(FusionIndicator indicator)
    {
        if (StringUtils.isBlank(indicator.getIndicatorCode()))
        {
            indicator.setIndicatorCode("NC-" + DateUtils.dateTimeNow("yyyyMMddHHmmssSSS"));
        }
        if (indicator.getVersionNo() == null || indicator.getVersionNo() < 1)
        {
            indicator.setVersionNo(1);
        }
        if (StringUtils.isBlank(indicator.getLifecycleStatus()))
        {
            indicator.setLifecycleStatus("draft");
        }
        if (StringUtils.isBlank(indicator.getStatus()))
        {
            indicator.setStatus("enabled".equals(indicator.getLifecycleStatus()) ? "0" : "1");
        }
        else if ("0".equals(indicator.getStatus()) && "draft".equals(indicator.getLifecycleStatus()))
        {
            indicator.setLifecycleStatus("enabled");
        }
        else if ("enabled".equals(indicator.getLifecycleStatus()))
        {
            indicator.setStatus("0");
        }
        else if ("disabled".equals(indicator.getLifecycleStatus()) || "archived".equals(indicator.getLifecycleStatus()))
        {
            indicator.setStatus("1");
        }
        if (StringUtils.isBlank(indicator.getPeriodType()))
        {
            indicator.setPeriodType("month");
        }
        if (StringUtils.isBlank(indicator.getDataType()))
        {
            indicator.setDataType("number");
        }
        if (StringUtils.isBlank(indicator.getAlgorithmType()))
        {
            indicator.setAlgorithmType("threshold");
        }
    }

    private void assertSingleEnabledVersion(FusionIndicator indicator)
    {
        if ("enabled".equals(indicator.getLifecycleStatus()) && "0".equals(indicator.getStatus())
                && indicatorMapper.countEnabledIndicatorByCode(indicator) > 0)
        {
            throw new ServiceException("同一指标编码只能存在一个启用版本");
        }
    }

    private boolean hasBusinessChange(FusionIndicator existing, FusionIndicator update)
    {
        return changed(existing.getIndicatorCode(), update.getIndicatorCode())
                || changed(existing.getExternalCode(), update.getExternalCode())
                || changed(existing.getSystemName(), update.getSystemName())
                || changed(existing.getCategoryName(), update.getCategoryName())
                || changed(existing.getYearName(), update.getYearName())
                || changed(existing.getFirstLevel(), update.getFirstLevel())
                || changed(existing.getSecondLevel(), update.getSecondLevel())
                || changed(existing.getIndicatorName(), update.getIndicatorName())
                || changed(existing.getPeriodType(), update.getPeriodType())
                || changed(existing.getDataType(), update.getDataType())
                || changed(existing.getScoringRule(), update.getScoringRule())
                || changed(existing.getAlgorithmType(), update.getAlgorithmType())
                || changed(existing.getAlgorithmParams(), update.getAlgorithmParams())
                || changed(existing.getResponsibleUnit(), update.getResponsibleUnit())
                || changed(existing.getRegionCode(), update.getRegionCode())
                || changed(existing.getRegionName(), update.getRegionName())
                || changed(existing.getDataSource(), update.getDataSource())
                || changed(existing.getTagNames(), update.getTagNames())
                || !Objects.equals(existing.getSortOrder(), update.getSortOrder());
    }

    private boolean changed(String left, String right)
    {
        return !StringUtils.defaultString(left).equals(StringUtils.defaultString(right));
    }
}
