<template>
  <div class="app-container">
    <div v-if="isDetailEntry" class="entry-title">问题详情与处理流程</div>
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="关键词"><el-input v-model="queryParams.issueTitle" clearable placeholder="编号/标题" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.issueStatus" clearable placeholder="全部"><el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item>
      <el-form-item label="地区"><el-input v-model="queryParams.regionName" clearable placeholder="地区" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col v-if="showAddAction" :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增问题</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="issueList">
      <el-table-column label="问题编号" prop="issueCode" width="160" />
      <el-table-column label="问题标题" prop="issueTitle" min-width="180" show-overflow-tooltip />
      <el-table-column label="级别" width="80">
        <template slot-scope="scope">{{ warningLevelText(scope.row.warningLevel) }}</template>
      </el-table-column>
      <el-table-column label="地区" prop="regionName" width="120" />
      <el-table-column label="责任单位" prop="responsibleUnitName" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" prop="issueStatus" width="110"><template slot-scope="scope">{{ statusText(scope.row.issueStatus) }}</template></el-table-column>
      <el-table-column label="期限" prop="deadline" width="160" />
      <el-table-column label="操作" width="280" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-view" @click="openDetail(scope.row)">{{ detailButtonText }}</el-button>
          <el-button v-if="scope.row.issueStatus==='pending_dispatch'" type="text" size="mini" @click="openAction(scope.row,'dispatch')">分配</el-button>
          <el-button v-if="scope.row.issueStatus==='rejected'" type="text" size="mini" @click="openAction(scope.row,'dispatch')">重新分配</el-button>
          <el-button v-if="['pending_rectification','rework'].includes(scope.row.issueStatus)" type="text" size="mini" @click="startRectification(scope.row)">{{ scope.row.issueStatus==='rework' ? '开始返工' : '开始整改' }}</el-button>
          <el-button v-if="scope.row.issueStatus==='rectifying'" type="text" size="mini" @click="openAction(scope.row,'submit')">提交</el-button>
          <el-button v-if="scope.row.issueStatus==='pending_review'" type="text" size="mini" @click="openAction(scope.row,'review')">审核</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="问题标题" prop="issueTitle"><el-input v-model="form.issueTitle" /></el-form-item>
        <el-form-item label="来源"><el-select v-model="form.sourceType"><el-option label="手动录入" value="manual" /><el-option label="预警转入" value="warning" /></el-select></el-form-item>
        <el-form-item label="预警级别"><el-select v-model="form.warningLevel" clearable><el-option label="一级" value="1" /><el-option label="二级" value="2" /><el-option label="三级" value="3" /></el-select></el-form-item>
        <el-form-item label="地区"><el-input v-model="form.regionName" /></el-form-item>
        <el-form-item label="责任单位"><el-input v-model="form.responsibleUnitName" /></el-form-item>
        <el-form-item label="责任人"><el-input v-model="form.responsiblePerson" /></el-form-item>
        <el-form-item label="整改期限"><el-date-picker v-model="form.deadline" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="选择期限" /></el-form-item>
        <el-form-item label="问题描述"><el-input v-model="form.issueDescription" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open=false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="处理流程" :visible.sync="detailOpen" width="760px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题">{{ detail.issueTitle }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.issueStatus) }}</el-descriptions-item>
        <el-descriptions-item label="责任单位">{{ detail.responsibleUnitName }}</el-descriptions-item>
        <el-descriptions-item label="责任人">{{ detail.responsiblePerson }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ detail.issueDescription }}</el-descriptions-item>
        <el-descriptions-item label="整改结果" :span="2">{{ detail.rectificationResult }}</el-descriptions-item>
      </el-descriptions>
      <el-timeline class="mt16">
        <el-timeline-item v-for="log in detail.logs || []" :key="log.logId" :timestamp="log.handleTime">
          {{ log.actionName }}：{{ log.handleOpinion || '-' }}
        </el-timeline-item>
      </el-timeline>
    </el-dialog>

    <el-dialog :title="actionTitle" :visible.sync="actionOpen" width="560px" append-to-body>
      <el-form :model="actionForm" label-width="110px">
        <template v-if="actionType==='dispatch'">
          <el-form-item label="责任单位"><el-input v-model="actionForm.responsibleUnitName" /></el-form-item>
          <el-form-item label="责任人"><el-input v-model="actionForm.responsiblePerson" /></el-form-item>
          <el-form-item label="督办人"><el-input v-model="actionForm.supervisorName" /></el-form-item>
          <el-form-item label="整改期限"><el-date-picker v-model="actionForm.deadline" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        </template>
        <template v-if="actionType==='submit'">
          <el-form-item label="整改结果"><el-input v-model="actionForm.rectificationResult" type="textarea" :rows="4" /></el-form-item>
          <el-form-item label="附件地址"><el-input v-model="actionForm.attachmentUrl" /></el-form-item>
        </template>
        <template v-if="actionType==='review'">
          <el-form-item label="审核结论"><el-radio-group v-model="actionForm.approved"><el-radio :label="true">通过</el-radio><el-radio :label="false">驳回</el-radio></el-radio-group></el-form-item>
          <el-form-item label="审核意见"><el-input v-model="actionForm.reviewOpinion" type="textarea" :rows="3" /></el-form-item>
        </template>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="actionOpen=false">取 消</el-button>
        <el-button type="primary" @click="submitAction">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listIssue, getIssue, addIssue, updateIssue, dispatchIssue, startIssue, submitIssue, reviewIssue, delIssue } from '@/api/nocontact/rectification/issue'
import { warningLevelText } from '@/utils/nocontactDisplay'

export default {
  name: 'RectificationIssue',
  props: {
    entryMode: {
      type: String,
      default: 'manage'
    }
  },
  data() {
    return {
      loading: false,
      entryBootstrapped: false,
      open: false,
      detailOpen: false,
      actionOpen: false,
      title: '',
      actionTitle: '',
      actionType: '',
      total: 0,
      issueList: [],
      detail: {},
      actionRow: {},
      actionForm: {},
      queryParams: { pageNum: 1, pageSize: 10 },
      form: {},
      statusOptions: [
        { label: '待分配', value: 'pending_dispatch' },
        { label: '待整改', value: 'pending_rectification' },
        { label: '整改中', value: 'rectifying' },
        { label: '待审核', value: 'pending_review' },
        { label: '已驳回', value: 'rejected' },
        { label: '返工中', value: 'rework' },
        { label: '已关闭', value: 'closed' }
      ],
      rules: { issueTitle: [{ required: true, message: '问题标题不能为空', trigger: 'blur' }] }
    }
  },
  created() {
    this.getList()
  },
  computed: {
    isDetailEntry() {
      return this.entryMode === 'detail'
    },
    showAddAction() {
      return !this.isDetailEntry
    },
    detailButtonText() {
      return this.isDetailEntry ? '详情流程' : '详情'
    }
  },
  methods: {
    warningLevelText,
    getList() {
      this.loading = true
      listIssue(this.queryParams).then(response => {
        this.issueList = response.rows || []
        this.total = response.total || 0
        this.loading = false
        this.bootstrapEntryMode()
      })
    },
    bootstrapEntryMode() {
      if (this.entryBootstrapped) {
        return
      }
      if (this.isDetailEntry && this.issueList.length) {
        this.entryBootstrapped = true
        this.openDetail(this.issueList[0])
        return
      }
      if (!this.isDetailEntry) {
        this.entryBootstrapped = true
      }
    },
    statusText(value) {
      const item = this.statusOptions.find(option => option.value === value)
      if (item) {
        return item.label
      }
      if (value === 'review_passed' || value === 'archived') {
        return '已关闭'
      }
      return value
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = { pageNum: 1, pageSize: 10 }
      this.getList()
    },
    handleAdd() {
      this.form = { sourceType: 'manual', issueStatus: 'pending_dispatch' }
      this.title = '新增问题'
      this.open = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.issueId ? updateIssue(this.form) : addIssue(this.form)
        request.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDetail(row) {
      getIssue(row.issueId).then(response => {
        this.detail = response.data || {}
        this.detailOpen = true
      })
    },
    openDetail(row) {
      this.handleDetail(row)
    },
    openAction(row, type) {
      this.actionRow = row
      this.actionType = type
      this.actionForm = { approved: true }
      this.actionTitle = type === 'dispatch'
        ? (row.issueStatus === 'rejected' ? '重新分配整改' : '分配整改')
        : type === 'submit' ? '提交整改' : '审核整改'
      this.actionOpen = true
    },
    startRectification(row) {
      startIssue(row.issueId).then(() => {
        this.$modal.msgSuccess(row.issueStatus === 'rework' ? '已开始返工' : '已开始整改')
        this.getList()
      })
    },
    submitAction() {
      const id = this.actionRow.issueId
      const request = this.actionType === 'dispatch'
        ? dispatchIssue(id, this.actionForm)
        : this.actionType === 'submit'
          ? submitIssue(id, this.actionForm)
          : reviewIssue(id, this.actionForm.approved, this.actionForm)
      request.then(() => {
        this.$modal.msgSuccess('处理成功')
        this.actionOpen = false
        this.getList()
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除问题"' + row.issueTitle + '"？').then(() => delIssue(row.issueId)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    }
  }
}
</script>

<style scoped>
.entry-title {
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.mt16 {
  margin-top: 16px;
}
</style>
