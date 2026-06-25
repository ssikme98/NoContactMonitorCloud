package com.ruoyi.survey;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 问卷模块
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class RuoYiSurveyApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiSurveyApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  问卷模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
