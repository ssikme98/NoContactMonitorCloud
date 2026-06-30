package com.ruoyi.nocontact.rectification.service.impl;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.rectification.domain.RectificationIssue;
import com.ruoyi.nocontact.rectification.mapper.RectificationIssueMapper;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.service.IWarningMessageService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RectificationIssueServiceImplTest
{
    private RectificationIssueServiceImpl service;

    @Mock
    private RectificationIssueMapper issueMapper;

    @Mock
    private IWarningMessageService warningMessageService;

    @Mock
    private IBusinessMessageService businessMessageService;

    @BeforeEach
    void setUp()
    {
        service = new RectificationIssueServiceImpl();
        ReflectionTestUtils.setField(service, "issueMapper", issueMapper);
        ReflectionTestUtils.setField(service, "warningMessageService", warningMessageService);
        ReflectionTestUtils.setField(service, "businessMessageService", businessMessageService);
        SecurityContextHolder.setUserName("scope-user");
        SecurityContextHolder.setPermission("nocontact:rectification:issue:list");
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser());
    }

    @Test
    void dispatchFromPendingCreatesPendingRectification()
    {
        when(issueMapper.selectIssueByScope(any(RectificationIssue.class))).thenReturn(issue("pending_dispatch", null));
        when(issueMapper.updateIssueStatus(any(RectificationIssue.class))).thenReturn(1);

        RectificationIssue form = new RectificationIssue();
        form.setResponsibleUnitName("省数据局");

        int rows = service.dispatch(101L, form, "admin");

        assertEquals(1, rows);
        ArgumentCaptor<RectificationIssue> captor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(issueMapper).updateIssueStatus(captor.capture());
        assertEquals("pending_rectification", captor.getValue().getIssueStatus());
        verify(issueMapper).insertLog(any());
    }

    @Test
    void dispatchFromRejectedCreatesRework()
    {
        when(issueMapper.selectIssueByScope(any(RectificationIssue.class))).thenReturn(issue("rejected", null));
        when(issueMapper.updateIssueStatus(any(RectificationIssue.class))).thenReturn(1);

        int rows = service.dispatch(101L, new RectificationIssue(), "admin");

        assertEquals(1, rows);
        ArgumentCaptor<RectificationIssue> captor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(issueMapper).updateIssueStatus(captor.capture());
        assertEquals("rework", captor.getValue().getIssueStatus());
    }

    @Test
    void startFromPendingRectificationMovesToRectifying()
    {
        when(issueMapper.selectIssueByScope(any(RectificationIssue.class))).thenReturn(issue("pending_rectification", null));
        when(issueMapper.updateIssueStatus(any(RectificationIssue.class))).thenReturn(1);

        int rows = service.start(101L, "admin");

        assertEquals(1, rows);
        ArgumentCaptor<RectificationIssue> captor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(issueMapper).updateIssueStatus(captor.capture());
        assertEquals("rectifying", captor.getValue().getIssueStatus());
        assertEquals("pending_rectification", captor.getValue().getExpectedStatus());
        verify(issueMapper).insertLog(any());
    }

    @Test
    void reviewApprovedClosesIssueAndLinkedWarning()
    {
        when(issueMapper.selectIssueByScope(any(RectificationIssue.class))).thenReturn(issue("pending_review", 4001L));
        when(issueMapper.updateIssueStatus(any(RectificationIssue.class))).thenReturn(1);

        RectificationIssue form = new RectificationIssue();
        form.setReviewOpinion("整改完成");

        int rows = service.review(101L, true, form, "reviewer");

        assertEquals(1, rows);
        ArgumentCaptor<RectificationIssue> captor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(issueMapper).updateIssueStatus(captor.capture());
        assertEquals("closed", captor.getValue().getIssueStatus());
        verify(warningMessageService).closeFromRectification(4001L, "整改完成", "reviewer");
        verify(issueMapper).insertLog(any());
    }

    @Test
    void updateIssueRejectsOutOfScopeIssue()
    {
        when(issueMapper.selectIssueByScope(any(RectificationIssue.class))).thenReturn(null);
        RectificationIssue form = new RectificationIssue();
        form.setIssueId(101L);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.updateIssue(form, "admin"));

        assertEquals("整改问题不存在", exception.getMessage());
        verify(issueMapper, never()).updateIssue(any(RectificationIssue.class));
    }

    @Test
    void deleteIssueByIdsRejectsOutOfScopeIssue()
    {
        when(issueMapper.selectIssueByScope(any(RectificationIssue.class))).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.deleteIssueByIds(new Long[] { 101L }));

        assertEquals("整改问题不存在", exception.getMessage());
        verify(issueMapper, never()).deleteIssueByIds(any(Long[].class));
    }

    @Test
    void dispatchFailsWhenIssueStatusChangedConcurrently()
    {
        when(issueMapper.selectIssueByScope(any(RectificationIssue.class))).thenReturn(issue("pending_dispatch", null));
        when(issueMapper.updateIssueStatus(any(RectificationIssue.class))).thenReturn(0);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.dispatch(101L, new RectificationIssue(), "admin"));

        assertEquals("整改问题状态已变化，请刷新后重试", exception.getMessage());
        verify(issueMapper, never()).insertLog(any());
    }

    @Test
    void createFromWarningCreatesPendingDispatchIssueAndPromotesWarning()
    {
        when(warningMessageService.selectMessageById(4001L)).thenReturn(warning("pending"));
        when(issueMapper.selectActiveIssueBySourceWarningId(4001L)).thenReturn(null);
        when(issueMapper.insertIssue(any(RectificationIssue.class))).thenAnswer(invocation -> {
            RectificationIssue issue = invocation.getArgument(0);
            issue.setIssueId(201L);
            return 1;
        });

        RectificationIssue form = new RectificationIssue();
        form.setIssueTitle("监测工作开展情况整改");
        form.setDeadline(new java.util.Date());
        form.setIssueDescription("需要核查逾期未报原因");

        int rows = service.createFromWarning(4001L, form, "admin");

        assertEquals(1, rows);
        ArgumentCaptor<RectificationIssue> captor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(issueMapper).insertIssue(captor.capture());
        assertEquals("warning", captor.getValue().getSourceType());
        assertEquals(Long.valueOf(4001L), captor.getValue().getSourceWarningId());
        assertEquals(Long.valueOf(200L), captor.getValue().getDeptId());
        assertEquals("pending_dispatch", captor.getValue().getIssueStatus());
        assertEquals("监测工作开展情况", captor.getValue().getIndicatorName());
        verify(warningMessageService).updateMessageStatus(4001L, "processing", "已转入问题整改流程", "admin");
        verify(issueMapper).insertLog(any());
    }

    @Test
    void createFromWarningRejectsDuplicateOpenIssue()
    {
        when(warningMessageService.selectMessageById(4001L)).thenReturn(warning("processing"));
        when(issueMapper.selectActiveIssueBySourceWarningId(4001L)).thenReturn(issue("pending_rectification", 4001L));

        RectificationIssue form = new RectificationIssue();
        form.setIssueTitle("重复整改");

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.createFromWarning(4001L, form, "admin"));

        assertEquals("该预警已存在未关闭的整改问题", exception.getMessage());
        verify(issueMapper, never()).insertIssue(any());
        verify(warningMessageService, never()).updateMessageStatus(eq(4001L), eq("processing"), any(), eq("admin"));
    }

    @Test
    void createFromProcessingWarningDoesNotRepeatStatusTransition()
    {
        when(warningMessageService.selectMessageById(4001L)).thenReturn(warning("processing"));
        when(issueMapper.selectActiveIssueBySourceWarningId(4001L)).thenReturn(null);
        when(issueMapper.insertIssue(any(RectificationIssue.class))).thenAnswer(invocation -> {
            RectificationIssue issue = invocation.getArgument(0);
            issue.setIssueId(202L);
            return 1;
        });

        RectificationIssue form = new RectificationIssue();
        form.setIssueTitle("处理中预警整改");

        int rows = service.createFromWarning(4001L, form, "admin");

        assertEquals(1, rows);
        verify(issueMapper).insertIssue(any(RectificationIssue.class));
        verify(warningMessageService, never()).updateMessageStatus(eq(4001L), eq("processing"), any(), eq("admin"));
    }

    @Test
    void insertIssueUsesCurrentUserDeptId()
    {
        RectificationIssue issue = new RectificationIssue();
        issue.setIssueTitle("手工整改问题");
        when(issueMapper.insertIssue(any(RectificationIssue.class))).thenReturn(1);

        int rows = service.insertIssue(issue, "admin");

        assertEquals(1, rows);
        ArgumentCaptor<RectificationIssue> captor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(issueMapper).insertIssue(captor.capture());
        assertEquals(Long.valueOf(88L), captor.getValue().getDeptId());
    }

    @Test
    void selectIssueListAppliesDeptDataScope()
    {
        when(issueMapper.selectIssueList(any(RectificationIssue.class))).thenReturn(Collections.singletonList(issue("pending_dispatch", null)));

        service.selectIssueList(new RectificationIssue());

        ArgumentCaptor<RectificationIssue> captor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(issueMapper).selectIssueList(captor.capture());
        String dataScope = String.valueOf(captor.getValue().getParams().get("dataScope"));
        org.junit.jupiter.api.Assertions.assertTrue(dataScope.contains("r.dept_id = 88"));
    }

    @Test
    void dashboardAppliesDeptDataScope()
    {
        when(issueMapper.selectStatusStats(any(RectificationIssue.class))).thenReturn(Collections.emptyList());
        when(issueMapper.selectRegionStats(any(RectificationIssue.class))).thenReturn(Collections.emptyList());

        service.dashboard();

        ArgumentCaptor<RectificationIssue> captor = ArgumentCaptor.forClass(RectificationIssue.class);
        verify(issueMapper).selectStatusStats(captor.capture());
        String dataScope = String.valueOf(captor.getValue().getParams().get("dataScope"));
        org.junit.jupiter.api.Assertions.assertTrue(dataScope.contains("r.dept_id = 88"));
    }

    private RectificationIssue issue(String status, Long sourceWarningId)
    {
        RectificationIssue issue = new RectificationIssue();
        issue.setIssueId(101L);
        issue.setIssueStatus(status);
        issue.setSourceWarningId(sourceWarningId);
        return issue;
    }

    private WarningMessage warning(String status)
    {
        WarningMessage message = new WarningMessage();
        message.setMessageId(4001L);
        message.setMessageStatus(status);
        message.setDeptId(200L);
        message.setRuleName("监测数据逾期未报红色预警");
        message.setIndicatorId(9001L);
        message.setIndicatorName("监测工作开展情况");
        message.setWarningLevel("1");
        message.setRegionName("省直单位");
        message.setResponsibleUnitName("省数据局");
        message.setCurrentValue(new java.math.BigDecimal("0"));
        message.setThresholdValue(new java.math.BigDecimal("1"));
        return message;
    }

    private LoginUser loginUser()
    {
        SysRole role = new SysRole();
        role.setRoleId(2L);
        role.setStatus("0");
        role.setDataScope("3");
        role.setPermissions(Collections.singleton("nocontact:rectification:issue:list"));
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
