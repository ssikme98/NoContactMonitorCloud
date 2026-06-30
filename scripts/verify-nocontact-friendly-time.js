const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8')
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

function assertContains(file, text) {
  assert(read(file).includes(text), `${file} missing ${text}`)
}

function assertNotContains(file, text) {
  assert(!read(file).includes(text), `${file} should not contain ${text}`)
}

const templateFiles = [
  'ruoyi-ui/src/views/nocontact/fusion/task/index.vue',
  'ruoyi-ui/src/views/nocontact/fusion/collection/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/message/index.vue',
  'ruoyi-ui/src/views/nocontact/report/task/index.vue',
  'ruoyi-ui/src/views/nocontact/integration/config/index.vue',
  'ruoyi-ui/src/views/nocontact/integration/log/index.vue',
  'ruoyi-ui/src/views/nocontact/support/message/index.vue'
]

assertContains('ruoyi-ui/src/utils/nocontactDisplay.js', 'export function formatFriendlyDateTime(value)')
assertContains('ruoyi-ui/src/utils/nocontactDisplay.js', "return parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}') || '-'")

assertContains('ruoyi-ui/src/views/nocontact/fusion/task/index.vue', '{{ formatFriendlyDateTime(scope.row.lastRunTime) }}')
assertNotContains('ruoyi-ui/src/views/nocontact/fusion/task/index.vue', '<el-table-column label="最后执行时间" prop="lastRunTime" width="160" />')

assertContains('ruoyi-ui/src/views/nocontact/fusion/collection/index.vue', '{{ formatFriendlyDateTime(scope.row.submitTime) }}')
assertContains('ruoyi-ui/src/views/nocontact/fusion/collection/index.vue', '{{ formatFriendlyDateTime(scope.row.auditTime) }}')
assertContains('ruoyi-ui/src/views/nocontact/fusion/collection/index.vue', '{{ formatFriendlyDateTime(scope.row.createTime) }}')

assertContains('ruoyi-ui/src/views/nocontact/warning/message/index.vue', '{{ formatFriendlyDateTime(scope.row.triggerTime) }}')
assertContains('ruoyi-ui/src/views/nocontact/warning/message/index.vue', '{{ formatFriendlyDateTime(scope.row.handleTime) }}')

assertContains('ruoyi-ui/src/views/nocontact/report/task/index.vue', '{{ formatFriendlyDateTime(scope.row.generatedTime) }}')
assertContains('ruoyi-ui/src/views/nocontact/integration/config/index.vue', '{{ formatFriendlyDateTime(scope.row.lastSyncTime) }}')
assertContains('ruoyi-ui/src/views/nocontact/integration/log/index.vue', '{{ formatFriendlyDateTime(scope.row.startedTime) }}')
assertContains('ruoyi-ui/src/views/nocontact/integration/log/index.vue', '{{ formatFriendlyDateTime(scope.row.syncTime) }}')
assertContains('ruoyi-ui/src/views/nocontact/support/message/index.vue', '{{ formatFriendlyDateTime(scope.row.eventTime) }}')

templateFiles.forEach((file) => {
  assertContains(file, 'formatFriendlyDateTime')
})

console.log(`verify-nocontact-friendly-time: ${templateFiles.length} templates checked`)
