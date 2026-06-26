package com.ruoyi.nocontact.fusion.mapper;

import com.ruoyi.nocontact.fusion.domain.FusionCollectionAuditLog;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionBatch;
import com.ruoyi.nocontact.fusion.domain.FusionCollectionItem;
import java.util.List;
import java.util.Map;

/**
 * 数据采集批次Mapper接口
 */
public interface FusionCollectionBatchMapper
{
    public List<FusionCollectionBatch> selectBatchList(FusionCollectionBatch batch);

    public FusionCollectionBatch selectBatchById(Long batchId);

    public List<FusionCollectionItem> selectItemsByBatchId(Long batchId);

    public List<FusionCollectionAuditLog> selectAuditLogsByBatchId(Long batchId);

    public int insertBatch(FusionCollectionBatch batch);

    public int insertItem(FusionCollectionItem item);

    public int updateBatchStatus(FusionCollectionBatch batch);

    public int updateBatchItemCount(FusionCollectionBatch batch);

    public int insertAuditLog(FusionCollectionAuditLog auditLog);

    public List<Map<String, Object>> selectBatchStatusStats();
}
