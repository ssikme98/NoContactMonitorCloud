const { execFileSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const gatewayUrl = process.env.NOCONTACT_GATEWAY_URL || 'http://127.0.0.1:18080';
const frontendUrl = process.env.NOCONTACT_FRONTEND_URL || 'http://127.0.0.1:18081';
const repoRoot = path.resolve(__dirname, '..', '..');
const writeFlagName = 'NOCONTACT_SMOKE_WRITE';

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function runAgentBrowser(args, options = {}) {
  return execFileSync('agent-browser', args, {
    encoding: 'utf8',
    stdio: ['pipe', 'pipe', 'pipe'],
    ...options
  }).trim();
}

function agentEval(script) {
  const output = runAgentBrowser(['eval', '--stdin'], {
    input: `${script}\n`
  });
  return output ? JSON.parse(output) : null;
}

function bodyText() {
  return agentEval('document.body.innerText') || '';
}

function buildRouteBootstrapScript(targetPath) {
  return `
(() => {
  const targetPath = ${JSON.stringify(targetPath)};
  const wait = (ms) => new Promise(resolve => setTimeout(resolve, ms));
  return (async () => {
    const root = document.querySelector('#app') && document.querySelector('#app').__vue__;
    if (!root || !root.$store || !root.$router) {
      return { ready: false, stage: 'root-missing' };
    }
    if ((root.$store.state.permission.sidebarRouters || []).length === 0) {
      await root.$store.dispatch('GetInfo');
      await root.$store.dispatch('GenerateRoutes');
    }
    if (root.$route.path !== targetPath) {
      await root.$router.replace(targetPath);
      await wait(150);
    }
    return {
      ready: root.$route.path === targetPath,
      route: root.$route.path,
      matched: (root.$route.matched || []).map(item => item.path),
      sidebarRouters: (root.$store.state.permission.sidebarRouters || []).length
    };
  })();
})()
`;
}

async function jsonRequest(method, requestPath, { token, body, query } = {}) {
  const url = new URL(requestPath, gatewayUrl);
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null) {
        url.searchParams.set(key, value);
      }
    }
  }
  const headers = { Accept: '*/*' };
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const response = await fetch(url, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  const text = await response.text();
  const data = text ? JSON.parse(text) : {};
  return { status: response.status, data, headers: response.headers };
}

async function ajaxOk(method, requestPath, options, label) {
  const response = await jsonRequest(method, requestPath, options);
  assert(response.status >= 200 && response.status < 300, `${label} HTTP ${response.status}`);
  assert(response.data.code === 200, `${label} failed: ${response.data.msg || JSON.stringify(response.data)}`);
  return response.data;
}

async function binaryRequest(method, requestPath, token) {
  const response = await fetch(new URL(requestPath, gatewayUrl), {
    method,
    headers: {
      Accept: '*/*',
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });
  assert(response.status >= 200 && response.status < 300, `download HTTP ${response.status}`);
  const disposition = response.headers.get('content-disposition') || '';
  const buffer = Buffer.from(await response.arrayBuffer());
  return { disposition, buffer };
}

function closeAllPages() {
  // Smoke 使用独立浏览器上下文，开始和结束时都清空 agent-browser 页面。
  runAgentBrowser(['close', '--all']);
}

function resolveFixedCaptchaCode() {
  const configFile = path.join(repoRoot, 'ruoyi-gateway/src/main/java/com/ruoyi/gateway/config/CaptchaConfig.java');
  const creatorFile = path.join(repoRoot, 'ruoyi-gateway/src/main/java/com/ruoyi/gateway/config/KaptchaTextCreator.java');
  assert(fs.existsSync(configFile), 'captcha config file missing');
  assert(fs.existsSync(creatorFile), 'captcha text creator file missing');
  const configText = fs.readFileSync(configFile, 'utf8');
  const creatorText = fs.readFileSync(creatorFile, 'utf8');
  assert(configText.includes('com.ruoyi.gateway.config.KaptchaTextCreator'), 'captcha smoke requires fixed KaptchaTextCreator');
  const match = creatorText.match(/FIXED_RESULT\s*=\s*"([^"]+)"/);
  assert(match, 'captcha fixed result missing');
  return match[1];
}

async function login() {
  const codeResponse = await ajaxOk('GET', '/code', {}, 'captcha');
  const body = {
    username: 'admin',
    password: 'admin123'
  };
  if (codeResponse.captchaEnabled) {
    assert(codeResponse.uuid, 'captcha uuid missing');
    body.code = resolveFixedCaptchaCode();
    body.uuid = codeResponse.uuid;
  }
  const loginResponse = await ajaxOk('POST', '/auth/login', { body }, 'login');
  const token = loginResponse.data && loginResponse.data.access_token;
  assert(token, 'login did not return access token');
  return {
    token,
    expiresIn: String(loginResponse.data.expires_in || 720)
  };
}

function renderValue(value) {
  if (typeof value === 'string') {
    return value;
  }
  try {
    return JSON.stringify(value);
  } catch (error) {
    return String(value);
  }
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function waitFor(check, label, { timeoutMs = 30000, intervalMs = 500 } = {}) {
  const deadline = Date.now() + timeoutMs;
  let lastValue = null;
  while (Date.now() < deadline) {
    lastValue = await check();
    if (lastValue) {
      return lastValue;
    }
    await sleep(intervalMs);
  }
  throw new Error(`${label} timed out${lastValue ? `: ${renderValue(lastValue)}` : ''}`);
}

async function waitForBodyIncludes(text, label, options) {
  return waitFor(() => bodyText().includes(text), label || `body includes ${text}`, options);
}

function normalizeRowMatch(parts) {
  return Array.isArray(parts) ? parts : [parts];
}

function buildRowFinder(parts) {
  const expectedParts = normalizeRowMatch(parts);
  return `
(() => {
  const parts = ${JSON.stringify(expectedParts)};
  const rows = Array.from(document.querySelectorAll('.el-table__body-wrapper tbody tr'));
  const row = rows.find(item => parts.every(part => item.innerText.includes(part)));
  return row ? row.innerText : null;
})()
`;
}

async function waitForTableRow(parts, label, options) {
  return waitFor(() => agentEval(buildRowFinder(parts)), label, options);
}

function clickTableRowAction(parts, actionText) {
  const result = agentEval(`
(() => {
  const parts = ${JSON.stringify(normalizeRowMatch(parts))};
  const actionText = ${JSON.stringify(actionText)};
  const rows = Array.from(document.querySelectorAll('.el-table__body-wrapper tbody tr'));
  const rowIndex = rows.findIndex(item => parts.every(part => item.innerText.includes(part)));
  if (rowIndex < 0) {
    return { ok: false, reason: 'row missing' };
  }
  const candidateRows = [rows[rowIndex]];
  const fixedRows = Array.from(document.querySelectorAll('.el-table__fixed-right .el-table__fixed-body-wrapper tbody tr, .el-table__fixed .el-table__fixed-body-wrapper tbody tr'));
  if (fixedRows[rowIndex]) {
    candidateRows.push(fixedRows[rowIndex]);
  }
  const buttons = candidateRows.flatMap(item => Array.from(item.querySelectorAll('button')));
  const button = buttons.find(item => item.innerText.replace(/\\s+/g, '').includes(actionText.replace(/\\s+/g, '')));
  if (!button) {
    return { ok: false, reason: 'button missing', rowText: rows[rowIndex].innerText };
  }
  button.click();
  return { ok: true };
})()
`);
  assert(result && result.ok, `row action ${actionText} failed: ${renderValue(result)}`);
}

function setInputValueByPlaceholder(placeholder, value) {
  const result = agentEval(`
(() => {
  const placeholder = ${JSON.stringify(placeholder)};
  const value = ${JSON.stringify(value)};
  const input = Array.from(document.querySelectorAll('input')).find(item => item.getAttribute('placeholder') === placeholder);
  if (!input) {
    return { ok: false, reason: 'input missing' };
  }
  input.focus();
  input.value = value;
  input.dispatchEvent(new Event('input', { bubbles: true }));
  input.dispatchEvent(new Event('change', { bubbles: true }));
  input.blur();
  return { ok: true };
})()
`);
  assert(result && result.ok, `set input failed: ${renderValue(result)}`);
}

function clickTextButton(text) {
  const result = agentEval(`
(() => {
  const text = ${JSON.stringify(text)};
  const button = Array.from(document.querySelectorAll('button')).find(item => item.innerText.replace(/\\s+/g, '').includes(text.replace(/\\s+/g, '')));
  if (!button) {
    return { ok: false, reason: 'button missing' };
  }
  button.click();
  return { ok: true };
})()
`);
  assert(result && result.ok, `click button failed: ${renderValue(result)}`);
}

function clickInputByPlaceholder(placeholder) {
  const result = agentEval(`
(() => {
  const placeholder = ${JSON.stringify(placeholder)};
  const input = Array.from(document.querySelectorAll('input')).find(item => item.getAttribute('placeholder') === placeholder);
  if (!input) {
    return { ok: false, reason: 'input missing' };
  }
  const target = input.closest('.el-input') || input;
  target.click();
  input.focus();
  return { ok: true };
})()
`);
  assert(result && result.ok, `click input failed: ${renderValue(result)}`);
}

function clickElementByText(text, selectors = '*') {
  const result = agentEval(`
(() => {
  const text = ${JSON.stringify(text)};
  const selectors = ${JSON.stringify(selectors)};
  const nodes = Array.from(document.querySelectorAll(selectors));
  const node = nodes.find(item => item.innerText && item.innerText.replace(/\\s+/g, '').includes(text.replace(/\\s+/g, '')));
  if (!node) {
    return { ok: false, reason: 'element missing' };
  }
  node.click();
  return { ok: true };
})()
`);
  assert(result && result.ok, `click element failed: ${renderValue(result)}`);
}

function tableCellTextByRowParts(parts, columnLabel) {
  const result = agentEval(`
(() => {
  const parts = ${JSON.stringify(normalizeRowMatch(parts))};
  const columnLabel = ${JSON.stringify(columnLabel)};
  const table = Array.from(document.querySelectorAll('.el-table')).find(item => {
    const rows = Array.from(item.querySelectorAll('.el-table__body-wrapper tbody tr'));
    return rows.some(row => parts.every(part => row.innerText.includes(part)));
  });
  if (!table) {
    return { ok: false, reason: 'table missing' };
  }
  const headers = Array.from(table.querySelectorAll('.el-table__header-wrapper thead th .cell')).map(item => item.innerText.trim());
  const columnIndex = headers.findIndex(text => text === columnLabel);
  if (columnIndex < 0) {
    return { ok: false, reason: 'column missing', headers };
  }
  const rows = Array.from(table.querySelectorAll('.el-table__body-wrapper tbody tr'));
  const row = rows.find(item => parts.every(part => item.innerText.includes(part)));
  if (!row) {
    return { ok: false, reason: 'row missing' };
  }
  const cells = Array.from(row.querySelectorAll('td'));
  const cell = cells[columnIndex];
  if (!cell) {
    return { ok: false, reason: 'cell missing', columnIndex };
  }
  return { ok: true, text: cell.innerText.trim() };
})()
`);
  assert(result && result.ok, `table cell lookup failed: ${renderValue(result)}`);
  return result.text;
}

async function openAuthedPage(routePath, token, expiresIn) {
  closeAllPages();
  runAgentBrowser(['open', `${frontendUrl}/login`]);
  await waitFor(() => agentEval('document.readyState === "complete"'), 'login page ready', { timeoutMs: 10000 });
  agentEval(`
(() => {
  document.cookie = "Admin-Token=${token}; path=/";
  document.cookie = "Admin-Expires-In=${expiresIn}; path=/";
  return 'cookies-set';
})()
  `);
  runAgentBrowser(['open', `${frontendUrl}${routePath}`]);
  await waitFor(() => agentEval('document.readyState === "complete"'), `${routePath} page ready`, { timeoutMs: 10000 });
  const bootstrap = await waitFor(() => agentEval(buildRouteBootstrapScript(routePath)), `${routePath} route bootstrap`, {
    timeoutMs: 20000,
    intervalMs: 500
  });
  assert(bootstrap && bootstrap.ready, `${routePath} route bootstrap failed: ${renderValue(bootstrap)}`);
  await waitFor(() => agentEval('document.readyState === "complete"'), `${routePath} page ready after bootstrap`, { timeoutMs: 10000 });
}

function encodedFilename(filename) {
  return encodeURIComponent(filename).replace(/%20/g, '+');
}

function requireWriteGuard(reason) {
  assert(process.env[writeFlagName] === '1', `${writeFlagName}=1 required: ${reason}`);
}

module.exports = {
  agentEval,
  ajaxOk,
  assert,
  binaryRequest,
  bodyText,
  clickElementByText,
  clickInputByPlaceholder,
  clickTableRowAction,
  clickTextButton,
  closeAllPages,
  encodedFilename,
  frontendUrl,
  gatewayUrl,
  login,
  buildRouteBootstrapScript,
  openAuthedPage,
  requireWriteGuard,
  setInputValueByPlaceholder,
  tableCellTextByRowParts,
  waitFor,
  waitForBodyIncludes,
  waitForTableRow
};
