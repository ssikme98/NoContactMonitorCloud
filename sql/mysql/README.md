# Manual Bootstrap Reference

## Legacy MySQL leftovers

The target architecture for this checkout is Kingbase only. Files under `sql/mysql/` are kept only as legacy comparison material and historical menu extraction input.

Do not treat these files as runtime bootstrap inputs:

1. `sql/ry_20260417.sql`
2. `sql/quartz.sql`
3. `sql/ry_seata_20210128.sql`
4. `sql/ry_config_20250902.sql`
## What the no-contact service actually runs on

`ruoyi-nocontact` runs only against Kingbase:

- config file: `sql/kingbase/ruoyi-nocontact-dev.yml`
- datasource URL: `jdbc:postgresql://${KINGBASE_HOST:localhost}:${KINGBASE_PORT:54321}/ry-cloud`
- driver class: `org.postgresql.Driver`
- username / password: `${KINGBASE_USERNAME:root}` / `${KINGBASE_PASSWORD:password}`

Important runtime note:

- the repository dependency is still `com.kingbase8:kingbase8`
- in the current environment, that JAR exposes `org.postgresql.Driver`
- therefore business services use the PostgreSQL-compatible JDBC protocol against the same Kingbase instance
- Nacos external storage still relies on the Kingbase datasource plugin path

So a legacy MySQL environment is not enough to run any of these business domains:

- survey
- fusion / warning
- rectification
- external integration
- report generation
- common support

## Business schema execution order

For the no-contact business database, execute the Kingbase scripts manually on an empty target database in this order:

1. `sql/kingbase/survey_002_enterprise.sql`
2. `sql/kingbase/survey_003_questionnaire.sql`
3. `sql/kingbase/survey_004_task.sql`
4. `sql/kingbase/survey_005_response.sql`
5. `sql/kingbase/nocontact_001_fusion_warning.sql`
6. `sql/kingbase/nocontact_002_p0_closure.sql`
7. `sql/kingbase/nocontact_003_indicator_import_scope.sql`
8. `sql/kingbase/nocontact_004_full_alignment.sql`
9. `sql/kingbase/nocontact_005_report_snapshot_closure.sql`
10. `sql/kingbase/nocontact_006_external_sync_idempotence.sql`
11. `sql/kingbase/nocontact_007_support_closure.sql`
12. `sql/kingbase/nocontact_008_ui_alignment.sql`

For a full Kingbase bootstrap, use the base-system files as well:

1. `sql/kingbase/ry-cloud.sql`
2. `sql/kingbase/quartz.sql`
3. `sql/kingbase/ry-config.sql`
4. `sql/kingbase/ry-config_updates.sql`
5. `sql/kingbase/ruoyi-gateway-dev-nocontact-route.yml`
6. `sql/kingbase/ruoyi-nocontact-dev.yml`
