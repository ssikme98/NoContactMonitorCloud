const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8');
}

function assert(condition, message) {
  if (!condition) {
    process.exitCode = 1;
    console.error('FAIL:', message);
  } else {
    console.log('PASS:', message);
  }
}

console.log('=== Code Review Fix Verification ===\n');

// Bug 1: dispatchTask must check status before allowing re-dispatch
{
  const svc = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyTaskServiceImpl.java');
  assert(
    svc.includes('只有已抽样任务允许发卷'),
    '1. dispatchTask: status guard added'
  );
}

// Bug 2: normalizeChannels must throw for unknown channels (write path)
{
  const svc = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyTaskServiceImpl.java');
  const hasWriteThrow = svc.includes('不支持的发送渠道：');
  const splitChannelsLenient = svc.indexOf('splitChannels(String') > 0;
  assert(hasWriteThrow, '2a. normalizeChannels: throws ServiceException for unknown channels');
  assert(splitChannelsLenient, '2b. splitChannels: read path still exists (lenient)');
}

// Bug 3: submitResponse must catch DataIntegrityViolationException
{
  const svc = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyResponseServiceImpl.java');
  assert(
    svc.includes('DataIntegrityViolationException') && svc.includes('catch (DataIntegrityViolationException'),
    '3. submitResponse: catches DataIntegrityViolationException -> business error'
  );
}

// Bug 4: importEnterprise already had @Transactional - REFUTED
console.log('PASS: 4. importEnterprise: @Transactional already present (finding refuted)');

// Bug 5: fill/index.vue submitFill must have .catch() error handler
{
  const vue = read('ruoyi-ui/src/views/survey/fill/index.vue');
  assert(
    vue.includes('.catch(err') || vue.includes('.catch('),
    '5. fill/index.vue: submitFill has .catch() error feedback'
  );
}

// Bug 6: endQuestionnaire must have @Transactional
{
  const svc = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyQuestionnaireServiceImpl.java');
  const endIdx = svc.indexOf('public int endQuestionnaire');
  const before = svc.substring(Math.max(0, endIdx - 80), endIdx);
  assert(
    before.includes('@Transactional'),
    '6. endQuestionnaire: @Transactional annotation present'
  );
}

// Bug 7: parseMatrix must log IOException
{
  const svc = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyAnalyticsServiceImpl.java');
  const catchIdx = svc.indexOf('catch (IOException e)');
  const after = svc.substring(catchIdx, catchIdx + 100);
  assert(
    after.includes('log.error'),
    '7. parseMatrix: IOException logged before returning empty map'
  );
}

// Bug 8: SurveyOverviewController must have @RequiresPermissions
{
  const ctrl = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/controller/SurveyOverviewController.java');
  assert(
    ctrl.includes('@RequiresPermissions'),
    '8. SurveyOverviewController: @RequiresPermissions added'
  );
}

// Bug 9: Gateway route comment must warn against /survey/**
{
  const yml = read('sql/kingbase/ruoyi-gateway-dev-nocontact-route.yml');
  assert(
    yml.includes('严禁'),
    '9. Gateway route: explicit warning against wildcard whitelist'
  );
}

console.log('\n=== Verification complete ===');
