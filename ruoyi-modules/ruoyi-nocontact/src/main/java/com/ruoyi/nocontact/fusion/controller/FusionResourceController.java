package com.ruoyi.nocontact.fusion.controller;

import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.fusion.domain.FusionResource;
import com.ruoyi.nocontact.fusion.mapper.FusionResourceMapper;
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

/**
 * 资源目录Controller
 */
@RestController
@RequestMapping("/fusion/resource")
public class FusionResourceController extends BaseController
{
    @Autowired
    private FusionResourceMapper resourceMapper;

    @RequiresPermissions("nocontact:fusion:resource:list")
    @GetMapping("/list")
    public TableDataInfo list(FusionResource resource)
    {
        startPage();
        List<FusionResource> list = resourceMapper.selectResourceList(resource);
        return getDataTable(list);
    }

    @RequiresPermissions("nocontact:fusion:resource:query")
    @GetMapping("/{resourceId}")
    public AjaxResult getInfo(@PathVariable Long resourceId)
    {
        return success(resourceMapper.selectResourceById(resourceId));
    }

    @RequiresPermissions("nocontact:fusion:resource:add")
    @Log(title = "资源目录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody FusionResource resource)
    {
        resource.setCreateBy(SecurityUtils.getUsername());
        resource.setCreateTime(DateUtils.getNowDate());
        return toAjax(resourceMapper.insertResource(resource));
    }

    @RequiresPermissions("nocontact:fusion:resource:edit")
    @Log(title = "资源目录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody FusionResource resource)
    {
        resource.setUpdateBy(SecurityUtils.getUsername());
        resource.setUpdateTime(DateUtils.getNowDate());
        return toAjax(resourceMapper.updateResource(resource));
    }

    @RequiresPermissions("nocontact:fusion:resource:remove")
    @Log(title = "资源目录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{resourceIds}")
    public AjaxResult remove(@PathVariable Long[] resourceIds)
    {
        return toAjax(resourceMapper.deleteResourceByIds(resourceIds));
    }
}
