<template>
  <div class="app-container">
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="对接名称"><el-input v-model="queryParams.integrationName" clearable placeholder="请输入对接名称" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="结果"><el-select v-model="queryParams.responseStatus" clearable><el-option label="成功" value="success" /><el-option label="失败" value="failed" /><el-option label="部分失败" value="partial_failed" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="同步批次" name="batch">
        <el-table v-loading="loading" :data="batchList">
          <el-table-column label="开始时间" prop="startedTime" width="160" />
          <el-table-column label="对接名称" prop="integrationName" min-width="160" />
          <el-table-column label="来源系统" prop="sourceSystem" min-width="140" />
          <el-table-column label="状态" prop="batchStatus" width="90">
            <template slot-scope="scope">{{ statusText(scope.row.batchStatus) }}</template>
          </el-table-column>
          <el-table-column label="新增" prop="successCount" width="80" align="right" />
          <el-table-column label="失败" prop="failureCount" width="80" align="right" />
          <el-table-column label="跳过" prop="skippedCount" width="80" align="right" />
          <el-table-column label="请求摘要" prop="requestSummary" min-width="220" show-overflow-tooltip />
          <el-table-column label="异常信息" prop="errorMessage" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="90" align="center">
            <template slot-scope="scope">
              <el-button v-if="canRetry(scope.row.batchStatus)" type="text" size="mini" icon="el-icon-refresh-right" @click="handleRetry(scope.row)">重试</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="运行日志" name="log">
        <el-table v-loading="loading" :data="logList">
          <el-table-column label="同步时间" prop="syncTime" width="160" />
          <el-table-column label="对接名称" prop="integrationName" min-width="160" />
          <el-table-column label="请求摘要" prop="requestSummary" min-width="220" show-overflow-tooltip />
          <el-table-column label="结果" prop="responseStatus" width="90">
            <template slot-scope="scope">{{ statusText(scope.row.responseStatus) }}</template>
          </el-table-column>
          <el-table-column label="成功数" prop="successCount" width="90" align="right" />
          <el-table-column label="失败数" prop="failureCount" width="90" align="right" />
          <el-table-column label="重试次数" prop="retryCount" width="90" align="right" />
          <el-table-column label="耗时(ms)" prop="durationMs" width="100" align="right" />
          <el-table-column label="异常信息" prop="errorMessage" min-width="160" show-overflow-tooltip />
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listSyncBatch, listSyncLog, retryBatch } from '@/api/nocontact/integration'

export default {
  name: 'IntegrationLog',
  data() {
    return {
      loading: false,
      activeTab: 'batch',
      total: 0,
      batchList: [],
      logList: [],
      queryParams: { pageNum: 1, pageSize: 10 }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      const params = Object.assign({}, this.queryParams)
      if (this.activeTab === 'batch') {
        params.batchStatus = params.responseStatus
        delete params.responseStatus
      }
      const request = this.activeTab === 'batch' ? listSyncBatch(params) : listSyncLog(params)
      request.then(response => {
        if (this.activeTab === 'batch') {
          this.batchList = response.rows || []
        } else {
          this.logList = response.rows || []
        }
        this.total = response.total || 0
        this.loading = false
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
    handleTabClick() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    handleRetry(row) {
      retryBatch(row.syncBatchId).then(() => {
        this.$modal.msgSuccess('重试已发起')
        this.getList()
      })
    },
    canRetry(status) {
      return status === 'failed' || status === 'partial_failed'
    },
    statusText(status) {
      if (status === 'success') return '成功'
      if (status === 'failed') return '失败'
      if (status === 'partial_failed') return '部分失败'
      return status || '-'
    }
  }
}
</script>
