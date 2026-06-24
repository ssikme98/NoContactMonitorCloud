DROP DATABASE IF EXISTS "ry-seata";

CREATE DATABASE "ry-seata";


-- -------------------------------- The script used when storeMode is 'db' --------------------------------
-- the table to store GlobalSession data
CREATE TABLE "global_table" (
    "xid" VARCHAR(128) NOT NULL,
    "transaction_id" BIGINT,
    "status" smallint      NOT NULL,
    "application_id" VARCHAR(32),
    "transaction_service_group" VARCHAR(32),
    "transaction_name" VARCHAR(128),
    "timeout" INT,
    "begin_time" BIGINT,
    "application_data" VARCHAR(2000),
    "gmt_create" timestamp,
    "gmt_modified" timestamp,
    PRIMARY KEY ("xid"));
CREATE INDEX "idx_gmt_modified_status" ON "global_table" ("gmt_modified","status");
CREATE INDEX "idx_transaction_id" ON "global_table" ("transaction_id");

-- the table to store BranchSession data
CREATE TABLE "branch_table" (
    "branch_id" BIGINT       NOT NULL,
    "xid" VARCHAR(128) NOT NULL,
    "transaction_id" BIGINT,
    "resource_group_id" VARCHAR(32),
    "resource_id" VARCHAR(256),
    "branch_type" VARCHAR(8),
    "status" smallint,
    "client_id" VARCHAR(64),
    "application_data" VARCHAR(2000),
    "gmt_create" timestamp(6),
    "gmt_modified" timestamp(6),
    PRIMARY KEY ("branch_id"));
CREATE INDEX "idx_xid" ON "branch_table" ("xid");

-- the table to store lock data
CREATE TABLE "lock_table" (
    "row_key" VARCHAR(128) NOT NULL,
    "xid" VARCHAR(96),
    "transaction_id" BIGINT,
    "branch_id" BIGINT       NOT NULL,
    "resource_id" VARCHAR(256),
    "table_name" VARCHAR(32),
    "pk" VARCHAR(36),
    "gmt_create" timestamp,
    "gmt_modified" timestamp,
    PRIMARY KEY ("row_key"));
CREATE INDEX "idx_branch_id" ON "lock_table" ("branch_id");

-- for AT mode you must to init this sql for you business database. the seata server not need it.
CREATE TABLE "undo_log" (
    "branch_id" bigint   NOT NULL,
    "xid" VARCHAR(100) NOT NULL,
    "context" VARCHAR(128) NOT NULL,
    "rollback_info" bytea     NOT NULL,
    "log_status" int      NOT NULL,
    "log_created" timestamp(6)  NOT NULL,
    "log_modified" timestamp(6)  NOT NULL,
    UNIQUE ("xid","branch_id")
);
COMMENT ON TABLE "undo_log" IS 'AT transaction mode undo table';
COMMENT ON COLUMN "undo_log"."branch_id" IS 'branch transaction id';
COMMENT ON COLUMN "undo_log"."xid" IS 'global transaction id';
COMMENT ON COLUMN "undo_log"."context" IS 'undo_log context,such as serialization';
COMMENT ON COLUMN "undo_log"."rollback_info" IS 'rollback info';
COMMENT ON COLUMN "undo_log"."log_status" IS '0:normal status,1:defense status';
COMMENT ON COLUMN "undo_log"."log_created" IS 'create datetime';
COMMENT ON COLUMN "undo_log"."log_modified" IS 'modify datetime';