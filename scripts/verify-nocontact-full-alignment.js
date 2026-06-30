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

function assertNotContains(file, text) {
  assert(!read(file).includes(text), `${file} should not contain ${text}`)
}

const expectedMenus = [
  '营商数据融合',
  '采集任务列表',
  '新增采集任务',
  '数据填报入口',
  '数据审核中心',
  '采集监控看板',
  '采集计划管理',
  '资源目录管理',
  '指标体系管理',
  '指标体系列表',
  '新增/编辑指标体系',
  '评价算法管理',
  '标签分类管理',
  '检测预警中心',
  '预警规则列表',
  '新增/编辑预警规则',
  '预警消息管理',
  '分级预警看板',
  '问题整改管理',
  '问题台账列表',
  '问题详情与处理流程',
  '整改跟踪看板',
  '问卷调研管理',
  '企业库管理',
  '问卷管理',
  '问卷设计器',
  '样本抽取与发卷',
  '填报追踪',
  '满意度统计分析',
  '外部系统对接',
  '对接配置管理',
  '新增/编辑对接配置',
  '同步日志与监控',
  '报告自动生成',
  '报告模板管理',
  '报告生成任务',
  '公共支撑',
  '我的待办',
  '消息中心',
  '业务设置'
]

const expectedFiles = [
  'ruoyi-ui/src/views/nocontact/collection/task/index.vue',
  'ruoyi-ui/src/views/nocontact/collection/create/index.vue',
  'ruoyi-ui/src/views/nocontact/collection/fill/index.vue',
  'ruoyi-ui/src/views/nocontact/collection/audit/index.vue',
  'ruoyi-ui/src/views/nocontact/collection/monitor/index.vue',
  'ruoyi-ui/src/views/nocontact/collection/schedule/index.vue',
  'ruoyi-ui/src/views/nocontact/collection/resource/index.vue',
  'ruoyi-ui/src/views/nocontact/indicator/list/index.vue',
  'ruoyi-ui/src/views/nocontact/indicator/editor/index.vue',
  'ruoyi-ui/src/views/nocontact/indicator/algorithm/index.vue',
  'ruoyi-ui/src/views/nocontact/indicator/tag/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/rule-list/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/rule-editor/index.vue',
  'ruoyi-ui/src/views/nocontact/rectification/issue/index.vue',
  'ruoyi-ui/src/views/nocontact/rectification/detail/index.vue',
  'ruoyi-ui/src/views/nocontact/rectification/dashboard/index.vue',
  'ruoyi-ui/src/views/nocontact/integration/config/index.vue',
  'ruoyi-ui/src/views/nocontact/integration/editor/index.vue',
  'ruoyi-ui/src/views/nocontact/integration/log/index.vue',
  'ruoyi-ui/src/views/nocontact/report/template/index.vue',
  'ruoyi-ui/src/views/nocontact/report/task/index.vue',
  'ruoyi-ui/src/views/nocontact/support/todo/index.vue',
  'ruoyi-ui/src/views/nocontact/support/message/index.vue',
  'ruoyi-ui/src/views/nocontact/support/settings/index.vue',
  'ruoyi-ui/src/api/nocontact/collection/resource.js',
  'ruoyi-ui/src/api/nocontact/indicator/workbench.js',
  'ruoyi-ui/src/api/nocontact/rectification/issue.js',
  'ruoyi-ui/src/api/nocontact/integration/index.js',
  'ruoyi-ui/src/api/nocontact/report/index.js',
  'ruoyi-ui/src/api/nocontact/support/index.js',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionResourceController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionIndicatorWorkbenchController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/rectification/controller/RectificationIssueController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/controller/ExternalIntegrationController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/report/controller/ReportGenerationController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/controller/SupportCenterController.java',
  'sql/kingbase/nocontact_004_full_alignment.sql',
  'sql/kingbase/nocontact_005_report_snapshot_closure.sql',
  'sql/kingbase/nocontact_007_support_closure.sql',
  'sql/kingbase/nocontact_008_ui_alignment.sql'
]

const sqlFiles = [
  'sql/kingbase/nocontact_004_full_alignment.sql',
  'sql/kingbase/nocontact_005_report_snapshot_closure.sql',
  'sql/kingbase/nocontact_007_support_closure.sql'
]

expectedFiles.forEach(assertExists)
sqlFiles.forEach(assertExists)
expectedMenus.forEach((menu) => {
  assert(sqlFiles.some((file) => read(file).includes(menu)), `menu seed missing ${menu}`)
})

assertContains('sql/kingbase/nocontact_004_full_alignment.sql', 'CREATE TABLE IF NOT EXISTS nc_rectification_issue')
assertContains('sql/kingbase/nocontact_004_full_alignment.sql', 'CREATE TABLE IF NOT EXISTS nc_external_integration_config')
assertContains('sql/kingbase/nocontact_004_full_alignment.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_resource_directory')
assertContains('sql/kingbase/nocontact_004_full_alignment.sql', 'CREATE TABLE IF NOT EXISTS nc_indicator_tag')
assertContains('sql/kingbase/nocontact_005_report_snapshot_closure.sql', 'CREATE TABLE IF NOT EXISTS nc_report_generation_task')
assertContains('ruoyi-ui/src/views/nocontact/rectification/issue/index.vue', '新增问题')
assertContains('ruoyi-ui/src/views/nocontact/integration/config/index.vue', '连接测试')
assertContains('ruoyi-ui/src/views/nocontact/report/task/index.vue', '立即生成')
assertContains('ruoyi-ui/src/utils/jobName.js', "'系统默认（无参）': '标准采集计划'")
assertContains('ruoyi-ui/src/utils/jobName.js', "'系统默认（有参）': '按条件采集计划'")
assertContains('ruoyi-ui/src/utils/jobName.js', "'系统默认（多参）': '批量采集计划'")
assertNotContains('ruoyi-ui/src/utils/jobName.js', '系统默认（标准采集）')
assertContains('ruoyi-ui/src/views/nocontact/report/template/index.vue', 'reportTypeText(scope.row.reportType)')
assertContains('ruoyi-ui/src/views/nocontact/integration/config/index.vue', 'integrationTypeText(scope.row.integrationType)')
assertContains('ruoyi-ui/src/views/nocontact/integration/config/index.vue', 'syncFrequencyText(scope.row.syncFrequency)')
assertContains('ruoyi-ui/src/views/nocontact/rectification/issue/index.vue', 'warningLevelText(scope.row.warningLevel)')
assertContains('ruoyi-ui/src/views/nocontact/warning/message/index.vue', 'channelText(scope.row.pushChannels)')
assertContains('ruoyi-ui/src/views/nocontact/warning/message/index.vue', 'channelText(detail.pushChannels)')
assertContains('ruoyi-ui/src/views/nocontact/warning/message/index.vue', '转整改')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', 'channelText(scope.row.pushChannels)')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/service/impl/SupportCenterServiceImpl.java', 'pendingFillCount')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/service/impl/SupportCenterServiceImpl.java', 'rework')
assertContains('ruoyi-ui/src/views/nocontact/support/message/index.vue', '标记已读')
assertContains('ruoyi-ui/src/views/nocontact/support/settings/index.vue', '默认报告周期')
assertContains('ruoyi-ui/src/api/nocontact/support/index.js', 'listTodoSummary')
assertContains('ruoyi-ui/src/api/nocontact/support/index.js', 'markBusinessMessageRead')
assertContains('ruoyi-ui/src/views/nocontact/integration/editor/index.vue', 'entry-mode="editor"')
assertContains('ruoyi-ui/src/views/nocontact/rectification/detail/index.vue', 'entry-mode="detail"')
assertContains('ruoyi-ui/src/views/nocontact/report/designer/index.vue', '<report-template />')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule-list/index.vue', 'entry-mode="list"')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule-editor/index.vue', 'entry-mode="editor"')
assertContains('ruoyi-ui/src/views/nocontact/integration/config/index.vue', 'entryMode')
assertContains('ruoyi-ui/src/views/nocontact/integration/config/index.vue', 'bootstrapEntryMode')
assertContains('ruoyi-ui/src/views/nocontact/integration/config/index.vue', 'isEditorEntry')
assertContains('ruoyi-ui/src/views/nocontact/rectification/issue/index.vue', 'entryMode')
assertContains('ruoyi-ui/src/views/nocontact/rectification/issue/index.vue', 'bootstrapEntryMode')
assertContains('ruoyi-ui/src/views/nocontact/rectification/issue/index.vue', 'isDetailEntry')
assertNotContains('ruoyi-ui/src/views/nocontact/report/template/index.vue', '报告模板设计器')
assertNotContains('ruoyi-ui/src/views/nocontact/report/template/index.vue', 'isDesignerEntry')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', 'entryMode')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', 'bootstrapEntryMode')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', 'isEditorEntry')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', 'isListEntry')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/rectification/controller/RectificationIssueController.java', '/from-warning/{messageId}')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/controller/SupportCenterController.java', '/support/todo/summary')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/support/controller/SupportCenterController.java', '/support/message/{messageId}/read')

console.log(`verify-nocontact-full-alignment: ${expectedMenus.length} menus and ${expectedFiles.length} files checked`)
