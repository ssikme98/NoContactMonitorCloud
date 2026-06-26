package com.ruoyi.nocontact.fusion.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionAuditLog;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import com.ruoyi.nocontact.fusion.mapper.FusionCollectionBatchMapper;
import com.ruoyi.nocontact.fusion.service.IFusionCollectionBatchService;
import com.ruoyi.nocontact.warning.service.IWarningEvaluationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
    private IWarningEvaluationService warningEvaluationService;

    @Override
    public List<FusionCollectionBatch> selectBatchList(FusionCollectionBatch batch)
    {
        return batchMapper.selectBatchList(batch);
    }

    @Override
    public FusionCollectionBatch selectBatchById(Long batchId)
    {
        FusionCollectionBatch batch = batchMapper.selectBatchById(batchId);
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
                batchMapper.insertItem(item);
            }
        }
        insertAuditLog(batch.getBatchId(), "draft", "pending_audit", "提交审核", "提交采集数据", operName);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveBatch(Long batchId, String opinion, String operName)
    {
        FusionCollectionBatch batch = batchMapper.selectBatchById(batchId);
        if (batch == null)
        {
            throw new ServiceException("采集批次不存在");
        }
        assertPendingAudit(batch);
        FusionCollectionBatch update = new FusionCollectionBatch();
        update.setBatchId(batchId);
        update.setBatchStatus("approved");
        update.setAuditBy(operName);
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditOpinion(opinion);
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = batchMapper.updateBatchStatus(update);
        insertAuditLog(batchId, batch.getBatchStatus(), "approved", "审核通过", opinion, operName);
        if (rows > 0)
        {
            warningEvaluationService.evaluateApprovedBatch(batchId, operName);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectBatch(Long batchId, String opinion, String operName)
    {
        FusionCollectionBatch batch = batchMapper.selectBatchById(batchId);
        if (batch == null)
        {
            throw new ServiceException("采集批次不存在");
        }
        assertPendingAudit(batch);
        FusionCollectionBatch update = new FusionCollectionBatch();
        update.setBatchId(batchId);
        update.setBatchStatus("rejected");
        update.setAuditBy(operName);
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditOpinion(opinion);
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = batchMapper.updateBatchStatus(update);
        insertAuditLog(batchId, batch.getBatchStatus(), "rejected", "审核驳回", opinion, operName);
        return rows;
    }

    @Override
    public Map<String, Object> selectBatchSummary()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("statusStats", batchMapper.selectBatchStatusStats());
        return data;
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
