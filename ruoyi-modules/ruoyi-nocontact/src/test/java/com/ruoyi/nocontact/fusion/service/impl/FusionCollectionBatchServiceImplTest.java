package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionImportFailure;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionImportRow;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.fusion.mapper.FusionIndicatorMapper;
import com.ruoyi.nocontact.warning.service.IWarningEvaluationService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FusionCollectionBatchServiceImplTest
{
    private FusionCollectionBatchServiceImpl service;

    @Mock
    private FusionCollectionBatchMapper batchMapper;

    @Mock
    private FusionIndicatorMapper indicatorMapper;

    @Mock
    private IWarningEvaluationService warningEvaluationService;

    @BeforeEach
    void setUp()
    {
        service = new FusionCollectionBatchServiceImpl();
        ReflectionTestUtils.setField(service, "batchMapper", batchMapper);
        ReflectionTestUtils.setField(service, "indicatorMapper", indicatorMapper);
        ReflectionTestUtils.setField(service, "warningEvaluationService", warningEvaluationService);
        lenient().when(batchMapper.selectDeptNameById(200L)).thenReturn("省数据局");
        loginAsAdmin();
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.remove();
    }

    @Test
    void detailIncludesItemsAndAuditLogs()
    {
        FusionCollectionBatch batch = batch("pending_audit");
        when(batchMapper.selectBatchByScope(any(FusionCollectionBatch.class))).thenReturn(batch);
        when(batchMapper.selectItemsByBatchId(5001L)).thenReturn(Collections.emptyList());
        when(batchMapper.selectAuditLogsByBatchId(5001L)).thenReturn(Collections.emptyList());

        FusionCollectionBatch result = service.selectBatchById(5001L);

        assertEquals(Collections.emptyList(), result.getItems());
        assertEquals(Collections.emptyList(), result.getAuditLogs());
    }

    @Test
    void approvingPendingBatchUpdatesStatusAndTriggersWarningEvaluation()
    {
        when(batchMapper.selectBatchByScope(any(FusionCollectionBatch.class))).thenReturn(batch("pending_audit"));
        when(batchMapper.updateBatchStatus(any(FusionCollectionBatch.class))).thenReturn(1);

        int rows = service.approveBatch(5001L, "通过", "admin");

        assertEquals(1, rows);
        ArgumentCaptor<FusionCollectionBatch> captor = ArgumentCaptor.forClass(FusionCollectionBatch.class);
        verify(batchMapper).updateBatchStatus(captor.capture());
        assertEquals("pending_audit", captor.getValue().getExpectedStatus());
        verify(batchMapper).insertAuditLog(any());
        verify(warningEvaluationService).evaluateApprovedBatch(5001L, "admin");
    }

    @Test
    void approvingNonPendingBatchIsRejected()
    {
        when(batchMapper.selectBatchByScope(any(FusionCollectionBatch.class))).thenReturn(batch("approved"));

        assertThrows(ServiceException.class, () -> service.approveBatch(5001L, "重复审核", "admin"));

        verify(batchMapper, never()).updateBatchStatus(any());
        verify(warningEvaluationService, never()).evaluateApprovedBatch(5001L, "admin");
    }

    @Test
    void approvingConcurrentStatusChangeDoesNotWriteAuditLogOrEvaluate()
    {
        when(batchMapper.selectBatchByScope(any(FusionCollectionBatch.class))).thenReturn(batch("pending_audit"));
        when(batchMapper.updateBatchStatus(any(FusionCollectionBatch.class))).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.approveBatch(5001L, "通过", "admin"));

        verify(batchMapper, never()).insertAuditLog(any());
        verify(warningEvaluationService, never()).evaluateApprovedBatch(5001L, "admin");
    }

    @Test
    void rejectingPendingBatchDoesNotTriggerWarningEvaluation()
    {
        when(batchMapper.selectBatchByScope(any(FusionCollectionBatch.class))).thenReturn(batch("pending_audit"));
        when(batchMapper.updateBatchStatus(any(FusionCollectionBatch.class))).thenReturn(1);

        int rows = service.rejectBatch(5001L, "数据缺失", "admin");

        assertEquals(1, rows);
        verify(batchMapper).insertAuditLog(any());
        verify(warningEvaluationService, never()).evaluateApprovedBatch(5001L, "admin");
    }

    @Test
    void rejectingConcurrentStatusChangeDoesNotWriteAuditLog()
    {
        when(batchMapper.selectBatchByScope(any(FusionCollectionBatch.class))).thenReturn(batch("pending_audit"));
        when(batchMapper.updateBatchStatus(any(FusionCollectionBatch.class))).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.rejectBatch(5001L, "重复驳回", "admin"));

        verify(batchMapper, never()).insertAuditLog(any());
        verify(warningEvaluationService, never()).evaluateApprovedBatch(5001L, "admin");
    }

    @Test
    void importCollectionCreatesPendingBatchAndPersistsFailures()
    {
        when(indicatorMapper.selectEnabledIndicatorByCode("NC-1003")).thenReturn(indicator());
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(indicator());
        when(batchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch batch = invocation.getArgument(0);
            batch.setBatchId(7001L);
            return 1;
        });

        Map<String, Object> result = service.importCollection(Arrays.asList(validRow(), invalidNumberRow()), "admin");

        assertEquals(1, result.get("successRows"));
        assertEquals(1, result.get("failureRows"));
        assertEquals(1, result.get("batchCount"));
        verify(batchMapper).insertImportFailure(any(FusionCollectionImportFailure.class));
        verify(batchMapper).insertBatch(any(FusionCollectionBatch.class));
        verify(batchMapper).insertItem(any(FusionCollectionItem.class));
    }

    @Test
    void importByCodeUsesEnabledIndicatorVersion()
    {
        when(indicatorMapper.selectEnabledIndicatorByCode("NC-1003")).thenReturn(indicator());
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(indicator());
        when(batchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch batch = invocation.getArgument(0);
            batch.setBatchId(7001L);
            return 1;
        });

        service.importCollection(Collections.singletonList(validRow()), "admin");

        verify(indicatorMapper).selectEnabledIndicatorByCode("NC-1003");
        verify(indicatorMapper, never()).selectIndicatorByCode("NC-1003");
    }

    @Test
    void importCollectionBackfillsResponsibleUnitNameFromDept()
    {
        FusionCollectionImportRow row = validRow();
        row.setResponsibleUnitName("");
        when(indicatorMapper.selectEnabledIndicatorByCode("NC-1003")).thenReturn(indicator());
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(indicator());
        when(batchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch batch = invocation.getArgument(0);
            batch.setBatchId(7001L);
            return 1;
        });

        Map<String, Object> result = service.importCollection(Collections.singletonList(row), "admin");

        assertEquals(1, result.get("successRows"));
        ArgumentCaptor<FusionCollectionBatch> batchCaptor = ArgumentCaptor.forClass(FusionCollectionBatch.class);
        verify(batchMapper).insertBatch(batchCaptor.capture());
        assertEquals("省数据局", batchCaptor.getValue().getResponsibleUnitName());
    }

    @Test
    void importCollectionRecordsRegionPairErrorAsRowFailure()
    {
        FusionCollectionImportRow row = validRow();
        row.setRegionName("");

        Map<String, Object> result = service.importCollection(Collections.singletonList(row), "admin");

        assertEquals(0, result.get("successRows"));
        assertEquals(1, result.get("failureRows"));
        verify(batchMapper).insertImportFailure(any(FusionCollectionImportFailure.class));
        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void importByIdRejectsDisabledIndicatorVersion()
    {
        FusionCollectionImportRow row = validRow();
        row.setIndicatorId(1003L);
        row.setIndicatorCode(null);
        FusionIndicator disabled = indicator();
        disabled.setStatus("1");
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(disabled);

        Map<String, Object> result = service.importCollection(Collections.singletonList(row), "admin");

        assertEquals(0, result.get("successRows"));
        assertEquals(1, result.get("failureRows"));
        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
        verify(batchMapper).insertImportFailure(any(FusionCollectionImportFailure.class));
    }

    @Test
    void importByIdAndCodeRequiresSameIndicatorCode()
    {
        FusionCollectionImportRow row = validRow();
        row.setIndicatorId(1003L);
        row.setIndicatorCode("NC-1003");
        FusionIndicator indicator = indicator();
        indicator.setIndicatorCode(null);
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(indicator);

        Map<String, Object> result = service.importCollection(Collections.singletonList(row), "admin");

        assertEquals(0, result.get("successRows"));
        assertEquals(1, result.get("failureRows"));
        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void importFailureBelongsToImporterDeptWhenResponsibilityDeptIsOutOfScope()
    {
        loginAsScopedUser(2L, 88L, Constants.Dept.DATA_SCOPE_DEPT);
        FusionCollectionImportRow row = validRow();
        row.setResponsibleUnitId(999L);
        row.setResponsibleUnitName("外部单位");
        when(batchMapper.countDeptInScope(any(FusionCollectionBatch.class))).thenReturn(0);

        service.importCollection(Collections.singletonList(row), "scope-user");

        ArgumentCaptor<FusionCollectionImportFailure> captor =
                ArgumentCaptor.forClass(FusionCollectionImportFailure.class);
        verify(batchMapper).insertImportFailure(captor.capture());
        assertEquals(Long.valueOf(88L), captor.getValue().getDeptId());
    }

    @Test
    void submitBatchRejectsResponsibilityDeptOutsideWritableScope()
    {
        loginAsScopedUser(2L, 88L, Constants.Dept.DATA_SCOPE_DEPT);
        when(batchMapper.countDeptInScope(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch scoped = invocation.getArgument(0);
            return Long.valueOf(88L).equals(scoped.getDeptId()) ? 1 : 0;
        });
        FusionCollectionBatch batch = batch("draft");
        batch.setDeptId(88L);
        batch.setResponsibleUnitId(999L);
        batch.setResponsibleUnitName("外部单位");
        batch.setItems(Collections.<FusionCollectionItem>emptyList());

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "scope-user"));

        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void submitBatchDoesNotUseAllScopeRoleWithoutCurrentPermission()
    {
        SecurityContextHolder.setUserId("2");
        SecurityContextHolder.setUserName("scope-user");
        SecurityContextHolder.setPermission("nocontact:fusion:collection:add");
        SysRole collectionRole = new SysRole();
        collectionRole.setRoleId(20L);
        collectionRole.setDataScope(Constants.Dept.DATA_SCOPE_DEPT);
        collectionRole.setStatus(UserConstants.ROLE_NORMAL);
        collectionRole.setPermissions(new HashSet<String>(Collections.singletonList("nocontact:fusion:collection:add")));
        SysRole unrelatedAllRole = new SysRole();
        unrelatedAllRole.setRoleId(21L);
        unrelatedAllRole.setDataScope(Constants.Dept.DATA_SCOPE_ALL);
        unrelatedAllRole.setStatus(UserConstants.ROLE_NORMAL);
        unrelatedAllRole.setPermissions(new HashSet<String>(Collections.singletonList("nocontact:warning:message:list")));
        SysUser user = new SysUser();
        user.setUserId(2L);
        user.setUserName("scope-user");
        user.setDeptId(88L);
        user.setRoles(Arrays.asList(collectionRole, unrelatedAllRole));
        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(user);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        when(batchMapper.countDeptInScope(any(FusionCollectionBatch.class))).thenReturn(0);
        FusionCollectionBatch batch = writableBatch();
        batch.setResponsibleUnitId(999L);
        batch.setResponsibleUnitName("外部单位");

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "scope-user"));

        verify(indicatorMapper, never()).selectIndicatorById(1003L);
        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void submitBatchRejectsDisabledItemIndicatorVersion()
    {
        FusionCollectionBatch batch = writableBatch();
        FusionIndicator disabled = indicator();
        disabled.setStatus("1");
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(disabled);

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "admin"));

        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void submitBatchRequiresLoginContextForWritableScope()
    {
        SecurityContextHolder.remove();
        FusionCollectionBatch batch = writableBatch();

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "system"));

        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void submitBatchNormalizesPeriodAndNumberValue()
    {
        FusionCollectionBatch batch = writableBatch();
        batch.setResponsibleUnitName("客户端伪造单位");
        batch.setPeriodQuarter(2);
        batch.getItems().get(0).setRawValue("abc");
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(indicator());
        when(batchMapper.insertBatch(any(FusionCollectionBatch.class))).thenAnswer(invocation -> {
            FusionCollectionBatch inserted = invocation.getArgument(0);
            inserted.setBatchId(7001L);
            return 1;
        });

        int rows = service.submitBatch(batch, "admin");

        assertEquals(1, rows);
        ArgumentCaptor<FusionCollectionBatch> batchCaptor = ArgumentCaptor.forClass(FusionCollectionBatch.class);
        verify(batchMapper).insertBatch(batchCaptor.capture());
        assertEquals(Integer.valueOf(2026), batchCaptor.getValue().getPeriodYear());
        assertEquals(Integer.valueOf(6), batchCaptor.getValue().getPeriodMonth());
        assertNull(batchCaptor.getValue().getPeriodQuarter());
        assertEquals("省数据局", batchCaptor.getValue().getResponsibleUnitName());
        ArgumentCaptor<FusionCollectionItem> itemCaptor = ArgumentCaptor.forClass(FusionCollectionItem.class);
        verify(batchMapper).insertItem(itemCaptor.capture());
        assertEquals("省数据局", itemCaptor.getValue().getResponsibleUnitName());
        assertEquals(new BigDecimal("72"), itemCaptor.getValue().getValueDecimal());
        assertEquals("72", itemCaptor.getValue().getRawValue());
    }

    @Test
    void submitBatchRejectsEmptyItems()
    {
        FusionCollectionBatch batch = writableBatch();
        batch.setItems(Collections.<FusionCollectionItem>emptyList());

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "admin"));

        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void submitBatchRejectsInvalidPeriodKey()
    {
        FusionCollectionBatch batch = writableBatch();
        batch.setPeriodKey("2026-13");

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "admin"));

        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void submitBatchRejectsRegionCodeWithoutRegionName()
    {
        FusionCollectionBatch batch = writableBatch();
        batch.setRegionName(" ");

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "admin"));

        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void submitBatchRejectsInvalidNumberValue()
    {
        FusionCollectionBatch batch = writableBatch();
        batch.getItems().get(0).setRawValue("abc");
        batch.getItems().get(0).setValueDecimal(null);
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(indicator());

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "admin"));

        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void submitBatchRejectsDuplicateItemsInSameRequest()
    {
        FusionCollectionBatch batch = writableBatch();
        FusionCollectionItem duplicate = new FusionCollectionItem();
        duplicate.setIndicatorId(1003L);
        duplicate.setIndicatorCode("NC-1003");
        duplicate.setIndicatorName("数字政务能力");
        duplicate.setRawValue("73");
        duplicate.setValueDecimal(new BigDecimal("73"));
        batch.setItems(Arrays.asList(batch.getItems().get(0), duplicate));
        when(indicatorMapper.selectIndicatorById(1003L)).thenReturn(indicator());

        assertThrows(ServiceException.class, () -> service.submitBatch(batch, "admin"));

        verify(batchMapper, never()).insertBatch(any(FusionCollectionBatch.class));
    }

    @Test
    void batchSummaryAppliesRoleDataScope()
    {
        loginAsScopedUser(2L, 88L, Constants.Dept.DATA_SCOPE_DEPT);
        when(batchMapper.selectBatchStatusStats(any(FusionCollectionBatch.class))).thenReturn(Collections.emptyList());

        service.selectBatchSummary();

        ArgumentCaptor<FusionCollectionBatch> captor = ArgumentCaptor.forClass(FusionCollectionBatch.class);
        verify(batchMapper).selectBatchStatusStats(captor.capture());
        assertTrue(String.valueOf(captor.getValue().getParams().get("dataScope")).contains("b.dept_id = 88"));
    }

    private FusionCollectionBatch batch(String status)
    {
        FusionCollectionBatch batch = new FusionCollectionBatch();
        batch.setBatchId(5001L);
        batch.setBatchStatus(status);
        return batch;
    }

    private FusionCollectionBatch writableBatch()
    {
        FusionCollectionBatch batch = batch("draft");
        batch.setBatchName("表单提交批次");
        batch.setResponsibleUnitId(200L);
        batch.setResponsibleUnitName("省数据局");
        batch.setRegionCode("433100");
        batch.setRegionName("湘西州");
        batch.setPeriodType("month");
        batch.setPeriodKey("2026-06");
        FusionCollectionItem item = new FusionCollectionItem();
        item.setIndicatorId(1003L);
        item.setIndicatorCode("NC-1003");
        item.setIndicatorName("数字政务能力");
        item.setRawValue("72");
        item.setValueDecimal(new BigDecimal("72"));
        batch.setItems(Collections.singletonList(item));
        return batch;
    }

    private FusionCollectionImportRow validRow()
    {
        FusionCollectionImportRow row = new FusionCollectionImportRow();
        row.setBatchName("2026年6月导入");
        row.setResponsibleUnitId(200L);
        row.setResponsibleUnitName("省数据局");
        row.setRegionCode("433100");
        row.setRegionName("湘西州");
        row.setPeriodType("month");
        row.setPeriodKey("2026-06");
        row.setIndicatorCode("NC-1003");
        row.setRawValue("72");
        row.setValueDecimal(new BigDecimal("72"));
        return row;
    }

    private FusionCollectionImportRow invalidNumberRow()
    {
        FusionCollectionImportRow row = validRow();
        row.setIndicatorCode("NC-1004");
        row.setRawValue("abc");
        row.setValueDecimal(null);
        return row;
    }

    private FusionIndicator indicator()
    {
        FusionIndicator indicator = new FusionIndicator();
        indicator.setIndicatorId(1003L);
        indicator.setIndicatorCode("NC-1003");
        indicator.setIndicatorName("数字政务能力");
        indicator.setLifecycleStatus("enabled");
        indicator.setStatus("0");
        return indicator;
    }

    private void loginAsScopedUser(Long userId, Long deptId, String dataScope)
    {
        SecurityContextHolder.setUserId(String.valueOf(userId));
        SecurityContextHolder.setUserName("scope-user");
        SecurityContextHolder.setPermission("nocontact:fusion:collection:query");
        SysRole role = new SysRole();
        role.setRoleId(20L);
        role.setDataScope(dataScope);
        role.setStatus(UserConstants.ROLE_NORMAL);
        role.setPermissions(new HashSet<String>(Arrays.asList("nocontact:fusion:collection:list", "nocontact:fusion:collection:query",
                "nocontact:fusion:collection:audit")));
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserName("scope-user");
        user.setDeptId(deptId);
        user.setRoles(Collections.singletonList(role));
        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(user);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
    }

    private void loginAsAdmin()
    {
        SecurityContextHolder.setUserId("1");
        SecurityContextHolder.setUserName("admin");
        SecurityContextHolder.setPermission("*:*:*");
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUserName("admin");
        user.setDeptId(103L);
        LoginUser loginUser = new LoginUser();
        loginUser.setSysUser(user);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
    }
}
