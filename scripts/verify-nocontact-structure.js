const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

function exists(file) {
  return fs.existsSync(path.join(root, file))
}

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8')
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

function assertContains(file, text) {
  assert(read(file).includes(text), `${file} missing ${text}`)
}

const checks = [
  () => assert(exists('ruoyi-modules/ruoyi-nocontact/pom.xml'), 'missing ruoyi-nocontact module'),
  () => assertContains('ruoyi-modules/pom.xml', '<module>ruoyi-nocontact</module>'),
  () => assert(!read('ruoyi-modules/pom.xml').includes('<module>ruoyi-survey</module>'), 'old ruoyi-survey module still registered'),
  () => assertContains('ruoyi-modules/ruoyi-nocontact/pom.xml', '<artifactId>ruoyi-modules-nocontact</artifactId>'),
  () => assert(exists('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/RuoYiNocontactApplication.java'), 'missing nocontact application'),
  () => assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/bootstrap.yml', 'name: ruoyi-nocontact'),
  () => assert(exists('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/controller/SurveyTaskController.java'), 'survey controller not under nocontact/survey'),
  () => assert(exists('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/fusion/controller/FusionOverviewController.java'), 'missing fusion controller skeleton'),
  () => assert(exists('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/warning/controller/WarningOverviewController.java'), 'missing warning controller skeleton'),
  () => assertContains('ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/survey/SurveyTaskMapper.xml', 'com.ruoyi.nocontact.survey.mapper.SurveyTaskMapper'),
  () => assertContains('sql/kingbase/ruoyi-nocontact-dev.yml', 'typeAliasesPackage: com.ruoyi.nocontact'),
  () => assertContains('sql/kingbase/ruoyi-gateway-dev-nocontact-route.yml', 'lb://ruoyi-nocontact'),
  () => assertContains('sql/kingbase/ruoyi-gateway-dev-nocontact-route.yml', 'Path=/survey/**'),
  () => assertContains('sql/kingbase/ruoyi-gateway-dev-nocontact-route.yml', 'Path=/nocontact/**'),
  () => assertContains('docker/docker-compose.yml', 'ruoyi-modules-nocontact'),
  () => assert(exists('docker/ruoyi/modules/nocontact/dockerfile'), 'missing nocontact dockerfile')
]

checks.forEach((check) => check())
console.log(`verify-nocontact-structure: ${checks.length} checks passed`)
