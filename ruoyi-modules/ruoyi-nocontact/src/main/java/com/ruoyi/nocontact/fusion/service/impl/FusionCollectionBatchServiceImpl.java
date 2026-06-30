package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.common.NocontactDataScopeHelper;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionAuditLog;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionImportFailure;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionImportRow;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.fusion.domain.FusionIndicator;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.fusion.mapper.FusionIndicatorMapper;
import com.ruoyi.nocontact.fusion.service.IFusionCollectionBatchService;
import com.ruoyi.nocontact.support.domain.BusinessMessage;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import com.ruoyi.nocontact.support.service.impl.BusinessMessageServiceImpl;
import com.ruoyi.nocontact.warning.service.IWarningEvaluationService;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.model.LoginUser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据采集批次Service业务层处理
 */
@Service
public class FusionCollectionBatchServiceImpl implements IFusionCollectionBatchService
{
    @Autowired
    private FusionCollectionBatchMapper batchMapper;

    @Autowired
    private FusionIndicatorMapper indicatorMapper;

    @Autowired
    private IWarningEvaluationService warningEvaluationService;

    @Autowired
    private IBusinessMessageService businessMessageService;

    @Override
    public List<FusionCollectionBatch> selectBatchList(FusionCollectionBatch batch)
    {
        NocontactDataScopeHelper.applyDataScope(batch, "b", "dept_id", "b", "create_by");
        return batchMapper.selectBatchList(batch);
    }

    @Override
    public FusionCollectionBatch selectBatchById(Long batchId)
    {
        FusionCollectionBatch query = new FusionCollectionBatch();
        query.setBatchId(batchId);
        NocontactDataScopeHelper.applyDataScope(query, "b", "dept_id", "b", "create_by");
        FusionCollectionBatch batch = batchMapper.selectBatchByScope(query);
        if (batch != null)
        {
            batch.setItems(batchMapper.selectItemsByBatchId(batchId));
            batch.setAuditLogs(batchMapper.selectAuditLogsByBatchId(batchId));
        }
        return batch;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitBatch(FusionCollectionBatch batch, String operName)
    {
        normalizeBatchOwnership(batch);
        normalizeBatchPeriod(batch);
        normalizeBatchItems(batch);
        batch.setCreateBy(operName);
        batch.setCreateTime(DateUtils.getNowDate());
        batch.setSubmitBy(operName);
        batch.setSubmitTime(DateUtils.getNowDate());
        batch.setBatchStatus("pending_audit");
        if (StringUtils.isBlank(batch.getSourceType()))
        {
            batch.setSourceType("form");
        }
        if (batch.getItemCount() == null)
        {
            batch.setItemCount(batch.getItems() == null ? 0 : batch.getItems().size());
        }
        int rows = batchMapper.insertBatch(batch);
        if (batch.getItems() != null)
        {
            for (FusionCollectionItem item : batch.getItems())
            {
                item.setBatchId(batch.getBatchId());
                if (StringUtils.isBlank(item.getValidationStatus()))
                {
                    item.setValidationStatus("valid");
                }
                if (StringUtils.isBlank(item.getIsCurrent()))
                {
                    item.setIsCurrent("1");
                }
                item.setCreateBy(operName);
                item.setCreateTime(DateUtils.getNowDate());
                insertItemGuardingScope(item);
            }
        }
        insertAuditLog(batch.getBatchId(), "draft", "pending_audit", "提交审核", "提交采集数据", operName);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveBatch(Long batchId, String opinion, String operName)
    {
        FusionCollectionBatch batch = selectScopedBatch(batchId);
        if (batch == null)
        {
            throw new ServiceException("采集批次不存在");
        }
        assertPendingAudit(batch);
        FusionCollectionBatch update = new FusionCollectionBatch();
        update.setBatchId(batchId);
        update.setBatchStatus("approved");
        update.setExpectedStatus(batch.getBatchStatus());
        update.setAuditBy(operName);
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditOpinion(opinion);
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = batchMapper.updateBatchStatus(update);
        if (rows == 0)
        {
            throw new ServiceException("采集批次状态已变化，请刷新后重试");
        }
        insertAuditLog(batchId, batch.getBatchStatus(), "approved", "审核通过", opinion, operName);
        warningEvaluationService.evaluateApprovedBatch(batchId, operName);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectBatch(Long batchId, String opinion, String operName)
    {
        FusionCollectionBatch batch = selectScopedBatch(batchId);
        if (batch == null)
        {
            throw new ServiceException("采集批次不存在");
        }
        assertPendingAudit(batch);
        FusionCollectionBatch update = new FusionCollectionBatch();
        update.setBatchId(batchId);
        update.setBatchStatus("rejected");
        update.setExpectedStatus(batch.getBatchStatus());
        update.setAuditBy(operName);
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditOpinion(opinion);
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = batchMapper.updateBatchStatus(update);
        if (rows == 0)
        {
            throw new ServiceException("采集批次状态已变化，请刷新后重试");
        }
        insertAuditLog(batchId, batch.getBatchStatus(), "rejected", "审核驳回", opinion, operName);
        markBatchItemsCurrent(batchId, "0", operName);
        createRejectMessage(batchId, opinion, operName);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importCollection(List<FusionCollectionImportRow> rows, String operName)
    {
        if (StringUtils.isEmpty(rows))
        {
            throw new ServiceException("导入采集数据不能为空");
        }
        String importBatchName = "Excel导入-" + DateUtils.dateTimeNow("yyyyMMddHHmmss");
        Map<String, FusionCollectionBatch> groups = new LinkedHashMap<String, FusionCollectionBatch>();
        List<FusionCollectionImportFailure> failures = new ArrayList<FusionCollectionImportFailure>();
        Set<String> duplicateKeys = new HashSet<String>();

        for (int i = 0; i < rows.size(); i++)
        {
            FusionCollectionImportRow row = rows.get(i);
            row.setRowNum(i + 2);
            FusionCollectionItem item = validateImportRow(row, importBatchName, duplicateKeys, failures, operName);
            if (item == null)
            {
                continue;
            }
            FusionCollectionBatch batch = batchFor(row, importBatchName, groups);
            batch.getItems().add(item);
        }

        for (FusionCollectionImportFailure failure : failures)
        {
            failure.setCreateBy(operName);
            failure.setCreateTime(DateUtils.getNowDate());
            batchMapper.insertImportFailure(failure);
        }

        int successRows = 0;
        int batchCount = 0;
        for (FusionCollectionBatch batch : groups.values())
        {
            if (StringUtils.isEmpty(batch.getItems()))
            {
                continue;
            }
            submitBatch(batch, operName);
            successRows += batch.getItems().size();
            batchCount++;
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("importBatchName", importBatchName);
        result.put("successRows", successRows);
        result.put("failureRows", failures.size());
        result.put("batchCount", batchCount);
        return result;
    }

    @Override
    public List<FusionCollectionImportFailure> selectImportFailures(FusionCollectionImportFailure failure)
    {
        NocontactDataScopeHelper.applyDataScope(failure, "f", "dept_id", "f", "create_by");
        return batchMapper.selectImportFailureList(failure);
    }

    @Override
    public Map<String, Object> selectBatchSummary()
    {
        FusionCollectionBatch scope = new FusionCollectionBatch();
        NocontactDataScopeHelper.applyDataScope(scope, "b", "dept_id", "b", "create_by");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("statusStats", batchMapper.selectBatchStatusStats(scope));
        return data;
    }

    private FusionCollectionItem validateImportRow(FusionCollectionImportRow row, String importBatchName,
            Set<String> duplicateKeys, List<FusionCollectionImportFailure> failures, String operName)
    {
        if (row.getResponsibleUnitId() == null || row.getResponsibleUnitId() <= 0)
        {
            addFailure(failures, row, importBatchName, "责任单位ID", String.valueOf(row.getResponsibleUnitId()),
                    "责任单位ID不能为空");
            return null;
        }
        row.setRegionCode(StringUtils.trim(row.getRegionCode()));
        row.setRegionName(StringUtils.trim(row.getRegionName()));
        if ((StringUtils.isBlank(row.getRegionCode()) && StringUtils.isNotBlank(row.getRegionName()))
                || (StringUtils.isNotBlank(row.getRegionCode()) && StringUtils.isBlank(row.getRegionName())))
        {
            addFailure(failures, row, importBatchName, "地区", row.getRegionCode() + "/" + row.getRegionName(),
                    "地区编码和地区名称必须同时填写");
            return null;
        }
        if (!canWriteDept(row.getResponsibleUnitId()))
        {
            addFailure(failures, row, importBatchName, "责任单位ID", String.valueOf(row.getResponsibleUnitId()),
                    "无权导入该责任单位数据");
            return null;
        }
        String deptName = batchMapper.selectDeptNameById(row.getResponsibleUnitId());
        if (StringUtils.isBlank(deptName))
        {
            addFailure(failures, row, importBatchName, "责任单位ID", String.valueOf(row.getResponsibleUnitId()),
                    "责任单位不存在");
            return null;
        }
        row.setResponsibleUnitName(deptName);
        if (!isAllowedPeriodType(row.getPeriodType()))
        {
            addFailure(failures, row, importBatchName, "周期类型", row.getPeriodType(), "周期类型必须是month、quarter或year");
            return null;
        }
        if (!isValidPeriodKey(row.getPeriodType(), row.getPeriodKey()))
        {
            addFailure(failures, row, importBatchName, "业务期间", row.getPeriodKey(), "业务期间格式不符合周期类型");
            return null;
        }

        FusionIndicator indicator = resolveIndicator(row.getIndicatorId(), row.getIndicatorCode());
        if (indicator == null)
        {
            addFailure(failures, row, importBatchName, "指标", StringUtils.defaultIfBlank(row.getIndicatorCode(),
                    String.valueOf(row.getIndicatorId())), "指标不存在");
            return null;
        }
        if (!isEnabledIndicator(indicator))
        {
            addFailure(failures, row, importBatchName, "指标", indicator.getIndicatorName(), "指标未启用");
            return null;
        }

        BigDecimal value = parseImportValue(row);
        if (value == null)
        {
            addFailure(failures, row, importBatchName, "数值", StringUtils.defaultString(row.getRawValue()),
                    "数值不能为空且必须是合法数字");
            return null;
        }

        String duplicateKey = indicator.getIndicatorId() + ":" + row.getResponsibleUnitId() + ":"
                + StringUtils.defaultString(row.getRegionCode()) + ":" + row.getPeriodKey();
        if (!duplicateKeys.add(duplicateKey))
        {
            addFailure(failures, row, importBatchName, "指标编码", indicator.getIndicatorCode(), "同批次存在重复指标");
            return null;
        }

        FusionCollectionItem item = new FusionCollectionItem();
        item.setIndicatorId(indicator.getIndicatorId());
        item.setIndicatorCode(indicator.getIndicatorCode());
        item.setIndicatorName(StringUtils.defaultIfBlank(row.getIndicatorName(), indicator.getIndicatorName()));
        item.setRawValue(StringUtils.defaultIfBlank(row.getRawValue(), String.valueOf(value)));
        item.setValueDecimal(value);
        item.setValidationStatus("valid");
        item.setValidationMessage("");
        item.setIsCurrent("1");
        item.setCreateBy(operName);
        item.setCreateTime(DateUtils.getNowDate());
        FusionCollectionBatch scope = batchFor(row, importBatchName, new LinkedHashMap<String, FusionCollectionBatch>());
        copyBatchSnapshot(scope, item);
        if (batchMapper.countActiveItemByScope(item) > 0)
        {
            addFailure(failures, row, importBatchName, "业务期间", row.getPeriodKey(), "同周期已存在未驳回采集数据");
            return null;
        }
        return item;
    }

    private void createRejectMessage(Long batchId, String opinion, String operName)
    {
        BusinessMessage message = new BusinessMessage();
        message.setMessageType(BusinessMessageServiceImpl.AUDIT_REJECTED);
        message.setTitle("采集数据审核被驳回");
        message.setContent(StringUtils.defaultIfBlank(opinion, "请根据驳回意见重新提交采集数据"));
        message.setBusinessType("collection");
        message.setBusinessId(batchId);
        message.setJumpTarget("/nocontact/collection/audit?batchId=" + batchId);
        message.setReceiverUserName(operName);
        message.setCreateBy(operName);
        businessMessageService.createMessage(message);
    }

    private FusionIndicator resolveIndicator(FusionCollectionImportRow row)
    {
        return resolveIndicator(row.getIndicatorId(), row.getIndicatorCode());
    }

    private FusionIndicator resolveIndicator(Long indicatorId, String indicatorCode)
    {
        if (indicatorId != null)
        {
            FusionIndicator indicator = indicatorMapper.selectIndicatorById(indicatorId);
            if (indicator != null && StringUtils.isNotBlank(indicatorCode)
                    && !indicatorCode.equals(indicator.getIndicatorCode()))
            {
                return null;
            }
            return indicator;
        }
        if (StringUtils.isNotBlank(indicatorCode))
        {
            return indicatorMapper.selectEnabledIndicatorByCode(indicatorCode);
        }
        return null;
    }

    private boolean isEnabledIndicator(FusionIndicator indicator)
    {
        return indicator != null && "enabled".equals(indicator.getLifecycleStatus()) && "0".equals(indicator.getStatus());
    }

    private BigDecimal parseImportValue(FusionCollectionImportRow row)
    {
        if (row.getValueDecimal() != null)
        {
            return row.getValueDecimal();
        }
        if (StringUtils.isBlank(row.getRawValue()))
        {
            return null;
        }
        try
        {
            return new BigDecimal(row.getRawValue().trim());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private FusionCollectionBatch batchFor(FusionCollectionImportRow row, String importBatchName,
            Map<String, FusionCollectionBatch> groups)
    {
        String batchName = StringUtils.defaultIfBlank(row.getBatchName(), importBatchName);
        String key = batchName + ":" + row.getResponsibleUnitId() + ":" + StringUtils.defaultString(row.getRegionCode())
                + ":" + row.getPeriodType() + ":" + row.getPeriodKey();
        FusionCollectionBatch batch = groups.get(key);
        if (batch == null)
        {
            batch = new FusionCollectionBatch();
            batch.setBatchName(batchName);
            batch.setSourceType("excel");
            batch.setSourceName("Excel导入");
            batch.setSourceRecordId(importBatchName);
            batch.setDeptId(row.getResponsibleUnitId());
            batch.setResponsibleUnitId(row.getResponsibleUnitId());
            batch.setResponsibleUnitName(row.getResponsibleUnitName());
            batch.setRegionCode(row.getRegionCode());
            batch.setRegionName(row.getRegionName());
            batch.setPeriodType(row.getPeriodType());
            batch.setPeriodKey(row.getPeriodKey());
            fillPeriodParts(batch);
            batch.setItems(new ArrayList<FusionCollectionItem>());
            groups.put(key, batch);
        }
        return batch;
    }

    private void addFailure(List<FusionCollectionImportFailure> failures, FusionCollectionImportRow row,
            String importBatchName, String fieldName, String rawValue, String reason)
    {
        FusionCollectionImportFailure failure = new FusionCollectionImportFailure();
        Long userDeptId = currentUserDeptId();
        failure.setDeptId(userDeptId == null ? row.getResponsibleUnitId() : userDeptId);
        failure.setImportBatchName(StringUtils.defaultIfBlank(row.getBatchName(), importBatchName));
        failure.setSourceRecordId(importBatchName);
        failure.setRowNum(row.getRowNum());
        failure.setFieldName(fieldName);
        failure.setRawValue(StringUtils.defaultString(rawValue));
        failure.setFailureReason(reason);
        failures.add(failure);
    }

    private boolean isAllowedPeriodType(String periodType)
    {
        return "month".equals(periodType) || "quarter".equals(periodType) || "year".equals(periodType);
    }

    private boolean isValidPeriodKey(String periodType, String periodKey)
    {
        if (StringUtils.isBlank(periodKey))
        {
            return false;
        }
        if ("month".equals(periodType))
        {
            return periodKey.matches("\\d{4}-(0[1-9]|1[0-2])");
        }
        if ("quarter".equals(periodType))
        {
            return periodKey.matches("\\d{4}-Q[1-4]");
        }
        return periodKey.matches("\\d{4}");
    }

    private void fillPeriodParts(FusionCollectionBatch batch)
    {
        batch.setPeriodYear(null);
        batch.setPeriodQuarter(null);
        batch.setPeriodMonth(null);
        if (StringUtils.isBlank(batch.getPeriodKey()))
        {
            return;
        }
        String periodKey = batch.getPeriodKey();
        try
        {
            if ("month".equals(batch.getPeriodType()) && periodKey.length() == 7)
            {
                batch.setPeriodYear(Integer.valueOf(periodKey.substring(0, 4)));
                batch.setPeriodMonth(Integer.valueOf(periodKey.substring(5, 7)));
            }
            else if ("quarter".equals(batch.getPeriodType()) && periodKey.length() == 7)
            {
                batch.setPeriodYear(Integer.valueOf(periodKey.substring(0, 4)));
                batch.setPeriodQuarter(Integer.valueOf(periodKey.substring(6, 7)));
            }
            else if ("year".equals(batch.getPeriodType()) && periodKey.length() == 4)
            {
                batch.setPeriodYear(Integer.valueOf(periodKey));
            }
        }
        catch (NumberFormatException e)
        {
            // 校验阶段已检查格式，这里只避免异常打断导入。
        }
    }

    private void normalizeBatchOwnership(FusionCollectionBatch batch)
    {
        if (batch.getResponsibleUnitId() == null)
        {
            throw new ServiceException("责任单位不能为空");
        }
        batch.setDeptId(batch.getResponsibleUnitId());
        if (!canWriteDept(batch.getResponsibleUnitId()))
        {
            throw new ServiceException("无权提交该责任单位数据");
        }
        String deptName = batchMapper.selectDeptNameById(batch.getResponsibleUnitId());
        if (StringUtils.isBlank(deptName))
        {
            throw new ServiceException("责任单位不存在");
        }
        batch.setResponsibleUnitName(deptName);
        normalizeRegionSnapshot(batch);
    }

    private void normalizeBatchPeriod(FusionCollectionBatch batch)
    {
        if (!isAllowedPeriodType(batch.getPeriodType()))
        {
            throw new ServiceException("周期类型必须是month、quarter或year");
        }
        if (!isValidPeriodKey(batch.getPeriodType(), batch.getPeriodKey()))
        {
            throw new ServiceException("业务期间格式不符合周期类型");
        }
        fillPeriodParts(batch);
    }

    private void normalizeBatchItems(FusionCollectionBatch batch)
    {
        if (StringUtils.isEmpty(batch.getItems()))
        {
            throw new ServiceException("采集明细不能为空");
        }
        Set<String> duplicateKeys = new HashSet<String>();
        for (FusionCollectionItem item : batch.getItems())
        {
            copyBatchSnapshot(batch, item);
            FusionIndicator indicator = resolveIndicator(item.getIndicatorId(), item.getIndicatorCode());
            if (indicator == null)
            {
                throw new ServiceException("采集指标不存在或编码不匹配");
            }
            if (!isEnabledIndicator(indicator))
            {
                throw new ServiceException("采集指标未启用");
            }
            item.setIndicatorId(indicator.getIndicatorId());
            item.setIndicatorCode(indicator.getIndicatorCode());
            item.setIndicatorName(StringUtils.defaultIfBlank(item.getIndicatorName(), indicator.getIndicatorName()));
            normalizeItemValue(item, indicator);
            String duplicateKey = item.getIndicatorId() + ":" + item.getResponsibleUnitId() + ":"
                    + StringUtils.defaultString(item.getRegionCode()) + ":" + item.getPeriodKey();
            if (!duplicateKeys.add(duplicateKey))
            {
                throw new ServiceException("同批次存在重复指标");
            }
            if (batchMapper.countActiveItemByScope(item) > 0)
            {
                throw new ServiceException("同周期已存在未驳回采集数据");
            }
        }
    }

    private void normalizeItemValue(FusionCollectionItem item, FusionIndicator indicator)
    {
        String dataType = StringUtils.defaultIfBlank(indicator.getDataType(), "number");
        if ("number".equals(dataType))
        {
            if (item.getValueDecimal() == null)
            {
                if (StringUtils.isBlank(item.getRawValue()))
                {
                    throw new ServiceException("数字指标数值不能为空");
                }
                try
                {
                    item.setValueDecimal(new BigDecimal(item.getRawValue().trim()));
                }
                catch (NumberFormatException e)
                {
                    throw new ServiceException("数字指标数值必须是合法数字");
                }
            }
            item.setRawValue(item.getValueDecimal().toPlainString());
            return;
        }
        if ("date".equals(dataType))
        {
            if (item.getValueDate() == null)
            {
                Date valueDate = DateUtils.parseDate(item.getRawValue());
                if (valueDate == null)
                {
                    throw new ServiceException("日期指标数值必须是合法日期");
                }
                item.setValueDate(valueDate);
            }
            if (StringUtils.isBlank(item.getRawValue()))
            {
                item.setRawValue(DateUtils.dateTime(item.getValueDate()));
            }
            return;
        }
        if (StringUtils.isBlank(item.getRawValue()) && StringUtils.isBlank(item.getValueText()))
        {
            throw new ServiceException("文本指标数值不能为空");
        }
        if (StringUtils.isBlank(item.getValueText()))
        {
            item.setValueText(item.getRawValue());
        }
        if (StringUtils.isBlank(item.getRawValue()))
        {
            item.setRawValue(item.getValueText());
        }
    }

    private Long currentUserDeptId()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getSysUser() == null)
        {
            return null;
        }
        return loginUser.getSysUser().getDeptId();
    }

    private void copyBatchSnapshot(FusionCollectionBatch batch, FusionCollectionItem item)
    {
        item.setDeptId(batch.getDeptId());
        item.setResponsibleUnitId(batch.getResponsibleUnitId());
        item.setResponsibleUnitName(batch.getResponsibleUnitName());
        item.setRegionCode(batch.getRegionCode());
        item.setRegionName(batch.getRegionName());
        item.setPeriodKey(batch.getPeriodKey());
    }

    private void normalizeRegionSnapshot(FusionCollectionBatch batch)
    {
        batch.setRegionCode(StringUtils.trim(batch.getRegionCode()));
        batch.setRegionName(StringUtils.trim(batch.getRegionName()));
        if ((StringUtils.isBlank(batch.getRegionCode()) && StringUtils.isNotBlank(batch.getRegionName()))
                || (StringUtils.isNotBlank(batch.getRegionCode()) && StringUtils.isBlank(batch.getRegionName())))
        {
            throw new ServiceException("地区编码和地区名称必须同时填写");
        }
    }

    private void insertItemGuardingScope(FusionCollectionItem item)
    {
        try
        {
            batchMapper.insertItem(item);
        }
        catch (DuplicateKeyException e)
        {
            throw new ServiceException("同周期已存在未驳回采集数据");
        }
    }

    private void markBatchItemsCurrent(Long batchId, String isCurrent, String operName)
    {
        FusionCollectionBatch update = new FusionCollectionBatch();
        update.setBatchId(batchId);
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        update.getParams().put("isCurrent", isCurrent);
        batchMapper.updateItemsCurrentByBatchId(update);
    }

    private boolean canWriteDept(Long deptId)
    {
        if (deptId == null)
        {
            return false;
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getSysUser() == null)
        {
            return false;
        }
        if (loginUser.getSysUser().isAdmin() || hasAllDeptDataScope(loginUser))
        {
            return true;
        }
        FusionCollectionBatch scope = new FusionCollectionBatch();
        scope.setDeptId(deptId);
        NocontactDataScopeHelper.applyDeptDataScope(scope, "d", "dept_id");
        String dataScope = String.valueOf(scope.getParams().get("dataScope"));
        return StringUtils.isNotBlank(dataScope) && batchMapper.countDeptInScope(scope) > 0;
    }

    private boolean hasAllDeptDataScope(LoginUser loginUser)
    {
        if (loginUser.getSysUser() == null || StringUtils.isEmpty(loginUser.getSysUser().getRoles()))
        {
            return false;
        }
        String permission = SecurityContextHolder.getPermission();
        for (SysRole role : loginUser.getSysUser().getRoles())
        {
            if (role != null && Constants.Dept.DATA_SCOPE_ALL.equals(role.getDataScope())
                    && !UserConstants.ROLE_DISABLE.equals(role.getStatus())
                    && roleMatchesPermission(role, permission))
            {
                return true;
            }
        }
        return false;
    }

    private boolean roleMatchesPermission(SysRole role, String permission)
    {
        return StringUtils.isEmpty(permission) || StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission));
    }

    private FusionCollectionBatch selectScopedBatch(Long batchId)
    {
        FusionCollectionBatch query = new FusionCollectionBatch();
        query.setBatchId(batchId);
        NocontactDataScopeHelper.applyDataScope(query, "b", "dept_id", "b", "create_by");
        return batchMapper.selectBatchByScope(query);
    }

    private void insertAuditLog(Long batchId, String fromStatus, String toStatus, String actionName, String opinion,
            String operName)
    {
        FusionCollectionAuditLog log = new FusionCollectionAuditLog();
        log.setBatchId(batchId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setActionName(actionName);
        log.setAuditBy(operName);
        log.setAuditTime(DateUtils.getNowDate());
        log.setAuditOpinion(opinion);
        log.setCreateBy(operName);
        log.setCreateTime(DateUtils.getNowDate());
        batchMapper.insertAuditLog(log);
    }

    private void assertPendingAudit(FusionCollectionBatch batch)
    {
        if (!"pending_audit".equals(batch.getBatchStatus()))
        {
            throw new ServiceException("只有待审核采集批次允许审核，当前状态：" + batch.getBatchStatus());
        }
    }
}
