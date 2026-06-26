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

function assertNotMatches(file, pattern, message) {
  assert(!pattern.test(read(file)), `${file} ${message}`)
}

function assertNotContains(file, text) {
  assert(!read(file).includes(text), `${file} must not contain ${text}`)
}

function assertNoLegacyP0Permissions(file) {
  const matches = read(file).match(/['"](?:fusion|warning):[^'"]+/g)
  assert(!matches, `${file} contains legacy P0 permission ${matches && matches[0]}`)
}

const files = [
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionCollectionTaskController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionCollectionBatchController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionIndicatorController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningEvaluationController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningRuleController.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningMessageController.java',
  'ruoyi-modules/ruoyi-job/src/main/java/com/ruoyi/job/nocontact/RemoteNocontactWarningService.java',
  'ruoyi-modules/ruoyi-job/src/main/java/com/ruoyi/job/nocontact/factory/RemoteNocontactWarningFallbackFactory.java',
  'ruoyi-modules/ruoyi-job/src/main/java/com/ruoyi/job/task/NocontactWarningTask.java',
  'ruoyi-common/ruoyi-common-core/src/main/java/com/ruoyi/common/core/constant/ServiceNameConstants.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/common/NocontactDataScopeHelper.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/domain/FusionCollectionBatch.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/domain/FusionCollectionItem.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/domain/FusionCollectionAuditLog.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/domain/FusionCollectionImportRow.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/domain/FusionCollectionImportFailure.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/domain/dto/FusionIndicatorForm.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/mapper/FusionCollectionBatchMapper.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/domain/WarningMessageHandleLog.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/domain/dto/WarningRuleForm.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/engine/WarningEvaluationInput.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionTaskMapper.xml',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionBatchMapper.xml',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionIndicatorMapper.xml',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningRuleMapper.xml',
  'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningMessageMapper.xml',
  'ruoyi-ui/src/api/nocontact/fusion/task.js',
  'ruoyi-ui/src/api/nocontact/fusion/collection.js',
  'ruoyi-ui/src/api/nocontact/fusion/indicator.js',
  'ruoyi-ui/src/api/nocontact/warning/rule.js',
  'ruoyi-ui/src/api/nocontact/warning/message.js',
  'ruoyi-ui/src/views/nocontact/fusion/task/index.vue',
  'ruoyi-ui/src/views/nocontact/fusion/collection/index.vue',
  'ruoyi-ui/src/views/nocontact/fusion/indicator/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/rule/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/message/index.vue',
  'ruoyi-ui/src/views/nocontact/warning/dashboard/index.vue',
  'sql/kingbase/nocontact_001_fusion_warning.sql',
  'sql/kingbase/nocontact_002_p0_closure.sql',
  'sql/kingbase/nocontact_003_indicator_import_scope.sql',
  'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/fusion/service/impl/FusionIndicatorServiceImplTest.java',
  'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/common/NocontactDataScopeHelperTest.java',
  'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/warning/engine/WarningRuleEvaluatorTest.java',
  'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/warning/engine/WarningGenerationWorkflowTest.java',
  'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/warning/service/impl/WarningRuleServiceImplTest.java',
  'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/fusion/service/impl/FusionCollectionBatchServiceImplTest.java',
  'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/warning/service/impl/WarningEvaluationServiceImplTest.java',
  'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/warning/service/impl/WarningMessageServiceImplTest.java'
]

files.forEach((file) => assert(exists(file), `missing ${file}`))

assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_collection_task')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_indicator')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', 'CREATE TABLE IF NOT EXISTS nc_warning_rule')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', 'CREATE TABLE IF NOT EXISTS nc_warning_message')
assertContains('sql/kingbase/nocontact_002_p0_closure.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_collection_batch')
assertContains('sql/kingbase/nocontact_002_p0_closure.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_collection_item')
assertContains('sql/kingbase/nocontact_002_p0_closure.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_collection_audit_log')
assertContains('sql/kingbase/nocontact_002_p0_closure.sql', 'CREATE TABLE IF NOT EXISTS nc_warning_message_handle_log')
assertContains('sql/kingbase/nocontact_002_p0_closure.sql', 'ALTER TABLE nc_warning_rule ADD COLUMN IF NOT EXISTS threshold_value_max')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS indicator_code')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS lifecycle_status')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'ALTER TABLE nc_fusion_indicator ALTER COLUMN lifecycle_status SET DEFAULT')
assertNotMatches('sql/kingbase/nocontact_003_indicator_import_scope.sql',
  /ADD COLUMN IF NOT EXISTS lifecycle_status[^;]*default 'draft'/i,
  'must not add lifecycle_status with draft default before historical backfill')
assertNotMatches('sql/kingbase/nocontact_003_indicator_import_scope.sql',
  /WHERE[^;]*lifecycle_status\s*=\s*'draft'/i,
  'must not remigrate existing draft lifecycle rows')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'CREATE TABLE IF NOT EXISTS nc_fusion_collection_import_failure')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'uk_nc_fusion_indicator_enabled_code')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'uk_nc_fusion_indicator_code_version')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'uk_nc_warning_message_open_business')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'uk_nc_fusion_item_active_scope')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'dedupe_current_items')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', "PARTITION BY indicator_id, responsible_unit_id, coalesce(region_code, ''), period_key")
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'ALTER TABLE nc_fusion_collection_batch ADD COLUMN IF NOT EXISTS dept_id')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'ALTER TABLE nc_warning_message ADD COLUMN IF NOT EXISTS dept_id')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'ALTER TABLE nc_warning_rule ADD COLUMN IF NOT EXISTS responsible_unit_id')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'idx_nc_warning_rule_scope')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'nocontactWarningTask.evaluateDueRules')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', '营商无感缺报逾期预警评估')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'nocontact:fusion:collection:import')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'nocontact:fusion:task:export')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'nocontact:fusion:indicator:export')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'nocontact:warning:rule:export')
assertContains('sql/kingbase/nocontact_003_indicator_import_scope.sql', 'nocontact:warning:message:export')
assertContains('sql/kingbase/nocontact_002_p0_closure.sql', 'nocontact:fusion:collection:audit')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', "'overdue'")
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', '营商数据融合')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', '检测预警中心')
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', "'table'")
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', "'bell'")
assertNotContains('sql/kingbase/nocontact_001_fusion_warning.sql', "'database'")
assertNotContains('sql/kingbase/nocontact_001_fusion_warning.sql', "'alert'")
assertContains('sql/kingbase/nocontact_001_fusion_warning.sql', "perms = 'nocontact:' || perms")
assertContains('ruoyi-ui/src/views/nocontact/fusion/task/index.vue', '新增采集任务')
assertContains('ruoyi-ui/src/views/nocontact/fusion/collection/index.vue', '提交采集数据')
assertContains('ruoyi-ui/src/views/nocontact/fusion/collection/index.vue', '导入Excel')
assertContains('ruoyi-ui/src/views/nocontact/fusion/collection/index.vue', '失败明细')
assertContains('ruoyi-ui/src/views/nocontact/fusion/collection/index.vue', '审核通过，已触发预警评估')
assertContains('ruoyi-ui/src/views/nocontact/fusion/indicator/index.vue', '指标目录')
assertContains('ruoyi-ui/src/views/nocontact/fusion/indicator/index.vue', '复制草稿')
assertContains('ruoyi-ui/src/views/nocontact/fusion/indicator/index.vue', '生命周期')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', '新增预警规则')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', "value: 'ne'")
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', "value: 'lte'")
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', "value: 'gte'")
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', "value: 'outside_range'")
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', 'thresholdValueMax')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', 'responsibleUnitId')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', 'periodType')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', '缺报或逾期规则必须设置周期类型')
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', "value: 'missing'")
assertContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', "value: 'overdue'")
assertNotContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', "value: 'between'")
assertNotContains('ruoyi-ui/src/views/nocontact/warning/rule/index.vue', "value: 'not_between'")
assertContains('ruoyi-ui/src/views/nocontact/warning/dashboard/index.vue', '分级预警看板')
assertContains('ruoyi-ui/src/views/nocontact/warning/message/index.vue', '处理日志')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningMessageMapper.xml', 'insertHandleLog')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningMessageMapper.xml', 'm.dept_id')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningMessageMapper.xml', 'message_status not in')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningMessageMapper.xml', 'message_status = #{expectedStatus}')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionBatchMapper.xml', 'selectAuditLogsByBatchId')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionBatchMapper.xml', 'batch_status = #{expectedStatus}')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionBatchMapper.xml', 'insertImportFailure')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionBatchMapper.xml', 'selectDeptNameById')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionBatchMapper.xml', 'countApprovedCurrentItemByScope')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionBatchMapper.xml', "b.batch_status = 'approved'")
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionCollectionBatchMapper.xml', "i.is_current = '1'")
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionIndicatorMapper.xml', 'indicator_code')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/fusion/FusionIndicatorMapper.xml', 'selectEnabledIndicatorByCode')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionIndicatorController.java', 'logical = Logical.OR')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionIndicatorController.java', 'nocontact:warning:rule:list')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionIndicatorController.java', 'indicator.setLifecycleStatus("enabled")')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionIndicatorController.java', '@RequestBody FusionIndicatorForm')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningRuleController.java', '@RequestBody WarningRuleForm')
assertNotContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionIndicatorController.java', '@RequestBody FusionIndicator ')
assertNotContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningRuleController.java', '@RequestBody WarningRule ')
assertNotContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionCollectionBatchController.java', 'boolean updateSupport')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/domain/dto/FusionIndicatorForm.java', 'public FusionIndicator toEntity()')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/domain/dto/WarningRuleForm.java', 'public WarningRule toEntity()')
assertContains('ruoyi-common/ruoyi-common-core/src/main/java/com/ruoyi/common/core/constant/ServiceNameConstants.java', 'NOCONTACT_SERVICE')
assertContains('ruoyi-modules/ruoyi-job/src/main/java/com/ruoyi/job/task/NocontactWarningTask.java', 'evaluateDueRules')
assertContains('ruoyi-modules/ruoyi-job/src/main/java/com/ruoyi/job/task/NocontactWarningTask.java', 'isQuarterDue')
assertContains('ruoyi-modules/ruoyi-job/src/main/java/com/ruoyi/job/task/NocontactWarningTask.java', 'isYearDue')
assertContains('ruoyi-modules/ruoyi-job/src/main/java/com/ruoyi/job/nocontact/RemoteNocontactWarningService.java', 'ServiceNameConstants.NOCONTACT_SERVICE')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningEvaluationController.java', '@InnerAuth')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningEvaluationController.java', 'evaluateScheduledRules(periodKey, "job")')
assertContains('ruoyi-ui/src/components/ExcelImportDialog/index.vue', 'showUpdateSupport')
assertContains('ruoyi-ui/src/views/nocontact/fusion/collection/index.vue', ':show-update-support="false"')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/service/impl/FusionCollectionBatchServiceImpl.java', '同批次存在重复指标')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/service/impl/FusionCollectionBatchServiceImpl.java', 'selectDeptNameById')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/service/impl/FusionCollectionBatchServiceImpl.java', '地区编码和地区名称必须同时填写')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/service/impl/FusionCollectionBatchServiceImpl.java', 'toPlainString()')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/engine/WarningRuleEvaluator.java', '"ne".equals(condition)')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/engine/WarningGenerationWorkflow.java', 'evaluateScheduled')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/service/IWarningEvaluationService.java', 'evaluateScheduledRules')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningRuleMapper.xml', 'selectScheduledRules')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningRuleMapper.xml', 'responsible_unit_id = #{responsibleUnitId}')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/warning/WarningMessageMapper.xml', 'updateOpenMessageHitByBusinessKey')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/service/impl/WarningRuleServiceImpl.java', '"outside_range".equals(condition)')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/service/impl/WarningRuleServiceImpl.java', '缺报或逾期规则必须绑定责任单位')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/service/impl/WarningRuleServiceImpl.java', '缺报或逾期规则必须设置周期类型')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/service/impl/WarningEvaluationServiceImpl.java', 'resolvePeriodType')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/service/impl/WarningEvaluationServiceImpl.java', 'isPastPeriod')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/service/impl/WarningEvaluationServiceImpl.java', 'countApprovedCurrentItemByScope')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/service/impl/WarningEvaluationServiceImpl.java', 'DuplicateKeyException')
assertContains('ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/warning/service/impl/WarningRuleServiceImplTest.java', 'outsideRangeRuleRequiresMaxThreshold')
assertContains('ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/warning/service/impl/WarningEvaluationServiceImplTest.java', 'scheduledMissingRuleCreatesMessageWhenCurrentItemAbsent')
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/common/NocontactDataScopeHelper.java', 'getParams().put(DATA_SCOPE')
assertContains('ruoyi-modules/ruoyi-nocontact/pom.xml', 'maven-surefire-plugin')

files.forEach((file) => assertNoLegacyP0Permissions(file))

console.log(`verify-nocontact-p0: ${files.length} files checked`)
