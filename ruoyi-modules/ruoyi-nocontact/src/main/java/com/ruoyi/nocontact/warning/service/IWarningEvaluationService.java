package com.ruoyi.nocontact.warning.service;

/**
 * 预警评估Service接口
 */
public interface IWarningEvaluationService
{
    public int evaluateApprovedBatch(Long batchId, String operName);

    public int evaluateScheduledRules(String periodKey, String operName);
}
