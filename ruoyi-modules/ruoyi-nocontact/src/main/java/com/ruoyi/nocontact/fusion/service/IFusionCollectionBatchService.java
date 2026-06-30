package com.ruoyi.nocontact.fusion.service;

import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionImportFailure;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionImportRow;
import java.util.List;
import java.util.Map;

/**
 * 数据采集批次Service接口
 */
public interface IFusionCollectionBatchService
{
    public List<FusionCollectionBatch> selectBatchList(FusionCollectionBatch batch);

    public FusionCollectionBatch selectBatchById(Long batchId);

    public int submitBatch(FusionCollectionBatch batch, String operName);

    public int approveBatch(Long batchId, String opinion, String operName);

    public int rejectBatch(Long batchId, String opinion, String operName);

    public Map<String, Object> importCollection(List<FusionCollectionImportRow> rows, String operName);

    public List<FusionCollectionImportFailure> selectImportFailures(FusionCollectionImportFailure failure);

    public Map<String, Object> selectBatchSummary();
}
