package com.ruoyi.job.nocontact;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.job.nocontact.factory.RemoteNocontactWarningFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 营商无感预警远程调度接口。
 */
@FeignClient(contextId = "remoteNocontactWarningService", value = ServiceNameConstants.NOCONTACT_SERVICE,
        fallbackFactory = RemoteNocontactWarningFallbackFactory.class)
public interface RemoteNocontactWarningService
{
    @PostMapping("/warning/evaluation/scheduled/{periodKey}")
    public R<Integer> evaluateScheduledRules(@PathVariable("periodKey") String periodKey,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
