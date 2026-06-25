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
  () => assert(exists('ruoyi-modules/ruoyi-survey/pom.xml'), 'missing ruoyi-survey pom'),
  () => assertContains('ruoyi-modules/pom.xml', '<module>ruoyi-survey</module>'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/RuoYiSurveyApplication.java'), 'missing survey application'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/bootstrap.yml', 'port: 9204'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/bootstrap.yml', 'name: ruoyi-survey'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyOverviewController.java'), 'missing overview controller'),
  () => assertContains('sql/kingbase/ruoyi-survey-dev.yml', 'typeAliasesPackage: com.ruoyi.survey'),
  () => assertContains('sql/kingbase/ruoyi-gateway-dev-survey-route.yml', 'Path=/survey/**'),
  () => assertContains('docker/docker-compose.yml', 'ruoyi-modules-survey'),
  () => assert(exists('docker/ruoyi/modules/survey/dockerfile'), 'missing survey dockerfile'),
  () => assert(exists('ruoyi-ui/src/api/survey/overview.js'), 'missing survey overview api'),
  () => assert(exists('ruoyi-ui/src/views/survey/index.vue'), 'missing survey overview view'),
  () => assertContains('ruoyi-ui/src/views/survey/index.vue', 'getSurveyOverview'),
];

for (const check of checks) {
  check();
}

console.log('survey issue 001 verification passed');
