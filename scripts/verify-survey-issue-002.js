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
  () => assertContains('ruoyi-modules/ruoyi-survey/pom.xml', '<artifactId>postgresql</artifactId>'),
  () => assertContains('ruoyi-modules/ruoyi-survey/pom.xml', '<artifactId>dynamic-datasource-spring-boot-starter</artifactId>'),
  () => assertContains('sql/kingbase/survey_002_enterprise.sql', 'CREATE TABLE IF NOT EXISTS survey_enterprise'),
  () => assertContains('sql/kingbase/survey_002_enterprise.sql', 'CONSTRAINT uk_survey_enterprise_credit_code UNIQUE'),
  () => assertContains('sql/kingbase/survey_002_enterprise.sql', 'CREATE TABLE IF NOT EXISTS survey_enterprise_group'),
  () => assertContains('sql/kingbase/survey_002_enterprise.sql', 'CREATE TABLE IF NOT EXISTS survey_enterprise_group_rel'),
  () => assertContains('sql/kingbase/survey_002_enterprise.sql', 'survey:enterprise:import'),
  () => assertContains('sql/kingbase/survey_002_enterprise.sql', 'survey:enterprise:group:list'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyEnterprise.java'), 'missing enterprise domain'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyEnterprise.java', 'private Long[] groupIds;'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyEnterprise.java', 'private Long groupId;'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyEnterpriseGroup.java'), 'missing enterprise group domain'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/vo/SurveyTreeSelect.java'), 'missing survey tree select vo'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/mapper/SurveyEnterpriseMapper.java'), 'missing enterprise mapper'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/mapper/SurveyEnterpriseGroupMapper.java'), 'missing enterprise group mapper'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyEnterpriseService.java'), 'missing enterprise service'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyEnterpriseService.java', 'importEnterprise('),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyEnterpriseGroupService.java'), 'missing enterprise group service'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyEnterpriseServiceImpl.java'), 'missing enterprise service impl'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyEnterpriseServiceImpl.java', 'insertEnterpriseGroupRel'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyEnterpriseController.java'), 'missing enterprise controller'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyEnterpriseController.java', '@PostMapping("/importData")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyEnterpriseController.java', '@PostMapping("/importTemplate")'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyEnterpriseGroupController.java'), 'missing enterprise group controller'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyEnterpriseMapper.xml'), 'missing enterprise mapper xml'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyEnterpriseMapper.xml', 'selectGroupIdsByEnterpriseId'),
  () => assertNotContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyEnterpriseMapper.xml', 'like concat('),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyEnterpriseMapper.xml', "credit_code like '%' || #{creditCode} || '%'"),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyEnterpriseGroupMapper.xml'), 'missing enterprise group mapper xml'),
  () => assertNotContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyEnterpriseGroupMapper.xml', 'like concat('),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyEnterpriseGroupMapper.xml', "group_name like '%' || #{groupName} || '%'"),
  () => assertContains('ruoyi-ui/src/api/survey/enterprise.js', "url: '/survey/enterprise/list'"),
  () => assertContains('ruoyi-ui/src/api/survey/enterprise.js', "url: '/survey/enterprise/group/treeselect'"),
  () => assertContains('ruoyi-ui/src/api/survey/enterprise.js', "url: '/survey/enterprise/group/list'"),
  () => assertContains('ruoyi-ui/src/views/survey/enterprise/index.vue', 'listEnterprise'),
  () => assertContains('ruoyi-ui/src/views/survey/enterprise/index.vue', '<tree-panel'),
  () => assertContains('ruoyi-ui/src/views/survey/enterprise/index.vue', '<excel-import-dialog'),
  () => assertContains('ruoyi-ui/src/views/survey/enterprise/index.vue', 'groupIds'),
];

for (const check of checks) {
  check();
}

console.log('survey issue 002 verification passed');
