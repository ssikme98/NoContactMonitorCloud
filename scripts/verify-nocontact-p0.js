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

function assertContains(file, text) {
  assert(read(file).includes(text), `${file} missing ${text}`)
}

const files = [
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionCollectionTaskController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionIndicatorController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningRuleController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningMessageController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionTaskMapper.xml',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionIndicatorMapper.xml',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningRuleMapper.xml',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningMessageMapper.xml',
  'ruoyi-ui/src/api/nocontact/fusion/task.js',
  'ruoyi-ui/src/api/nocontact/fusion/indicator.js',
  'ruoyi-ui/src/api/nocontact/warning/rule.js',
  'ruoyi-ui/src/api/nocontact/warning/message.js',
  'ruoyi-ui/src/views/nocontact/fusion/task/index.vue',
  'ruoyi-ui/src/views/nocontact/fusion/indicator/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/rule/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/message/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/dashboard/index.vue',
  'sql/kingbase/nocontact_001_fusion_warning.sql'
]

files.forEach((file) => assert(exists(file), `missing ${file}`))

assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_collection_task')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_indicator')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', 'CREATE TABLE IF NOT EXISTS nc_warning_rule')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', 'CREATE TABLE IF NOT EXISTS nc_warning_message')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', '营商数据融合')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', '检测预警中心')
assertContains('ruoyi-ui/src/views/nocontact/fusion/task/index.vue', '新增采集任务')
assertContains('ruoyi-ui/src/views/nocontact/fusion/indicator/index.vue', '指标目录')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', '新增预警规则')
assertContains('ruoyi-ui/src/views/nocontact/warning/dashboard/index.vue', '分级预警看板')

console.log(`verify-nocontact-p0: ${files.length} files checked`)
