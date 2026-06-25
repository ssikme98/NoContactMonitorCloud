const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');

function read(file) {
  return fs.readFileSync(path.join(root, file), 'utf8');
}

function exists(file) {
  return fs.existsSync(path.join(root, file));
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function assertContains(file, expected) {
  const body = read(file);
  assert(body.includes(expected), `${file} missing ${expected}`);
}

function assertNotContains(file, unexpected) {
  const body = read(file);
  assert(!body.includes(unexpected), `${file} must not contain ${unexpected}`);
}

const checks = [
  () => assertContains('sql/kingbase/survey_003_questionnaire.sql', 'CREATE TABLE IF NOT EXISTS survey_questionnaire'),
  () => assertContains('sql/kingbase/survey_003_questionnaire.sql', 'CREATE TABLE IF NOT EXISTS survey_question'),
  () => assertContains('sql/kingbase/survey_003_questionnaire.sql', 'CREATE TABLE IF NOT EXISTS survey_question_option'),
  () => assertContains('sql/kingbase/survey_003_questionnaire.sql', 'survey:questionnaire:publish'),
  () => assertContains('sql/kingbase/survey_003_questionnaire.sql', 'survey:questionnaire:end'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyQuestionnaire.java'), 'missing questionnaire domain'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyQuestion.java'), 'missing question domain'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyQuestionOption.java'), 'missing question option domain'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyQuestionnaire.java', 'private List<SurveyQuestion> questions;'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/domain/SurveyQuestion.java', 'private List<SurveyQuestionOption> options;'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/mapper/SurveyQuestionnaireMapper.java'), 'missing questionnaire mapper'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyQuestionnaireService.java'), 'missing questionnaire service'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyQuestionnaireService.java', 'createDraftFromPublished'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/ISurveyQuestionnaireService.java', 'publishQuestionnaire'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyQuestionnaireServiceImpl.java'), 'missing questionnaire service impl'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyQuestionnaireServiceImpl.java', 'QUESTION_TYPE_LIKERT'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/service/impl/SurveyQuestionnaireServiceImpl.java', 'createDraftFromPublished'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyQuestionnaireController.java'), 'missing questionnaire controller'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyQuestionnaireController.java', '@PostMapping("/{questionnaireId}/publish")'),
  () => assertContains('ruoyi-modules/ruoyi-survey/src/main/java/com/ruoyi/survey/controller/SurveyQuestionnaireController.java', '@PostMapping("/{questionnaireId}/draft")'),
  () => assert(exists('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyQuestionnaireMapper.xml'), 'missing questionnaire mapper xml'),
  () => assertNotContains('ruoyi-modules/ruoyi-survey/src/main/resources/mapper/survey/SurveyQuestionnaireMapper.xml', 'like concat('),
  () => assertContains('ruoyi-ui/src/api/survey/questionnaire.js', "url: '/survey/questionnaire/list'"),
  () => assertContains('ruoyi-ui/src/api/survey/questionnaire.js', "url: '/survey/questionnaire/' + questionnaireId + '/publish'"),
  () => assertContains('ruoyi-ui/src/views/survey/questionnaire/index.vue', 'QUESTION_TYPES'),
  () => assertContains('ruoyi-ui/src/views/survey/questionnaire/index.vue', 'matrixRows'),
  () => assertContains('ruoyi-ui/src/views/survey/questionnaire/index.vue', 'previewMode'),
  () => assertContains('ruoyi-ui/src/views/survey/questionnaire/index.vue', 'moveQuestion'),
];

for (const check of checks) {
  check();
}

console.log('survey issue 003 verification passed');
