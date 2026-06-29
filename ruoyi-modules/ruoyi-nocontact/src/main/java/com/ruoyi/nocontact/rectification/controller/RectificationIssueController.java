package com.ruoyi.nocontact.rectification.controller;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.rectification.domain.RectificationIssue;
import com.ruoyi.nocontact.rectification.service.IRectificationIssueService;
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
@RequestMapping("/rectification/issue")
public class RectificationIssueController extends BaseController
{
    @Autowired
    private IRectificationIssueService issueService;

    @RequiresPermissions("nocontact:rectification:issue:list")
    @GetMapping("/list")
    public TableDataInfo list(RectificationIssue issue)
    {
        startPage();
        List<RectificationIssue> list = issueService.selectIssueList(issue);
        return getDataTable(list);
    }

    @RequiresPermissions("nocontact:rectification:issue:query")
    @GetMapping("/{issueId}")
    public AjaxResult getInfo(@PathVariable Long issueId)
    {
        return success(issueService.selectIssueById(issueId));
    }

    @RequiresPermissions("nocontact:rectification:issue:list")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(issueService.dashboard());
    }

    @RequiresPermissions("nocontact:rectification:issue:add")
    @Log(title = "问题整改", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody RectificationIssue issue)
    {
        return toAjax(issueService.insertIssue(issue, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:rectification:issue:edit")
    @Log(title = "问题整改", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody RectificationIssue issue)
    {
        return toAjax(issueService.updateIssue(issue, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:rectification:issue:edit")
    @Log(title = "分配整改", businessType = BusinessType.UPDATE)
    @PutMapping("/{issueId}/dispatch")
    public AjaxResult dispatch(@PathVariable Long issueId, @RequestBody RectificationIssue issue)
    {
        return toAjax(issueService.dispatch(issueId, issue, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:rectification:issue:edit")
    @Log(title = "提交整改", businessType = BusinessType.UPDATE)
    @PutMapping("/{issueId}/submit")
    public AjaxResult submit(@PathVariable Long issueId, @RequestBody RectificationIssue issue)
    {
        return toAjax(issueService.submit(issueId, issue, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:rectification:issue:edit")
    @Log(title = "审核整改", businessType = BusinessType.UPDATE)
    @PutMapping("/{issueId}/review/{approved}")
    public AjaxResult review(@PathVariable Long issueId, @PathVariable Boolean approved, @RequestBody RectificationIssue issue)
    {
        return toAjax(issueService.review(issueId, approved.booleanValue(), issue, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:rectification:issue:edit")
    @Log(title = "归档整改", businessType = BusinessType.UPDATE)
    @PutMapping("/{issueId}/archive")
    public AjaxResult archive(@PathVariable Long issueId)
    {
        return toAjax(issueService.archive(issueId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:rectification:issue:remove")
    @Log(title = "问题整改", businessType = BusinessType.DELETE)
    @DeleteMapping("/{issueIds}")
    public AjaxResult remove(@PathVariable Long[] issueIds)
    {
        return toAjax(issueService.deleteIssueByIds(issueIds));
    }
}
