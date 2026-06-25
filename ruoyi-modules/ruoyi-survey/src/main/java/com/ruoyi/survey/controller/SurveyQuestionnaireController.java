package com.ruoyi.survey.controller;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.survey.domain.SurveyQuestionnaire;
import com.ruoyi.survey.service.ISurveyQuestionnaireService;
import java.util.List;
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

/**
 * 问卷Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/questionnaire")
public class SurveyQuestionnaireController extends BaseController
{
    @Autowired
    private ISurveyQuestionnaireService questionnaireService;

    @RequiresPermissions("survey:questionnaire:list")
    @GetMapping("/list")
    public TableDataInfo list(SurveyQuestionnaire questionnaire)
    {
        startPage();
        List<SurveyQuestionnaire> list = questionnaireService.selectQuestionnaireList(questionnaire);
        return getDataTable(list);
    }

    @RequiresPermissions("survey:questionnaire:query")
    @GetMapping(value = "/{questionnaireId}")
    public AjaxResult getInfo(@PathVariable Long questionnaireId)
    {
        return success(questionnaireService.selectQuestionnaireById(questionnaireId));
    }

    @RequiresPermissions("survey:questionnaire:add")
    @Log(title = "问卷", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SurveyQuestionnaire questionnaire)
    {
        questionnaire.setCreateBy(SecurityUtils.getUsername());
        return toAjax(questionnaireService.insertQuestionnaire(questionnaire));
    }

    @RequiresPermissions("survey:questionnaire:edit")
    @Log(title = "问卷", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SurveyQuestionnaire questionnaire)
    {
        questionnaire.setUpdateBy(SecurityUtils.getUsername());
        return success(questionnaireService.updateQuestionnaire(questionnaire));
    }

    @RequiresPermissions("survey:questionnaire:edit")
    @Log(title = "问卷", businessType = BusinessType.INSERT)
    @PostMapping("/{questionnaireId}/draft")
    public AjaxResult createDraft(@PathVariable Long questionnaireId)
    {
        return success(questionnaireService.createDraftFromPublished(questionnaireId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("survey:questionnaire:publish")
    @Log(title = "问卷", businessType = BusinessType.UPDATE)
    @PostMapping("/{questionnaireId}/publish")
    public AjaxResult publish(@PathVariable Long questionnaireId)
    {
        return toAjax(questionnaireService.publishQuestionnaire(questionnaireId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("survey:questionnaire:end")
    @Log(title = "问卷", businessType = BusinessType.UPDATE)
    @PostMapping("/{questionnaireId}/end")
    public AjaxResult end(@PathVariable Long questionnaireId)
    {
        return toAjax(questionnaireService.endQuestionnaire(questionnaireId, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("survey:questionnaire:remove")
    @Log(title = "问卷", businessType = BusinessType.DELETE)
    @DeleteMapping("/{questionnaireIds}")
    public AjaxResult remove(@PathVariable Long[] questionnaireIds)
    {
        return toAjax(questionnaireService.deleteQuestionnaireByIds(questionnaireIds));
    }
}
