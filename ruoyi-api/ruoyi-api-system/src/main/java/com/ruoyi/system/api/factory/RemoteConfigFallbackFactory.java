package com.ruoyi.system.api.factory;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 参数配置服务降级处理
 */
@Component
public class RemoteConfigFallbackFactory implements FallbackFactory<RemoteConfigService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteConfigFallbackFactory.class);

    @Override
    public RemoteConfigService create(final Throwable throwable)
    {
        log.error("参数配置服务调用失败:{}", throwable.getMessage());
        return new RemoteConfigService()
        {
            @Override
            public R<String> getConfigKey(String configKey, String source)
            {
                return R.fail("获取参数失败:" + throwable.getMessage());
            }
        };
    }
}
