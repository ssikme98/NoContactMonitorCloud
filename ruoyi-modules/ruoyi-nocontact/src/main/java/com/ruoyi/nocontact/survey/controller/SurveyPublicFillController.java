package com.ruoyi.nocontact.survey.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.nocontact.survey.domain.SurveyResponse;
import com.ruoyi.nocontact.survey.service.ISurveyResponseService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开问卷填报Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/public/fill")
public class SurveyPublicFillController
{
    @Autowired
    private ISurveyResponseService responseService;

    @GetMapping("/{token}")
    public AjaxResult getFill(@PathVariable String token)
    {
        return AjaxResult.success(responseService.getFill(token));
    }

    @PostMapping("/{token}")
    public AjaxResult submit(@PathVariable String token, @RequestBody SurveyResponse response, HttpServletRequest request)
    {
        responseService.submitResponse(token, response, request.getRemoteAddr());
        return AjaxResult.success("提交成功");
    }
}
