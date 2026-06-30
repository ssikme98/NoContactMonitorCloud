package com.ruoyi.nocontact.support.service.impl;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.nocontact.common.NocontactDataScopeHelper;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionTask;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionTaskMapper;
import com.ruoyi.nocontact.rectification.domain.RectificationIssue;
import com.ruoyi.nocontact.rectification.mapper.RectificationIssueMapper;
import com.ruoyi.nocontact.support.domain.SupportPublicSettings;
import com.ruoyi.nocontact.support.domain.SupportTodoItem;
import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.mapper.SurveyTaskMapper;
import com.ruoyi.nocontact.support.service.IAmapGeocodeService;
import com.ruoyi.nocontact.support.service.ISupportCenterService;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.mapper.WarningMessageMapper;
import com.ruoyi.system.api.RemoteConfigService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportCenterServiceImpl implements ISupportCenterService
{
    private static final String DEFAULT_AMAP_FRONTEND_KEY = "";
    private static final String DEFAULT_AMAP_SECURITY_JS_CODE = "";
    private static final String DEFAULT_FILE_BASE_PATH = "/data/nocontact";
    private static final String DEFAULT_WARNING_PUSH_ENABLED = "1";
    private static final String DEFAULT_REPORT_DEFAULT_PERIOD = "quarter";
    private static final String DEFAULT_INTEGRATION_GLOBAL_ENABLED = "1";

    @Autowired
    private FusionCollectionTaskMapper taskMapper;

    @Autowired
    private FusionCollectionBatchMapper batchMapper;

    @Autowired
    private SurveyTaskMapper surveyTaskMapper;

    @Autowired
    private WarningMessageMapper warningMessageMapper;

    @Autowired
    private RectificationIssueMapper rectificationIssueMapper;

    @Autowired
    private RemoteConfigService remoteConfigService;

    @Autowired
    private IAmapGeocodeService amapGeocodeService;

    @Override
    public List<SupportTodoItem> listTodoSummary()
    {
        List<SupportTodoItem> items = new ArrayList<SupportTodoItem>();

        FusionCollectionTask taskQuery = new FusionCollectionTask();
        taskQuery.setTaskType("fill");
        taskQuery.setTaskStatus("published");
        NocontactDataScopeHelper.applyDataScope(taskQuery, "u", "dept_id", "t", "create_by");
        int pendingFillCount = taskMapper.countTaskList(taskQuery);
        SurveyTask surveyQuery = new SurveyTask();
        NocontactDataScopeHelper.applyDataScope(surveyQuery, "u", "dept_id", "t", "create_by");
        pendingFillCount += surveyTaskMapper.countPendingTodoSamples(surveyQuery);
        items.add(new SupportTodoItem("待填报", "待填报", pendingFillCount, "/support/todo?focus=fill"));

        FusionCollectionBatch batchQuery = new FusionCollectionBatch();
        batchQuery.setBatchStatus("pending_audit");
        NocontactDataScopeHelper.applyDataScope(batchQuery, "b", "dept_id", "b", "create_by");
        items.add(new SupportTodoItem("待审核", "待审核", batchMapper.countBatchList(batchQuery), "/collection/audit"));

        WarningMessage warningQuery = new WarningMessage();
        warningQuery.setMessageStatus("pending");
        NocontactDataScopeHelper.applyDataScope(warningQuery, "m", "dept_id", "m", "create_by");
        items.add(new SupportTodoItem("待处理预警", "待处理预警", warningMessageMapper.countMessageList(warningQuery), "/warning/message"));

        RectificationIssue rectificationQuery = new RectificationIssue();
        rectificationQuery.setIssueStatus("pending_rectification");
        NocontactDataScopeHelper.applyDataScope(rectificationQuery, "r", "dept_id", "r", "create_by");
        int pendingRectificationCount = rectificationIssueMapper.countIssueList(rectificationQuery);
        RectificationIssue reworkQuery = new RectificationIssue();
        reworkQuery.setIssueStatus("rework");
        NocontactDataScopeHelper.applyDataScope(reworkQuery, "r", "dept_id", "r", "create_by");
        pendingRectificationCount += rectificationIssueMapper.countIssueList(reworkQuery);
        RectificationIssue rectifyingQuery = new RectificationIssue();
        rectifyingQuery.setIssueStatus("rectifying");
        NocontactDataScopeHelper.applyDataScope(rectifyingQuery, "r", "dept_id", "r", "create_by");
        pendingRectificationCount += rectificationIssueMapper.countIssueList(rectifyingQuery);
        items.add(new SupportTodoItem("待整改", "待整改", pendingRectificationCount, "/rectification/issue"));

        RectificationIssue reviewQuery = new RectificationIssue();
        reviewQuery.setIssueStatus("pending_review");
        NocontactDataScopeHelper.applyDataScope(reviewQuery, "r", "dept_id", "r", "create_by");
        items.add(new SupportTodoItem("待复核", "待复核", rectificationIssueMapper.countIssueList(reviewQuery), "/rectification/issue"));

        return items;
    }

    @Override
    public SupportPublicSettings loadPublicSettings()
    {
        SupportPublicSettings settings = new SupportPublicSettings();
        settings.setAmapFrontendKey(selectConfig("nocontact.amap.frontendKey", DEFAULT_AMAP_FRONTEND_KEY));
        settings.setAmapSecurityJsCode(selectConfig("nocontact.amap.securityJsCode", DEFAULT_AMAP_SECURITY_JS_CODE));
        settings.setFileBasePath(selectConfig("nocontact.file.basePath", DEFAULT_FILE_BASE_PATH));
        settings.setWarningPushEnabled(selectConfig("nocontact.warning.pushEnabled", DEFAULT_WARNING_PUSH_ENABLED));
        settings.setReportDefaultPeriod(selectConfig("nocontact.report.defaultPeriod", DEFAULT_REPORT_DEFAULT_PERIOD));
        settings.setIntegrationGlobalEnabled(selectConfig("nocontact.integration.globalEnabled", DEFAULT_INTEGRATION_GLOBAL_ENABLED));
        return settings;
    }

    @Override
    public AjaxResult geocodeEnterprise(String regionName, String address)
    {
        return amapGeocodeService.geocodeEnterprise(regionName, address);
    }

    private String selectConfig(String configKey, String defaultValue)
    {
        R<String> response = remoteConfigService.getConfigKey(configKey, SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            return defaultValue;
        }
        String value = response.getData();
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
