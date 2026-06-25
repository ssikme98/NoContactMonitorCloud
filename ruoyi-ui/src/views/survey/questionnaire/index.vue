<template>
  <div class="app-container questionnaire-page">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="问卷名称" prop="questionnaireName">
        <el-input v-model="queryParams.questionnaireName" placeholder="请输入问卷名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['survey:questionnaire:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['survey:questionnaire:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="questionnaireList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="问卷名称" prop="questionnaireName" min-width="220" show-overflow-tooltip />
      <el-table-column label="版本" prop="versionNo" width="80" align="center">
        <template slot-scope="scope">v{{ scope.row.versionNo || 1 }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建人" prop="createBy" width="110" />
      <el-table-column label="创建时间" prop="createTime" width="160" />
      <el-table-column label="发布时间" prop="publishedTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="310">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['survey:questionnaire:edit']">{{ scope.row.status === '1' ? '查看' : '设计' }}</el-button>
          <el-button v-if="scope.row.status === '0'" size="mini" type="text" icon="el-icon-position" @click="handlePublish(scope.row)" v-hasPermi="['survey:questionnaire:publish']">发布</el-button>
          <el-button v-if="scope.row.status === '1'" size="mini" type="text" icon="el-icon-document-copy" @click="handleDraft(scope.row)" v-hasPermi="['survey:questionnaire:edit']">新版</el-button>
          <el-button v-if="scope.row.status === '1'" size="mini" type="text" icon="el-icon-circle-close" @click="handleEnd(scope.row)" v-hasPermi="['survey:questionnaire:end']">结束</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['survey:questionnaire:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" :visible.sync="open" width="1120px" append-to-body class="questionnaire-dialog">
      <el-form ref="form" :model="form" :rules="rules" label-width="92px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="问卷名称" prop="questionnaireName">
              <el-input v-model="form.questionnaireName" :disabled="readonly" maxlength="120" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="版本">
              <el-input :value="'v' + (form.versionNo || 1)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="预览宽度">
              <el-radio-group v-model="previewMode" size="mini">
                <el-radio-button label="pc">PC</el-radio-button>
                <el-radio-button label="mobile">移动端</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="问卷说明">
          <el-input v-model="form.description" :disabled="readonly" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>

        <div class="designer-shell">
          <div class="designer-editor">
            <div class="designer-toolbar">
              <el-button type="primary" plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addQuestion('single')">单选题</el-button>
              <el-button plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addQuestion('multiple')">多选题</el-button>
              <el-button plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addQuestion('text')">填空题</el-button>
              <el-button plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addQuestion('score')">评分题</el-button>
              <el-button plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addQuestion('matrix_score')">矩阵评分</el-button>
              <el-button plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addQuestion('likert')">Likert</el-button>
            </div>

            <el-empty v-if="!form.questions.length" description="暂无题目" :image-size="80" />
            <div v-for="(question, index) in form.questions" :key="question.localId" class="question-editor">
              <div class="question-editor__head">
                <span class="question-index">Q{{ index + 1 }}</span>
                <el-select v-model="question.questionType" :disabled="readonly" size="mini" class="question-type" @change="resetQuestionOptions(question)">
                  <el-option v-for="item in QUESTION_TYPES" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <el-switch v-model="question.requiredFlag" :disabled="readonly" active-value="1" inactive-value="0" active-text="必填" />
                <el-button-group>
                  <el-button size="mini" icon="el-icon-top" :disabled="readonly || index === 0" @click="moveQuestion(index, -1)" />
                  <el-button size="mini" icon="el-icon-bottom" :disabled="readonly || index === form.questions.length - 1" @click="moveQuestion(index, 1)" />
                  <el-button size="mini" icon="el-icon-delete" :disabled="readonly" @click="removeQuestion(index)" />
                </el-button-group>
              </div>
              <el-input v-model="question.questionTitle" :disabled="readonly" placeholder="请输入题目标题" maxlength="300" show-word-limit />
              <el-row :gutter="12" class="question-meta">
                <el-col :span="12">
                  <el-input v-model="question.dimension" :disabled="readonly" size="small" placeholder="维度，如政策知晓度、服务满意度" />
                </el-col>
                <el-col v-if="isScoreQuestion(question)" :span="6">
                  <el-input-number v-model="question.scoreMax" :disabled="readonly" size="small" :min="1" :max="10" label="评分上限" />
                </el-col>
              </el-row>

              <div v-if="needsOptions(question)" class="option-editor">
                <div class="option-editor__title">选项</div>
                <div v-for="(option, optionIndex) in optionRows(question, 'option')" :key="option.localId" class="option-line">
                  <el-input v-model="option.optionLabel" :disabled="readonly" size="small" placeholder="选项文本" />
                  <el-input-number v-model="option.scoreValue" :disabled="readonly" size="small" :min="0" :max="10" controls-position="right" />
                  <el-button size="mini" icon="el-icon-delete" :disabled="readonly || optionRows(question, 'option').length <= 1" @click="removeOption(question, option)" />
                  <span class="option-order">{{ optionIndex + 1 }}</span>
                </div>
                <el-button plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addOption(question, 'option')">添加选项</el-button>
              </div>

              <div v-if="isMatrixQuestion(question)" class="matrix-editor">
                <div class="matrix-column">
                  <div class="option-editor__title">矩阵行</div>
                  <div v-for="row in matrixRows(question)" :key="row.localId" class="option-line">
                    <el-input v-model="row.optionLabel" :disabled="readonly" size="small" placeholder="行标题" />
                    <el-button size="mini" icon="el-icon-delete" :disabled="readonly || matrixRows(question).length <= 1" @click="removeOption(question, row)" />
                  </div>
                  <el-button plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addOption(question, 'row')">添加行</el-button>
                </div>
                <div class="matrix-column">
                  <div class="option-editor__title">矩阵列</div>
                  <div v-for="column in matrixColumns(question)" :key="column.localId" class="option-line">
                    <el-input v-model="column.optionLabel" :disabled="readonly" size="small" placeholder="列标题" />
                    <el-input-number v-model="column.scoreValue" :disabled="readonly" size="small" :min="0" :max="10" controls-position="right" />
                    <el-button size="mini" icon="el-icon-delete" :disabled="readonly || matrixColumns(question).length <= 1" @click="removeOption(question, column)" />
                  </div>
                  <el-button plain size="mini" icon="el-icon-plus" :disabled="readonly" @click="addOption(question, 'column')">添加列</el-button>
                </div>
              </div>
            </div>
          </div>

          <div class="designer-preview" :class="'designer-preview--' + previewMode">
            <div class="preview-surface">
              <h3>{{ form.questionnaireName || '未命名问卷' }}</h3>
              <p v-if="form.description">{{ form.description }}</p>
              <div v-for="(question, index) in form.questions" :key="question.localId" class="preview-question">
                <div class="preview-title">{{ index + 1 }}. {{ question.questionTitle || '未填写题目标题' }} <span v-if="question.requiredFlag === '1'">*</span></div>
                <el-radio-group v-if="question.questionType === 'single'" disabled>
                  <el-radio v-for="option in optionRows(question, 'option')" :key="option.localId" :label="option.optionValue">{{ option.optionLabel }}</el-radio>
                </el-radio-group>
                <el-checkbox-group v-else-if="question.questionType === 'multiple'" disabled>
                  <el-checkbox v-for="option in optionRows(question, 'option')" :key="option.localId" :label="option.optionValue">{{ option.optionLabel }}</el-checkbox>
                </el-checkbox-group>
                <el-input v-else-if="question.questionType === 'text'" disabled placeholder="填空回答" />
                <el-rate v-else-if="question.questionType === 'score'" disabled :max="question.scoreMax || 5" />
                <div v-else-if="isMatrixQuestion(question)" class="preview-matrix">
                  <table>
                    <thead>
                      <tr>
                        <th></th>
                        <th v-for="column in matrixColumns(question)" :key="column.localId">{{ column.optionLabel }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="row in matrixRows(question)" :key="row.localId">
                        <td>{{ row.optionLabel }}</td>
                        <td v-for="column in matrixColumns(question)" :key="column.localId"><el-radio disabled :label="column.optionValue">&nbsp;</el-radio></td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <el-radio-group v-else-if="question.questionType === 'likert'" disabled>
                  <el-radio v-for="option in optionRows(question, 'option')" :key="option.localId" :label="option.optionValue">{{ option.optionLabel }}</el-radio>
                </el-radio-group>
              </div>
            </div>
          </div>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取 消</el-button>
        <el-button v-if="!readonly" type="primary" @click="submitForm">保存草稿</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listQuestionnaire,
  getQuestionnaire,
  addQuestionnaire,
  updateQuestionnaire,
  delQuestionnaire,
  createDraftQuestionnaire,
  publishQuestionnaire,
  endQuestionnaire
} from '@/api/survey/questionnaire'

const QUESTION_TYPES = [
  { label: '单选题', value: 'single' },
  { label: '多选题', value: 'multiple' },
  { label: '填空题', value: 'text' },
  { label: '评分题', value: 'score' },
  { label: '矩阵评分', value: 'matrix_score' },
  { label: 'Likert量表', value: 'likert' }
]

export default {
  name: 'SurveyQuestionnaire',
  data() {
    return {
      QUESTION_TYPES,
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      questionnaireList: [],
      open: false,
      dialogTitle: '',
      readonly: false,
      previewMode: 'pc',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        questionnaireName: null,
        status: null
      },
      statusOptions: [
        { label: '草稿', value: '0' },
        { label: '已发布', value: '1' },
        { label: '已结束', value: '2' }
      ],
      form: this.emptyForm(),
      rules: {
        questionnaireName: [
          { required: true, message: '问卷名称不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listQuestionnaire(this.queryParams).then(response => {
        this.questionnaireList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      })
    },
    statusText(status) {
      const item = this.statusOptions.find(option => option.value === status)
      return item ? item.label : '未知'
    },
    statusTag(status) {
      return status === '1' ? 'success' : status === '2' ? 'info' : 'warning'
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.questionnaireId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.form = this.emptyForm()
      this.readonly = false
      this.previewMode = 'pc'
      this.dialogTitle = '新增问卷'
      this.open = true
    },
    handleUpdate(row) {
      getQuestionnaire(row.questionnaireId).then(response => {
        this.form = this.normalizeQuestionnaire(response.data)
        this.readonly = this.form.status === '1' || this.form.status === '2'
        this.previewMode = 'pc'
        this.dialogTitle = this.readonly ? '问卷预览' : '设计问卷'
        this.open = true
      })
    },
    handleDraft(row) {
      this.$modal.confirm('确认基于该已发布问卷创建新版草稿？').then(() => {
        return createDraftQuestionnaire(row.questionnaireId)
      }).then(response => {
        this.$modal.msgSuccess('新版草稿已创建')
        this.form = this.normalizeQuestionnaire(response.data)
        this.readonly = false
        this.dialogTitle = '设计新版草稿'
        this.open = true
        this.getList()
      })
    },
    handlePublish(row) {
      this.$modal.confirm('发布后该版本不可原地修改，确认发布？').then(() => {
        return publishQuestionnaire(row.questionnaireId)
      }).then(() => {
        this.$modal.msgSuccess('发布成功')
        this.getList()
      })
    },
    handleEnd(row) {
      this.$modal.confirm('确认结束该问卷？').then(() => {
        return endQuestionnaire(row.questionnaireId)
      }).then(() => {
        this.$modal.msgSuccess('已结束')
        this.getList()
      })
    },
    handleDelete(row) {
      const questionnaireIds = row.questionnaireId || this.ids
      this.$modal.confirm('确认删除选中的问卷？').then(() => {
        return delQuestionnaire(questionnaireIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        this.normalizeOrder()
        const request = this.form.questionnaireId ? updateQuestionnaire(this.form) : addQuestionnaire(this.form)
        request.then(response => {
          if (response.data) {
            this.form = this.normalizeQuestionnaire(response.data)
          }
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    emptyForm() {
      return {
        questionnaireId: null,
        questionnaireName: '',
        description: '',
        status: '0',
        versionNo: 1,
        questions: []
      }
    },
    normalizeQuestionnaire(data) {
      const form = Object.assign(this.emptyForm(), data || {})
      form.questions = (form.questions || []).map(question => this.normalizeQuestion(question))
      return form
    },
    normalizeQuestion(question) {
      const row = Object.assign({
        localId: this.localId(),
        questionTitle: '',
        questionType: 'single',
        requiredFlag: '0',
        dimension: '',
        scoreMax: 5,
        orderNum: 0,
        options: []
      }, question || {})
      row.localId = row.localId || this.localId()
      row.options = (row.options || []).map(option => Object.assign({
        localId: this.localId(),
        optionType: 'option',
        optionLabel: '',
        optionValue: '',
        scoreValue: 0,
        orderNum: 0
      }, option))
      if (!row.options.length && row.questionType !== 'text' && row.questionType !== 'score') {
        this.resetQuestionOptions(row)
      }
      return row
    },
    localId() {
      return 'q_' + Date.now() + '_' + Math.random().toString(36).slice(2)
    },
    addQuestion(type) {
      const question = this.normalizeQuestion({
        questionType: type,
        questionTitle: '',
        requiredFlag: '1',
        orderNum: this.form.questions.length + 1
      })
      this.resetQuestionOptions(question)
      this.form.questions.push(question)
    },
    removeQuestion(index) {
      this.form.questions.splice(index, 1)
      this.normalizeOrder()
    },
    moveQuestion(index, offset) {
      const target = index + offset
      if (target < 0 || target >= this.form.questions.length) {
        return
      }
      const rows = this.form.questions
      rows.splice(target, 0, rows.splice(index, 1)[0])
      this.normalizeOrder()
    },
    normalizeOrder() {
      this.form.questions.forEach((question, index) => {
        question.orderNum = index + 1
        question.options.forEach((option, optionIndex) => {
          option.orderNum = optionIndex + 1
          option.optionValue = option.optionValue || String(optionIndex + 1)
        })
      })
    },
    resetQuestionOptions(question) {
      if (question.questionType === 'text' || question.questionType === 'score') {
        question.options = []
        return
      }
      if (question.questionType === 'matrix_score') {
        question.scoreMax = question.scoreMax || 5
        question.options = []
        this.addOption(question, 'row', '服务响应')
        this.addOption(question, 'row', '办理效率')
        this.addOption(question, 'column', '1分', 1)
        this.addOption(question, 'column', '2分', 2)
        this.addOption(question, 'column', '3分', 3)
        this.addOption(question, 'column', '4分', 4)
        this.addOption(question, 'column', '5分', 5)
        return
      }
      if (question.questionType === 'likert') {
        question.options = []
        this.addOption(question, 'option', '非常不同意', 1)
        this.addOption(question, 'option', '不同意', 2)
        this.addOption(question, 'option', '一般', 3)
        this.addOption(question, 'option', '同意', 4)
        this.addOption(question, 'option', '非常同意', 5)
        return
      }
      question.options = []
      this.addOption(question, 'option', '选项1')
      this.addOption(question, 'option', '选项2')
    },
    addOption(question, optionType, label, scoreValue) {
      const rows = question.options || []
      rows.push({
        localId: this.localId(),
        optionType,
        optionLabel: label || '',
        optionValue: String(rows.length + 1),
        scoreValue: scoreValue || 0,
        orderNum: rows.length + 1
      })
      question.options = rows
    },
    removeOption(question, option) {
      question.options = question.options.filter(item => item !== option)
      this.normalizeOrder()
    },
    optionRows(question, optionType) {
      return (question.options || []).filter(option => option.optionType === optionType)
    },
    matrixRows(question) {
      return this.optionRows(question, 'row')
    },
    matrixColumns(question) {
      return this.optionRows(question, 'column')
    },
    needsOptions(question) {
      return question.questionType === 'single' || question.questionType === 'multiple' || question.questionType === 'likert'
    },
    isMatrixQuestion(question) {
      return question.questionType === 'matrix_score'
    },
    isScoreQuestion(question) {
      return question.questionType === 'score' || question.questionType === 'matrix_score'
    }
  }
}
</script>

<style scoped>
.questionnaire-page .designer-shell {
  display: flex;
  gap: 16px;
  min-height: 520px;
}

.designer-editor {
  flex: 1;
  min-width: 0;
}

.designer-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.question-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 12px;
  padding: 12px;
}

.question-editor__head {
  align-items: center;
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.question-index {
  color: #303133;
  font-weight: 600;
  min-width: 34px;
}

.question-type {
  width: 132px;
}

.question-meta {
  margin-top: 10px;
}

.option-editor,
.matrix-editor {
  margin-top: 12px;
}

.option-editor__title {
  color: #606266;
  font-size: 13px;
  margin-bottom: 6px;
}

.option-line {
  align-items: center;
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}

.option-line .el-input {
  flex: 1;
}

.option-order {
  color: #909399;
  font-size: 12px;
  width: 22px;
}

.matrix-editor {
  display: grid;
  gap: 14px;
  grid-template-columns: 1fr 1fr;
}

.designer-preview {
  border-left: 1px solid #ebeef5;
  flex: 0 0 360px;
  padding-left: 16px;
}

.designer-preview--mobile {
  flex-basis: 260px;
}

.preview-surface {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 460px;
  padding: 16px;
}

.preview-surface h3 {
  font-size: 18px;
  line-height: 26px;
  margin: 0 0 8px;
}

.preview-surface p {
  color: #606266;
  font-size: 13px;
  line-height: 20px;
  margin: 0 0 16px;
}

.preview-question {
  border-top: 1px solid #ebeef5;
  padding: 12px 0;
}

.preview-title {
  color: #303133;
  font-weight: 600;
  margin-bottom: 8px;
}

.preview-title span {
  color: #f56c6c;
}

.preview-matrix {
  overflow-x: auto;
}

.preview-matrix table {
  border-collapse: collapse;
  width: 100%;
}

.preview-matrix th,
.preview-matrix td {
  border: 1px solid #ebeef5;
  font-size: 12px;
  padding: 6px;
  text-align: center;
}

@media (max-width: 1200px) {
  .questionnaire-page .designer-shell {
    flex-direction: column;
  }

  .designer-preview,
  .designer-preview--mobile {
    border-left: 0;
    flex-basis: auto;
    padding-left: 0;
  }
}
</style>
