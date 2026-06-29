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
      <el-table-column label="范围" prop="reportScope" min-width="140" />
      <el-table-column label="生成方式" prop="generateMode" width="100" />
      <el-table-column label="状态" prop="taskStatus" width="100" />
      <el-table-column label="文件" prop="generatedFileName" min-width="160" />
      <el-table-column label="生成时间" prop="generatedTime" width="160" />
      <el-table-column label="操作" width="120" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-document-checked" @click="handleGenerate(scope.row)">立即生成</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="新建生成任务" :visible.sync="open" width="620px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="任务名称" prop="taskName"><el-input v-model="form.taskName" /></el-form-item>
        <el-form-item label="模板名称"><el-input v-model="form.templateName" placeholder="可先填写模板名称" /></el-form-item>
        <el-form-item label="数据周期"><el-input v-model="form.reportPeriod" placeholder="2026Q2" /></el-form-item>
        <el-form-item label="报告范围"><el-input v-model="form.reportScope" placeholder="全省/指定市州/指定指标" /></el-form-item>
        <el-form-item label="生成方式"><el-radio-group v-model="form.generateMode"><el-radio label="manual">立即生成</el-radio><el-radio label="scheduled">定时生成</el-radio></el-radio-group></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open=false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listReportTask, addReportTask, generateReportTask } from '@/api/nocontact/report'

export default {
  name: 'ReportTask',
  data() {
    return {
      loading: false,
      open: false,
      total: 0,
      taskList: [],
      queryParams: { pageNum: 1, pageSize: 10 },
      form: {},
      rules: { taskName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }] }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listReportTask(this.queryParams).then(response => {
        this.taskList = response.rows || []
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
      this.form = { generateMode: 'manual', taskStatus: 'pending' }
      this.open = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        addReportTask(this.form).then(() => {
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
    }
  }
}
</script>
