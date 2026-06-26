package com.ruoyi.job.nocontact.factory;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.job.nocontact.RemoteNocontactWarningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 营商无感预警远程调度降级处理。
 */
@Component
public class RemoteNocontactWarningFallbackFactory implements FallbackFactory<RemoteNocontactWarningService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteNocontactWarningFallbackFactory.class);

    @Override
    public RemoteNocontactWarningService create(Throwable throwable)
    {
        log.error("营商无感预警调度调用失败:{}", throwable.getMessage());
        return new RemoteNocontactWarningService()
        {
            @Override
            public R<Integer> evaluateScheduledRules(String periodKey, String source)
            {
                return R.fail("营商无感预警调度调用失败:" + throwable.getMessage());
            }
        };
    }
}
