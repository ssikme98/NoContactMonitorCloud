const {
  ajaxOk,
  assert,
  binaryRequest,
  clickTableRowAction,
  clickTextButton,
  closeAllPages,
  encodedFilename,
  login,
  openAuthedPage,
  requireWriteGuard,
  setInputValueByPlaceholder,
  waitFor,
  waitForBodyIncludes,
  waitForTableRow
} = require('./lib/nocontactSmoke');

const smokeTaskName = 'SMOKE-REPORT-TASK';

async function getTaskById(token, taskId) {
  const response = await ajaxOk('GET', `/nocontact/report/task/${taskId}`, { token }, 'report task info');
  return response.data;
}

async function listTaskByName(token, taskName) {
  const response = await ajaxOk('GET', '/nocontact/report/task/list', {
    token,
    query: { pageNum: 1, pageSize: 20, taskName }
  }, 'report task list');
  return (response.rows || []).filter(item => item.taskName === taskName).sort((left, right) => Number(right.taskId) - Number(left.taskId));
}

async function ensureSmokeTask(token) {
  const existing = await listTaskByName(token, smokeTaskName);
  if (existing.length > 0) {
    return existing[0];
  }
  const templateResponse = await ajaxOk('GET', '/nocontact/report/template/list', {
    token,
    query: { pageNum: 1, pageSize: 20, status: '0' }
  }, 'report template list');
  const template = (templateResponse.rows || [])[0];
  assert(template, 'no enabled report template available for smoke task');
  await ajaxOk('POST', '/nocontact/report/task', {
    token,
    body: {
      taskName: smokeTaskName,
      templateId: template.templateId,
      reportPeriod: '2026Q2',
      reportScope: 'province',
      generateMode: 'manual',
      taskStatus: 'pending'
    }
  }, 'create smoke report task');
  const created = await listTaskByName(token, smokeTaskName);
  assert(created.length > 0, 'smoke report task was not created');
  return created[0];
}

async function ensureGeneratedBaseline(token, task) {
  const current = await getTaskById(token, task.taskId);
  if (current.generatedWordFileName && current.generatedExcelFileName) {
    return current;
  }
  await ajaxOk('PUT', `/nocontact/report/task/${task.taskId}/generate`, { token }, 'seed smoke report task');
  return waitFor(async () => {
    const updated = await getTaskById(token, task.taskId);
    if (updated.generatedWordFileName && updated.generatedExcelFileName) {
      return updated;
    }
    return null;
  }, 'seeded report task output', { timeoutMs: 30000, intervalMs: 1000 });
}

async function main() {
  requireWriteGuard('report smoke will create or update the dedicated report task and append a snapshot');
  const { token, expiresIn } = await login();
  const smokeTask = await ensureSmokeTask(token);
  const beforeTask = await ensureGeneratedBaseline(token, smokeTask);
  const snapshotResponse = await ajaxOk('GET', `/nocontact/report/task/${beforeTask.taskId}/snapshot/list`, {
    token
  }, 'report snapshot list before regenerate');
  const beforeSnapshotCount = (snapshotResponse.data || []).length;
  assert(beforeTask.generatedWordFileName, 'report task has no generated Word file');
  assert(beforeTask.generatedExcelFileName, 'report task has no generated Excel file');

  await openAuthedPage('/report/task', token, expiresIn);
  await waitForBodyIncludes('报告生成任务', 'report task page did not render', { timeoutMs: 60000 });
  setInputValueByPlaceholder('请输入任务名称', smokeTaskName);
  clickTextButton('查询');
  await waitForTableRow([smokeTaskName, beforeTask.generatedWordFileName], 'fixture report task row', { timeoutMs: 60000 });

  clickTableRowAction([smokeTaskName, beforeTask.generatedWordFileName], '再次生成');
  await waitForBodyIncludes('生成完成', 'regenerate success toast', { timeoutMs: 30000 });

  const updatedTask = await waitFor(async () => {
    const current = await getTaskById(token, beforeTask.taskId);
    if (current.generatedWordFileName !== beforeTask.generatedWordFileName && current.generatedExcelFileName !== beforeTask.generatedExcelFileName) {
      return current;
    }
    return null;
  }, 'report task regenerate reflected in API', { timeoutMs: 30000, intervalMs: 1000 });

  await waitForBodyIncludes(updatedTask.generatedWordFileName, 'new word filename missing from page', { timeoutMs: 30000 });
  await waitForBodyIncludes(updatedTask.generatedExcelFileName, 'new excel filename missing from page', { timeoutMs: 30000 });

  clickTableRowAction([smokeTaskName, updatedTask.generatedWordFileName], '生成历史');
  await waitForBodyIncludes(`生成历史 - ${smokeTaskName}`, 'history dialog missing');
  await waitForBodyIncludes(beforeTask.generatedWordFileName, 'history missing previous word snapshot');
  await waitForBodyIncludes(updatedTask.generatedWordFileName, 'history missing new word snapshot');

  const latestSnapshot = await waitFor(async () => {
    const snapshots = await ajaxOk('GET', `/nocontact/report/task/${beforeTask.taskId}/snapshot/list`, {
      token
    }, 'report snapshot list after regenerate');
    if ((snapshots.data || []).length > beforeSnapshotCount) {
      return snapshots.data[0];
    }
    return null;
  }, 'snapshot list did not grow after regenerate', { timeoutMs: 30000, intervalMs: 1000 });
  assert(latestSnapshot.generatedWordFileName === updatedTask.generatedWordFileName, 'latest snapshot not aligned with current word file');

  const taskWord = await binaryRequest('POST', `/nocontact/report/task/${beforeTask.taskId}/download/word`, token);
  const taskExcel = await binaryRequest('POST', `/nocontact/report/task/${beforeTask.taskId}/download/excel`, token);
  const snapshotWord = await binaryRequest('POST', `/nocontact/report/task/snapshot/${latestSnapshot.snapshotId}/download/word`, token);
  assert(taskWord.buffer.length > 0, 'task word download is empty');
  assert(taskExcel.buffer.length > 0, 'task excel download is empty');
  assert(snapshotWord.buffer.length > 0, 'snapshot word download is empty');
  assert(taskWord.disposition.includes(encodedFilename(updatedTask.generatedWordFileName)), 'task word disposition mismatch');
  assert(taskExcel.disposition.includes(encodedFilename(updatedTask.generatedExcelFileName)), 'task excel disposition mismatch');
  assert(snapshotWord.disposition.includes(encodedFilename(latestSnapshot.generatedWordFileName)), 'snapshot word disposition mismatch');

  closeAllPages();
  console.log('smoke-issue-013: report generation e2e passed');
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
