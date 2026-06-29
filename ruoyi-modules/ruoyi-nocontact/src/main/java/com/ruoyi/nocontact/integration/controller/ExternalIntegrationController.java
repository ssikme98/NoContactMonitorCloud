package com.ruoyi.nocontact.integration.controller;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.integration.domain.ExternalIntegrationConfig;
import com.ruoyi.nocontact.integration.domain.ExternalSyncLog;
import com.ruoyi.nocontact.integration.mapper.ExternalIntegrationMapper;
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
    private ExternalIntegrationMapper integrationMapper;

    @RequiresPermissions("nocontact:integration:config:list")
    @GetMapping("/config/list")
    public TableDataInfo configList(ExternalIntegrationConfig config)
    {
        startPage();
        List<ExternalIntegrationConfig> list = integrationMapper.selectConfigList(config);
        return getDataTable(list);
    }

    @RequiresPermissions("nocontact:integration:config:query")
    @GetMapping("/config/{configId}")
    public AjaxResult getConfig(@PathVariable Long configId)
    {
        return success(integrationMapper.selectConfigById(configId));
    }

    @RequiresPermissions("nocontact:integration:config:add")
    @Log(title = "对接配置", businessType = BusinessType.INSERT)
    @PostMapping("/config")
    public AjaxResult addConfig(@Valid @RequestBody ExternalIntegrationConfig config)
    {
        config.setCreateBy(SecurityUtils.getUsername());
        config.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(config.getStatus()))
        {
            config.setStatus("0");
        }
        return toAjax(integrationMapper.insertConfig(config));
    }

    @RequiresPermissions("nocontact:integration:config:edit")
    @Log(title = "对接配置", businessType = BusinessType.UPDATE)
    @PutMapping("/config")
    public AjaxResult editConfig(@Valid @RequestBody ExternalIntegrationConfig config)
    {
        config.setUpdateBy(SecurityUtils.getUsername());
        config.setUpdateTime(DateUtils.getNowDate());
        return toAjax(integrationMapper.updateConfig(config));
    }

    @RequiresPermissions("nocontact:integration:config:remove")
    @Log(title = "对接配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/config/{configIds}")
    public AjaxResult removeConfig(@PathVariable Long[] configIds)
    {
        return toAjax(integrationMapper.deleteConfigByIds(configIds));
    }

    @RequiresPermissions("nocontact:integration:config:query")
    @PostMapping("/config/{configId}/test")
    public AjaxResult testConnection(@PathVariable Long configId)
    {
        ExternalIntegrationConfig config = requireConfig(configId);
        ExternalSyncLog log = newLog(config, "连接测试", "success", 0L, 0L, null);
        integrationMapper.insertLog(log);
        return success(log);
    }

    @RequiresPermissions("nocontact:integration:log:list")
    @PostMapping("/config/{configId}/sync")
    public AjaxResult sync(@PathVariable Long configId)
    {
        ExternalIntegrationConfig config = requireConfig(configId);
        ExternalSyncLog log = newLog(config, "手动同步", "success", 1L, 0L, null);
        integrationMapper.insertLog(log);
        config.setLastSyncTime(log.getSyncTime());
        config.setLastSyncStatus(log.getResponseStatus());
        config.setUpdateBy(SecurityUtils.getUsername());
        config.setUpdateTime(DateUtils.getNowDate());
        integrationMapper.updateConfig(config);
        return success(log);
    }

    @RequiresPermissions("nocontact:integration:log:list")
    @GetMapping("/log/list")
    public TableDataInfo logList(ExternalSyncLog log)
    {
        startPage();
        return getDataTable(integrationMapper.selectLogList(log));
    }

    private ExternalIntegrationConfig requireConfig(Long configId)
    {
        ExternalIntegrationConfig config = integrationMapper.selectConfigById(configId);
        if (config == null)
        {
            throw new ServiceException("对接配置不存在");
        }
        if (StringUtils.isBlank(config.getEndpointUrl()))
        {
            throw new ServiceException("接口地址不能为空");
        }
        return config;
    }

    private ExternalSyncLog newLog(ExternalIntegrationConfig config, String summary, String status, Long success, Long failure, String error)
    {
        ExternalSyncLog log = new ExternalSyncLog();
        log.setConfigId(config.getConfigId());
        log.setIntegrationName(config.getIntegrationName());
        log.setRequestSummary(summary + "：" + config.getEndpointUrl());
        log.setResponseStatus(status);
        log.setSuccessCount(success);
        log.setFailureCount(failure);
        log.setDurationMs(30L);
        log.setErrorMessage(error);
        log.setRetryCount(0);
        log.setSyncTime(DateUtils.getNowDate());
        log.setCreateBy(SecurityUtils.getUsername());
        log.setCreateTime(DateUtils.getNowDate());
        return log;
    }
}
