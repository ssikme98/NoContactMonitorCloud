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

function assertOrder(file, before, after) {
  const content = read(file)
  const beforeIndex = content.indexOf(before)
  const afterIndex = content.indexOf(after)
  assert(beforeIndex >= 0, `${file} missing ${before}`)
  assert(afterIndex >= 0, `${file} missing ${after}`)
  assert(beforeIndex < afterIndex, `${file} must place ${before} before ${after}`)
}

const adapter = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/adapter/ExternalDataAdapter.java'
const registry = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/adapter/ExternalDataAdapterRegistry.java'
const jsonAdapter = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/adapter/JsonExternalDataAdapter.java'
const service = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/service/impl/ExternalIntegrationServiceImpl.java'
const controller = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/controller/ExternalIntegrationController.java'
const mapper = 'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/integration/ExternalIntegrationMapper.xml'
const sql = 'sql/kingbase/nocontact_004_full_alignment.sql'
const migration = 'sql/kingbase/nocontact_006_external_sync_idempotence.sql'
const api = 'ruoyi-ui/src/api/nocontact/integration/index.js'
const configVue = 'ruoyi-ui/src/views/nocontact/integration/config/index.vue'
const logVue = 'ruoyi-ui/src/views/nocontact/integration/log/index.vue'
const serviceTest = 'ruoyi-modules/ruoyi-nocontact/src/test/java/com/ruoyi/nocontact/integration/service/impl/ExternalIntegrationServiceImplTest.java'
const smoke = 'scripts/smoke-issue-012.js'
const helper = 'scripts/lib/nocontactSmoke.js'

;[
  adapter,
  registry,
  jsonAdapter,
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/domain/ExternalDataRecord.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/domain/ExternalSyncBatch.java',
  'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/integration/domain/ExternalSyncPayload.java',
  service,
  controller,
  mapper,
  api,
  configVue,
  logVue,
  serviceTest,
  migration,
  smoke,
  helper
].forEach(assertExists)

assertContains(adapter, 'List<ExternalDataRecord> pull')
assertContains(registry, 'public ExternalDataAdapter resolve')
assertContains(jsonAdapter, 'mock://')
assertContains(jsonAdapter, 'Authorization')
assertContains(jsonAdapter, 'externalId不能为空')
assertContains(jsonAdapter, 'setInstanceFollowRedirects(false)')
assertContains(jsonAdapter, 'resolveRedirect')

assertContains(service, 'adapter.pull(config)')
assertContains(service, 'preparePayloadAttempt')
assertOrder(service, 'preparePayloadAttempt(batch, record, candidate, existing, operName)', 'insertBatch(collectionBatch)')
assertContains(service, 'selectPayloadByUnique')
assertContains(service, 'selectPayloadListByBatchId')
assertContains(service, 'retryBatch')
assertContains(service, 'partial_failed')
assertContains(service, 'reuseFailedPayload')
assertContains(service, 'hideCredential')
assertContains(service, 'requireEnabled(config)')
assertContains(service, 'rerunSourcePull')
assertContains(service, 'adapter.acknowledge(config, records)')
assertContains(service, 'config.setAuthCredential(null)')
assertContains(service, 'replaceAll("(?i)(token=)[^&\\\\s]+", "$1***")')
assertContains(service, 'MessageDigest.getInstance("SHA-256")')

assertContains(controller, '@PostMapping("/batch/{syncBatchId}/retry")')
assertContains(controller, '@GetMapping("/batch/list")')
assertContains(controller, 'excludeParamNames = { "authCredential" }')

assertContains(mapper, 'insertSyncBatch')
assertContains(mapper, 'selectSyncBatchList')
assertContains(mapper, 'insertPayload')
assertContains(mapper, 'updatePayload')
assertContains(mapper, 'selectPayloadByUnique')
assertContains(mapper, 'ON CONFLICT (source_system, external_id, version_hash) DO NOTHING')

assertContains(sql, 'ALTER TABLE nc_external_integration_config')
assertContains(sql, 'ADD COLUMN IF NOT EXISTS auth_credential')
assertContains(sql, 'CREATE TABLE IF NOT EXISTS nc_external_sync_batch')
assertContains(sql, 'CREATE TABLE IF NOT EXISTS nc_external_sync_payload')
assertContains(sql, 'uk_nc_external_payload_source_version')
assert(!read(sql).includes('DROP INDEX IF EXISTS uk_nc_external_payload_batch_source'), `${sql} should not drop payload indexes in bootstrap`)
assertContains(migration, 'DROP INDEX IF EXISTS uk_nc_external_payload_batch_source')
assertContains(migration, 'CREATE UNIQUE INDEX IF NOT EXISTS uk_nc_external_payload_source_version')

assertContains(api, 'retryBatch')
assertContains(api, '/nocontact/integration/batch/list')
assertContains(api, '/nocontact/integration/log/list')

assertContains(configVue, 'type="password"')
assertContains(configVue, 'API接口')
assertContains(configVue, '访问Token')
assertContains(configVue, "result.responseStatus === 'failed'")
assertContains(configVue, "result.batchStatus === 'success'")
assertContains(configVue, 'partial_failed')
assertContains(logVue, 'retryBatch')
assertContains(logVue, 'partial_failed')
assertContains(logVue, 'params.batchStatus = params.responseStatus')
assertContains(logVue, 'delete params.responseStatus')

assertContains(helper, 'clickElementByText')
assertContains(smoke, 'SMOKE-INTEGRATION-SUCCESS')
assertContains(smoke, 'SMOKE-INTEGRATION-FAIL')
assertContains(smoke, 'mock://integration-success')
assertContains(smoke, 'ftp://example.com/integration-failure')
assertContains(smoke, '连接测试通过')
assertContains(smoke, '同步完成')
assertContains(smoke, '重试已发起')
assertContains(smoke, '运行日志')
assertContains(smoke, 'smoke-issue-012: external integration e2e passed')

assertContains(serviceTest, 'configQueriesDoNotExposeStoredCredential')
assertContains(serviceTest, 'blankCredentialUpdateDoesNotClearStoredCredential')
assertContains(serviceTest, 'syncRejectsDisabledConfig')
assertContains(serviceTest, 'testConnectionPullsAdapterBeforeSuccess')
assertContains(serviceTest, 'syncPersistsPayloadThenCreatesStandardCollectionBatch')
assertContains(serviceTest, 'retryPullFailureBatchRerunsAdapterPull')
assertContains(serviceTest, 'duplicatePayloadDoesNotCreateAnotherBusinessBatch')
assertContains(serviceTest, 'duplicateInsertConflictIsTreatedAsSkip')
assertContains(serviceTest, 'failureLogDoesNotExposeToken')
assertContains(serviceTest, 'mixedSyncResultsAreMarkedPartialFailed')

console.log('verify-issue-012: external integration sync closure checked')
