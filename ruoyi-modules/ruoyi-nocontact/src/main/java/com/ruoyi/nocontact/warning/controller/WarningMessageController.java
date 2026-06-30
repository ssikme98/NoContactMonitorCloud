package com.ruoyi.nocontact.warning.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.service.IWarningMessageService;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预警消息Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/warning/message")
public class WarningMessageController extends BaseController
{
    @Autowired
    private IWarningMessageService messageService;

    @RequiresPermissions("nocontact:warning:message:list")
    @GetMapping("/list")
    public TableDataInfo list(WarningMessage message)
    {
        startPage();
        List<WarningMessage> list = messageService.selectMessageList(message);
        return getDataTable(list);
    }

    @RequiresPermissions("nocontact:warning:message:query")
    @GetMapping("/{messageId}")
    public AjaxResult getInfo(@PathVariable Long messageId)
    {
        return success(messageService.selectMessageById(messageId));
    }

    @RequiresPermissions("nocontact:warning:dashboard:query")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(messageService.selectDashboard());
    }

    @Log(title = "预警消息", businessType = BusinessType.EXPORT)
    @RequiresPermissions("nocontact:warning:message:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WarningMessage message)
    {
        List<WarningMessage> list = messageService.selectMessageList(message);
        ExcelUtil<WarningMessage> util = new ExcelUtil<WarningMessage>(WarningMessage.class);
        util.exportExcel(response, list, "预警消息");
    }

    @RequiresPermissions("nocontact:warning:message:edit")
    @Log(title = "预警消息状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{messageId}/status/{status}")
    public AjaxResult changeStatus(@PathVariable Long messageId, @PathVariable String status,
            @RequestParam(required = false) String opinion)
    {
        return toAjax(messageService.updateMessageStatus(messageId, status, opinion, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("nocontact:warning:message:remove")
    @Log(title = "预警消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds)
    {
        return toAjax(messageService.deleteMessageByIds(messageIds));
    }
}
