const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const {
  agentEval,
  ajaxOk,
  assert,
  binaryRequest,
  bodyText,
  clickElementByText,
  clickInputByPlaceholder,
  clickTableRowAction,
  clickTextButton,
  closeAllPages,
  login,
  openAuthedPage,
  requireWriteGuard,
  setInputValueByPlaceholder,
  tableCellTextByRowParts,
  waitFor,
  waitForBodyIncludes,
  waitForTableRow
} = require('./lib/nocontactSmoke');

const kingbaseContainer = process.env.NOCONTACT_KINGBASE_CONTAINER || 'nocontact-postgres';
const fixturePrefix = 'SMOKE-SURVEY-009-010';
const runTag = `${Date.now().toString().slice(-4)}${String(process.pid % 100).padStart(2, '0')}`;
const parentGroupName = `${fixturePrefix}-PARENT-${runTag}`;
const childGroupName = `${fixturePrefix}-CHILD-${runTag}`;
const questionnaireName = `${fixturePrefix}-QUESTIONNAIRE-${runTag}`;
const taskName = `${fixturePrefix}-TASK-${runTag}`;
const enterpriseFixtures = [
  {
    enterpriseName: `${fixturePrefix}-长沙样本企业-${runTag}`,
    creditCode: `91430100${runTag}0001`,
    regionCode: '430100',
    regionName: '长沙市',
    contactPhone: '13800090001'
  },
  {
    enterpriseName: `${fixturePrefix}-株洲样本企业-${runTag}`,
    creditCode: `91430200${runTag}0002`,
    regionCode: '430200',
    regionName: '株洲市',
    contactPhone: '13900090002'
  }
];
const expectedEnterpriseHeaders = ['企业名称', '统一社会信用代码', '行业分类', '城市', '企业规模', '联系人', '联系电话', '详细地址', '状态'];
const timestampPattern = /^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}$/;

function escapeSqlLiteral(value) {
  return value.replace(/'/g, "''");
}

function quoteSqlLiteral(value) {
  return `'${escapeSqlLiteral(value)}'`;
}

function runKingbaseSql(sql, label) {
  try {
    return execFileSync(
      'docker',
      ['exec', kingbaseContainer, 'psql', '-v', 'ON_ERROR_STOP=1', '-U', 'root', '-d', 'ry-cloud', '-At', '-c', sql],
      { encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe'] }
    ).trim();
  } catch (error) {
    const detail = error.stderr ? String(error.stderr).trim() : error.message;
    throw new Error(`${label} failed: ${detail}`);
  }
}

async function cleanupFixtures() {
  const enterpriseNameSql = enterpriseFixtures.map(item => quoteSqlLiteral(item.enterpriseName)).join(', ');
  const sql = `
delete from survey_response_answer
 where response_id in (
   select response_id from survey_response
    where task_id in (select task_id from survey_task where task_name = ${quoteSqlLiteral(taskName)})
       or questionnaire_id in (select questionnaire_id from survey_questionnaire where questionnaire_name = ${quoteSqlLiteral(questionnaireName)})
       or enterprise_id in (select enterprise_id from survey_enterprise where enterprise_name in (${enterpriseNameSql}))
       or sample_id in (
         select sample_id from survey_task_sample
          where task_id in (select task_id from survey_task where task_name = ${quoteSqlLiteral(taskName)})
             or enterprise_id in (select enterprise_id from survey_enterprise where enterprise_name in (${enterpriseNameSql}))
       )
 );
delete from nc_business_message
 where business_type = 'survey'
   and business_id in (select task_id from survey_task where task_name = ${quoteSqlLiteral(taskName)});
delete from survey_response
 where task_id in (select task_id from survey_task where task_name = ${quoteSqlLiteral(taskName)})
    or questionnaire_id in (select questionnaire_id from survey_questionnaire where questionnaire_name = ${quoteSqlLiteral(questionnaireName)})
    or enterprise_id in (select enterprise_id from survey_enterprise where enterprise_name in (${enterpriseNameSql}))
    or sample_id in (
      select sample_id from survey_task_sample
       where task_id in (select task_id from survey_task where task_name = ${quoteSqlLiteral(taskName)})
          or enterprise_id in (select enterprise_id from survey_enterprise where enterprise_name in (${enterpriseNameSql}))
    );
delete from survey_task_send_record
 where task_id in (select task_id from survey_task where task_name = ${quoteSqlLiteral(taskName)})
    or sample_id in (
      select sample_id from survey_task_sample
       where task_id in (select task_id from survey_task where task_name = ${quoteSqlLiteral(taskName)})
          or enterprise_id in (select enterprise_id from survey_enterprise where enterprise_name in (${enterpriseNameSql}))
    )
    or enterprise_id in (select enterprise_id from survey_enterprise where enterprise_name in (${enterpriseNameSql}));
delete from survey_task_sample
 where task_id in (select task_id from survey_task where task_name = ${quoteSqlLiteral(taskName)})
    or enterprise_id in (select enterprise_id from survey_enterprise where enterprise_name in (${enterpriseNameSql}));
delete from survey_task
 where task_name = ${quoteSqlLiteral(taskName)};
delete from survey_question_option
 where question_id in (
   select question_id from survey_question
    where questionnaire_id in (select questionnaire_id from survey_questionnaire where questionnaire_name = ${quoteSqlLiteral(questionnaireName)})
 );
delete from survey_question
 where questionnaire_id in (select questionnaire_id from survey_questionnaire where questionnaire_name = ${quoteSqlLiteral(questionnaireName)});
delete from survey_questionnaire
 where questionnaire_name = ${quoteSqlLiteral(questionnaireName)};
delete from survey_enterprise_group_rel
 where group_id in (
   select group_id from survey_enterprise_group
    where group_name in (${quoteSqlLiteral(parentGroupName)}, ${quoteSqlLiteral(childGroupName)})
 )
    or enterprise_id in (select enterprise_id from survey_enterprise where enterprise_name in (${enterpriseNameSql}));
delete from survey_enterprise
 where enterprise_name in (${enterpriseNameSql});
delete from survey_enterprise_group
 where group_name in (${quoteSqlLiteral(parentGroupName)}, ${quoteSqlLiteral(childGroupName)});
`;
  runKingbaseSql(sql, 'cleanup survey smoke fixtures');
}

function decodeXmlText(value) {
  return value
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&#10;/g, '\n')
    .replace(/&#13;/g, '\r')
    .replace(/&#9;/g, '\t')
    .replace(/&amp;/g, '&');
}

function unzipEntry(file, entry) {
  return execFileSync('unzip', ['-p', file, entry], {
    encoding: 'utf8',
    stdio: ['pipe', 'pipe', 'pipe']
  });
}

function extractCellText(fragment) {
  return Array.from(fragment.matchAll(/<t[^>]*>([\s\S]*?)<\/t>/g)).map(match => decodeXmlText(match[1])).join('');
}

function readSharedStrings(file) {
  try {
    const xml = unzipEntry(file, 'xl/sharedStrings.xml');
    return Array.from(xml.matchAll(/<si>([\s\S]*?)<\/si>/g)).map(match => extractCellText(match[1]));
  } catch (error) {
    return [];
  }
}

function readExcelRow(buffer, rowNumber) {
  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'nocontact-issue-009-010-'));
  const file = path.join(tempDir, `row-${rowNumber}.xlsx`);
  fs.writeFileSync(file, buffer);
  try {
    const sheetXml = unzipEntry(file, 'xl/worksheets/sheet1.xml');
    const sharedStrings = readSharedStrings(file);
    const rowMatch = sheetXml.match(new RegExp(`<row[^>]*r="${rowNumber}"[^>]*>([\\s\\S]*?)<\\/row>`));
    assert(rowMatch, `excel row ${rowNumber} missing`);
    return Array.from(rowMatch[1].matchAll(/<c\b([^>]*)>([\s\S]*?)<\/c>/g)).map(match => {
      const attrs = match[1];
      const body = match[2];
      const typeMatch = attrs.match(/\st="([^"]+)"/);
      const type = typeMatch ? typeMatch[1] : '';
      if (type === 'inlineStr') {
        return extractCellText(body);
      }
      const valueMatch = body.match(/<v>([\s\S]*?)<\/v>/);
      if (!valueMatch) {
        return '';
      }
      if (type === 's') {
        return sharedStrings[Number(valueMatch[1])] || '';
      }
      return decodeXmlText(valueMatch[1]);
    });
  } catch (error) {
    const detail = error.stderr ? String(error.stderr).trim() : error.message;
    throw new Error(`read excel row ${rowNumber} failed: ${detail}`);
  } finally {
    fs.rmSync(tempDir, { recursive: true, force: true });
  }
}

function assertEnterpriseHeaders(headers, label) {
  expectedEnterpriseHeaders.forEach(header => {
    assert(headers.includes(header), `${label} missing ${header}`);
  });
  assert(!headers.includes('地区'), `${label} still contains 地区`);
  assert(!headers.includes('地区编码'), `${label} still contains 地区编码`);
}

function assertFormattedTimestamp(value, label) {
  assert(timestampPattern.test(value), `${label} missing formatted timestamp: ${value}`);
}

function maskCreditCode(value) {
  return value.slice(0, 4) + '**********' + value.slice(-4);
}

function maskPhone(value) {
  return value.slice(0, 3) + '****' + value.slice(-4);
}

async function listEnterpriseGroups(token) {
  const response = await ajaxOk('GET', '/survey/enterprise/group/list', {
    token,
    query: { groupName: fixturePrefix }
  }, 'survey enterprise group list');
  return response.data || [];
}

async function listEnterprises(token, enterpriseName) {
  const response = await ajaxOk('GET', '/survey/enterprise/list', {
    token,
    query: { pageNum: 1, pageSize: 100, enterpriseName }
  }, 'survey enterprise list');
  return response.rows || [];
}

async function listQuestionnaires(token, name) {
  const response = await ajaxOk('GET', '/survey/questionnaire/list', {
    token,
    query: { pageNum: 1, pageSize: 100, questionnaireName: name }
  }, 'survey questionnaire list');
  return response.rows || [];
}

async function listTasks(token, name) {
  const response = await ajaxOk('GET', '/survey/task/list', {
    token,
    query: { pageNum: 1, pageSize: 100, taskName: name }
  }, 'survey task list');
  return response.rows || [];
}

async function listBusinessMessages(token) {
  const response = await ajaxOk('GET', '/nocontact/support/message/list', { token }, 'support message list');
  return response.rows || [];
}

async function ensureGroup(token, groupName, parentId) {
  await ajaxOk('POST', '/survey/enterprise/group', {
    token,
    body: {
      parentId,
      groupName,
      orderNum: parentId === 0 ? 1 : 2,
      status: '0',
      remark: fixturePrefix
    }
  }, 'create survey enterprise group');
  const groups = await listEnterpriseGroups(token);
  const matched = groups.find(item => item.groupName === groupName);
  assert(matched, `survey enterprise group ${groupName} missing after create`);
  return matched;
}

async function createEnterprise(token, childGroupId, fixture) {
  await ajaxOk('POST', '/survey/enterprise', {
    token,
    body: {
      enterpriseName: fixture.enterpriseName,
      creditCode: fixture.creditCode,
      groupIds: [childGroupId],
      industryCategory: '软件和信息服务',
      regionCode: fixture.regionCode,
      regionName: fixture.regionName,
      enterpriseScale: '中型',
      contactName: '联系人',
      contactPhone: fixture.contactPhone,
      status: '0',
      remark: fixturePrefix
    }
  }, 'create survey enterprise');
  const enterprises = await listEnterprises(token, fixture.enterpriseName);
  const matched = enterprises.find(item => item.enterpriseName === fixture.enterpriseName);
  assert(matched, `survey enterprise ${fixture.enterpriseName} missing after create`);
  return matched;
}

async function createQuestionnaire(token) {
  await ajaxOk('POST', '/survey/questionnaire', {
    token,
    body: {
      questionnaireName,
      description: 'survey smoke fixture questionnaire',
      remark: fixturePrefix,
      questions: [
        {
          questionTitle: '本次事项办理体验是否顺畅',
          questionType: 'single',
          requiredFlag: '1',
          orderNum: 1,
          options: [
            { optionLabel: '顺畅', optionValue: 'smooth', scoreValue: 5, optionType: 'option', orderNum: 1 },
            { optionLabel: '一般', optionValue: 'normal', scoreValue: 3, optionType: 'option', orderNum: 2 }
          ]
        },
        {
          questionTitle: '请简述本次办理体验',
          questionType: 'text',
          requiredFlag: '1',
          orderNum: 2
        },
        {
          questionTitle: '请为整体满意度评分',
          questionType: 'score',
          requiredFlag: '1',
          scoreMax: 5,
          orderNum: 3
        }
      ]
    }
  }, 'create survey questionnaire');
  const questionnaires = await listQuestionnaires(token, questionnaireName);
  const matched = questionnaires.find(item => item.questionnaireName === questionnaireName);
  assert(matched, 'survey questionnaire missing after create');
  await ajaxOk('POST', `/survey/questionnaire/${matched.questionnaireId}/publish`, { token }, 'publish survey questionnaire');
  return waitFor(async () => {
    const items = await listQuestionnaires(token, questionnaireName);
    return items.find(item => item.questionnaireId === matched.questionnaireId && item.status === '1') || null;
  }, 'published survey questionnaire', { timeoutMs: 30000, intervalMs: 1000 });
}

async function createTask(token, questionnaireId, enterpriseIds) {
  await ajaxOk('POST', '/survey/task', {
    token,
    body: {
      taskName,
      questionnaireId,
      sampleSource: 'enterprise',
      samplingMethod: 'specified',
      enterpriseIds,
      sendChannels: ['sms', 'site'],
      remark: fixturePrefix
    }
  }, 'create survey task');
  const tasks = await listTasks(token, taskName);
  const matched = tasks.find(item => item.taskName === taskName);
  assert(matched, 'survey task missing after create');
  await ajaxOk('POST', `/survey/task/${matched.taskId}/dispatch`, { token }, 'dispatch survey task');
  return waitFor(async () => {
    const detail = await ajaxOk('GET', `/survey/task/${matched.taskId}`, { token }, 'survey task detail');
    if (detail.data && detail.data.status === '2' && (detail.data.samples || []).length === enterpriseIds.length) {
      return detail.data;
    }
    return null;
  }, 'dispatched survey task detail', { timeoutMs: 30000, intervalMs: 1000 });
}

function buildAnswers(fill) {
  return (fill.data.questionnaire.questions || []).map(question => {
    const answer = {
      questionId: question.questionId,
      questionType: question.questionType,
      optionValue: '',
      answerText: '',
      scoreValue: null
    };
    const options = question.options || [];
    const optionValues = options.filter(item => item.optionType === 'option').map(item => item.optionValue);
    if (question.questionType === 'single') {
      answer.optionValue = optionValues[0] || '';
    } else if (question.questionType === 'text') {
      answer.answerText = '烟测提交的主观反馈';
    } else if (question.questionType === 'score') {
      answer.scoreValue = Math.min(question.scoreMax || 5, 4);
    }
    return answer;
  });
}

async function submitOneResponse(token, taskDetail) {
  const sample = (taskDetail.samples || [])[0];
  assert(sample && sample.token, 'survey task sample token missing');
  const fill = await ajaxOk('GET', `/survey/public/fill/${sample.token}`, {}, 'survey public fill detail');
  await ajaxOk('POST', `/survey/public/fill/${sample.token}`, {
    body: { answers: buildAnswers(fill) }
  }, 'survey public fill submit');
  return waitFor(async () => {
    const detail = await ajaxOk('GET', `/survey/task/${taskDetail.taskId}`, { token }, 'survey task detail after submit');
    const submitted = (detail.data.samples || []).find(item => item.sampleId === sample.sampleId && item.status === '2');
    return submitted ? detail.data : null;
  }, 'survey response reflected in task detail', { timeoutMs: 30000, intervalMs: 1000 });
}

async function assertQuestionnaireLifecycle(token, publishedQuestionnaireId) {
  const before = await ajaxOk('GET', `/survey/questionnaire/${publishedQuestionnaireId}`, { token }, 'collecting questionnaire detail before edit');
  assert(before.data && before.data.status === '3', 'questionnaire should enter collecting status after dispatch');
  const originalTitle = before.data.questions[0] && before.data.questions[0].questionTitle;
  assert(originalTitle, 'published questionnaire missing first question title');

  const editBody = JSON.parse(JSON.stringify(before.data));
  editBody.description = 'survey smoke fixture questionnaire version 2';
  editBody.questions[0].questionTitle = `${originalTitle}（新版）`;
  const updated = await ajaxOk('PUT', '/survey/questionnaire', {
    token,
    body: editBody
  }, 'edit collecting questionnaire');
  assert(updated.data && updated.data.questionnaireId !== publishedQuestionnaireId, 'editing collecting questionnaire should create a new draft');
  assert(updated.data.status === '0', 'new questionnaire draft should remain in draft status');
  assert(Number(updated.data.versionNo || 0) > Number(before.data.versionNo || 0), 'new draft version number did not increase');
  assert(updated.data.sourceQuestionnaireId === publishedQuestionnaireId, 'new draft did not link back to original questionnaire');
  assert(updated.data.questions[0].questionTitle === `${originalTitle}（新版）`, 'new draft did not retain edited structure');

  const publishedAfterEdit = await ajaxOk('GET', `/survey/questionnaire/${publishedQuestionnaireId}`, { token }, 'collecting questionnaire detail after edit');
  assert(publishedAfterEdit.data.status === '3', 'collecting questionnaire status changed after edit');
  assert(publishedAfterEdit.data.questions[0].questionTitle === originalTitle, 'collecting questionnaire structure was mutated in place');

  const versions = await listQuestionnaires(token, questionnaireName);
  assert(versions.some(item => item.questionnaireId === publishedQuestionnaireId && item.status === '3'), 'collecting questionnaire row missing after versioning');
  assert(versions.some(item => item.questionnaireId === updated.data.questionnaireId && item.status === '0'), 'new draft row missing after versioning');

  await ajaxOk('POST', `/survey/questionnaire/${publishedQuestionnaireId}/end`, { token }, 'end collecting questionnaire');
  const ended = await ajaxOk('GET', `/survey/questionnaire/${publishedQuestionnaireId}`, { token }, 'ended questionnaire detail');
  assert(ended.data.status === '2', 'questionnaire did not enter ended status');
}

async function queryEnterprisePage(name) {
  setInputValueByPlaceholder('请输入企业名称', name);
  clickTextButton('搜索');
  return waitForTableRow([name], `enterprise row ${name}`, { timeoutMs: 30000 });
}

async function queryQuestionnairePage(name) {
  setInputValueByPlaceholder('请输入问卷名称', name);
  clickTextButton('搜索');
  return waitForTableRow([name], `questionnaire row ${name}`, { timeoutMs: 30000 });
}

async function queryTaskPage(name) {
  setInputValueByPlaceholder('请输入任务名称', name);
  clickTextButton('搜索');
  return waitForTableRow([name], `task row ${name}`, { timeoutMs: 30000 });
}

function isDialogVisible(title) {
  return agentEval(`
(() => {
  const title = ${JSON.stringify(title)};
  return Array.from(document.querySelectorAll('.el-dialog__wrapper')).some(wrapper => {
    const style = window.getComputedStyle(wrapper);
    return style.display !== 'none' && wrapper.innerText.includes(title);
  });
})()
`);
}

function closeDialogByTitle(title) {
  const result = agentEval(`
(() => {
  const title = ${JSON.stringify(title)};
  const wrapper = Array.from(document.querySelectorAll('.el-dialog__wrapper')).find(item => {
    const style = window.getComputedStyle(item);
    return style.display !== 'none' && item.innerText.includes(title);
  });
  if (!wrapper) {
    return { ok: false, reason: 'dialog missing' };
  }
  const button = wrapper.querySelector('.el-dialog__headerbtn');
  if (!button) {
    return { ok: false, reason: 'close button missing' };
  }
  button.click();
  return { ok: true };
})()
`);
  assert(result && result.ok, `close dialog ${title} failed: ${JSON.stringify(result)}`);
}

async function closeTaskDetailIfPresent() {
  if (isDialogVisible('调研任务详情')) {
    closeDialogByTitle('调研任务详情');
    await waitFor(() => !isDialogVisible('调研任务详情'), 'close task detail dialog', { timeoutMs: 10000 });
  }
}

function taskDetailDescriptionLabels() {
  return agentEval(`
(() => Array.from(document.querySelectorAll('.el-descriptions .el-descriptions-item__label'))
  .map(node => (node.innerText || '').trim())
  .filter(Boolean))()
`);
}

async function main() {
  requireWriteGuard('survey smoke will recreate dedicated survey groups, enterprises, questionnaire, task, and one submitted response');
  await cleanupFixtures();
  try {
    const { token, expiresIn } = await login();
    const parentGroup = await ensureGroup(token, parentGroupName, 0);
    const childGroup = await ensureGroup(token, childGroupName, parentGroup.groupId);
    const enterprises = [];
    for (const fixture of enterpriseFixtures) {
      enterprises.push(await createEnterprise(token, childGroup.groupId, fixture));
    }
    const questionnaire = await createQuestionnaire(token);
    const taskDetail = await createTask(token, questionnaire.questionnaireId, enterprises.map(item => item.enterpriseId));
    await submitOneResponse(token, taskDetail);
    const surveyDispatchMessage = await waitFor(async () => {
      const messages = await listBusinessMessages(token);
      return messages.find(item => item.businessType === 'survey' && item.businessId === taskDetail.taskId) || null;
    }, 'survey dispatch business message', { timeoutMs: 30000, intervalMs: 1000 });
    assert(surveyDispatchMessage.readStatus === '0', 'survey dispatch message should start unread');
    assert(surveyDispatchMessage.jumpTarget === `/survey/tracking?taskId=${taskDetail.taskId}`, 'survey dispatch message jump target mismatch');

    const importTemplate = await binaryRequest('POST', '/survey/enterprise/importTemplate', token);
    const importHeaders = readExcelRow(importTemplate.buffer, 1);
    assertEnterpriseHeaders(importHeaders, 'enterprise import template');

    const exportFile = await binaryRequest('POST', `/survey/enterprise/export?enterpriseName=${encodeURIComponent(enterpriseFixtures[0].enterpriseName)}`, token);
    const exportHeaders = readExcelRow(exportFile.buffer, 1);
    const exportFirstRow = readExcelRow(exportFile.buffer, 2);
    assertEnterpriseHeaders(exportHeaders, 'enterprise export header');
    assert(exportFirstRow.some(value => value === '长沙市' || value === '株洲市'), 'enterprise export missing city data');

    await openAuthedPage('/survey/enterprise', token, expiresIn);
    await waitForBodyIncludes('城市', 'survey enterprise page did not render', { timeoutMs: 60000 });
    assert(!bodyText().includes('地区编码'), 'enterprise page still shows 地区编码');
    assert(!bodyText().includes('请选择地区'), 'enterprise page still uses 地区 wording');
    clickElementByText(parentGroupName, '.survey-group-node .node-label, .tree-node .node-label, .el-tree-node__label');
    clickInputByPlaceholder('请选择城市');
    await waitForBodyIncludes('长沙市', 'enterprise city options missing changsha');
    await waitForBodyIncludes('株洲市', 'enterprise city options missing zhuzhou');
    clickElementByText('长沙市', '.el-select-dropdown__item span');
    const enterpriseRow = await queryEnterprisePage(enterpriseFixtures[0].enterpriseName);
    assert(enterpriseRow.includes(maskCreditCode(enterpriseFixtures[0].creditCode)), 'enterprise row missing masked credit code');
    assert(!enterpriseRow.includes(enterpriseFixtures[0].creditCode), 'enterprise row leaked raw credit code');
    assert(enterpriseRow.includes(maskPhone(enterpriseFixtures[0].contactPhone)), 'enterprise row missing masked phone');
    assert(!enterpriseRow.includes(enterpriseFixtures[0].contactPhone), 'enterprise row leaked raw phone');
    assert(enterpriseRow.includes('长沙市'), 'enterprise row missing city label');
    clickTextButton('新增');
    await waitForBodyIncludes('地址解析', 'enterprise add dialog missing city form');
    clickInputByPlaceholder('请选择城市');
    await waitForBodyIncludes('长沙市', 'enterprise add dialog missing changsha option');
    await waitForBodyIncludes('株洲市', 'enterprise add dialog missing zhuzhou option');
    clickTextButton('取 消');

    await openAuthedPage('/survey/designer', token, expiresIn);
    await waitForBodyIncludes('问卷设计器', 'survey designer page did not render', { timeoutMs: 60000 });
    await waitForBodyIncludes('单选题', 'survey designer toolbar missing');
    assert(bodyText().includes('多选题'), 'survey designer missing 多选题');
    assert(bodyText().includes('填空题'), 'survey designer missing 填空题');
    assert(bodyText().includes('评分题'), 'survey designer missing 评分题');
    assert(bodyText().includes('矩阵评分'), 'survey designer missing 矩阵评分');
    assert(!bodyText().includes('Likert量表'), 'survey designer still shows Likert option');
    assert(!bodyText().includes('维度，如政策知晓度、服务满意度'), 'survey designer still shows removed dimension placeholder');
    clickTextButton('取 消');

    await openAuthedPage('/survey/questionnaire', token, expiresIn);
    await waitForBodyIncludes('问卷名称', 'survey questionnaire page did not render', { timeoutMs: 60000 });
    const questionnaireRow = await queryQuestionnairePage(questionnaireName);
    assert(questionnaireRow.includes('收集中'), 'questionnaire row missing 收集中 state');
    assertFormattedTimestamp(tableCellTextByRowParts([questionnaireName], '创建时间'), 'questionnaire create time');
    assertFormattedTimestamp(tableCellTextByRowParts([questionnaireName], '发布时间'), 'questionnaire publish time');
    clickTableRowAction([questionnaireName], '查看');
    await waitForBodyIncludes('问卷查看', 'questionnaire readonly dialog missing');
    assert(!bodyText().includes('保存草稿'), 'questionnaire readonly dialog still exposes 保存草稿');
    assert(!bodyText().includes('添加选项'), 'questionnaire readonly dialog still exposes 添加选项');
    assert(!bodyText().includes('PC'), 'questionnaire readonly dialog still exposes preview mode switch');
    assert(!bodyText().includes('移动端'), 'questionnaire readonly dialog still exposes mobile preview switch');
    clickTextButton('取 消');

    await openAuthedPage('/support/message', token, expiresIn);
    await waitForBodyIncludes('消息中心', 'support message page did not render', { timeoutMs: 60000 });
    await waitForTableRow(['问卷任务已发放', String(taskDetail.taskId), '未读'], 'survey dispatch support message row');
    clickTableRowAction(['问卷任务已发放', String(taskDetail.taskId), '未读'], '打开');
    await waitForBodyIncludes('填报追踪', 'support dispatch message open did not reach tracking page', { timeoutMs: 60000 });
    await waitForBodyIncludes('调研任务详情', 'support dispatch message did not auto-open tracking detail', { timeoutMs: 60000 });
    await waitForBodyIncludes(taskName, 'support dispatch message opened wrong task detail', { timeoutMs: 60000 });
    closeDialogByTitle('调研任务详情');

    await openAuthedPage('/survey/sample', token, expiresIn);
    await waitForBodyIncludes('样本抽取与发卷', 'survey sample page did not render', { timeoutMs: 60000 });
    await waitForBodyIncludes('样本抽取任务', 'sample add dialog did not auto-open');
    assert(bodyText().includes('可用问卷'), 'sample add dialog missing questionnaire selector');
    assert(bodyText().includes('对象池来源'), 'sample add dialog missing sample source selector');
    assert(!bodyText().includes('token有效期'), 'sample add dialog still shows token有效期');
    assert(!bodyText().includes('token失效时间'), 'sample add dialog still shows token失效时间');
    clickTextButton('取 消');
    const sampleRow = await queryTaskPage(taskName);
    assert(sampleRow.includes('已发卷'), 'sample task row missing collecting state');
    assertFormattedTimestamp(tableCellTextByRowParts([taskName], '创建时间'), 'task create time');
    assertFormattedTimestamp(tableCellTextByRowParts([taskName], '发卷时间'), 'task dispatch time');

    await openAuthedPage('/survey/tracking', token, expiresIn);
    await waitForBodyIncludes('填报追踪', 'survey tracking page did not render', { timeoutMs: 60000 });
    await closeTaskDetailIfPresent();
    const trackingRow = await queryTaskPage(taskName);
    assert(trackingRow.includes('追踪'), 'tracking wrapper missing 追踪 action');
    clickTableRowAction([taskName], '追踪');
    await waitForBodyIncludes('调研任务详情', 'survey task detail dialog missing');
    await waitForBodyIncludes('已填报数', 'tracking detail missing submitted metric');
    await waitForBodyIncludes('未填报数', 'tracking detail missing unsubmitted metric');
    await waitForBodyIncludes('填报率', 'tracking detail missing response metric');
    assert(!bodyText().includes('token有效期'), 'tracking detail still shows token有效期');
    assert(!taskDetailDescriptionLabels().includes('状态'), 'tracking detail header still shows 状态 field');
    clickInputByPlaceholder('请选择城市');
    await waitForBodyIncludes('长沙市', 'tracking city filter missing changsha option');
    await waitForBodyIncludes('株洲市', 'tracking city filter missing zhuzhou option');
    clickElementByText('长沙市', '.el-select-dropdown__item span');
    clickTextButton('搜索');
    await waitForBodyIncludes('长沙市', 'tracking detail missing submitted city');
    assert(!bodyText().includes('株洲市\t'), 'tracking city filter did not narrow detail rows');
    assertFormattedTimestamp(tableCellTextByRowParts(['长沙市'], '发送时间'), 'tracking send time');
    assertFormattedTimestamp(tableCellTextByRowParts(['长沙市'], '提交时间'), 'tracking submit time');
    clickTextButton('重置');
    await waitForBodyIncludes('株洲市', 'tracking detail reset did not restore pending city');
    ['填报明细', '城市统计', '填报趋势', '满意度分析', '任务样本', '发送记录'].forEach(label => {
      assert(bodyText().includes(label), `tracking detail missing ${label} tab`);
    });
    clickElementByText('发送记录', '.el-tabs__item');
    await waitForBodyIncludes('填报时间', 'send record tab missing recovery time');
    await waitForBodyIncludes('已填报', 'send record tab missing filled status');
    closeDialogByTitle('调研任务详情');

    await openAuthedPage('/survey/analytics', token, expiresIn);
    await waitForBodyIncludes('满意度统计分析', 'survey analytics page did not render', { timeoutMs: 60000 });
    await closeTaskDetailIfPresent();
    const analyticsRow = await queryTaskPage(taskName);
    assert(analyticsRow.includes('分析'), 'analytics wrapper missing 分析 action');
    clickTableRowAction([taskName], '分析');
    await waitForBodyIncludes('有效答卷数', 'analytics detail missing response count');
    await waitForBodyIncludes('总体满意度', 'analytics detail missing overall score');
    await waitForBodyIncludes('本次事项办理体验是否顺畅', 'analytics detail missing distribution question');
    await waitForBodyIncludes('城市', 'analytics detail missing city analysis');
    closeDialogByTitle('调研任务详情');

    await assertQuestionnaireLifecycle(token, questionnaire.questionnaireId);

    console.log('smoke-issue-009-010: survey defect closure e2e passed');
  } finally {
    try {
      await cleanupFixtures();
    } finally {
      try {
        closeAllPages();
      } catch (error) {
        // ignore close failure on abort
      }
    }
  }
}

main().catch(error => {
  console.error(error.message || error);
  process.exit(1);
});
