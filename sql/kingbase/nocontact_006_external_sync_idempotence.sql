-- 营商无感业务模块 006：外部对接 payload 幂等索引修正
-- 执行前请先确认 nc_external_sync_payload 中不存在
-- source_system + external_id + version_hash 的重复记录。

DROP INDEX IF EXISTS uk_nc_external_payload_batch_source;

CREATE UNIQUE INDEX IF NOT EXISTS uk_nc_external_payload_source_version
  ON nc_external_sync_payload(source_system, external_id, version_hash);
