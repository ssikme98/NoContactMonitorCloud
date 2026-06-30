const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8');
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function assertContains(file, text) {
  assert(read(file).includes(text), `${file} missing ${text}`);
}

function assertNotContains(file, text) {
  assert(!read(file).includes(text), `${file} still contains ${text}`);
}

const smoke = 'scripts/smoke-issue-009-010.js';
const helper = 'scripts/lib/nocontactSmoke.js';
const questionnaireVue = 'ruoyi-ui/src/views/survey/questionnaire/index.vue';
const taskVue = 'ruoyi-ui/src/views/survey/task/index.vue';
const taskMapper = 'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/survey/SurveyTaskMapper.xml';
const questionnaireDomain = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/domain/SurveyQuestionnaire.java';
const taskDomain = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/domain/SurveyTask.java';
const taskSampleDomain = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/domain/SurveyTaskSample.java';
const taskSendRecordDomain = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/domain/SurveyTaskSendRecord.java';
const taskTrackingDetailDomain = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/domain/vo/SurveyTaskTrackingDetail.java';
const taskService = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyTaskServiceImpl.java';
const responseService = 'ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyResponseServiceImpl.java';
const questionnaireMapper = 'ruoyi-modules/ruoyi-nocontact/src/main/resources/mapper/survey/SurveyQuestionnaireMapper.xml';

assertContains(helper, 'clickInputByPlaceholder');
assertContains(helper, 'binaryRequest');
assertContains(helper, 'tableCellTextByRowParts');

assertContains(smoke, 'SMOKE-SURVEY-009-010');
assertContains(smoke, 'NOCONTACT_KINGBASE_CONTAINER');
assertContains(smoke, 'cleanup survey smoke fixtures');
assertContains(smoke, 'read excel row');
assertContains(smoke, '/survey/enterprise/importTemplate');
assertContains(smoke, '/survey/enterprise/export?enterpriseName=');
assertContains(smoke, '/nocontact/support/message/list');
assertContains(smoke, 'assertQuestionnaireLifecycle');
assertContains(smoke, 'survey dispatch business message');
assertContains(smoke, '问卷任务已发放');
assertContains(smoke, 'survey dispatch support message row');
assertContains(smoke, '/survey/tracking?taskId=');
assertContains(smoke, 'support dispatch message open did not reach tracking page');
assertContains(smoke, 'editing collecting questionnaire should create a new draft');
assertContains(smoke, 'end collecting questionnaire');
assertContains(smoke, 'questionnaire should enter collecting status after dispatch');
assertContains(smoke, "tableCellTextByRowParts([questionnaireName], '创建时间')");
assertContains(smoke, "tableCellTextByRowParts([questionnaireName], '发布时间')");
assertContains(smoke, "tableCellTextByRowParts([taskName], '创建时间')");
assertContains(smoke, "tableCellTextByRowParts([taskName], '发卷时间')");
assertContains(smoke, '样本抽取任务');
assertContains(smoke, '填报明细');
assertContains(smoke, '城市统计');
assertContains(smoke, '填报趋势');
assertContains(smoke, '满意度分析');
assertContains(smoke, '任务样本');
assertContains(smoke, '发送记录');
assertContains(smoke, 'smoke-issue-009-010: survey defect closure e2e passed');
assertContains(smoke, 'finally');
assertContains(smoke, 'await cleanupFixtures();');
assertNotContains(smoke, 'analytics/report');
assertNotContains(smoke, "runAgentBrowser(['wait'");
assertNotContains(smoke, 'http://127.0.0.1:18080');

assertContains(taskMapper, 'left join survey_enterprise_group g on g.group_id = r.group_id');
assertContains(taskMapper, "',' || g.ancestors || ',' LIKE '%,' || #{groupId} || ',%'");

assertNotContains(questionnaireVue, "addQuestion('likert')");
assertNotContains(questionnaireVue, "label: 'Likert量表'");
assertNotContains(questionnaireVue, 'placeholder="维度，如政策知晓度、服务满意度"');
assertContains(questionnaireVue, 'preview-only-toolbar');
assertContains(questionnaireVue, 'preview-only-meta');
assertContains(questionnaireVue, "{ label: '收集中', value: '3' }");
assertContains(questionnaireVue, "'问卷查看'");
assertContains(questionnaireVue, "return status === '1' || status === '3'");
assertContains(questionnaireVue, 'parseTime(scope.row.createTime)');
assertContains(questionnaireVue, 'parseTime(scope.row.publishedTime)');
assertContains(questionnaireDomain, '@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")');
assertContains('sql/kingbase/survey_003_questionnaire.sql', '题型（single multiple text score matrix_score）');
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/controller/SurveyQuestionnaireController.java', '@PostMapping("/{questionnaireId}/draft")');
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/controller/SurveyQuestionnaireController.java', '@PostMapping("/{questionnaireId}/end")');
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyQuestionnaireServiceImpl.java', 'return createDraftFromSubmitted(questionnaire, exists, questionnaire.getUpdateBy())');
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyQuestionnaireServiceImpl.java', 'private static final String STATUS_COLLECTING = "3"');
assertNotContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyQuestionnaireServiceImpl.java', 'QUESTION_TYPE_MATRIX_SCORE,\n            QUESTION_TYPE_LIKERT');
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyQuestionnaireServiceImpl.java', 'question.setDimension(null);');
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyQuestionnaireServiceImpl.java', 'throw new ServiceException("只有已发布或收集中问卷才能创建新版草稿")');
assertContains('ruoyi-modules/ruoyi-nocontact/src/main/java/com/ruoyi/nocontact/survey/service/impl/SurveyQuestionnaireServiceImpl.java', 'throw new ServiceException("只有已发布或收集中问卷允许结束")');

assertNotContains(taskVue, 'label="token有效期"');
assertNotContains(taskVue, 'tokenExpireHours }}小时');
assertNotContains(taskVue, 'token失效时间');
assertNotContains(taskVue, 'prop="tokenExpireTime"');
assertContains(taskVue, 'label="可用问卷"');
assertContains(taskVue, 'label="城市"');
assertContains(taskVue, 'label="填报明细"');
assertContains(taskVue, 'label="城市统计"');
assertContains(taskVue, 'label="填报趋势"');
assertContains(taskVue, 'label="满意度分析"');
assertContains(taskVue, 'label="任务样本"');
assertContains(taskVue, 'label="发送记录"');
assertContains(taskVue, "Promise.all([");
assertContains(taskVue, "listQuestionnaire({ status: '3', pageNum: 1, pageSize: 200 })");
assertContains(taskVue, 'label="抽样批次"');
assertContains(taskVue, 'label="抽样时间"');
assertContains(taskVue, 'label="填报状态"');
assertContains(taskVue, 'label="填报时间"');
assertContains(taskVue, 'HUNAN_CITY_OPTIONS');
assertContains(taskVue, 'parseTime(scope.row.dispatchTime)');
assertContains(taskVue, 'parseTime(scope.row.createTime)');
assertContains(taskVue, 'parseTime(scope.row.sendTime)');
assertContains(taskService, 'private static final String QUESTIONNAIRE_STATUS_COLLECTING = "3"');
assertContains(taskService, 'promoteQuestionnaireToCollecting(task.getQuestionnaireId(), operName)');
assertContains(taskService, '调研任务只能绑定已发布或收集中问卷版本');
assertContains(taskService, 'setSamplingBatchNo(');
assertContains(taskService, 'setSamplingBatchTime(');
assertContains(taskService, 'setSamplingFilterSnapshot(');
assertContains(taskService, 'setSubmitStatus("0")');
assertContains(taskService, 'createDispatchMessage(');
assertContains(questionnaireMapper, 'and status = #{currentStatus}');
assertContains(responseService, 'updateSendRecordSubmitted(sample.getSampleId(), response.getSubmitTime())');
assertContains(taskDomain, '@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")');
assertContains(taskSampleDomain, '@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")');
assertContains(taskSendRecordDomain, '@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")');
assertContains(taskSendRecordDomain, 'private String submitStatus;');
assertContains(taskSendRecordDomain, 'private Date recoveryTime;');
assertContains(taskTrackingDetailDomain, '@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")');
assertContains(taskTrackingDetailDomain, 'private Date recoveryTime;');

console.log('verify-issue-009-010: survey defect closure contracts checked');
