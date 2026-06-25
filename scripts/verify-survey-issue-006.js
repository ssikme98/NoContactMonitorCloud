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
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/vo/SurveyTaskTrackingSummary.java'), 'missing tracking summary vo'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/vo/SurveyTaskRegionStat.java'), 'missing region stat vo'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/vo/SurveyTaskTrackingDetail.java'), 'missing tracking detail vo'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/vo/SurveyTaskResponseTrend.java'), 'missing response trend vo'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', '@GetMapping("/{taskId}/track/summary")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', '@GetMapping("/{taskId}/track/regions")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', '@GetMapping("/{taskId}/track/details")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', '@GetMapping("/{taskId}/track/trend")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', 'survey:task:query'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyTaskMapper.xml', 'selectTrackingSummary'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyTaskMapper.xml', 'selectTrackingRegionStats'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyTaskMapper.xml', 'selectTrackingDetailList'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyTaskMapper.xml', 'selectTrackingTrend'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyTaskMapper.xml', "to_char(submit_time, 'YYYY-MM-DD')"),
  () => assertNotContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyTaskMapper.xml', 'like concat('),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', "url: '/survey/task/' + taskId + '/track/summary'"),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', "url: '/survey/task/' + taskId + '/track/details'"),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'tracking-summary'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'trackingQuery.enterpriseName'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'listTaskTrackingDetails'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'trendWidth'),
];

for (const check of checks) {
  check();
}

console.log('survey issue 006 verification passed');
