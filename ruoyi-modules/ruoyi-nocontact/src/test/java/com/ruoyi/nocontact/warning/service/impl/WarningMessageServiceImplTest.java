package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.mapper.WarningMessageMapper;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningMessageServiceImplTest
{
    private WarningMessageServiceImpl service;

    @Mock
    private WarningMessageMapper messageMapper;

    @BeforeEach
    void setUp()
    {
        service = new WarningMessageServiceImpl();
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.remove();
    }

    @Test
    void pendingWarningCanMoveToProcessingAndWritesHandleLog()
    {
        when(messageMapper.selectMessageByScope(any(WarningMessage.class))).thenReturn(message("pending"));
        when(messageMapper.updateMessageStatus(any(WarningMessage.class))).thenReturn(1);

        int rows = service.updateMessageStatus(4001L, "processing", "开始处理", "admin");

        assertEquals(1, rows);
        verify(messageMapper).insertHandleLog(any());
    }

    @Test
    void processingWarningCanClose()
    {
        when(messageMapper.selectMessageByScope(any(WarningMessage.class))).thenReturn(message("processing"));
        when(messageMapper.updateMessageStatus(any(WarningMessage.class))).thenReturn(1);

        int rows = service.updateMessageStatus(4001L, "closed", "已核查", "admin");

        assertEquals(1, rows);
        ArgumentCaptor<WarningMessage> captor = ArgumentCaptor.forClass(WarningMessage.class);
        verify(messageMapper).updateMessageStatus(captor.capture());
        assertEquals("processing", captor.getValue().getExpectedStatus());
        verify(messageMapper).insertHandleLog(any());
    }

    @Test
    void concurrentStatusChangeDoesNotWriteHandleLog()
    {
        when(messageMapper.selectMessageByScope(any(WarningMessage.class))).thenReturn(message("pending"));
        when(messageMapper.updateMessageStatus(any(WarningMessage.class))).thenReturn(0);

        assertThrows(ServiceException.class,
                () -> service.updateMessageStatus(4001L, "processing", "开始处理", "admin"));

        verify(messageMapper).updateMessageStatus(any());
        verify(messageMapper, never()).insertHandleLog(any());
    }

    @Test
    void closedWarningCannotChangeAgain()
    {
        when(messageMapper.selectMessageByScope(any(WarningMessage.class))).thenReturn(message("closed"));

        assertThrows(ServiceException.class, () -> service.updateMessageStatus(4001L, "ignored", "忽略", "admin"));

        verify(messageMapper, never()).updateMessageStatus(any());
        verify(messageMapper, never()).insertHandleLog(any());
    }

    @Test
    void unsupportedStatusIsRejected()
    {
        when(messageMapper.selectMessageByScope(any(WarningMessage.class))).thenReturn(message("pending"));

        assertThrows(ServiceException.class, () -> service.updateMessageStatus(4001L, "handled", "旧状态", "admin"));

        verify(messageMapper, never()).updateMessageStatus(any());
    }

    @Test
    void nonAdminListIsScopedToCurrentUserDept()
    {
        loginAsScopedUser(2L, 88L, Constants.Dept.DATA_SCOPE_DEPT);
        when(messageMapper.selectMessageList(any(WarningMessage.class))).thenReturn(Collections.<WarningMessage>emptyList());

        service.selectMessageList(new WarningMessage());

        ArgumentCaptor<WarningMessage> captor = ArgumentCaptor.forClass(WarningMessage.class);
        verify(messageMapper).selectMessageList(captor.capture());
        assertTrue(String.valueOf(captor.getValue().getParams().get("dataScope")).contains("m.dept_id = 88"));
    }

    @Test
    void deleteRejectsMessageOutsideRoleDataScope()
    {
        loginAsScopedUser(2L, 88L, Constants.Dept.DATA_SCOPE_DEPT);
        SecurityContextHolder.setPermission("nocontact:warning:message:remove");
        when(messageMapper.selectMessageByScope(any(WarningMessage.class))).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.deleteMessageByIds(new Long[] {4001L}));

        verify(messageMapper, never()).deleteMessageByIds(any());
    }

    @Test
    void statusChangeIsTransactionalWithHandleLog() throws Exception
    {
        Method method = WarningMessageServiceImpl.class.getMethod("updateMessageStatus",
                Long.class, String.class, String.class, String.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertTrue(transactional != null);
        assertTrue(Arrays.asList(transactional.rollbackFor()).contains(Exception.class));
    }

    private WarningMessage message(String status)
    {
        WarningMessage message = new WarningMessage();
        message.setMessageId(4001L);
        message.setMessageStatus(status);
        return message;
    }

    private void loginAsScopedUser(Long userId, Long deptId, String dataScope)
    {
        SecurityContextHolder.setUserId(String.valueOf(userId));
        SecurityContextHolder.setUserName("scope-user");
        SecurityContextHolder.setPermission("nocontact:warning:message:list");
        SysRole role = new SysRole();
        role.setRoleId(20L);
        role.setDataScope(dataScope);
        role.setStatus(UserConstants.ROLE_NORMAL);
        role.setPermissions(new HashSet<String>(Arrays.asList("nocontact:warning:message:list", "nocontact:warning:message:query",
                "nocontact:warning:message:edit", "nocontact:warning:message:remove", "nocontact:warning:dashboard:query")));
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName("scope-user");
        user.setDeptId(deptId);
        user.setRoles(Collections.singletonList(role));
        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(user);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
    }
}
