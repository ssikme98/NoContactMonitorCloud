-- 营商无感业务模块 003：指标版本化、Excel导入和P0数据权限快照
-- 手工执行，幂等，不由服务启动自动迁移。

ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS indicator_code varchar(80) default '';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS external_code varchar(120) default '';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS system_name varchar(160) default '';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS category_name varchar(160) default '';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS version_no int default 1;
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS lifecycle_status varchar(24);
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS period_type varchar(24) default 'month';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS data_type varchar(24) default 'number';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS algorithm_type varchar(40) default 'threshold';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS algorithm_params text;
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS region_code varchar(32) default '';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS region_name varchar(64) default '';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS tag_names varchar(300) default '';
ALTER TABLE nc_fusion_indicator ADD COLUMN IF NOT EXISTS sort_order int default 0;

UPDATE nc_fusion_indicator
SET indicator_code = 'NC-' || indicator_id
WHERE indicator_code IS NULL OR indicator_code = '';

UPDATE nc_fusion_indicator
SET system_name = '营商环境无感监测指标体系'
WHERE system_name IS NULL OR system_name = '';

UPDATE nc_fusion_indicator
SET category_name = first_level || case when coalesce(second_level, '') = '' then '' else '/' || second_level end
WHERE category_name IS NULL OR category_name = '';

UPDATE nc_fusion_indicator
SET lifecycle_status = case when status = '0' then 'enabled' else 'disabled' end
WHERE lifecycle_status IS NULL OR lifecycle_status = '';

ALTER TABLE nc_fusion_indicator ALTER COLUMN lifecycle_status SET DEFAULT 'draft';

UPDATE nc_fusion_indicator
SET period_type = 'month'
WHERE period_type IS NULL OR period_type = '';

UPDATE nc_fusion_indicator
SET data_type = 'number'
WHERE data_type IS NULL OR data_type = '';

UPDATE nc_fusion_indicator
SET algorithm_type = 'threshold'
WHERE algorithm_type IS NULL OR algorithm_type = '';

WITH version_rows AS (
  SELECT indicator_id,
         indicator_code,
         max(version_no) OVER (PARTITION BY indicator_code) AS max_version_no,
         version_no,
         row_number() OVER (PARTITION BY indicator_code, version_no ORDER BY indicator_id) AS version_rank
  FROM nc_fusion_indicator
  WHERE del_flag = '0'
    AND coalesce(indicator_code, '') <> ''
),
duplicate_versions AS (
  SELECT indicator_id,
         max_version_no,
         row_number() OVER (PARTITION BY indicator_code ORDER BY version_no, indicator_id) AS duplicate_seq
  FROM version_rows
  WHERE version_rank > 1
)
UPDATE nc_fusion_indicator i
SET version_no = d.max_version_no + d.duplicate_seq
FROM duplicate_versions d
WHERE i.indicator_id = d.indicator_id;

WITH enabled_versions AS (
  SELECT indicator_id,
         row_number() OVER (PARTITION BY indicator_code ORDER BY version_no DESC, indicator_id DESC) AS enabled_rank
  FROM nc_fusion_indicator
  WHERE del_flag = '0'
    AND coalesce(indicator_code, '') <> ''
    AND lifecycle_status = 'enabled'
    AND status = '0'
)
UPDATE nc_fusion_indicator i
SET lifecycle_status = 'disabled',
    status = '1',
    update_time = current_timestamp
FROM enabled_versions e
WHERE i.indicator_id = e.indicator_id
  AND e.enabled_rank > 1;

CREATE INDEX IF NOT EXISTS idx_nc_fusion_indicator_code ON nc_fusion_indicator(indicator_code);
CREATE INDEX IF NOT EXISTS idx_nc_fusion_indicator_lifecycle ON nc_fusion_indicator(lifecycle_status);
CREATE INDEX IF NOT EXISTS idx_nc_fusion_indicator_system ON nc_fusion_indicator(system_name, category_name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_nc_fusion_indicator_code_version
ON nc_fusion_indicator(indicator_code, version_no)
WHERE del_flag = '0' AND coalesce(indicator_code, '') <> '';
CREATE UNIQUE INDEX IF NOT EXISTS uk_nc_fusion_indicator_enabled_code
ON nc_fusion_indicator(indicator_code)
WHERE del_flag = '0'
  AND coalesce(indicator_code, '') <> ''
  AND lifecycle_status = 'enabled'
  AND status = '0';

ALTER TABLE nc_fusion_collection_batch ADD COLUMN IF NOT EXISTS dept_id bigint;
UPDATE nc_fusion_collection_batch
SET dept_id = responsible_unit_id
WHERE dept_id IS NULL;

ALTER TABLE nc_fusion_collection_item ADD COLUMN IF NOT EXISTS dept_id bigint;
ALTER TABLE nc_fusion_collection_item ADD COLUMN IF NOT EXISTS responsible_unit_id bigint;
ALTER TABLE nc_fusion_collection_item ADD COLUMN IF NOT EXISTS responsible_unit_name varchar(160) default '';
ALTER TABLE nc_fusion_collection_item ADD COLUMN IF NOT EXISTS region_code varchar(32) default '';
ALTER TABLE nc_fusion_collection_item ADD COLUMN IF NOT EXISTS region_name varchar(64) default '';
ALTER TABLE nc_fusion_collection_item ADD COLUMN IF NOT EXISTS period_key varchar(32) default '';

UPDATE nc_fusion_collection_item i
SET dept_id = b.dept_id,
    responsible_unit_id = b.responsible_unit_id,
    responsible_unit_name = b.responsible_unit_name,
    region_code = b.region_code,
    region_name = b.region_name,
    period_key = b.period_key
FROM nc_fusion_collection_batch b
WHERE i.batch_id = b.batch_id
  AND i.dept_id IS NULL;

ALTER TABLE nc_warning_message ADD COLUMN IF NOT EXISTS dept_id bigint;
UPDATE nc_warning_message
SET dept_id = responsible_unit_id
WHERE dept_id IS NULL;

UPDATE nc_warning_message
SET business_key = rule_id || ':' || indicator_id || ':' || coalesce(responsible_unit_id, 0)
                   || ':' || coalesce(region_code, '') || ':' || coalesce(period_key, '')
WHERE business_key IS NULL
   OR business_key = ''
   OR business_key = rule_id || ':' || indicator_id || ':' || coalesce(responsible_unit_id, 0)
                     || ':' || coalesce(period_key, '');

WITH duplicate_open_messages AS (
  SELECT message_id,
         row_number() OVER (
           PARTITION BY business_key
           ORDER BY coalesce(latest_hit_time, trigger_time, create_time) DESC, message_id DESC
         ) AS open_rank
  FROM nc_warning_message
  WHERE del_flag = '0'
    AND coalesce(business_key, '') <> ''
    AND message_status not in ('closed', 'ignored', 'handled', 'archived')
)
UPDATE nc_warning_message m
SET message_status = 'archived',
    handle_opinion = case when coalesce(handle_opinion, '') = '' then '系统自动归档重复未关闭预警' else handle_opinion end,
    update_time = current_timestamp
FROM duplicate_open_messages d
WHERE m.message_id = d.message_id
  AND d.open_rank > 1;

CREATE TABLE IF NOT EXISTS nc_fusion_collection_import_failure (
  failure_id bigint not null GENERATED BY DEFAULT AS IDENTITY,
  dept_id bigint,
  import_batch_name varchar(160) default '',
  source_record_id varchar(120) default '',
  row_num int,
  field_name varchar(80) default '',
  raw_value varchar(500) default '',
  failure_reason varchar(500) default '',
  create_by varchar(64) default '',
  create_time timestamp,
  remark varchar(500) default null,
  CONSTRAINT pk_nc_fusion_collection_import_failure PRIMARY KEY (failure_id)
);

COMMENT ON TABLE nc_fusion_collection_import_failure IS '数据采集导入失败明细';
COMMENT ON COLUMN nc_fusion_collection_import_failure.row_num IS 'Excel行号';

CREATE INDEX IF NOT EXISTS idx_nc_fusion_batch_dept ON nc_fusion_collection_batch(dept_id);
CREATE INDEX IF NOT EXISTS idx_nc_fusion_item_scope ON nc_fusion_collection_item(indicator_id, responsible_unit_id, region_code, period_key);
CREATE INDEX IF NOT EXISTS idx_nc_fusion_item_dept ON nc_fusion_collection_item(dept_id);
CREATE INDEX IF NOT EXISTS idx_nc_warning_message_dept ON nc_warning_message(dept_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_nc_warning_message_open_business
ON nc_warning_message(business_key)
WHERE del_flag = '0'
  AND coalesce(business_key, '') <> ''
  AND message_status not in ('closed', 'ignored', 'handled', 'archived');
CREATE INDEX IF NOT EXISTS idx_nc_fusion_import_failure_source ON nc_fusion_collection_import_failure(source_record_id);
CREATE INDEX IF NOT EXISTS idx_nc_fusion_import_failure_dept ON nc_fusion_collection_import_failure(dept_id);

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 3034, '采集Excel导入', 3030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'fusion:collection:import', '#', 'admin', current_timestamp, '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 3034);

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 3025, '指标复制草稿', 3020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'fusion:indicator:add', '#', 'admin', current_timestamp, '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 3025);
