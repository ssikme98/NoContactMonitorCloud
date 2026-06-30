package com.ruoyi.nocontact.support.service.impl;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.domain.R;
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
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.mapper.WarningMessageMapper;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.system.api.RemoteConfigService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportCenterServiceImplTest
{
    private SupportCenterServiceImpl service;

    @Mock
    private FusionCollectionTaskMapper taskMapper;

    @Mock
    private FusionCollectionBatchMapper batchMapper;

    @Mock
    private SurveyTaskMapper surveyTaskMapper;

    @Mock
    private WarningMessageMapper warningMessageMapper;

    @Mock
    private RectificationIssueMapper rectificationIssueMapper;

    @Mock
    private RemoteConfigService remoteConfigService;

    @BeforeEach
    void setUp()
    {
        service = new SupportCenterServiceImpl();
        ReflectionTestUtils.setField(service, "taskMapper", taskMapper);
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "surveyTaskMapper", surveyTaskMapper);
        ReflectionTestUtils.setField(service, "warningMessageMapper", warningMessageMapper);
        ReflectionTestUtils.setField(service, "rectificationIssueMapper", rectificationIssueMapper);
        ReflectionTestUtils.setField(service, "remoteConfigService", remoteConfigService);
        SecurityContextHolder.setUserName("admin");
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser());
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.remove();
    }

    @Test
    void listTodoSummaryAggregatesSixBuckets()
    {
        when(taskMapper.countTaskList(any(FusionCollectionTask.class))).thenReturn(2);
        when(surveyTaskMapper.countPendingTodoSamples(any(SurveyTask.class))).thenReturn(4);
        when(batchMapper.countBatchList(any(FusionCollectionBatch.class))).thenReturn(1);
        when(warningMessageMapper.countMessageList(any(WarningMessage.class))).thenReturn(3);
        when(rectificationIssueMapper.countIssueList(any(RectificationIssue.class)))
                .thenReturn(2)
                .thenReturn(1)
                .thenReturn(1)
                .thenReturn(2);

        List<SupportTodoItem> items = service.listTodoSummary();

        assertEquals(5, items.size());
        assertEquals("待填报", items.get(0).getTodoType());
        assertEquals(Integer.valueOf(6), items.get(0).getTodoCount());
        assertEquals("/support/todo?focus=fill", items.get(0).getJumpTarget());
        assertEquals("待审核", items.get(1).getTodoType());
        assertEquals("待处理预警", items.get(2).getTodoType());
        assertEquals("待整改", items.get(3).getTodoType());
        assertEquals(Integer.valueOf(4), items.get(3).getTodoCount());
        assertEquals("待复核", items.get(4).getTodoType());

        ArgumentCaptor<FusionCollectionTask> taskCaptor = ArgumentCaptor.forClass(FusionCollectionTask.class);
        verify(taskMapper).countTaskList(taskCaptor.capture());
        assertDeptScoped(taskCaptor.getValue().getParams().get("dataScope"), "u.dept_id = 88");

        ArgumentCaptor<SurveyTask> surveyCaptor = ArgumentCaptor.forClass(SurveyTask.class);
        verify(surveyTaskMapper).countPendingTodoSamples(surveyCaptor.capture());
        assertDeptScoped(surveyCaptor.getValue().getParams().get("dataScope"), "u.dept_id = 88");

        ArgumentCaptor<FusionCollectionBatch> batchCaptor = ArgumentCaptor.forClass(FusionCollectionBatch.class);
        verify(batchMapper).countBatchList(batchCaptor.capture());
        assertDeptScoped(batchCaptor.getValue().getParams().get("dataScope"), "b.dept_id = 88");

        ArgumentCaptor<WarningMessage> warningCaptor = ArgumentCaptor.forClass(WarningMessage.class);
        verify(warningMessageMapper).countMessageList(warningCaptor.capture());
        assertDeptScoped(warningCaptor.getValue().getParams().get("dataScope"), "m.dept_id = 88");

        ArgumentCaptor<RectificationIssue> rectificationCaptor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(rectificationIssueMapper, times(4)).countIssueList(rectificationCaptor.capture());
        for (RectificationIssue query : rectificationCaptor.getAllValues())
        {
            assertDeptScoped(query.getParams().get("dataScope"), "r.dept_id = 88");
        }
        assertEquals("pending_rectification", rectificationCaptor.getAllValues().get(0).getIssueStatus());
        assertEquals("rework", rectificationCaptor.getAllValues().get(1).getIssueStatus());
        assertEquals("rectifying", rectificationCaptor.getAllValues().get(2).getIssueStatus());
        assertEquals("pending_review", rectificationCaptor.getAllValues().get(3).getIssueStatus());
    }

    @Test
    void publicSettingsExposeFrontendMapKeysButHideGeocodeKey()
    {
        when(remoteConfigService.getConfigKey("nocontact.amap.frontendKey", SecurityConstants.INNER)).thenReturn(R.ok("frontend-key"));
        when(remoteConfigService.getConfigKey("nocontact.amap.securityJsCode", SecurityConstants.INNER)).thenReturn(R.ok("security-code"));
        when(remoteConfigService.getConfigKey("nocontact.file.basePath", SecurityConstants.INNER)).thenReturn(R.ok("/data/nocontact"));
        when(remoteConfigService.getConfigKey("nocontact.warning.pushEnabled", SecurityConstants.INNER)).thenReturn(R.ok("1"));
        when(remoteConfigService.getConfigKey("nocontact.report.defaultPeriod", SecurityConstants.INNER)).thenReturn(R.ok("quarter"));
        when(remoteConfigService.getConfigKey("nocontact.integration.globalEnabled", SecurityConstants.INNER)).thenReturn(R.ok("0"));

        SupportPublicSettings settings = service.loadPublicSettings();

        assertEquals("frontend-key", settings.getAmapFrontendKey());
        assertEquals("security-code", settings.getAmapSecurityJsCode());
        assertEquals("/data/nocontact", settings.getFileBasePath());
        assertEquals("1", settings.getWarningPushEnabled());
        assertEquals("quarter", settings.getReportDefaultPeriod());
        assertEquals("0", settings.getIntegrationGlobalEnabled());
    }

    @Test
    void publicSettingsFallbackToEmptyMapConfigWhenRemoteConfigUnavailable()
    {
        when(remoteConfigService.getConfigKey("nocontact.amap.frontendKey", SecurityConstants.INNER)).thenReturn(R.fail("404"));
        when(remoteConfigService.getConfigKey("nocontact.amap.securityJsCode", SecurityConstants.INNER)).thenReturn(R.fail("404"));
        when(remoteConfigService.getConfigKey("nocontact.file.basePath", SecurityConstants.INNER)).thenReturn(R.fail("404"));
        when(remoteConfigService.getConfigKey("nocontact.warning.pushEnabled", SecurityConstants.INNER)).thenReturn(R.fail("404"));
        when(remoteConfigService.getConfigKey("nocontact.report.defaultPeriod", SecurityConstants.INNER)).thenReturn(R.fail("404"));
        when(remoteConfigService.getConfigKey("nocontact.integration.globalEnabled", SecurityConstants.INNER)).thenReturn(R.fail("404"));

        SupportPublicSettings settings = service.loadPublicSettings();

        assertEquals("", settings.getAmapFrontendKey());
        assertEquals("", settings.getAmapSecurityJsCode());
        assertEquals("/data/nocontact", settings.getFileBasePath());
        assertEquals("1", settings.getWarningPushEnabled());
        assertEquals("quarter", settings.getReportDefaultPeriod());
        assertEquals("1", settings.getIntegrationGlobalEnabled());
    }

    private void assertDeptScoped(Object value, String deptFragment)
    {
        String scope = String.valueOf(value);
        assertTrue(scope.contains(deptFragment));
        assertFalse(scope.contains("create_by = 'scope-user'"));
    }

    private LoginUser loginUser()
    {
        SysRole role = new SysRole();
        role.setRoleId(20L);
        role.setDataScope(Constants.Dept.DATA_SCOPE_DEPT);
        role.setStatus(UserConstants.ROLE_NORMAL);
        role.setPermissions(new HashSet<String>(Collections.singletonList("nocontact:support:todo:list")));
        SysUser user = new SysUser();
        user.setUserId(2L);
        user.setUserName("scope-user");
        user.setDeptId(88L);
        user.setRoles(Collections.singletonList(role));
        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(user);
        return loginUser;
    }
}
