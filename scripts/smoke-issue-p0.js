const {
  agentEval,
  ajaxOk,
  assert,
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
} = require('./lib/nocontactSmoke')

async function listPublishedTasks(token) {
  const response = await ajaxOk('GET', '/nocontact/fusion/task/list', {
    token,
    query: { pageNum: 1, pageSize: 50, taskStatus: 'published' }
  }, 'fusion published task list')
  return response.rows || []
}

async function listApprovedBatches(token) {
  const response = await ajaxOk('GET', '/nocontact/fusion/collection/list', {
    token,
    query: { pageNum: 1, pageSize: 50, batchStatus: 'approved' }
  }, 'fusion approved batch list')
  return response.rows || []
}

async function listPendingBatches(token) {
  const response = await ajaxOk('GET', '/nocontact/fusion/collection/list', {
    token,
    query: { pageNum: 1, pageSize: 50, batchStatus: 'pending_audit' }
  }, 'fusion pending batch list')
  return response.rows || []
}

async function approveBatch(token, batchId) {
  await ajaxOk('PUT', `/nocontact/fusion/collection/${batchId}/approve`, {
    token,
    query: { opinion: '烟测审核通过' }
  }, 'approve fusion batch')
}

async function listWarningRules(token) {
  const response = await ajaxOk('GET', '/nocontact/warning/rule/list', {
    token,
    query: { pageNum: 1, pageSize: 50, status: '0' }
  }, 'warning rule list')
  return response.rows || []
}

async function listWarnings(token, query = {}) {
  const response = await ajaxOk('GET', '/nocontact/warning/message/list', {
    token,
    query: Object.assign({ pageNum: 1, pageSize: 200 }, query)
  }, 'warning message list')
  return response.rows || []
}

async function getWarning(token, messageId) {
  const response = await ajaxOk('GET', `/nocontact/warning/message/${messageId}`, { token }, 'warning message detail')
  return response.data
}

async function getWarningDashboard(token) {
  const response = await ajaxOk('GET', '/nocontact/warning/message/dashboard', { token }, 'warning dashboard api')
  return response.data || {}
}

async function triggerScheduledEvaluation(periodKey) {
  const response = await fetch(`http://127.0.0.1:9204/warning/evaluation/scheduled/${periodKey}`, {
    method: 'POST',
    headers: { 'from-source': 'inner' }
  })
  assert(response.ok, `scheduled warning evaluation HTTP ${response.status}`)
  const payload = await response.json()
  assert(payload.code === 200, `scheduled warning evaluation failed: ${payload.msg || JSON.stringify(payload)}`)
  return payload.data
}

function monthKeyMonthsAgo(monthsAgo) {
  const now = new Date()
  const baseMonth = now.getFullYear() * 12 + now.getMonth() - monthsAgo
  const year = Math.floor(baseMonth / 12)
  const month = baseMonth % 12
  return `${year}-${String(month + 1).padStart(2, '0')}`
}

async function ensureGeneratedWarning(token) {
  const rules = await listWarningRules(token)
  const overdueRule = rules.find(item => item.triggerCondition === 'overdue' && item.status === '0')
  assert(overdueRule, 'no enabled overdue warning rule available for P0 smoke')

  const existingWarnings = await listWarnings(token)
  const occupiedPeriods = new Set(
    existingWarnings
      .filter(item => item.ruleId === overdueRule.ruleId && item.messageStatus !== 'closed' && item.messageStatus !== 'ignored')
      .map(item => item.periodKey)
      .filter(Boolean)
  )

  for (let monthsAgo = 1; monthsAgo <= 60; monthsAgo++) {
    const periodKey = monthKeyMonthsAgo(monthsAgo)
    if (occupiedPeriods.has(periodKey)) {
      continue
    }
    await triggerScheduledEvaluation(periodKey)
    const warning = await waitFor(async () => {
      const warnings = await listWarnings(token, { ruleName: overdueRule.ruleName, periodKey })
      return warnings.find(item => item.ruleId === overdueRule.ruleId
        && item.periodKey === periodKey
        && item.messageStatus === 'pending') || null
    }, `generated warning for ${periodKey}`, { timeoutMs: 30000, intervalMs: 1000 })
    if (warning) {
      return warning
    }
  }

  throw new Error('failed to generate a new warning message for P0 smoke')
}

async function main() {
  requireWriteGuard('P0 smoke will trigger scheduled warning evaluation and update one warning to processing')
  const { token, expiresIn } = await login()

  const publishedTasks = await listPublishedTasks(token)
  assert(publishedTasks.length > 0, 'no published fusion task available for P0 smoke')
  const publishedTask = publishedTasks[0]

  let approvedBatches = await listApprovedBatches(token)
  if (approvedBatches.length === 0) {
    const pendingBatches = await listPendingBatches(token)
    assert(pendingBatches.length > 0, 'no auditable collection batch available for P0 smoke')
    await approveBatch(token, pendingBatches[0].batchId)
    approvedBatches = await waitFor(async () => {
      const rows = await listApprovedBatches(token)
      return rows.find(item => Number(item.batchId) === Number(pendingBatches[0].batchId)) || null
    }, 'approved fusion batch after smoke approval', { timeoutMs: 30000, intervalMs: 1000 }).then(row => [row])
  }
  const approvedBatch = approvedBatches[0]

  const warning = await ensureGeneratedWarning(token)
  const dashboard = await getWarningDashboard(token)
  assert(Array.isArray(dashboard.levelStats) && dashboard.levelStats.length > 0, 'warning dashboard level stats are empty')
  assert(Array.isArray(dashboard.regionStats) && dashboard.regionStats.length > 0, 'warning dashboard region stats are empty')
  assert(Array.isArray(dashboard.indicatorStats) && dashboard.indicatorStats.length > 0, 'warning dashboard indicator stats are empty')

  await openAuthedPage('/collection/task', token, expiresIn)
  await waitForBodyIncludes('新增采集任务', 'fusion task page did not render', { timeoutMs: 60000 })
  setInputValueByPlaceholder('请输入任务名称', publishedTask.taskName)
  clickTextButton('查询')
  await waitForTableRow([publishedTask.taskName], 'published fusion task row', { timeoutMs: 60000 })

  await openAuthedPage('/collection/audit', token, expiresIn)
  await waitForBodyIncludes('提交采集数据', 'fusion audit page did not render', { timeoutMs: 60000 })
  setInputValueByPlaceholder('请输入批次名称', approvedBatch.batchName)
  clickTextButton('查询')
  await waitForTableRow([approvedBatch.batchName, '已审核'], 'approved fusion batch row', { timeoutMs: 60000 })

  await openAuthedPage('/warning/message', token, expiresIn)
  await waitForBodyIncludes('规则名称', 'warning message page did not render', { timeoutMs: 60000 })
  setInputValueByPlaceholder('规则名称', warning.ruleName)
  clickTextButton('查询')
  await waitForTableRow([warning.ruleName, warning.indicatorName, warning.periodKey, '待处理'], 'generated warning row', { timeoutMs: 60000 })
  clickTableRowAction([warning.ruleName, warning.indicatorName, warning.periodKey], '处理')
  await waitForBodyIncludes('请输入处理意见', 'warning processing prompt missing', { timeoutMs: 30000 })
  const confirmProcessing = agentEval(`
(() => {
  const wrapper = Array.from(document.querySelectorAll('.el-message-box__wrapper'))
    .find(item => item.style.display !== 'none');
  if (!wrapper) {
    return { ok: false, reason: 'prompt missing' };
  }
  const input = wrapper.querySelector('input, textarea');
  if (input) {
    input.focus();
    input.value = '开始处理';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    input.dispatchEvent(new Event('change', { bubbles: true }));
    input.blur();
  }
  const button = Array.from(wrapper.querySelectorAll('button'))
    .find(item => item.innerText.replace(/\\s+/g, '') === '确定');
  if (!button) {
    return { ok: false, reason: 'confirm button missing' };
  }
  button.click();
  return { ok: true };
})()
  `)
  assert(confirmProcessing && confirmProcessing.ok, `warning processing prompt confirm failed: ${JSON.stringify(confirmProcessing)}`)
  await waitForBodyIncludes('状态已更新', 'warning processing toast missing', { timeoutMs: 30000 })

  await waitFor(async () => {
    const updated = await getWarning(token, warning.messageId)
    return updated && updated.messageStatus === 'processing' ? updated : null
  }, 'warning processing status reflected in api', { timeoutMs: 30000, intervalMs: 1000 })

  setInputValueByPlaceholder('规则名称', warning.ruleName)
  clickTextButton('查询')
  await waitForTableRow([warning.ruleName, warning.indicatorName, warning.periodKey, '处理中'], 'processing warning row', { timeoutMs: 60000 })

  await openAuthedPage('/warning/dashboard', token, expiresIn)
  await waitForBodyIncludes('分级预警看板', 'warning dashboard page did not render', { timeoutMs: 60000 })
  for (const label of ['当前预警总数', '今日新增预警数', '待处理数', '级别分布', '地区分布', '指标TOP10', '近30天预警趋势']) {
    await waitForBodyIncludes(label, `warning dashboard missing ${label}`, { timeoutMs: 30000 })
  }

  closeAllPages()
  console.log('smoke-issue-p0: fusion warning runtime closure e2e passed')
}

main().catch(error => {
  try {
    closeAllPages()
  } catch (closeError) {
    // ignore close failure on abort
  }
  console.error(error.message || error)
  process.exit(1)
})
