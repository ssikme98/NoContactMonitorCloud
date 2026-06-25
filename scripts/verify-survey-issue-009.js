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

const checks = [
  () => assertContains('docker/docker-compose.yml', 'ruoyi-modules-survey'),
  () => assertContains('docker/docker-compose.yml', '"9204:9204"'),
  () => assertContains('docker/ruoyi/modules/survey/dockerfile', 'ruoyi-modules-survey.jar'),
  () => assertContains('docker/redis/conf/redis.conf', 'dir /data'),
  () => assertContains('docker/copy.sh', 'ruoyi-modules-survey'),
  () => assertContains('sql/mysql/survey_menu_local.sql', 'SET NAMES utf8mb4'),
  () => assertContains('sql/mysql/survey_menu_local.sql', 'ON DUPLICATE KEY UPDATE'),
  () => assertContains('sql/mysql/survey_menu_local.sql', 'survey/task/index'),
  () => assertContains('ruoyi-modules/pom.xml', '<module>ruoyi-survey</module>'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/bootstrap.yml', 'name: ruoyi-survey'),
  () => assertContains('sql/kingbase/ruoyi-gateway-dev-survey-route.yml', 'lb://ruoyi-survey'),
  () => assertContains('sql/kingbase/ruoyi-gateway-dev-survey-route.yml', '/survey/public/**'),
  () => assertContains('ruoyi-ui/src/router/index.js', "path: '/survey/fill/:token'"),
  () => assertContains('ruoyi-ui/src/permission.js', "const whiteList = ['/login', '/register', '/survey/fill/*']"),
  () => assert(exists('ruoyi-ui/src/views/survey/enterprise/index.vue'), 'missing enterprise page'),
  () => assert(exists('ruoyi-ui/src/views/survey/questionnaire/index.vue'), 'missing questionnaire page'),
  () => assert(exists('ruoyi-ui/src/views/survey/task/index.vue'), 'missing task page'),
  () => assert(exists('ruoyi-ui/src/views/survey/fill/index.vue'), 'missing public fill page'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', '满意度分析'),
  () => assertContains('ruoyi-ui/src/views/survey/task/index.vue', 'exportSatisfactionReport'),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', '/track/summary'),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', '/analytics/satisfaction'),
  () => assertContains('ruoyi-ui/src/api/survey/task.js', '/analytics/report'),
  () => assert(exists('.scratch/survey-module/smoke-issue-009.js'), 'missing local e2e smoke script'),
];

for (const check of checks) {
  check();
}

console.log('survey issue 009 verification passed');
