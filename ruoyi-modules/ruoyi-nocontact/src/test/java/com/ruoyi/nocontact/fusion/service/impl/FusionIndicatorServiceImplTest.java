package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import com.ruoyi.nocontact.fusion.mapper.FusionIndicatorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FusionIndicatorServiceImplTest
{
    private FusionIndicatorServiceImpl service;

    @Mock
    private FusionIndicatorMapper indicatorMapper;

    @BeforeEach
    void setUp()
    {
        service = new FusionIndicatorServiceImpl();
        ReflectionTestUtils.setField(service, "indicatorMapper", indicatorMapper);
    }

    @Test
    void enabledIndicatorCannotBeChangedInPlace()
    {
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(enabledIndicator("数字政务能力"));
        FusionIndicator update = enabledIndicator("数字政务能力修改");
        update.setIndicatorId(1003L);

        assertThrows(ServiceException.class, () -> service.updateIndicator(update));
    }

    @Test
    void copyIndicatorDraftCreatesNextDraftVersion()
    {
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(enabledIndicator("数字政务能力"));
        when(indicatorMapper.selectMaxVersionNoByCode("NC-1003")).thenReturn(4);

        service.copyIndicatorDraft(1003L, "admin");

        ArgumentCaptor<FusionIndicator> captor = ArgumentCaptor.forClass(FusionIndicator.class);
        verify(indicatorMapper).insertIndicator(captor.capture());
        FusionIndicator draft = captor.getValue();
        assertEquals("NC-1003", draft.getIndicatorCode());
        assertEquals(Integer.valueOf(5), draft.getVersionNo());
        assertEquals("draft", draft.getLifecycleStatus());
        assertEquals("1", draft.getStatus());
        assertEquals("admin", draft.getCreateBy());
    }

    @Test
    void newIndicatorDefaultsToDraftVersion()
    {
        FusionIndicator indicator = new FusionIndicator();
        indicator.setIndicatorName("新增指标");

        service.insertIndicator(indicator);

        verify(indicatorMapper).insertIndicator(any(FusionIndicator.class));
        assertEquals(Integer.valueOf(1), indicator.getVersionNo());
        assertEquals("draft", indicator.getLifecycleStatus());
        assertEquals("1", indicator.getStatus());
        assertEquals("month", indicator.getPeriodType());
        assertEquals("number", indicator.getDataType());
    }

    private FusionIndicator enabledIndicator(String indicatorName)
    {
        FusionIndicator indicator = new FusionIndicator();
        indicator.setIndicatorId(1003L);
        indicator.setIndicatorCode("NC-1003");
        indicator.setExternalCode("EXT-1003");
        indicator.setSystemName("营商环境无感监测指标体系");
        indicator.setCategoryName("服务效能/政务服务");
        indicator.setVersionNo(1);
        indicator.setLifecycleStatus("enabled");
        indicator.setYearName("2026");
        indicator.setFirstLevel("服务效能");
        indicator.setSecondLevel("政务服务");
        indicator.setIndicatorName(indicatorName);
        indicator.setPeriodType("month");
        indicator.setDataType("number");
        indicator.setScoringRule("低于基准扣分");
        indicator.setAlgorithmType("threshold");
        indicator.setAlgorithmParams("{\"baseline\":80}");
        indicator.setResponsibleUnit("省数据局");
        indicator.setRegionCode("433100");
        indicator.setRegionName("湘西州");
        indicator.setDataSource("指标汇总表");
        indicator.setTagNames("数字政务");
        indicator.setSortOrder(10);
        indicator.setStatus("0");
        return indicator;
    }
}
