const {
  ajaxOk,
  agentEval,
  assert,
  closeAllPages,
  login,
  openAuthedPage,
  requireWriteGuard,
  setInputValueByPlaceholder,
  waitFor,
  waitForBodyIncludes,
  waitForTableRow
} = require('./lib/nocontactSmoke');

async function listWarnings(token) {
  const response = await ajaxOk('GET', '/nocontact/warning/message/list', {
    token,
    query: { pageNum: 1, pageSize: 200 }
  }, 'warning message list');
  return response.rows || [];
}

async function listWarningRules(token) {
  const response = await ajaxOk('GET', '/nocontact/warning/rule/list', {
    token,
    query: { pageNum: 1, pageSize: 50 }
  }, 'warning rule list');
  return response.rows || [];
}

async function getWarning(token, messageId) {
  const response = await ajaxOk('GET', `/nocontact/warning/message/${messageId}`, { token }, 'warning message detail');
  return response.data;
}

async function listIssues(token, query = {}) {
  const response = await ajaxOk('GET', '/nocontact/rectification/issue/list', {
    token,
    query: Object.assign({ pageNum: 1, pageSize: 50 }, query)
  }, 'rectification issue list');
  return response.rows || [];
}

async function triggerScheduledEvaluation(periodKey) {
  const response = await fetch(`http://127.0.0.1:9204/warning/evaluation/scheduled/${periodKey}`, {
    method: 'POST',
    headers: { 'from-source': 'inner' }
  });
  assert(response.ok, `scheduled warning evaluation HTTP ${response.status}`);
  const payload = await response.json();
  assert(payload.code === 200, `scheduled warning evaluation failed: ${payload.msg || JSON.stringify(payload)}`);
  return payload.data;
}

function monthKeyMonthsAgo(monthsAgo) {
  const now = new Date();
  const baseMonth = now.getFullYear() * 12 + now.getMonth() - monthsAgo;
  const year = Math.floor(baseMonth / 12);
  const month = baseMonth % 12;
  return `${year}-${String(month + 1).padStart(2, '0')}`;
}

function hasActiveIssue(issues, warningId) {
  return issues.some(item => item.sourceWarningId === warningId && item.issueStatus !== 'closed' && item.issueStatus !== 'archived');
}

async function ensureWarningFixture(token) {
  const rules = await listWarningRules(token);
  const overdueRule = rules.find(item => item.triggerCondition === 'overdue' && item.status === '0');
  assert(overdueRule, 'no enabled overdue warning rule available for rectification smoke');

  const existingWarnings = await listWarnings(token);
  const existingIssues = await listIssues(token);
  for (const row of existingWarnings) {
    if (row.ruleId !== overdueRule.ruleId || !row.periodKey || row.messageStatus !== 'pending') {
      continue;
    }
    if (!hasActiveIssue(existingIssues, row.messageId)) {
      return row;
    }
  }

  const occupiedPeriods = new Set(
    existingWarnings
      .filter(item => item.ruleId === overdueRule.ruleId && item.messageStatus !== 'closed' && item.messageStatus !== 'ignored')
      .map(item => item.periodKey)
      .filter(Boolean)
  );

  for (let monthsAgo = 1; monthsAgo <= 60; monthsAgo++) {
    const periodKey = monthKeyMonthsAgo(monthsAgo);
    if (occupiedPeriods.has(periodKey)) {
      continue;
    }
    await triggerScheduledEvaluation(periodKey);
    const warning = await waitFor(async () => {
      const warnings = await listWarnings(token);
      const issues = await listIssues(token);
      return warnings.find(item => item.ruleId === overdueRule.ruleId
        && item.periodKey === periodKey
        && item.messageStatus === 'pending'
        && !hasActiveIssue(issues, item.messageId)) || null;
    }, `scheduled warning fixture ${periodKey}`, { timeoutMs: 30000, intervalMs: 1000 });
    if (warning) {
      return warning;
    }
  }
  throw new Error('no reusable scheduled warning available for rectification smoke');
}

async function main() {
  requireWriteGuard('warning rectification smoke will generate a dedicated overdue warning and create a rectification issue from it');
  const { token, expiresIn } = await login();
  const warning = await ensureWarningFixture(token);
  const issueTitle = `SMOKE-WARNING-RECT-${warning.messageId}`;

  await openAuthedPage('/warning/message', token, expiresIn);
  await waitForBodyIncludes('规则名称', 'warning message page did not render', { timeoutMs: 60000 });
  setInputValueByPlaceholder('规则名称', warning.ruleName);
  agentEval(`
  (() => {
  const button = Array.from(document.querySelectorAll('button')).find(item => item.innerText.replace(/\\s+/g, '').includes('查询'));
  if (!button) return false;
  button.click();
  return true;
})()
`);
  await waitForTableRow([warning.ruleName, warning.indicatorName, warning.periodKey], 'warning row for rectification', { timeoutMs: 60000 });

  const openResult = agentEval(`
(() => {
  const rows = Array.from(document.querySelectorAll('.el-table__body-wrapper tbody tr'));
  const row = rows.find(item => item.innerText.includes(${JSON.stringify(warning.ruleName)})
    && item.innerText.includes(${JSON.stringify(warning.indicatorName)})
    && item.innerText.includes(${JSON.stringify(warning.periodKey)}));
  if (!row) return { ok: false, reason: 'row missing' };
  const fixedRows = Array.from(document.querySelectorAll('.el-table__fixed-right .el-table__fixed-body-wrapper tbody tr, .el-table__fixed .el-table__fixed-body-wrapper tbody tr'));
  const rowIndex = rows.indexOf(row);
  const candidates = [row];
  if (fixedRows[rowIndex]) candidates.push(fixedRows[rowIndex]);
  const buttons = candidates.flatMap(item => Array.from(item.querySelectorAll('button')));
  const button = buttons.find(item => item.innerText.replace(/\\s+/g, '').includes('转整改'));
  if (!button) return { ok: false, reason: 'button missing', rowText: row.innerText };
  button.click();
  return { ok: true };
})()
`);
  assert(openResult && openResult.ok, `open rectification dialog failed: ${JSON.stringify(openResult)}`);

  await waitForBodyIncludes('转入整改', 'rectification dialog did not render', { timeoutMs: 30000 });
  agentEval(`
(() => {
  const values = {
    title: ${JSON.stringify(issueTitle)},
    unit: ${JSON.stringify(warning.responsibleUnitName || '省数据局')},
    person: '烟测责任人',
    supervisor: '烟测督办人',
    description: '来源预警自动转整改闭环烟测'
  };
  const inputs = Array.from(document.querySelectorAll('.el-dialog__body input'));
  if (inputs[0]) { inputs[0].value = values.title; inputs[0].dispatchEvent(new Event('input', { bubbles: true })); }
  if (inputs[1]) { inputs[1].value = values.unit; inputs[1].dispatchEvent(new Event('input', { bubbles: true })); }
  if (inputs[2]) { inputs[2].value = values.person; inputs[2].dispatchEvent(new Event('input', { bubbles: true })); }
  if (inputs[3]) { inputs[3].value = values.supervisor; inputs[3].dispatchEvent(new Event('input', { bubbles: true })); }
  const textarea = document.querySelector('.el-dialog__body textarea');
  if (textarea) { textarea.value = values.description; textarea.dispatchEvent(new Event('input', { bubbles: true })); }
  return true;
})()
`);
  agentEval(`
(() => {
  const dialogs = Array.from(document.querySelectorAll('.el-dialog')).filter(item => item.style.display !== 'none');
  const dialog = dialogs[dialogs.length - 1];
  if (!dialog) return false;
  const button = Array.from(dialog.querySelectorAll('button')).find(item => item.innerText.replace(/\\s+/g, '') === '确定' || item.innerText.replace(/\\s+/g, '') === '确 定');
  if (!button) return false;
  button.click();
  return true;
})()
`);
  await waitForBodyIncludes('已转入整改', 'rectification success toast', { timeoutMs: 30000 });

  const createdIssue = await waitFor(async () => {
    const rows = await listIssues(token, { issueTitle });
    return rows.find(item => item.issueTitle === issueTitle) || null;
  }, 'created rectification issue', { timeoutMs: 30000, intervalMs: 1000 });
  assert(createdIssue.sourceWarningId === warning.messageId, 'rectification issue did not keep source warning');
  assert(createdIssue.issueStatus === 'pending_dispatch', 'rectification issue status mismatch');

  const updatedWarning = await getWarning(token, warning.messageId);
  assert(updatedWarning.messageStatus === 'processing', 'warning status was not promoted to processing');

  await openAuthedPage('/rectification/issue', token, expiresIn);
  await waitForTableRow([issueTitle, '待分配'], 'created rectification issue row', { timeoutMs: 60000 });

  closeAllPages();
  console.log('smoke-issue-warning-rectification: warning to rectification e2e passed');
}

main().catch(error => {
  try {
    closeAllPages();
  } catch (closeError) {
    // ignore close failure on abort
  }
  console.error(error.message || error);
  process.exit(1);
});
