const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8')
}

function exists(file) {
  return fs.existsSync(path.join(root, file))
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

function assertExists(file) {
  assert(exists(file), `missing ${file}`)
}

function assertContains(file, text) {
  assert(read(file).includes(text), `${file} missing ${text}`)
}

const files = [
  'scripts/verify-issue-014.js',
  'ruoyi-ui/src/api/nocontact/support/index.js',
  'ruoyi-ui/src/views/nocontact/support/message/index.vue',
  'ruoyi-ui/src/views/nocontact/support/todo/index.vue',
  'ruoyi-ui/src/views/nocontact/support/settings/index.vue',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/controller/SupportCenterController.java',
  'scripts/lib/nocontactSmoke.js',
  'scripts/smoke-issue-014.js'
]

files.forEach(assertExists)

assertContains('ruoyi-ui/src/views/nocontact/support/message/index.vue', '标记已读')
assertContains('ruoyi-ui/src/views/nocontact/support/message/index.vue', 'handleRead')

assertContains('ruoyi-ui/src/views/nocontact/support/todo/index.vue', 'todoLabel')
assertContains('ruoyi-ui/src/views/nocontact/support/todo/index.vue', 'listTodoSummary')
assertContains('ruoyi-ui/src/views/nocontact/support/todo/index.vue', 'todoList')
assertContains('ruoyi-ui/src/views/nocontact/support/todo/index.vue', '待填报处理入口')
assertContains('ruoyi-ui/src/views/nocontact/support/todo/index.vue', '采集任务列表')
assertContains('ruoyi-ui/src/views/nocontact/support/todo/index.vue', '问卷填报追踪')

assertContains('ruoyi-ui/src/views/nocontact/support/settings/index.vue', '地图展示配置')
assertContains('ruoyi-ui/src/views/nocontact/support/settings/index.vue', '地图安全校验')
assertContains('ruoyi-ui/src/views/nocontact/support/settings/index.vue', '地址解析服务')
assertContains('ruoyi-ui/src/views/nocontact/support/settings/index.vue', '默认报告周期')
assertContains('ruoyi-ui/src/views/nocontact/support/settings/index.vue', '外部同步开关')

assertContains('ruoyi-ui/src/api/nocontact/support/index.js', 'listBusinessMessage')
assertContains('ruoyi-ui/src/api/nocontact/support/index.js', 'markBusinessMessageRead')
assertContains('ruoyi-ui/src/api/nocontact/support/index.js', 'listTodoSummary')
assertContains('ruoyi-ui/src/api/nocontact/support/index.js', 'getSupportPublicSettings')

assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/controller/SupportCenterController.java', '/support/message/list')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/controller/SupportCenterController.java', '/support/message/{messageId}/read')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/controller/SupportCenterController.java', '/support/todo/summary')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/controller/SupportCenterController.java', '/support/settings/public')

assertContains('scripts/lib/nocontactSmoke.js', 'NOCONTACT_SMOKE_WRITE')
assertContains('scripts/lib/nocontactSmoke.js', 'agent-browser')
assertContains('scripts/lib/nocontactSmoke.js', 'clickTableRowAction')
assertContains('scripts/lib/nocontactSmoke.js', 'waitFor')

assertContains('scripts/smoke-issue-014.js', 'requireWriteGuard')
assertContains('scripts/smoke-issue-014.js', 'mark its message as read')
assertContains('scripts/smoke-issue-014.js', 'prepareUnreadFixtureMessage')
assertContains('scripts/smoke-issue-014.js', '待填报')
assertContains('scripts/smoke-issue-014.js', '/support/todo?focus=fill')
assertContains('scripts/smoke-issue-014.js', '待填报处理入口')
assertContains('scripts/smoke-issue-014.js', '采集任务列表')
assertContains('scripts/smoke-issue-014.js', '问卷填报追踪')
assertContains('scripts/smoke-issue-014.js', '待审核')
assertContains('scripts/smoke-issue-014.js', '标记已读')
assertContains('scripts/smoke-issue-014.js', '待处理预警')
assertContains('scripts/smoke-issue-014.js', '待整改')
assertContains('scripts/smoke-issue-014.js', '待复核')
assertContains('scripts/smoke-issue-014.js', 'todo fill bucket mismatch')
assertContains('scripts/smoke-issue-014.js', 'todo audit bucket mismatch')
assertContains('scripts/smoke-issue-014.js', 'todo warning bucket mismatch')
assertContains('scripts/smoke-issue-014.js', 'todo rectification bucket mismatch')
assertContains('scripts/smoke-issue-014.js', 'todo review bucket mismatch')
assertContains('scripts/smoke-issue-014.js', 'rectifying')
assertContains('scripts/smoke-issue-014.js', 'message payload missing business metadata')
assertContains('scripts/smoke-issue-014.js', 'support settings should not expose geocode key')
assertContains('scripts/smoke-issue-014.js', 'settings page should not expose frontend key raw text')
assertContains('scripts/smoke-issue-014.js', 'settings page should not expose security code raw text')
assertContains('scripts/smoke-issue-014.js', 'settings page missing geocode secrecy tip')
assertContains('scripts/smoke-issue-014.js', 'message read status reflected in API')
assertContains('scripts/smoke-issue-014.js', 'smoke-issue-014: support center e2e passed')
assert(!read('scripts/smoke-issue-014.js').includes("runAgentBrowser(['wait'"), 'scripts/smoke-issue-014.js should not use fixed agent-browser waits')

console.log('verify-issue-014: support center closure checked')
