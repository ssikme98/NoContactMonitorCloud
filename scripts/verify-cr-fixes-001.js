const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8');
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

// 1. SurveyOverviewController must have @RequiresPermissions
const overviewCtrl = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/controller/SurveyOverviewController.java');
assert(overviewCtrl.includes('@RequiresPermissions'), 'SurveyOverviewController missing @RequiresPermissions');
assert(overviewCtrl.includes('import com.ruoyi.common.security.annotation.RequiresPermissions;'), 'SurveyOverviewController missing import RequiresPermissions');

// 2. endQuestionnaire must have @Transactional
const questionnaireSvc = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyQuestionnaireServiceImpl.java');
// Find endQuestionnaire method and check @Transactional is right before it
const endMethodIdx = questionnaireSvc.indexOf('public int endQuestionnaire');
const beforeEnd = questionnaireSvc.substring(Math.max(0, endMethodIdx - 50), endMethodIdx);
assert(beforeEnd.includes('@Transactional'), 'endQuestionnaire missing @Transactional annotation');

// 3. parseMatrix must log IOException
const analyticsSvc = read('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyAnalyticsServiceImpl.java');
assert(analyticsSvc.includes('private static final Logger log = LoggerFactory.getLogger'), 'SurveyAnalyticsServiceImpl missing Logger');
const parseMatrixIdx = analyticsSvc.indexOf('catch (IOException e)');
const afterCatch = analyticsSvc.substring(parseMatrixIdx, parseMatrixIdx + 100);
assert(afterCatch.includes('log.error'), 'parseMatrix IOException catch missing log.error');

// 4. SurveyQuestionnaireServiceImpl endQuestionnaire @Transactional must include rollbackFor
const endMethodBlock = questionnaireSvc.substring(endMethodIdx - 80, endMethodIdx);
assert(endMethodBlock.includes('rollbackFor'), 'endQuestionnaire @Transactional should include rollbackFor');

console.log('CR fixes 001 (overview-perms + end-tx + parseMatrix-log): verified');
