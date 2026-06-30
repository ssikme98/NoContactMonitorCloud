const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')

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

function assertNotContains(file, text) {
  assert(!read(file).includes(text), `${file} should not contain ${text}`)
}

assertContains('docker/nacos/conf/application.properties', 'spring.datasource.platform=kingbase')
assertContains('docker/nacos/conf/application.properties', 'spring.sql.init.platform=kingbase')
assertContains('docker/nacos/conf/application.properties', 'db.url.0=jdbc:kingbase8://${KINGBASE_HOST:localhost}:${KINGBASE_PORT:54321}/ry-config')
assertContains('docker/nacos/conf/application.properties', 'db.pool.config.driverClassName=com.kingbase8.Driver')
assertContains('docker/nacos/dockerfile', 'COPY ./plugins/ /home/nacos/plugins/')
assertContains('docker/nacos/plugins/README.md', 'nacos-kingbase-datasource-plugin-ext-*.jar')
assertContains('docker/nacos/plugins/README.md', 'kingbase8-*.jar')
assertContains('sql/mysql/README.md', 'ruoyi-nocontact')
assertContains('sql/mysql/README.md', 'sql/kingbase/nocontact_004_full_alignment.sql')
assertContains('sql/mysql/README.md', 'sql/kingbase/nocontact_005_report_snapshot_closure.sql')
assertContains('sql/mysql/README.md', 'sql/kingbase/nocontact_007_support_closure.sql')
assertContains('sql/mysql/README.md', 'jdbc:postgresql://${KINGBASE_HOST:localhost}:${KINGBASE_PORT:54321}/ry-cloud')
assertContains('sql/mysql/README.md', 'org.postgresql.Driver')
assertContains('sql/mysql/README.md', 'Nacos external storage still relies on the Kingbase datasource plugin path')
assertContains('sql/kingbase/ry-config.sql', 'driver-class-name: org.postgresql.Driver')
assertContains('sql/kingbase/ry-config.sql', 'jdbc:postgresql://${KINGBASE_HOST:localhost}:${KINGBASE_PORT:54321}/ry-cloud')
assertContains('sql/kingbase/ry-config.sql', 'username: ${KINGBASE_USERNAME:root}')
assertContains('sql/kingbase/ry-config.sql', 'password: ${KINGBASE_PASSWORD:password}')
assertContains('sql/kingbase/ry-config.sql', 'host: ${REDIS_HOST:localhost}')
assertNotContains('sql/kingbase/ry-config.sql', 'com.mysql.cj.jdbc.Driver')
assertNotContains('sql/kingbase/ry-config.sql', 'jdbc:mysql://localhost:3306/ry-cloud')
assertNotContains('sql/kingbase/ry-config.sql', 'jdbc:mysql://localhost:3306/ry-config')
assertNotContains('docs/survey.md', '纯单次 import')
assertNotContains('docs/survey.md', '没有任何业务定制代码')
assertNotContains('docs/survey.md', 'MySQL（业务）作为基础设施')
assertContains('docs/survey.md', '人大金仓 Kingbase')
assertContains('docs/survey.md', 'ruoyi-nocontact')
assertContains('docs/survey.md', '不再自动导入')
assertContains('sql/.wiki/overview.md', 'Kingbase')
assertContains('DEPLOY_KINGBASE.md', 'helperDialect: kingbase8')
assertContains('DEPLOY_KINGBASE.md', 'nacos-kingbase-datasource-plugin-ext')
assertContains('DEPLOY_KINGBASE.md', '业务服务配置请使用 `org.postgresql.Driver`')
assertContains('DEPLOY_KINGBASE.md', 'Nacos 外置存储仍按插件路径使用 `com.kingbase8.Driver` + `jdbc:kingbase8://...`')

console.log('verify-db-bootstrap-path: manual bootstrap path checked')
