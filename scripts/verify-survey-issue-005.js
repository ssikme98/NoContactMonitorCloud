const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8');
}

function exists(file) {
  return fs.existsSync(path.join(root, file));
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function assertContains(file, expected) {
  const body = read(file);
  assert(body.includes(expected), `${file} missing ${expected}`);
}

function assertNotContains(file, unexpected) {
  const body = read(file);
  assert(!body.includes(unexpected), `${file} must not contain ${unexpected}`);
}

const checks = [
  () => assertContains('sql/kingbase/survey_005_response.sql', 'CREATE TABLE IF NOT EXISTS survey_response'),
  () => assertContains('sql/kingbase/survey_005_response.sql', 'CREATE TABLE IF NOT EXISTS survey_response_answer'),
  () => assertContains('sql/kingbase/survey_005_response.sql', 'uk_survey_response_sample'),
  () => assertContains('sql/kingbase/ruoyi-gateway-dev-survey-route.yml', '/survey/public/**'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyResponse.java'), 'missing response domain'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyResponseAnswer.java'), 'missing response answer domain'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/vo/SurveyPublicFill.java'), 'missing public fill vo'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/mapper/SurveyResponseMapper.java'), 'missing response mapper'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyResponseService.java'), 'missing response service'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyResponseService.java', 'submitResponse'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyResponseServiceImpl.java'), 'missing response service impl'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyResponseServiceImpl.java', 'validateRequiredAnswers'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyResponseServiceImpl.java', 'token已失效'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyResponseServiceImpl.java', '已提交'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyPublicFillController.java'), 'missing public fill controller'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyPublicFillController.java', '@RequestMapping("/public/fill")'),
  () => assertNotContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyPublicFillController.java', '@RequiresPermissions'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyResponseMapper.xml'), 'missing response mapper xml'),
  () => assertNotContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyResponseMapper.xml', 'like concat('),
  () => assertContains('ruoyi-ui/src/router/index.js', "path: '/survey/fill/:token'"),
  () => assertContains('ruoyi-ui/src/api/survey/fill.js', "headers: { isToken: false }"),
  () => assertContains('ruoyi-ui/src/views/survey/fill/index.vue', 'submitFill'),
  () => assertContains('ruoyi-ui/src/views/survey/fill/index.vue', 'validateAnswers'),
  () => assertContains('ruoyi-ui/src/views/survey/fill/index.vue', 'fill-mobile'),
];

for (const check of checks) {
  check();
}

console.log('survey issue 005 verification passed');
