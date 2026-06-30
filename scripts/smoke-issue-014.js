const {
  ajaxOk,
  assert,
  clickElementByText,
  clickTableRowAction,
  closeAllPages,
  login,
  openAuthedPage,
  requireWriteGuard,
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

async function listMessages(token) {
  const response = await ajaxOk('GET', '/nocontact/support/message/list', { token }, 'support message list');
  return response.rows || [];
}

async function countPendingAudit(token) {
  const response = await ajaxOk('GET', '/nocontact/fusion/collection/list', {
    token,
    query: { pageNum: 1, pageSize: 200, batchStatus: 'pending_audit' }
  }, 'fusion pending audit list');
  return Number(response.total || 0);
}

async function countPendingSurveySamples(token) {
  const taskResponse = await ajaxOk('GET', '/survey/task/list', {
    token,
    query: { pageNum: 1, pageSize: 200, status: '2' }
  }, 'survey dispatched task list');
  const rows = taskResponse.rows || [];
  let total = 0;
  for (const item of rows) {
    const trackingResponse = await ajaxOk('GET', `/survey/task/${item.taskId}/track/summary`, { token }, `survey task ${item.taskId} tracking summary`);
    const summary = trackingResponse.data || {};
    total += Number(summary.unsubmittedCount || 0);
  }
  return total;
}

async function countPendingWarnings(token) {
  const response = await ajaxOk('GET', '/nocontact/warning/message/list', {
    token,
    query: { pageNum: 1, pageSize: 200, messageStatus: 'pending' }
  }, 'warning pending list');
  return Number(response.total || 0);
}

async function countRectificationIssues(token, issueStatus) {
  const response = await ajaxOk('GET', '/nocontact/rectification/issue/list', {
    token,
    query: { pageNum: 1, pageSize: 200, issueStatus }
  }, `rectification ${issueStatus} list`);
  return Number(response.total || 0);
}

async function markMessageRead(token, messageId) {
  await ajaxOk('PUT', `/nocontact/support/message/${messageId}/read`, { token }, 'mark support message read');
}

async function prepareUnreadFixtureMessage(token) {
  const task = await ensureSmokeTask(token);
  await getTaskById(token, task.taskId);
  const messages = await listMessages(token);
  for (const message of messages.filter(item => item.businessId === task.taskId && item.readStatus !== '1')) {
    await markMessageRead(token, message.messageId);
  }
  await ajaxOk('PUT', `/nocontact/report/task/${task.taskId}/generate`, { token }, 'generate support fixture message');
  const unreadMessage = await waitFor(async () => {
    const latestMessages = await listMessages(token);
    return latestMessages.find(item => item.businessId === task.taskId && item.readStatus !== '1') || null;
  }, 'fixture unread message', { timeoutMs: 30000, intervalMs: 1000 });
  return { task, unreadMessage };
}

async function main() {
  requireWriteGuard('support smoke will regenerate the dedicated report task and mark its message as read');
  const { token, expiresIn } = await login();
  const { unreadMessage } = await prepareUnreadFixtureMessage(token);

  const todoSummary = await ajaxOk('GET', '/nocontact/support/todo/summary', { token }, 'todo summary');
  const todoRows = todoSummary.data || [];
  assert(todoRows.length === 5, 'todo summary buckets are incomplete');
  const fillTodo = todoRows.find(item => item.todoType === '待填报');
  const rectificationTodo = todoRows.find(item => item.todoType === '待整改');
  assert(fillTodo, 'todo fill bucket missing');
  assert(fillTodo.jumpTarget === '/support/todo?focus=fill', 'todo fill bucket jump target mismatch');
  const fusionFillResponse = await ajaxOk('GET', '/nocontact/fusion/task/list', {
    token,
    query: { pageNum: 1, pageSize: 200, taskType: 'fill', taskStatus: 'published' }
  }, 'fusion fill tasks');
  const surveyPendingCount = await countPendingSurveySamples(token);
  assert(fillTodo.todoCount === (fusionFillResponse.total || 0) + surveyPendingCount, 'todo fill bucket mismatch');
  assert(rectificationTodo, 'todo rectification bucket missing');
  const auditTodo = todoRows.find(item => item.todoType === '待审核');
  const warningTodo = todoRows.find(item => item.todoType === '待处理预警');
  const reviewTodo = todoRows.find(item => item.todoType === '待复核');
  assert(auditTodo, 'todo audit bucket missing');
  assert(warningTodo, 'todo warning bucket missing');
  assert(reviewTodo, 'todo review bucket missing');
  assert(auditTodo.todoCount === await countPendingAudit(token), 'todo audit bucket mismatch');
  assert(warningTodo.todoCount === await countPendingWarnings(token), 'todo warning bucket mismatch');
  const pendingRectificationCount = await countRectificationIssues(token, 'pending_rectification');
  const reworkCount = await countRectificationIssues(token, 'rework');
  const rectifyingCount = await countRectificationIssues(token, 'rectifying');
  assert(rectificationTodo.todoCount === pendingRectificationCount + reworkCount + rectifyingCount, 'todo rectification bucket mismatch');
  assert(reviewTodo.todoCount === await countRectificationIssues(token, 'pending_review'), 'todo review bucket mismatch');

  const publicSettings = await ajaxOk('GET', '/nocontact/support/settings/public', { token }, 'support settings');
  assert(publicSettings.data, 'support settings payload is empty');
  assert(publicSettings.data.reportDefaultPeriod, 'default report period missing');
  assert(!Object.prototype.hasOwnProperty.call(publicSettings.data, 'amapGeocodeKey'), 'support settings should not expose geocode key');
  assert(typeof publicSettings.data.amapFrontendKey === 'string', 'frontend amap key payload type mismatch');
  assert(typeof publicSettings.data.amapSecurityJsCode === 'string', 'frontend amap security code payload type mismatch');

  await openAuthedPage('/support/message', token, expiresIn);
  await waitForBodyIncludes('消息中心', 'support message page did not render');
  await waitForTableRow(['报告生成完成', String(unreadMessage.businessId), '未读'], 'fixture unread message row');
  clickTableRowAction(['报告生成完成', String(unreadMessage.businessId), '未读'], '标记已读');
  await waitForBodyIncludes('已标记为已读', 'message read success toast', { timeoutMs: 30000 });
  await waitFor(async () => {
    const messages = await listMessages(token);
    return messages.find(item => item.messageId === unreadMessage.messageId && item.readStatus === '1') || null;
  }, 'message read status reflected in API', { timeoutMs: 30000, intervalMs: 1000 });
  const refreshedMessages = await listMessages(token);
  const refreshedMessage = refreshedMessages.find(item => item.messageId === unreadMessage.messageId);
  assert(refreshedMessage, 'fixture message disappeared after mark read');
  for (const key of ['businessType', 'businessId', 'readStatus', 'jumpTarget']) {
    assert(refreshedMessage[key] !== undefined && refreshedMessage[key] !== null && refreshedMessage[key] !== '', `message payload missing business metadata: ${key}`);
  }

  await openAuthedPage('/support/todo', token, expiresIn);
  for (const label of ['待填报', '待审核', '待处理预警', '待整改', '待复核']) {
    await waitForBodyIncludes(label, `todo page missing ${label}`);
  }
  clickElementByText('待填报', 'div.todo-card, button.todo-card');
  await waitForBodyIncludes('待填报处理入口', 'todo fill focus panel missing');
  await waitForBodyIncludes('采集任务列表', 'todo fill panel missing collection action');
  await waitForBodyIncludes('问卷填报追踪', 'todo fill panel missing survey action');
  clickElementByText('问卷填报追踪', 'button');
  await waitForBodyIncludes('填报追踪', 'todo fill survey action did not open tracking page', { timeoutMs: 30000 });
  await openAuthedPage('/support/todo', token, expiresIn);
  clickElementByText('待填报', 'div.todo-card, button.todo-card');
  clickElementByText('采集任务列表', 'button');
  await waitForBodyIncludes('新增采集任务', 'todo fill collection action did not open task page', { timeoutMs: 30000 });

  await openAuthedPage('/support/settings', token, expiresIn);
  for (const label of ['地图展示配置', '地图安全校验', '地址解析服务', '默认报告周期', '外部同步开关']) {
    await waitForBodyIncludes(label, `settings page missing ${label}`);
  }
  await waitForBodyIncludes('仅后端使用，已单独保管', 'settings page missing geocode secrecy tip');
  if (publicSettings.data.amapFrontendKey) {
    assert(!bodyText().includes(publicSettings.data.amapFrontendKey), 'settings page should not expose frontend key raw text');
  }
  if (publicSettings.data.amapSecurityJsCode) {
    assert(!bodyText().includes(publicSettings.data.amapSecurityJsCode), 'settings page should not expose security code raw text');
  }

  closeAllPages();
  console.log('smoke-issue-014: support center e2e passed');
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
