package com.ruoyi.nocontact.survey.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.survey.domain.SurveyEnterprise;
import com.ruoyi.nocontact.survey.service.ISurveyEnterpriseService;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 企业库Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/enterprise")
public class SurveyEnterpriseController extends BaseController
{
    @Autowired
    private ISurveyEnterpriseService enterpriseService;

    @RequiresPermissions("survey:enterprise:list")
    @GetMapping("/list")
    public TableDataInfo list(SurveyEnterprise enterprise)
    {
        startPage();
        List<SurveyEnterprise> list = enterpriseService.selectEnterpriseList(enterprise);
        return getDataTable(list);
    }

    @Log(title = "企业库", businessType = BusinessType.EXPORT)
    @RequiresPermissions("survey:enterprise:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SurveyEnterprise enterprise)
    {
        List<SurveyEnterprise> list = enterpriseService.selectEnterpriseList(enterprise);
        ExcelUtil<SurveyEnterprise> util = new ExcelUtil<SurveyEnterprise>(SurveyEnterprise.class);
        util.exportExcel(response, list, "企业库数据");
    }

    @Log(title = "企业库", businessType = BusinessType.IMPORT)
    @RequiresPermissions("survey:enterprise:import")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SurveyEnterprise> util = new ExcelUtil<SurveyEnterprise>(SurveyEnterprise.class);
        List<SurveyEnterprise> enterpriseList = util.importExcel(file.getInputStream());
        String operName = SecurityUtils.getUsername();
        String message = enterpriseService.importEnterprise(enterpriseList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws IOException
    {
        ExcelUtil<SurveyEnterprise> util = new ExcelUtil<SurveyEnterprise>(SurveyEnterprise.class);
        util.importTemplateExcel(response, "企业库数据");
    }

    @RequiresPermissions("survey:enterprise:query")
    @GetMapping(value = "/{enterpriseId}")
    public AjaxResult getInfo(@PathVariable Long enterpriseId)
    {
        return success(enterpriseService.selectEnterpriseById(enterpriseId));
    }

    @RequiresPermissions("survey:enterprise:add")
    @Log(title = "企业库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SurveyEnterprise enterprise)
    {
        if (!enterpriseService.checkCreditCodeUnique(enterprise))
        {
            return error("新增企业'" + enterprise.getEnterpriseName() + "'失败，统一社会信用代码已存在");
        }
        enterprise.setCreateBy(SecurityUtils.getUsername());
        return toAjax(enterpriseService.insertEnterprise(enterprise));
    }

    @RequiresPermissions("survey:enterprise:edit")
    @Log(title = "企业库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SurveyEnterprise enterprise)
    {
        if (!enterpriseService.checkCreditCodeUnique(enterprise))
        {
            return error("修改企业'" + enterprise.getEnterpriseName() + "'失败，统一社会信用代码已存在");
        }
        enterprise.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(enterpriseService.updateEnterprise(enterprise));
    }

    @RequiresPermissions("survey:enterprise:remove")
    @Log(title = "企业库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{enterpriseIds}")
    public AjaxResult remove(@PathVariable Long[] enterpriseIds)
    {
        return toAjax(enterpriseService.deleteEnterpriseByIds(enterpriseIds));
    }
}
