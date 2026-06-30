package com.ruoyi.nocontact.support.controller;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.nocontact.support.domain.BusinessMessage;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import com.ruoyi.nocontact.support.service.ISupportCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SupportCenterController extends BaseController
{
    @Autowired
    private IBusinessMessageService businessMessageService;

    @Autowired
    private ISupportCenterService supportCenterService;

    @RequiresPermissions("nocontact:support:message:list")
    @GetMapping("/support/message/list")
    public TableDataInfo listBusinessMessage(BusinessMessage query)
    {
        startPage();
        return getDataTable(businessMessageService.selectCurrentUserMessages(query));
    }

    @RequiresPermissions("nocontact:support:message:list")
    @PutMapping("/support/message/{messageId}/read")
    public AjaxResult markRead(@PathVariable Long messageId)
    {
        return toAjax(businessMessageService.markRead(messageId));
    }

    @RequiresPermissions("nocontact:support:todo:list")
    @GetMapping("/support/todo/summary")
    public AjaxResult listTodoSummary()
    {
        return success(supportCenterService.listTodoSummary());
    }

    @RequiresPermissions("nocontact:support:settings:list")
    @GetMapping("/support/settings/public")
    public AjaxResult getSupportPublicSettings()
    {
        return success(supportCenterService.loadPublicSettings());
    }

    @RequiresPermissions("survey:enterprise:edit")
    @GetMapping("/support/geocode/enterprise")
    public AjaxResult geocodeEnterpriseAddress(@RequestParam String regionName, @RequestParam String address)
    {
        return supportCenterService.geocodeEnterprise(regionName, address);
    }
}
