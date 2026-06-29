<template>
  <div class="app-container">
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="对接名称"><el-input v-model="queryParams.integrationName" clearable placeholder="请输入对接名称" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="queryParams.status" clearable><el-option label="启用" value="0" /><el-option label="停用" value="1" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增对接</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="configList">
      <el-table-column label="对接名称" prop="integrationName" min-width="180" show-overflow-tooltip />
      <el-table-column label="平台" prop="platformName" min-width="150" />
      <el-table-column label="类型" prop="integrationType" width="90" />
      <el-table-column label="同步频率" prop="syncFrequency" width="110" />
      <el-table-column label="状态" prop="status" width="80"><template slot-scope="scope">{{ scope.row.status === '0' ? '启用' : '停用' }}</template></el-table-column>
      <el-table-column label="最近同步" prop="lastSyncTime" width="160" />
      <el-table-column label="同步结果" prop="lastSyncStatus" width="100" />
      <el-table-column label="操作" width="260" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="text" size="mini" icon="el-icon-connection" @click="handleTest(scope.row)">连接测试</el-button>
          <el-button type="text" size="mini" icon="el-icon-refresh-right" @click="handleSync(scope.row)">同步</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="对接名称" prop="integrationName"><el-input v-model="form.integrationName" /></el-form-item>
        <el-form-item label="对接平台"><el-input v-model="form.platformName" /></el-form-item>
        <el-form-item label="对接类型"><el-select v-model="form.integrationType"><el-option label="API" value="api" /><el-option label="数据库" value="database" /><el-option label="文件交换" value="file" /></el-select></el-form-item>
        <el-form-item label="API地址"><el-input v-model="form.endpointUrl" /></el-form-item>
        <el-form-item label="认证方式"><el-select v-model="form.authType"><el-option label="Token" value="token" /><el-option label="OAuth2" value="oauth2" /><el-option label="证书" value="cert" /><el-option label="用户名密码" value="password" /></el-select></el-form-item>
        <el-form-item label="同步频率"><el-select v-model="form.syncFrequency"><el-option label="实时" value="realtime" /><el-option label="每小时" value="hourly" /><el-option label="每日" value="daily" /><el-option label="每周" value="weekly" /></el-select></el-form-item>
        <el-form-item label="同步方式"><el-select v-model="form.syncMode"><el-option label="全量同步" value="full" /><el-option label="增量同步" value="incremental" /></el-select></el-form-item>
        <el-form-item label="字段映射"><el-input v-model="form.mappingRule" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="转换规则"><el-input v-model="form.transformRule" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="异常策略"><el-input v-model="form.retryPolicy" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open=false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listConfig, addConfig, updateConfig, delConfig, testConfig, syncConfig } from '@/api/nocontact/integration'

export default {
  name: 'IntegrationConfig',
  data() {
    return {
      loading: false,
      open: false,
      title: '',
      total: 0,
      configList: [],
      queryParams: { pageNum: 1, pageSize: 10 },
      form: {},
      rules: { integrationName: [{ required: true, message: '对接名称不能为空', trigger: 'blur' }] }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listConfig(this.queryParams).then(response => {
        this.configList = response.rows || []
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
    handleAdd() {
      this.form = { integrationType: 'api', authType: 'token', syncFrequency: 'daily', syncMode: 'incremental', status: '0' }
      this.title = '新增对接配置'
      this.open = true
    },
    handleUpdate(row) {
      this.form = Object.assign({}, row)
      this.title = '编辑对接配置'
      this.open = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.configId ? updateConfig(this.form) : addConfig(this.form)
        request.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleTest(row) {
      testConfig(row.configId).then(() => this.$modal.msgSuccess('连接测试通过'))
    },
    handleSync(row) {
      syncConfig(row.configId).then(() => {
        this.$modal.msgSuccess('同步完成')
        this.getList()
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除对接"' + row.integrationName + '"？').then(() => delConfig(row.configId)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    }
  }
}
</script>
