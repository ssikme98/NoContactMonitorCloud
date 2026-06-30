package com.ruoyi.nocontact.survey.service.impl;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.support.domain.BusinessMessage;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import com.ruoyi.nocontact.support.service.impl.BusinessMessageServiceImpl;
import com.ruoyi.nocontact.survey.domain.SurveyQuestionnaire;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSendRecord;
import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSample;
import com.ruoyi.nocontact.survey.domain.SurveyEnterprise;
import com.ruoyi.nocontact.survey.mapper.SurveyQuestionnaireMapper;
import com.ruoyi.nocontact.survey.mapper.SurveyTaskMapper;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyTaskServiceImplTest
{
    private SurveyTaskServiceImpl service;

    @Mock
    private SurveyTaskMapper taskMapper;

    @Mock
    private SurveyQuestionnaireMapper questionnaireMapper;

    @Mock
    private IBusinessMessageService businessMessageService;

    @BeforeEach
    void setUp()
    {
        service = new SurveyTaskServiceImpl();
        ReflectionTestUtils.setField(service, "taskMapper", taskMapper);
        ReflectionTestUtils.setField(service, "questionnaireMapper", questionnaireMapper);
        ReflectionTestUtils.setField(service, "businessMessageService", businessMessageService);
        SecurityContextHolder.setUserName("scope-user");
        SecurityContextHolder.setPermission("survey:task:list");
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser());
    }

    @Test
    void selectTaskListAppliesCurrentUserDataScope()
    {
        when(taskMapper.selectTaskList(any(SurveyTask.class))).thenReturn(Collections.<SurveyTask>emptyList());

        service.selectTaskList(new SurveyTask());

        ArgumentCaptor<SurveyTask> captor = ArgumentCaptor.forClass(SurveyTask.class);
        verify(taskMapper).selectTaskList(captor.capture());
        String dataScope = String.valueOf(captor.getValue().getParams().get("dataScope"));
        assertTrue(dataScope.contains("u.dept_id = 88"));
    }

    @Test
    void dispatchTaskPromotesPublishedQuestionnaireToCollecting()
    {
        SurveyTask task = new SurveyTask();
        task.setTaskId(8L);
        task.setTaskName("样本发卷任务");
        task.setQuestionnaireId(22L);
        task.setStatus("1");
        task.setSendChannels(new String[] { "sms" });
        SurveyTaskSample sample = new SurveyTaskSample();
        sample.setSampleId(66L);
        sample.setEnterpriseId(9L);
        sample.setEnterpriseName("长沙企业");
        sample.setContactPhone("13800000000");
        sample.setQrContent("/survey/fill?token=test");
        task.setSamples(Collections.singletonList(sample));
        SurveyQuestionnaire questionnaire = new SurveyQuestionnaire();
        questionnaire.setQuestionnaireId(22L);
        questionnaire.setStatus("1");

        when(taskMapper.selectTaskByScope(any(SurveyTask.class))).thenReturn(task);
        when(taskMapper.selectSamplesByTaskId(8L)).thenReturn(Collections.singletonList(sample));
        when(taskMapper.selectSendRecordsByTaskId(8L)).thenReturn(Collections.emptyList());
        when(taskMapper.updateTaskStatus(any(SurveyTask.class))).thenReturn(1);
        when(businessMessageService.createMessage(any(BusinessMessage.class))).thenReturn(1);

        int rows = service.dispatchTask(8L, "admin");

        assertEquals(1, rows);
        ArgumentCaptor<SurveyQuestionnaire> questionnaireCaptor = ArgumentCaptor.forClass(SurveyQuestionnaire.class);
        verify(questionnaireMapper).updateQuestionnaireStatus(questionnaireCaptor.capture());
        assertEquals(22L, questionnaireCaptor.getValue().getQuestionnaireId());
        assertEquals("3", questionnaireCaptor.getValue().getStatus());
        assertEquals("1", ReflectionTestUtils.getField(questionnaireCaptor.getValue(), "currentStatus"));
        assertEquals("admin", questionnaireCaptor.getValue().getUpdateBy());

        ArgumentCaptor<List> sendRecordCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskMapper).batchSendRecord(sendRecordCaptor.capture());
        SurveyTaskSendRecord sendRecord = (SurveyTaskSendRecord) sendRecordCaptor.getValue().get(0);
        assertEquals("0", sendRecord.getSendStatus());
        assertEquals("0", ReflectionTestUtils.getField(sendRecord, "submitStatus"));
        assertNull(ReflectionTestUtils.getField(sendRecord, "recoveryTime"));

        ArgumentCaptor<SurveyTask> updateCaptor = ArgumentCaptor.forClass(SurveyTask.class);
        verify(taskMapper).updateTaskStatus(updateCaptor.capture());
        assertEquals("1", updateCaptor.getValue().getExpectedStatus());

        ArgumentCaptor<BusinessMessage> messageCaptor = ArgumentCaptor.forClass(BusinessMessage.class);
        verify(businessMessageService).createMessage(messageCaptor.capture());
        assertEquals(BusinessMessageServiceImpl.SURVEY_DISPATCHED, messageCaptor.getValue().getMessageType());
        assertEquals("survey", messageCaptor.getValue().getBusinessType());
        assertEquals(8L, messageCaptor.getValue().getBusinessId());
        assertEquals("/survey/tracking?taskId=8", messageCaptor.getValue().getJumpTarget());
        assertEquals("admin", messageCaptor.getValue().getReceiverUserName());
    }

    @Test
    void insertTaskStoresSamplingBatchAndSnapshot()
    {
        SurveyTask task = new SurveyTask();
        task.setTaskName("抽样任务");
        task.setQuestionnaireId(22L);
        task.setSampleSource("group");
        task.setSamplingMethod("random");
        task.setSampleSize(1);
        task.setGroupId(5L);
        task.setSendChannels(new String[] { "sms", "site" });
        task.setCreateBy("admin");

        SurveyQuestionnaire questionnaire = new SurveyQuestionnaire();
        questionnaire.setQuestionnaireId(22L);
        questionnaire.setStatus("1");

        SurveyEnterprise enterprise = new SurveyEnterprise();
        enterprise.setEnterpriseId(9L);
        enterprise.setEnterpriseName("长沙企业");
        enterprise.setCreditCode("914301001234567890");
        enterprise.setRegionName("长沙市");
        enterprise.setIndustryCategory("电子信息");
        enterprise.setEnterpriseScale("中型");
        enterprise.setContactPhone("13800000000");

        when(questionnaireMapper.selectQuestionnaireById(22L)).thenReturn(questionnaire);
        when(taskMapper.selectEnterprisePool(any(SurveyTask.class))).thenReturn(Collections.singletonList(enterprise));
        when(taskMapper.insertTask(any(SurveyTask.class))).thenAnswer(invocation -> {
            SurveyTask inserted = invocation.getArgument(0);
            inserted.setTaskId(99L);
            return 1;
        });

        service.insertTask(task);

        ArgumentCaptor<SurveyTask> taskCaptor = ArgumentCaptor.forClass(SurveyTask.class);
        verify(taskMapper).insertTask(taskCaptor.capture());
        SurveyTask inserted = taskCaptor.getValue();
        assertNotNull(ReflectionTestUtils.getField(inserted, "samplingBatchNo"));
        assertNotNull(ReflectionTestUtils.getField(inserted, "samplingBatchTime"));
        String snapshot = (String) ReflectionTestUtils.getField(inserted, "samplingFilterSnapshot");
        assertTrue(snapshot.contains("\"sampleSource\":\"group\""));
        assertTrue(snapshot.contains("\"groupId\":5"));
        assertTrue(snapshot.contains("\"samplingMethod\":\"random\""));
        assertTrue(snapshot.contains("\"selectedEnterpriseIds\":[9]"));
        assertTrue(snapshot.contains("长沙企业"));
        verify(taskMapper).insertTaskSample(any(SurveyTaskSample.class));
    }

    @Test
    void dispatchTaskFailsWhenBusinessMessageCreationFails()
    {
        SurveyTask task = new SurveyTask();
        task.setTaskId(8L);
        task.setTaskName("样本发卷任务");
        task.setQuestionnaireId(22L);
        task.setStatus("1");
        task.setSendChannels(new String[] { "sms" });
        SurveyTaskSample sample = new SurveyTaskSample();
        sample.setSampleId(66L);
        sample.setEnterpriseId(9L);
        sample.setEnterpriseName("长沙企业");
        sample.setContactPhone("13800000000");
        sample.setQrContent("/survey/fill?token=test");
        task.setSamples(Collections.singletonList(sample));

        when(taskMapper.selectTaskByScope(any(SurveyTask.class))).thenReturn(task);
        when(taskMapper.selectSamplesByTaskId(8L)).thenReturn(Collections.singletonList(sample));
        when(taskMapper.selectSendRecordsByTaskId(8L)).thenReturn(Collections.emptyList());
        when(taskMapper.updateTaskStatus(any(SurveyTask.class))).thenReturn(1);
        doThrow(new RuntimeException("message failure")).when(businessMessageService).createMessage(any(BusinessMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.dispatchTask(8L, "admin"));

        assertEquals("message failure", exception.getMessage());
    }

    @Test
    void selectTaskByIdReturnsNullWhenTaskOutOfScope()
    {
        when(taskMapper.selectTaskByScope(any(SurveyTask.class))).thenReturn(null);

        SurveyTask task = service.selectTaskById(8L);

        assertNull(task);
    }

    @Test
    void deleteTaskByIdsRejectsOutOfScopeTask()
    {
        when(taskMapper.selectTaskByScope(any(SurveyTask.class))).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.deleteTaskByIds(new Long[] { 8L }));

        assertEquals("调研任务不存在", exception.getMessage());
    }

    @Test
    void trackingSummaryRejectsOutOfScopeTask()
    {
        when(taskMapper.selectTaskByScope(any(SurveyTask.class))).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.selectTrackingSummary(8L));

        assertEquals("调研任务不存在", exception.getMessage());
    }

    private LoginUser loginUser()
    {
        SysRole role = new SysRole();
        role.setRoleId(20L);
        role.setDataScope(Constants.Dept.DATA_SCOPE_DEPT);
        role.setStatus(UserConstants.ROLE_NORMAL);
        role.setPermissions(new HashSet<String>(Collections.singletonList("survey:task:list")));
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
