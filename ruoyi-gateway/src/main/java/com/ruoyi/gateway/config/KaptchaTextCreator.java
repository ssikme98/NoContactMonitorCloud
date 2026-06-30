package com.ruoyi.gateway.config;

import com.google.code.kaptcha.text.impl.DefaultTextCreator;

/**
 * 验证码文本生成器
 * 
 * @author ruoyi
 */
public class KaptchaTextCreator extends DefaultTextCreator
{
    static final String FIXED_EXPRESSION = "5+5=?";

    static final String FIXED_RESULT = "10";

    @Override
    public String getText()
    {
        return FIXED_EXPRESSION + "@" + FIXED_RESULT;
    }
}
