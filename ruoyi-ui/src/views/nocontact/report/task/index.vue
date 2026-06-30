<template>
  <div class="app-container">
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="任务名称"><el-input v-model="queryParams.taskName" clearable placeholder="请输入任务名称" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.taskStatus" clearable><el-option label="待生成" value="pending" /><el-option label="已完成" value="completed" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新建生成任务</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="taskList">
      <el-table-column label="任务名称" prop="taskName" min-width="180" />
      <el-table-column label="模板" prop="templateName" min-width="160" />
      <el-table-column label="周期" prop="reportPeriod" width="120" />
      <el-table-column label="范围" min-width="140">
        <template slot-scope="scope">{{ scopeText(scope.row.reportScope) }}</template>
      </el-table-column>
      <el-table-column label="生成方式" width="100">
        <template slot-scope="scope">{{ generateModeText(scope.row.generateMode) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">{{ taskStatusText(scope.row.taskStatus) }}</template>
      </el-table-column>
      <el-table-column label="Word文件" prop="generatedWordFileName" min-width="160" show-overflow-tooltip />
      <el-table-column label="Excel文件" prop="generatedExcelFileName" min-width="160" show-overflow-tooltip />
      <el-table-column label="生成时间" width="180">
        <template slot-scope="scope">{{ formatFriendlyDateTime(scope.row.generatedTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-document-checked" @click="handleGenerate(scope.row)">{{ scope.row.generatedTime ? '再次生成' : '立即生成' }}</el-button>
          <el-button v-if="scope.row.generatedTime" type="text" size="mini" icon="el-icon-time" @click="handleHistory(scope.row)">生成历史</el-button>
          <el-button v-if="scope.row.generatedWordFileName" type="text" size="mini" icon="el-icon-download" @click="handleDownload(scope.row, 'word')">下载Word</el-button>
          <el-button v-if="scope.row.generatedExcelFileName" type="text" size="mini" icon="el-icon-download" @click="handleDownload(scope.row, 'excel')">下载Excel</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="新建生成任务" :visible.sync="open" width="620px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="任务名称" prop="taskName"><el-input v-model="form.taskName" /></el-form-item>
        <el-form-item label="报告模板" prop="templateId">
          <el-select v-model="form.templateId" filterable placeholder="请选择已保存模板">
            <el-option v-for="item in templateOptions" :key="item.templateId" :label="item.templateName" :value="item.templateId" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据周期" prop="reportPeriodType">
          <el-select v-model="form.reportPeriodType" placeholder="请选择周期类型">
            <el-option label="年度" value="year" />
            <el-option label="季度" value="quarter" />
            <el-option label="月度" value="month" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.reportPeriodType === 'year'" label="报告年份">
          <el-date-picker v-model="form.reportYear" type="year" value-format="yyyy" placeholder="请选择年份" />
        </el-form-item>
        <template v-else-if="form.reportPeriodType === 'quarter'">
          <el-form-item label="报告年份">
            <el-date-picker v-model="form.reportYear" type="year" value-format="yyyy" placeholder="请选择年份" />
          </el-form-item>
          <el-form-item label="报告季度">
            <el-select v-model="form.reportQuarter" placeholder="请选择季度">
              <el-option label="第一季度" value="1" />
              <el-option label="第二季度" value="2" />
              <el-option label="第三季度" value="3" />
              <el-option label="第四季度" value="4" />
            </el-select>
          </el-form-item>
        </template>
        <el-form-item v-else-if="form.reportPeriodType === 'month'" label="报告月份">
          <el-date-picker v-model="form.reportMonth" type="month" value-format="yyyy-MM" placeholder="请选择月份" />
        </el-form-item>
        <el-form-item label="报告范围" prop="reportScopeType">
          <el-select v-model="form.reportScopeType" placeholder="请选择范围">
            <el-option label="全省" value="province" />
            <el-option label="指定市州" value="region" />
            <el-option label="指定指标" value="indicator" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.reportScopeType === 'region'" label="指定市州">
          <el-select v-model="form.reportScopeRegion" filterable placeholder="请选择市州">
            <el-option v-for="item in regionOptions" :key="item.code" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item v-else-if="form.reportScopeType === 'indicator'" label="指定指标">
          <el-select v-model="form.reportScopeIndicatorId" filterable placeholder="请选择指标">
            <el-option v-for="item in indicatorOptions" :key="item.indicatorId" :label="item.indicatorName" :value="item.indicatorId" />
          </el-select>
        </el-form-item>
        <el-form-item label="生成方式">
          <el-radio-group v-model="form.generateMode">
            <el-radio label="manual">手动生成</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open=false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="historyTitle" :visible.sync="historyOpen" width="900px" append-to-body>
      <el-table v-loading="historyLoading" :data="snapshotList">
        <el-table-column label="生成时间" width="180">
          <template slot-scope="scope">{{ formatFriendlyDateTime(scope.row.generatedTime) }}</template>
        </el-table-column>
        <el-table-column label="生成人" prop="generatedBy" width="120" />
        <el-table-column label="报告周期" prop="reportPeriod" width="120" />
        <el-table-column label="报告范围" min-width="120">
          <template slot-scope="scope">{{ scopeText(scope.row.reportScope) }}</template>
        </el-table-column>
        <el-table-column label="模板版本" prop="templateVersion" width="100" align="right" />
        <el-table-column label="Word文件" prop="generatedWordFileName" min-width="180" show-overflow-tooltip />
        <el-table-column label="Excel文件" prop="generatedExcelFileName" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center">
          <template slot-scope="scope">
            <el-button v-if="scope.row.generatedWordFileName" type="text" size="mini" icon="el-icon-download" @click="handleSnapshotDownload(scope.row, 'word')">下载Word</el-button>
            <el-button v-if="scope.row.generatedExcelFileName" type="text" size="mini" icon="el-icon-download" @click="handleSnapshotDownload(scope.row, 'excel')">下载Excel</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { listTemplate, listReportTask, addReportTask, generateReportTask, listReportSnapshot, reportTaskDownloadUrl, reportSnapshotDownloadUrl } from '@/api/nocontact/report'
import { listIndicatorOptions } from '@/api/nocontact/fusion/indicator'
import { formatFriendlyDateTime } from '@/utils/nocontactDisplay'

const regionOptions = [
  { code: '430100', name: '长沙市' },
  { code: '430200', name: '株洲市' },
  { code: '430300', name: '湘潭市' },
  { code: '430400', name: '衡阳市' },
  { code: '430500', name: '邵阳市' },
  { code: '430600', name: '岳阳市' },
  { code: '430700', name: '常德市' },
  { code: '430800', name: '张家界市' },
  { code: '430900', name: '益阳市' },
  { code: '431000', name: '郴州市' },
  { code: '431100', name: '永州市' },
  { code: '431200', name: '怀化市' },
  { code: '431300', name: '娄底市' },
  { code: '433100', name: '湘西州' }
]

export default {
  name: 'ReportTask',
  data() {
    return {
      loading: false,
      open: false,
      historyOpen: false,
      historyLoading: false,
      historyTitle: '生成历史',
      total: 0,
      taskList: [],
      snapshotList: [],
      templateOptions: [],
      indicatorOptions: [],
      regionOptions,
      queryParams: { pageNum: 1, pageSize: 10 },
      form: {},
      rules: {
        taskName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
        templateId: [{ required: true, message: '报告模板不能为空', trigger: 'change' }],
        reportPeriodType: [{ required: true, message: '报告周期不能为空', trigger: 'change' }],
        reportScopeType: [{ required: true, message: '报告范围不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
    this.getTemplateOptions()
    this.getIndicatorOptions()
  },
  methods: {
    formatFriendlyDateTime,
    getList() {
      this.loading = true
      listReportTask(this.queryParams).then(response => {
        this.taskList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      })
    },
    getTemplateOptions() {
      listTemplate({ pageNum: 1, pageSize: 1000, status: '0' }).then(response => {
        this.templateOptions = response.rows || []
      })
    },
    getIndicatorOptions() {
      listIndicatorOptions({ status: '0' }).then(response => {
        this.indicatorOptions = response.data || []
      })
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
      this.form = {
        generateMode: 'manual',
        taskStatus: 'pending',
        reportPeriodType: 'quarter',
        reportScopeType: 'province'
      }
      this.open = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const payload = this.buildPayload()
        if (!payload) return
        addReportTask(payload).then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleGenerate(row) {
      generateReportTask(row.taskId).then(() => {
        this.$modal.msgSuccess('生成完成')
        this.getList()
      })
    },
    handleHistory(row) {
      this.historyTitle = '生成历史 - ' + row.taskName
      this.historyOpen = true
      this.historyLoading = true
      listReportSnapshot(row.taskId).then(response => {
        this.snapshotList = response.data || []
        this.historyLoading = false
      }).catch(() => {
        this.historyLoading = false
      })
    },
    handleDownload(row, fileType) {
      const filename = fileType === 'word' ? row.generatedWordFileName : row.generatedExcelFileName
      this.download(reportTaskDownloadUrl(row.taskId, fileType), {}, filename)
    },
    handleSnapshotDownload(row, fileType) {
      const filename = fileType === 'word' ? row.generatedWordFileName : row.generatedExcelFileName
      this.download(reportSnapshotDownloadUrl(row.snapshotId, fileType), {}, filename)
    },
    buildPayload() {
      const reportPeriod = this.buildReportPeriod()
      if (!reportPeriod) return null
      const reportScope = this.buildReportScope()
      if (!reportScope) return null
      return {
        taskName: this.form.taskName,
        templateId: this.form.templateId,
        reportPeriod,
        reportScope,
        generateMode: this.form.generateMode,
        taskStatus: this.form.taskStatus
      }
    },
    buildReportPeriod() {
      if (this.form.reportPeriodType === 'year') {
        if (!this.form.reportYear) {
          this.$modal.msgError('请选择报告年份')
          return null
        }
        return this.form.reportYear
      }
      if (this.form.reportPeriodType === 'quarter') {
        if (!this.form.reportYear || !this.form.reportQuarter) {
          this.$modal.msgError('请选择报告年份和季度')
          return null
        }
        return this.form.reportYear + 'Q' + this.form.reportQuarter
      }
      if (this.form.reportPeriodType === 'month') {
        if (!this.form.reportMonth) {
          this.$modal.msgError('请选择报告月份')
          return null
        }
        return this.form.reportMonth
      }
      this.$modal.msgError('请选择报告周期')
      return null
    },
    buildReportScope() {
      if (this.form.reportScopeType === 'province') {
        return 'province'
      }
      if (this.form.reportScopeType === 'region') {
        if (!this.form.reportScopeRegion) {
          this.$modal.msgError('请选择指定市州')
          return null
        }
        return 'region:' + this.form.reportScopeRegion
      }
      if (this.form.reportScopeType === 'indicator') {
        if (!this.form.reportScopeIndicatorId) {
          this.$modal.msgError('请选择指定指标')
          return null
        }
        return 'indicator:' + this.form.reportScopeIndicatorId
      }
      this.$modal.msgError('请选择报告范围')
      return null
    },
    scopeText(value) {
      if (value === 'province' || value === '全省') return '全省'
      if (value && value.indexOf('region:') === 0) return value.substring(7)
      if (value && value.indexOf('indicator:') === 0) {
        const id = Number(value.substring(10))
        const indicator = this.indicatorOptions.find(item => item.indicatorId === id)
        return indicator ? indicator.indicatorName : '指标#' + value.substring(10)
      }
      return value || '-'
    },
    generateModeText(value) {
      return value === 'manual' ? '手动生成' : (value || '手动生成')
    },
    taskStatusText(value) {
      if (value === 'completed') return '已完成'
      if (value === 'pending') return '待生成'
      return value || '-'
    }
  }
}
</script>
