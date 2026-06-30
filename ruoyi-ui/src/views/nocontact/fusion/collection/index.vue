<template>
  <div class="app-container fusion-collection-page">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="批次名称" prop="batchName">
        <el-input v-model="queryParams.batchName" placeholder="请输入批次名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="batchStatus">
        <el-select v-model="queryParams.batchStatus" placeholder="全部" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="责任单位" prop="responsibleUnitName">
        <el-input v-model="queryParams.responsibleUnitName" placeholder="责任单位" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="业务期间" prop="periodKey">
        <el-input v-model="queryParams.periodKey" placeholder="例如 2026-06" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleSubmitBatch" v-hasPermi="['nocontact:fusion:collection:add']">提交采集数据</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['nocontact:fusion:collection:import']">导入Excel</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-document" size="mini" @click="handleImportFailures" v-hasPermi="['nocontact:fusion:collection:query']">失败明细</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="batchList">
      <el-table-column label="批次名称" prop="batchName" min-width="180" show-overflow-tooltip />
      <el-table-column label="来源" prop="sourceType" width="100">
        <template slot-scope="scope">{{ sourceText(scope.row.sourceType) }}</template>
      </el-table-column>
      <el-table-column label="责任单位" prop="responsibleUnitName" min-width="150" show-overflow-tooltip />
      <el-table-column label="地区" prop="regionName" width="120" />
      <el-table-column label="业务期间" prop="periodKey" width="110" />
      <el-table-column label="明细数" prop="itemCount" width="80" align="right" />
      <el-table-column label="状态" prop="batchStatus" width="110">
        <template slot-scope="scope">
          <el-tag size="mini" :type="statusTag(scope.row.batchStatus)">{{ statusText(scope.row.batchStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交人" prop="submitBy" width="110" />
      <el-table-column label="提交时间" width="180">
        <template slot-scope="scope">{{ formatFriendlyDateTime(scope.row.submitTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)" v-hasPermi="['nocontact:fusion:collection:query']">详情</el-button>
          <el-button v-if="scope.row.batchStatus === 'pending_audit'" size="mini" type="text" icon="el-icon-check" @click="handleApprove(scope.row)" v-hasPermi="['nocontact:fusion:collection:audit']">通过</el-button>
          <el-button v-if="scope.row.batchStatus === 'pending_audit'" size="mini" type="text" icon="el-icon-close" @click="handleReject(scope.row)" v-hasPermi="['nocontact:fusion:collection:audit']">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="提交采集数据" :visible.sync="submitOpen" width="920px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="批次名称" prop="batchName">
              <el-input v-model="form.batchName" maxlength="160" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任单位" prop="responsibleUnitName">
              <el-input v-model="form.responsibleUnitName" maxlength="160" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="责任单位ID">
              <el-input-number v-model="form.responsibleUnitId" :min="1" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="地区编码">
              <el-input v-model="form.regionCode" maxlength="32" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="地区名称">
              <el-input v-model="form.regionName" maxlength="64" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="周期类型">
              <el-select v-model="form.periodType">
                <el-option label="月度" value="month" />
                <el-option label="季度" value="quarter" />
                <el-option label="年度" value="year" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务期间" prop="periodKey">
              <el-input v-model="form.periodKey" placeholder="例如 2026-06" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源类型">
              <el-select v-model="form.sourceType">
                <el-option label="在线填报" value="form" />
                <el-option label="Excel导入" value="excel" />
                <el-option label="接口拉取" value="api" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">采集明细</el-divider>
        <el-table :data="form.items" border size="small">
          <el-table-column label="指标ID" width="120">
            <template slot-scope="scope"><el-input-number v-model="scope.row.indicatorId" :min="1" controls-position="right" /></template>
          </el-table-column>
          <el-table-column label="指标编码" width="140">
            <template slot-scope="scope"><el-input v-model="scope.row.indicatorCode" /></template>
          </el-table-column>
          <el-table-column label="指标名称" min-width="180">
            <template slot-scope="scope"><el-input v-model="scope.row.indicatorName" /></template>
          </el-table-column>
          <el-table-column label="原始值" width="140">
            <template slot-scope="scope"><el-input v-model="scope.row.rawValue" /></template>
          </el-table-column>
          <el-table-column label="数值" width="150">
            <template slot-scope="scope"><el-input-number v-model="scope.row.valueDecimal" controls-position="right" /></template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope"><el-button type="text" icon="el-icon-delete" @click="removeItem(scope.$index)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <el-button class="mt8" size="mini" icon="el-icon-plus" @click="addItem">新增明细</el-button>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="submitOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">提交审核</el-button>
      </div>
    </el-dialog>

    <el-dialog title="采集批次详情" :visible.sync="detailOpen" width="920px" append-to-body>
      <el-descriptions v-if="detail.batchId" :column="3" border size="small">
        <el-descriptions-item label="批次名称">{{ detail.batchName }}</el-descriptions-item>
        <el-descriptions-item label="责任单位">{{ detail.responsibleUnitName }}</el-descriptions-item>
        <el-descriptions-item label="业务期间">{{ detail.periodKey }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.batchStatus) }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.auditBy }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ formatFriendlyDateTime(detail.auditTime) }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">明细</el-divider>
      <el-table :data="detail.items || []" size="small" border>
        <el-table-column label="指标编码" prop="indicatorCode" width="140" />
        <el-table-column label="指标名称" prop="indicatorName" min-width="180" />
        <el-table-column label="原始值" prop="rawValue" width="140" />
        <el-table-column label="数值" prop="valueDecimal" width="120" align="right" />
        <el-table-column label="校验状态" prop="validationStatus" width="120" />
        <el-table-column label="校验信息" prop="validationMessage" min-width="160" show-overflow-tooltip />
      </el-table>
      <el-divider content-position="left">审核日志</el-divider>
      <el-table :data="detail.auditLogs || []" size="small" border>
        <el-table-column label="动作" prop="actionName" width="120" />
        <el-table-column label="原状态" prop="fromStatus" width="120">
          <template slot-scope="scope">{{ statusText(scope.row.fromStatus) }}</template>
        </el-table-column>
        <el-table-column label="目标状态" prop="toStatus" width="120">
          <template slot-scope="scope">{{ statusText(scope.row.toStatus) }}</template>
        </el-table-column>
        <el-table-column label="操作人" prop="auditBy" width="110" />
        <el-table-column label="操作时间" width="180">
          <template slot-scope="scope">{{ formatFriendlyDateTime(scope.row.auditTime) }}</template>
        </el-table-column>
        <el-table-column label="意见" prop="auditOpinion" min-width="180" show-overflow-tooltip />
      </el-table>
      <div slot="footer" class="dialog-footer"><el-button @click="detailOpen = false">关 闭</el-button></div>
    </el-dialog>

    <el-dialog title="导入失败明细" :visible.sync="failureOpen" width="920px" append-to-body>
      <el-form ref="failureQueryForm" :model="failureQuery" size="small" :inline="true">
        <el-form-item label="导入批次" prop="importBatchName">
          <el-input v-model="failureQuery.importBatchName" placeholder="请输入导入批次" clearable @keyup.enter.native="getFailureList" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="getFailureList">查询</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetFailureQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="failureLoading" :data="failureList" border size="small">
        <el-table-column label="导入批次" prop="importBatchName" min-width="170" show-overflow-tooltip />
        <el-table-column label="行号" prop="rowNum" width="80" align="right" />
        <el-table-column label="字段" prop="fieldName" width="120" />
        <el-table-column label="原始值" prop="rawValue" min-width="140" show-overflow-tooltip />
        <el-table-column label="失败原因" prop="failureReason" min-width="220" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template slot-scope="scope">{{ formatFriendlyDateTime(scope.row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <pagination v-show="failureTotal > 0" :total="failureTotal" :page.sync="failureQuery.pageNum" :limit.sync="failureQuery.pageSize" @pagination="getFailureList" />
      <div slot="footer" class="dialog-footer"><el-button @click="failureOpen = false">关 闭</el-button></div>
    </el-dialog>

    <excel-import-dialog ref="importDialog" title="采集数据导入" action="/nocontact/fusion/collection/importData" template-action="/nocontact/fusion/collection/importTemplate" template-file-name="采集导入模板" :show-update-support="false" @success="getList" />
  </div>
</template>

<script>
import ExcelImportDialog from '@/components/ExcelImportDialog'
import { listCollection, getCollection, listImportFailures, submitCollection, approveCollection, rejectCollection } from '@/api/nocontact/fusion/collection'
import { formatFriendlyDateTime } from '@/utils/nocontactDisplay'

export default {
  name: 'FusionCollection',
  components: { ExcelImportDialog },
  data() {
    return {
      loading: false,
      failureLoading: false,
      showSearch: true,
      total: 0,
      failureTotal: 0,
      batchList: [],
      failureList: [],
      submitOpen: false,
      detailOpen: false,
      failureOpen: false,
      detail: {},
      queryParams: { pageNum: 1, pageSize: 10, batchName: undefined, batchStatus: undefined, responsibleUnitName: undefined, periodKey: undefined },
      failureQuery: { pageNum: 1, pageSize: 10, importBatchName: undefined },
      form: {},
      statusOptions: [
        { label: '草稿', value: 'draft' },
        { label: '待审核', value: 'pending_audit' },
        { label: '已审核', value: 'approved' },
        { label: '已驳回', value: 'rejected' }
      ],
      rules: {
        batchName: [{ required: true, message: '批次名称不能为空', trigger: 'blur' }],
        responsibleUnitName: [{ required: true, message: '责任单位不能为空', trigger: 'blur' }],
        periodKey: [{ required: true, message: '业务期间不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    formatFriendlyDateTime,
    getList() {
      this.loading = true
      listCollection(this.queryParams).then(response => {
        this.batchList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    reset() {
      this.form = {
        batchName: '',
        sourceType: 'form',
        responsibleUnitId: 200,
        responsibleUnitName: '省数据局',
        regionCode: '433100',
        regionName: '湘西州',
        periodType: 'month',
        periodKey: '2026-06',
        items: [this.emptyItem()]
      }
      this.resetForm('form')
    },
    emptyItem() {
      return { indicatorId: 1003, indicatorCode: 'NC-1003', indicatorName: '数字政务能力', rawValue: '72', valueDecimal: 72 }
    },
    sourceText(value) {
      const map = { form: '在线填报', excel: 'Excel导入', api: '接口拉取' }
      return map[value] || value
    },
    statusText(value) {
      const item = this.statusOptions.find(item => item.value === value)
      return item ? item.label : value
    },
    statusTag(value) {
      return value === 'approved' ? 'success' : value === 'rejected' ? 'danger' : value === 'pending_audit' ? 'warning' : 'info'
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSubmitBatch() {
      this.reset()
      this.submitOpen = true
    },
    handleImport() {
      this.$refs.importDialog.open()
    },
    handleImportFailures() {
      this.failureOpen = true
      this.getFailureList()
    },
    getFailureList() {
      this.failureLoading = true
      listImportFailures(this.failureQuery).then(response => {
        this.failureList = response.rows
        this.failureTotal = response.total
        this.failureLoading = false
      })
    },
    resetFailureQuery() {
      this.resetForm('failureQueryForm')
      this.failureQuery.pageNum = 1
      this.getFailureList()
    },
    addItem() {
      this.form.items.push(this.emptyItem())
    },
    removeItem(index) {
      if (this.form.items.length === 1) {
        this.$modal.msgWarning('至少保留一条明细')
        return
      }
      this.form.items.splice(index, 1)
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        submitCollection(this.form).then(() => {
          this.$modal.msgSuccess('已提交审核')
          this.submitOpen = false
          this.getList()
        })
      })
    },
    handleDetail(row) {
      getCollection(row.batchId).then(response => {
        this.detail = response.data || {}
        this.detailOpen = true
      })
    },
    handleApprove(row) {
      this.$prompt('请输入审核意见', '审核通过', { inputValue: '数据审核通过' }).then(({ value }) => {
        return approveCollection(row.batchId, value)
      }).then(() => {
        this.$modal.msgSuccess('审核通过，已触发预警评估')
        this.getList()
      })
    },
    handleReject(row) {
      this.$prompt('请输入驳回原因', '审核驳回', { inputValidator: value => !!value || '驳回原因不能为空' }).then(({ value }) => {
        return rejectCollection(row.batchId, value)
      }).then(() => {
        this.$modal.msgSuccess('已驳回')
        this.getList()
      })
    }
  }
}
</script>
