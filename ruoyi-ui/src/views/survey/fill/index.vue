<template>
  <div class="fill-page" :class="{ 'fill-mobile': isMobile }">
    <div class="fill-shell">
      <div v-if="loading" class="state-panel">加载中...</div>
      <div v-else-if="!fill.valid" class="state-panel">
        <h2>无法填写</h2>
        <p>{{ fill.message || '链接无效' }}</p>
      </div>
      <div v-else-if="fill.submitted || submitted" class="state-panel">
        <h2>已提交</h2>
        <p>该任务样本已经提交答卷，不能重复提交。</p>
      </div>
      <div v-else class="fill-card">
        <header class="fill-header">
          <h1>{{ questionnaire.questionnaireName }}</h1>
          <p v-if="questionnaire.description">{{ questionnaire.description }}</p>
          <div class="enterprise-line">{{ sample.enterpriseName }}</div>
        </header>

        <section v-for="(question, index) in questions" :key="question.questionId" class="question-block">
          <div class="question-title">
            <span>{{ index + 1 }}.</span>
            <strong>{{ question.questionTitle }}</strong>
            <em v-if="question.requiredFlag === '1'">*</em>
          </div>
          <el-radio-group v-if="question.questionType === 'single' || question.questionType === 'likert'" v-model="answers[question.questionId].optionValue">
            <el-radio v-for="option in optionRows(question, 'option')" :key="option.optionId" :label="option.optionValue">{{ option.optionLabel }}</el-radio>
          </el-radio-group>
          <el-checkbox-group v-else-if="question.questionType === 'multiple'" v-model="answers[question.questionId].values">
            <el-checkbox v-for="option in optionRows(question, 'option')" :key="option.optionId" :label="option.optionValue">{{ option.optionLabel }}</el-checkbox>
          </el-checkbox-group>
          <el-input v-else-if="question.questionType === 'text'" v-model="answers[question.questionId].answerText" type="textarea" :rows="4" placeholder="请输入" />
          <el-rate v-else-if="question.questionType === 'score'" v-model="answers[question.questionId].scoreValue" :max="question.scoreMax || 5" />
          <div v-else-if="question.questionType === 'matrix_score'" class="matrix-fill">
            <table>
              <thead>
                <tr>
                  <th></th>
                  <th v-for="column in matrixColumns(question)" :key="column.optionId">{{ column.optionLabel }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in matrixRows(question)" :key="row.optionId">
                  <td>{{ row.optionLabel }}</td>
                  <td v-for="column in matrixColumns(question)" :key="column.optionId">
                    <el-radio v-model="answers[question.questionId].matrix[row.optionValue]" :label="column.optionValue">&nbsp;</el-radio>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <div class="submit-bar">
          <el-button type="primary" :loading="submitting" @click="submitAnswer">提交答卷</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getFill, submitFill } from '@/api/nocontact/fill'

export default {
  name: 'SurveyPublicFill',
  data() {
    return {
      loading: true,
      submitting: false,
      submitted: false,
      fill: {},
      answers: {},
      isMobile: window.innerWidth <= 640
    }
  },
  computed: {
    token() {
      return this.$route.params.token
    },
    questionnaire() {
      return this.fill.questionnaire || {}
    },
    sample() {
      return this.fill.sample || {}
    },
    questions() {
      return this.questionnaire.questions || []
    }
  },
  created() {
    this.loadFill()
  },
  methods: {
    loadFill() {
      this.loading = true
      getFill(this.token).then(response => {
        this.fill = response.data || {}
        this.initAnswers()
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    initAnswers() {
      const answers = {}
      this.questions.forEach(question => {
        answers[question.questionId] = {
          questionId: question.questionId,
          questionType: question.questionType,
          optionValue: '',
          answerText: '',
          scoreValue: 0,
          values: [],
          matrix: {}
        }
      })
      this.answers = answers
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
    validateAnswers() {
      for (const question of this.questions) {
        const answer = this.answers[question.questionId]
        if (question.requiredFlag !== '1') {
          continue
        }
        if ((question.questionType === 'single' || question.questionType === 'likert') && !answer.optionValue) {
          this.$message.error(`请填写：${question.questionTitle}`)
          return false
        }
        if (question.questionType === 'multiple' && !answer.values.length) {
          this.$message.error(`请填写：${question.questionTitle}`)
          return false
        }
        if (question.questionType === 'text' && !answer.answerText) {
          this.$message.error(`请填写：${question.questionTitle}`)
          return false
        }
        if (question.questionType === 'score' && !answer.scoreValue) {
          this.$message.error(`请评分：${question.questionTitle}`)
          return false
        }
        if (question.questionType === 'matrix_score') {
          const rows = this.matrixRows(question)
          if (rows.some(row => !answer.matrix[row.optionValue])) {
            this.$message.error(`请填写：${question.questionTitle}`)
            return false
          }
        }
      }
      return true
    },
    buildPayload() {
      return {
        answers: this.questions.map(question => {
          const answer = this.answers[question.questionId]
          const row = {
            questionId: question.questionId,
            questionType: question.questionType,
            optionValue: answer.optionValue,
            answerText: answer.answerText,
            scoreValue: answer.scoreValue || null
          }
          if (question.questionType === 'multiple') {
            row.answerText = answer.values.join(',')
          }
          if (question.questionType === 'matrix_score') {
            row.answerText = JSON.stringify(answer.matrix)
          }
          return row
        })
      }
    },
    submitAnswer() {
      if (!this.validateAnswers()) {
        return
      }
      this.submitting = true
      submitFill(this.token, this.buildPayload()).then(() => {
        this.submitted = true
        this.$message.success('提交成功')
      }).catch(err => {
        this.$message.error(err.message || '提交失败，请重试')
      }).finally(() => {
        this.submitting = false
      })
    }
  }
}
</script>

<style scoped>
.fill-page {
  background: #f5f7fa;
  min-height: 100vh;
  padding: 24px;
}

.fill-shell {
  margin: 0 auto;
  max-width: 860px;
}

.fill-card,
.state-panel {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 24px;
}

.state-panel {
  color: #606266;
  text-align: center;
}

.state-panel h2 {
  color: #303133;
  font-size: 22px;
  margin: 0 0 12px;
}

.fill-header {
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 18px;
  padding-bottom: 16px;
}

.fill-header h1 {
  color: #303133;
  font-size: 24px;
  line-height: 32px;
  margin: 0;
}

.fill-header p {
  color: #606266;
  line-height: 22px;
  margin: 10px 0 0;
}

.enterprise-line {
  color: #909399;
  font-size: 13px;
  margin-top: 10px;
}

.question-block {
  border-bottom: 1px solid #ebeef5;
  padding: 18px 0;
}

.question-title {
  color: #303133;
  display: flex;
  gap: 6px;
  line-height: 24px;
  margin-bottom: 12px;
}

.question-title em {
  color: #f56c6c;
  font-style: normal;
}

.matrix-fill {
  overflow-x: auto;
}

.matrix-fill table {
  border-collapse: collapse;
  min-width: 520px;
  width: 100%;
}

.matrix-fill th,
.matrix-fill td {
  border: 1px solid #ebeef5;
  padding: 8px;
  text-align: center;
}

.submit-bar {
  padding-top: 20px;
  text-align: center;
}

.fill-mobile {
  padding: 0;
}

.fill-mobile .fill-card,
.fill-mobile .state-panel {
  border: 0;
  border-radius: 0;
  padding: 18px 14px;
}

.fill-mobile .fill-header h1 {
  font-size: 21px;
}
</style>
