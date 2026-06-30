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

const controller = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/report/controller/ReportGenerationController.java'
const taskVue = 'ruoyi-ui/src/views/nocontact/report/task/index.vue'
const api = 'ruoyi-ui/src/api/nocontact/report/index.js'
const smoke = 'scripts/smoke-issue-013.js'
const helper = 'scripts/lib/nocontactSmoke.js'

;[controller, taskVue, api, smoke, helper].forEach(assertExists)

assertContains(controller, '@PutMapping("/task/{taskId}/generate")')
assertContains(controller, '@PostMapping("/task/{taskId}/download/{fileType}")')
assertContains(controller, '@GetMapping("/task/{taskId}/snapshot/list")')
assertContains(controller, '@PostMapping("/task/snapshot/{snapshotId}/download/{fileType}")')

assertContains(api, 'reportTaskDownloadUrl')
assertContains(api, 'generateReportTask')
assertContains(api, 'listReportSnapshot')
assertContains(api, 'reportSnapshotDownloadUrl')

assertContains(taskVue, '下载Word')
assertContains(taskVue, '下载Excel')
assertContains(taskVue, '立即生成')
assertContains(taskVue, '生成历史')

assertContains(helper, 'NOCONTACT_SMOKE_WRITE')
assertContains(helper, 'agent-browser')
assertContains(helper, 'clickTableRowAction')
assertContains(helper, 'setInputValueByPlaceholder')
assertContains(helper, 'waitFor')

assertContains(smoke, 'SMOKE-REPORT-TASK')
assertContains(smoke, 'requireWriteGuard')
assertContains(smoke, 'setInputValueByPlaceholder')
assertContains(smoke, 'clickTableRowAction')
assertContains(smoke, '生成历史')
assertContains(smoke, 'snapshot list did not grow after regenerate')
assertContains(smoke, 'download/word')
assertContains(smoke, 'download/excel')
assertContains(smoke, '/snapshot/list')
assertContains(smoke, 'smoke-issue-013: report generation e2e passed')
assert(!read(smoke).includes("runAgentBrowser(['wait'"), `${smoke} should not use fixed agent-browser waits`)

console.log('verify-issue-013: report generation closure checked')
