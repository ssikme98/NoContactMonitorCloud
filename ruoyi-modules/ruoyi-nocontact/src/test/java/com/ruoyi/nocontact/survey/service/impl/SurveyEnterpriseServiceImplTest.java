package com.ruoyi.nocontact.survey.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.survey.domain.SurveyEnterprise;
import com.ruoyi.nocontact.survey.mapper.SurveyEnterpriseMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyEnterpriseServiceImplTest
{
    private SurveyEnterpriseServiceImpl service;

    @Mock
    private SurveyEnterpriseMapper enterpriseMapper;

    @BeforeEach
    void setUp()
    {
        service = new SurveyEnterpriseServiceImpl();
        ReflectionTestUtils.setField(service, "enterpriseMapper", enterpriseMapper);
    }

    @Test
    void insertEnterpriseBackfillsHunanCityCode()
    {
        when(enterpriseMapper.insertEnterprise(any(SurveyEnterprise.class))).thenReturn(1);

        int rows = service.insertEnterprise(enterprise("长沙市"));

        assertEquals(1, rows);
        ArgumentCaptor<SurveyEnterprise> captor = ArgumentCaptor.forClass(SurveyEnterprise.class);
        verify(enterpriseMapper).insertEnterprise(captor.capture());
        assertEquals("长沙市", captor.getValue().getRegionName());
        assertEquals("430100", captor.getValue().getRegionCode());
    }

    @Test
    void insertEnterpriseRejectsNonHunanCity()
    {
        SurveyEnterprise enterprise = enterprise("武汉市");

        assertThrows(ServiceException.class, () -> service.insertEnterprise(enterprise));

        verify(enterpriseMapper, never()).insertEnterprise(any(SurveyEnterprise.class));
    }

    private SurveyEnterprise enterprise(String city)
    {
        SurveyEnterprise enterprise = new SurveyEnterprise();
        enterprise.setEnterpriseName("湖南样本企业");
        enterprise.setCreditCode("91430100MA4TEST001");
        enterprise.setRegionName(city);
        enterprise.setStatus("0");
        return enterprise;
    }
}
