package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionTask;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionTaskMapper;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FusionCollectionTaskServiceImplTest
{
    private FusionCollectionTaskServiceImpl service;

    @Mock
    private FusionCollectionTaskMapper taskMapper;

    @BeforeEach
    void setUp()
    {
        service = new FusionCollectionTaskServiceImpl();
        ReflectionTestUtils.setField(service, "taskMapper", taskMapper);
        SecurityContextHolder.setUserName("scope-user");
        SecurityContextHolder.setPermission("nocontact:fusion:task:list");
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser());
    }

    @Test
    void selectTaskListAppliesCurrentUserDataScope()
    {
        when(taskMapper.selectTaskList(any(FusionCollectionTask.class))).thenReturn(Collections.<FusionCollectionTask>emptyList());

        service.selectTaskList(new FusionCollectionTask());

        ArgumentCaptor<FusionCollectionTask> captor = ArgumentCaptor.forClass(FusionCollectionTask.class);
        verify(taskMapper).selectTaskList(captor.capture());
        String dataScope = String.valueOf(captor.getValue().getParams().get("dataScope"));
        assertTrue(dataScope.contains("u.dept_id = 88"));
    }

    @Test
    void selectTaskSummaryAppliesCurrentUserDataScope()
    {
        SecurityContextHolder.setPermission("nocontact:fusion:task:query");
        when(taskMapper.selectTaskStatusStats(any(FusionCollectionTask.class))).thenReturn(Collections.emptyList());
        when(taskMapper.selectTaskTypeStats(any(FusionCollectionTask.class))).thenReturn(Collections.emptyList());

        service.selectTaskSummary();

        ArgumentCaptor<FusionCollectionTask> captor = ArgumentCaptor.forClass(FusionCollectionTask.class);
        verify(taskMapper).selectTaskStatusStats(captor.capture());
        String dataScope = String.valueOf(captor.getValue().getParams().get("dataScope"));
        assertTrue(dataScope.contains("u.dept_id = 88"));
    }

    @Test
    void selectTaskByIdReturnsNullWhenTaskOutOfScope()
    {
        when(taskMapper.selectTaskByScope(any(FusionCollectionTask.class))).thenReturn(null);

        FusionCollectionTask task = service.selectTaskById(12L);

        org.junit.jupiter.api.Assertions.assertNull(task);
    }

    @Test
    void updateTaskRejectsOutOfScopeTask()
    {
        when(taskMapper.selectTaskByScope(any(FusionCollectionTask.class))).thenReturn(null);
        FusionCollectionTask task = new FusionCollectionTask();
        task.setTaskId(12L);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.updateTask(task));

        assertEquals("采集任务不存在", exception.getMessage());
        verify(taskMapper, never()).updateTask(any(FusionCollectionTask.class));
    }

    @Test
    void updateTaskStatusRejectsOutOfScopeTask()
    {
        when(taskMapper.selectTaskByScope(any(FusionCollectionTask.class))).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateTaskStatus(12L, "running", "admin"));

        assertEquals("采集任务不存在", exception.getMessage());
        verify(taskMapper, never()).updateTaskStatus(any(FusionCollectionTask.class));
    }

    @Test
    void deleteTaskByIdsRejectsOutOfScopeTask()
    {
        when(taskMapper.selectTaskByScope(any(FusionCollectionTask.class))).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.deleteTaskByIds(new Long[] { 12L }));

        assertEquals("采集任务不存在", exception.getMessage());
        verify(taskMapper, never()).deleteTaskByIds(any(Long[].class));
    }

    private LoginUser loginUser()
    {
        SysRole role = new SysRole();
        role.setRoleId(20L);
        role.setDataScope(Constants.Dept.DATA_SCOPE_DEPT);
        role.setStatus(UserConstants.ROLE_NORMAL);
        role.setPermissions(new HashSet<String>(java.util.Arrays.asList("nocontact:fusion:task:list", "nocontact:fusion:task:query")));
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
