package com.ruoyi.nocontact.support.service;

import com.ruoyi.common.core.web.domain.AjaxResult;

public interface IAmapGeocodeService
{
    AjaxResult geocodeEnterprise(String regionName, String address);
}
