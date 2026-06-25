const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const sqlFile = 'sql/kingbase/survey_008_demo_seed.sql';
const batch = 'DEMO-HN-20260624-V1';

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8');
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function assertContains(body, expected, label = sqlFile) {
  assert(body.includes(expected), `${label} missing ${expected}`);
}

const sql = read(sqlFile);

for (const prerequisite of [
  'survey_002_enterprise.sql',
  'survey_003_questionnaire.sql',
  'survey_004_task.sql',
  'survey_005_response.sql',
]) {
  assertContains(sql, prerequisite);
}

assertContains(sql, batch);
assertContains(sql, "seed_user text := 'demo-seed-008'");
assertContains(sql, 'DELETE FROM survey_response_answer');
assertContains(sql, 'DELETE FROM survey_enterprise WHERE remark = batch');
assertContains(sql, 'INSERT INTO survey_enterprise(');
assertContains(sql, 'INSERT INTO survey_enterprise_group_rel');
assertContains(sql, 'INSERT INTO survey_questionnaire(');
assertContains(sql, 'INSERT INTO survey_task(');
assertContains(sql, 'INSERT INTO survey_task_sample(');
assertContains(sql, 'INSERT INTO survey_task_send_record(');
assertContains(sql, 'INSERT INTO survey_response(');
assertContains(sql, 'INSERT INTO survey_response_answer');
assertContains(sql, '/survey/fill?token=');
assertContains(sql, 'CROSS JOIN (VALUES (\'sms\'), (\'site\'))');
assertContains(sql, 'right(s.token, 3)::int <= 35');
assertContains(sql, 'right(s.token, 3)::int <= 25');

for (const type of ['single', 'multiple', 'text', 'score', 'matrix_score', 'likert']) {
  assertContains(sql, `'${type}'`);
}

for (const region of ['长沙市', '株洲市', '湘潭市', '衡阳市', '岳阳市', '常德市', '益阳市', '郴州市', '永州市', '怀化市', '娄底市', '湘西州']) {
  assertContains(sql, region);
}

for (const signal of ['slow', 'none', 'disagree', 'neutral', 'WHEN seq_no % 11 = 0 THEN 5', 'WHEN seq_no % 10 = 0 THEN 5']) {
  assertContains(sql, signal);
}

const enterpriseRows = sql.match(/\n\s*\(\d+,\s*'[^']+样本[^']*有限公司'/g) || [];
assert(enterpriseRows.length === 50, `expected 50 enterprise seed rows, got ${enterpriseRows.length}`);

const questionnaireInserts = sql.match(/INSERT INTO survey_questionnaire\(/g) || [];
assert(questionnaireInserts.length === 2, `expected 2 questionnaire inserts, got ${questionnaireInserts.length}`);

const taskInserts = sql.match(/INSERT INTO survey_task\(/g) || [];
assert(taskInserts.length === 2, `expected 2 task inserts, got ${taskInserts.length}`);

const sampleInserts = sql.match(/INSERT INTO survey_task_sample\(/g) || [];
assert(sampleInserts.length === 2, `expected 2 sample insert blocks, got ${sampleInserts.length}`);

console.log('survey issue 008 verification passed');
