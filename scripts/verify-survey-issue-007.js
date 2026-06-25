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
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyAnalyticsService.java'), 'missing analytics service interface'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java'), 'missing analytics service impl'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/vo/SurveySatisfactionAnalytics.java'), 'missing satisfaction analytics vo'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/vo/SurveyQuestionDistribution.java'), 'missing question distribution vo'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java', 'QUESTION_TYPE_SCORE'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java', 'QUESTION_TYPE_MATRIX_SCORE'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java', 'QUESTION_TYPE_LIKERT'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java', 'toScore'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java', 'QUESTION_TYPE_SINGLE'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java', 'QUESTION_TYPE_MULTIPLE'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java', 'QUESTION_TYPE_TEXT'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyAnalyticsServiceImpl.java', 'XWPFDocument'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', '@GetMapping("/{taskId}/analytics/satisfaction")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', '@GetMapping("/{taskId}/analytics/report")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/mapper/SurveyResponseMapper.java', 'selectAnswerRowsByTaskId'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyResponseMapper.xml', 'survey_response_answer'),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', "url: '/survey/task/' + taskId + '/analytics/satisfaction'"),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', "url: '/survey/task/' + taskId + '/analytics/report'"),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', '满意度分析'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'exportSatisfactionReport'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'analysis-bar'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'distributionRows'),
  () => assertNotContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyResponseMapper.xml', 'like concat('),
];

for (const check of checks) {
  check();
}

console.log('survey issue 007 verification passed');
