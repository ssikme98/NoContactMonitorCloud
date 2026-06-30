package com.ruoyi.nocontact.support.service;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.nocontact.support.domain.SupportPublicSettings;
import com.ruoyi.nocontact.support.domain.SupportTodoItem;
import java.util.List;

public interface ISupportCenterService
{
    List<SupportTodoItem> listTodoSummary();

    SupportPublicSettings loadPublicSettings();

    AjaxResult geocodeEnterprise(String regionName, String address);
}
