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
  () => assertContains('ruoyi-modules/ruoyi-survey/pom.xml', '<groupId>com.google.zxing</groupId>'),
  () => assertContains('ruoyi-modules/ruoyi-survey/pom.xml', '<artifactId>core</artifactId>'),
  () => assertContains('sql/kingbase/survey_004_task.sql', 'CREATE TABLE IF NOT EXISTS survey_task'),
  () => assertContains('sql/kingbase/survey_004_task.sql', 'CREATE TABLE IF NOT EXISTS survey_task_sample'),
  () => assertContains('sql/kingbase/survey_004_task.sql', 'CREATE TABLE IF NOT EXISTS survey_task_send_record'),
  () => assertContains('sql/kingbase/survey_004_task.sql', 'survey:task:dispatch'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyTask.java'), 'missing survey task domain'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyTaskSample.java'), 'missing survey task sample domain'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyTaskSendRecord.java'), 'missing survey task send record domain'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyTask.java', 'private Long[] enterpriseIds;'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyTask.java', 'private String[] sendChannels;'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/mapper/SurveyTaskMapper.java'), 'missing survey task mapper'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyTaskService.java'), 'missing survey task service'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyTaskService.java', 'dispatchTask'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyTaskServiceImpl.java'), 'missing survey task service impl'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyTaskServiceImpl.java', 'SAMPLING_STRATIFIED'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyTaskServiceImpl.java', 'IdUtils.fastUUID()'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java'), 'missing survey task controller'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', '@PostMapping("/{taskId}/dispatch")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', '@GetMapping("/sample/{sampleId}/qrcode")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyTaskController.java', 'QRCodeWriter'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyTaskMapper.xml'), 'missing survey task mapper xml'),
  () => assertNotContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyTaskMapper.xml', 'like concat('),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', "url: '/survey/task/list'"),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', "url: '/survey/task/' + taskId + '/dispatch'"),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'SAMPLING_METHODS'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'sampleSource'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'downloadQrCode'),
];

for (const check of checks) {
  check();
}

console.log('survey issue 004 verification passed');
