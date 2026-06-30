ALTER TABLE survey_task
  ADD COLUMN IF NOT EXISTS sampling_batch_no varchar(40) default '';

ALTER TABLE survey_task
  ADD COLUMN IF NOT EXISTS sampling_batch_time timestamp;

ALTER TABLE survey_task
  ADD COLUMN IF NOT EXISTS sampling_filter_snapshot text;

COMMENT ON COLUMN survey_task.sampling_batch_no IS '抽样批次号';
COMMENT ON COLUMN survey_task.sampling_batch_time IS '抽样批次时间';
COMMENT ON COLUMN survey_task.sampling_filter_snapshot IS '抽样筛选快照';

ALTER TABLE survey_task_send_record
  ADD COLUMN IF NOT EXISTS submit_status char(1) default '0';

ALTER TABLE survey_task_send_record
  ADD COLUMN IF NOT EXISTS recovery_time timestamp;

COMMENT ON COLUMN survey_task_send_record.submit_status IS '填报状态（0未填报 1已填报）';
COMMENT ON COLUMN survey_task_send_record.recovery_time IS '填报时间';
