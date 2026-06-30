const {
  ajaxOk,
  assert,
  clickElementByText,
  clickTableRowAction,
  clickTextButton,
  closeAllPages,
  login,
  openAuthedPage,
  requireWriteGuard,
  setInputValueByPlaceholder,
  waitFor,
  waitForBodyIncludes,
  waitForTableRow
} = require('./lib/nocontactSmoke');

const successConfigName = 'SMOKE-INTEGRATION-SUCCESS';
const failureConfigName = 'SMOKE-INTEGRATION-FAIL';

function buildConfigPayload(integrationName, platformName, endpointUrl) {
  return {
    integrationName,
    platformName,
    integrationType: 'api',
    endpointUrl,
    authType: 'token',
    authCredential: 'smoke-token',
    syncFrequency: 'daily',
    syncMode: 'incremental',
    mappingRule: '{"indicatorId":"indicatorId"}',
    transformRule: '{"value":"valueDecimal"}',
    retryPolicy: 'manual',
    status: '0',
    remark: 'smoke-fixture'
  };
}

async function listConfigs(token, integrationName) {
  const response = await ajaxOk('GET', '/nocontact/integration/config/list', {
    token,
    query: { pageNum: 1, pageSize: 50, integrationName }
  }, 'integration config list');
  return (response.rows || []).filter(item => item.integrationName === integrationName).sort((left, right) => Number(right.configId) - Number(left.configId));
}

async function deleteConfig(token, configId) {
  await ajaxOk('DELETE', `/nocontact/integration/config/${configId}`, { token }, 'delete smoke integration config');
}

async function ensureConfig(token, payload) {
  const existing = await listConfigs(token, payload.integrationName);
  const keep = existing[0] || null;
  for (const duplicate of existing.slice(1)) {
    await deleteConfig(token, duplicate.configId);
  }
  if (!keep) {
    await ajaxOk('POST', '/nocontact/integration/config', { token, body: payload }, 'create smoke integration config');
  } else {
    await ajaxOk('PUT', '/nocontact/integration/config', {
      token,
      body: Object.assign({}, keep, payload, { configId: keep.configId })
    }, 'update smoke integration config');
  }
  const refreshed = await listConfigs(token, payload.integrationName);
  assert(refreshed.length > 0, `integration config ${payload.integrationName} missing after save`);
  return refreshed[0];
}

async function listBatches(token, integrationName, batchStatus) {
  const response = await ajaxOk('GET', '/nocontact/integration/batch/list', {
    token,
    query: { pageNum: 1, pageSize: 50, integrationName, batchStatus }
  }, 'integration batch list');
  return response.rows || [];
}

async function listLogs(token, integrationName, responseStatus) {
  const response = await ajaxOk('GET', '/nocontact/integration/log/list', {
    token,
    query: { pageNum: 1, pageSize: 50, integrationName, responseStatus }
  }, 'integration log list');
  return response.rows || [];
}

function maxBatchId(rows) {
  return rows.reduce((max, item) => Math.max(max, Number(item.syncBatchId || 0)), 0);
}

function maxLogId(rows) {
  return rows.reduce((max, item) => Math.max(max, Number(item.logId || 0)), 0);
}

async function waitForNewBatch(token, integrationName, afterBatchId, batchStatus, label) {
  return waitFor(async () => {
    const rows = await listBatches(token, integrationName, batchStatus);
    return rows.find(item => Number(item.syncBatchId) > afterBatchId) || null;
  }, label, { timeoutMs: 30000, intervalMs: 1000 });
}

async function waitForNewLog(token, integrationName, afterLogId, responseStatus, label) {
  return waitFor(async () => {
    const rows = await listLogs(token, integrationName, responseStatus);
    return rows.find(item => Number(item.logId) > afterLogId) || null;
  }, label, { timeoutMs: 30000, intervalMs: 1000 });
}

async function queryConfigInPage(configName) {
  setInputValueByPlaceholder('请输入对接名称', configName);
  clickTextButton('查询');
  await waitForTableRow([configName], `integration config row ${configName}`, { timeoutMs: 60000 });
}

async function main() {
  requireWriteGuard('integration smoke will create or update dedicated integration configs, execute sync, and append retry batches');
  const { token, expiresIn } = await login();

  const successConfig = await ensureConfig(token, buildConfigPayload(successConfigName, '烟测成功平台', 'mock://integration-success'));
  const failureConfig = await ensureConfig(token, buildConfigPayload(failureConfigName, '烟测失败平台', 'ftp://example.com/integration-failure'));

  const successLogBefore = maxLogId(await listLogs(token, successConfigName));
  const successBatchBefore = maxBatchId(await listBatches(token, successConfigName));
  const failureLogBefore = maxLogId(await listLogs(token, failureConfigName));
  const failureBatchBefore = maxBatchId(await listBatches(token, failureConfigName));

  await openAuthedPage('/integration/editor', token, expiresIn);
  await waitForBodyIncludes('新增/编辑对接配置', 'integration editor page did not render', { timeoutMs: 60000 });
  await waitForBodyIncludes('认证凭据', 'integration editor form did not render', { timeoutMs: 60000 });

  await openAuthedPage('/integration/config', token, expiresIn);
  await waitForBodyIncludes('连接测试', 'integration config page did not render', { timeoutMs: 60000 });

  await queryConfigInPage(successConfigName);
  clickTableRowAction([successConfigName], '连接测试');
  await waitForBodyIncludes('连接测试通过', 'integration connection test success toast', { timeoutMs: 30000 });
  await waitForNewLog(token, successConfigName, successLogBefore, 'success', 'integration test log');

  await queryConfigInPage(successConfigName);
  clickTableRowAction([successConfigName], '同步');
  await waitForBodyIncludes('同步完成', 'integration sync success toast', { timeoutMs: 30000 });
  const successBatch = await waitForNewBatch(token, successConfigName, successBatchBefore, 'success', 'integration success batch');
  assert(Number(successBatch.failureCount || 0) === 0, 'integration success batch should not contain failed records');
  assert(Number(successBatch.successCount || 0) + Number(successBatch.skippedCount || 0) >= 1,
    'integration success batch did not process any records');

  await queryConfigInPage(failureConfigName);
  clickTableRowAction([failureConfigName], '同步');
  await waitForBodyIncludes('外部系统地址只支持HTTP或HTTPS', 'integration sync failure toast', { timeoutMs: 30000 });
  const failureBatch = await waitForNewBatch(token, failureConfigName, failureBatchBefore, 'failed', 'integration failed batch');
  await waitForNewLog(token, failureConfigName, failureLogBefore, 'failed', 'integration failed log');

  await openAuthedPage('/integration/log', token, expiresIn);
  await waitForBodyIncludes('同步批次', 'integration log page did not render', { timeoutMs: 60000 });
  await queryConfigInPage(failureConfigName);
  await waitForTableRow([failureConfigName, '失败'], 'failed integration batch row', { timeoutMs: 60000 });
  clickTableRowAction([failureConfigName, '失败'], '重试');
  await waitForBodyIncludes('重试已发起', 'integration retry toast', { timeoutMs: 30000 });
  const retriedBatch = await waitFor(async () => {
    const rows = await listBatches(token, failureConfigName);
    return rows.find(item => Number(item.syncBatchId) > Number(failureBatch.syncBatchId) && Number(item.retryCount || 0) >= 1) || null;
  }, 'integration retry batch', { timeoutMs: 30000, intervalMs: 1000 });
  assert(Number(retriedBatch.retryCount || 0) >= 1, 'integration retry batch missing retry count');

  clickElementByText('运行日志', '.el-tabs__item');
  await waitForBodyIncludes('重试次数', 'integration runtime log tab did not render', { timeoutMs: 30000 });
  await waitForTableRow([failureConfigName, '失败'], 'failed integration runtime log row', { timeoutMs: 30000 });

  closeAllPages();
  console.log('smoke-issue-012: external integration e2e passed');
}

main().catch(error => {
  try {
    closeAllPages();
  } catch (closeError) {
    // ignore close failure on test abort
  }
  console.error(error.message || error);
  process.exit(1);
});
