<template>
  <div class="app-container">
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="对接名称"><el-input v-model="queryParams.integrationName" clearable placeholder="请输入对接名称" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="结果"><el-select v-model="queryParams.responseStatus" clearable><el-option label="成功" value="success" /><el-option label="失败" value="failed" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="logList">
      <el-table-column label="同步时间" prop="syncTime" width="160" />
      <el-table-column label="对接名称" prop="integrationName" min-width="160" />
      <el-table-column label="请求摘要" prop="requestSummary" min-width="220" show-overflow-tooltip />
      <el-table-column label="结果" prop="responseStatus" width="90" />
      <el-table-column label="成功数" prop="successCount" width="90" align="right" />
      <el-table-column label="失败数" prop="failureCount" width="90" align="right" />
      <el-table-column label="耗时(ms)" prop="durationMs" width="100" align="right" />
      <el-table-column label="异常信息" prop="errorMessage" min-width="160" show-overflow-tooltip />
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listSyncLog } from '@/api/nocontact/integration'

export default {
  name: 'IntegrationLog',
  data() {
    return {
      loading: false,
      total: 0,
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
      listSyncLog(this.queryParams).then(response => {
        this.logList = response.rows || []
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
    }
  }
}
</script>
