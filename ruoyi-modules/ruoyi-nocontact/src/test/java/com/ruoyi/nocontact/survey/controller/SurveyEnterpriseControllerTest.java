package com.ruoyi.nocontact.survey.controller;

import com.ruoyi.nocontact.survey.domain.SurveyEnterprise;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurveyEnterpriseControllerTest
{
    @Test
    void maskListSensitiveFieldsMasksCreditCodeAndPhone()
    {
        SurveyEnterprise enterprise = new SurveyEnterprise();
        enterprise.setCreditCode("91430100MA4E2E0001");
        enterprise.setContactPhone("13800138000");

        List<SurveyEnterprise> list = Collections.singletonList(enterprise);
        SurveyEnterpriseController controller = new SurveyEnterpriseController();

        ReflectionTestUtils.invokeMethod(controller, "maskListSensitiveFields", list);

        assertEquals("9143**********0001", enterprise.getCreditCode());
        assertEquals("138****8000", enterprise.getContactPhone());
    }
}
