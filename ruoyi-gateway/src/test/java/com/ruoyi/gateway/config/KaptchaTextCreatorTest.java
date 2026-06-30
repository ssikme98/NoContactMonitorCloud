package com.ruoyi.gateway.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KaptchaTextCreatorTest
{
    @Test
    void shouldGenerateFixedCaptchaExpressionAndResult()
    {
        KaptchaTextCreator creator = new KaptchaTextCreator();

        assertEquals("5+5=?@10", creator.getText());
        assertEquals("5+5=?", KaptchaTextCreator.FIXED_EXPRESSION);
        assertEquals("10", KaptchaTextCreator.FIXED_RESULT);
    }
}
