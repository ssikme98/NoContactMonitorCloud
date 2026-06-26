package com.ruoyi.nocontact;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 营商无感业务模块
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class RuoYiNocontactApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiNocontactApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  营商无感业务模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
