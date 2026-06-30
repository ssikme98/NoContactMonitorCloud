package com.ruoyi.nocontact.integration.controller;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import com.ruoyi.nocontact.integration.domain.ExternalSyncBatch;
import com.ruoyi.nocontact.integration.domain.ExternalSyncLog;
import com.ruoyi.nocontact.integration.service.IExternalIntegrationService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/integration")
public class ExternalIntegrationController extends BaseController
{
    @Autowired
    private IExternalIntegrationService integrationService;

    @RequiresPermissions("nocontact:integration:config:list")
    @GetMapping("/config/list")
    public TableDataInfo configList(ExternalIntegrationConfig config)
    {
        startPage();
        List<ExternalIntegrationConfig> list = integrationService.selectConfigList(config);
        return getDataTable(list);
    }

    @RequiresPermissions("nocontact:integration:config:query")
    @GetMapping("/config/{configId}")
    public AjaxResult getConfig(@PathVariable Long configId)
    {
        return success(integrationService.selectConfigById(configId));
    }

    @RequiresPermissions("nocontact:integration:config:add")
    @Log(title = "对接配置", businessType = BusinessType.INSERT, excludeParamNames = { "authCredential" })
    @PostMapping("/config")
    public AjaxResult addConfig(@Valid @RequestBody ExternalIntegrationConfig config)
    {
        return toAjax(integrationService.insertConfig(config, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:integration:config:edit")
    @Log(title = "对接配置", businessType = BusinessType.UPDATE, excludeParamNames = { "authCredential" })
    @PutMapping("/config")
    public AjaxResult editConfig(@Valid @RequestBody ExternalIntegrationConfig config)
    {
        return toAjax(integrationService.updateConfig(config, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:integration:config:remove")
    @Log(title = "对接配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/config/{configIds}")
    public AjaxResult removeConfig(@PathVariable Long[] configIds)
    {
        return toAjax(integrationService.deleteConfigByIds(configIds));
    }

    @RequiresPermissions("nocontact:integration:config:query")
    @Log(title = "对接连接测试", businessType = BusinessType.OTHER)
    @PostMapping("/config/{configId}/test")
    public AjaxResult testConnection(@PathVariable Long configId)
    {
        return success(integrationService.testConnection(configId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:integration:log:list")
    @Log(title = "手动执行同步", businessType = BusinessType.OTHER)
    @PostMapping("/config/{configId}/sync")
    public AjaxResult sync(@PathVariable Long configId)
    {
        return success(integrationService.syncConfig(configId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:integration:log:list")
    @Log(title = "重试同步批次", businessType = BusinessType.UPDATE)
    @PostMapping("/batch/{syncBatchId}/retry")
    public AjaxResult retry(@PathVariable Long syncBatchId)
    {
        return success(integrationService.retryBatch(syncBatchId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:integration:log:list")
    @GetMapping("/batch/list")
    public TableDataInfo batchList(ExternalSyncBatch batch)
    {
        startPage();
        return getDataTable(integrationService.selectSyncBatchList(batch));
    }

    @RequiresPermissions("nocontact:integration:log:list")
    @GetMapping("/log/list")
    public TableDataInfo logList(ExternalSyncLog log)
    {
        startPage();
        return getDataTable(integrationService.selectLogList(log));
    }
}
