<template>
  <div class="app-container">
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="模板名称"><el-input v-model="queryParams.templateName" clearable placeholder="请输入模板名称" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="报告类型"><el-select v-model="queryParams.reportType" clearable><el-option label="总报告" value="overall" /><el-option label="分地区报告" value="region" /><el-option label="分指标报告" value="indicator" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增模板</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="templateList">
      <el-table-column label="模板名称" prop="templateName" min-width="180" />
      <el-table-column label="报告类型" prop="reportType" width="120" />
      <el-table-column label="章节配置" prop="sections" min-width="240" show-overflow-tooltip />
      <el-table-column label="版本" prop="versionNo" width="80" align="right" />
      <el-table-column label="状态" prop="status" width="80"><template slot-scope="scope">{{ scope.row.status === '0' ? '启用' : '停用' }}</template></el-table-column>
      <el-table-column label="操作" width="140" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="740px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模板名称" prop="templateName"><el-input v-model="form.templateName" /></el-form-item>
        <el-form-item label="报告类型"><el-select v-model="form.reportType"><el-option label="总报告" value="overall" /><el-option label="分地区报告" value="region" /><el-option label="分指标报告" value="indicator" /></el-select></el-form-item>
        <el-form-item label="章节"><el-input v-model="form.sections" type="textarea" :rows="3" placeholder="概述,总体评价,指标分析,问题与建议" /></el-form-item>
        <el-form-item label="数据范围"><el-input v-model="form.dataScope" /></el-form-item>
        <el-form-item label="样式配置"><el-input v-model="form.styleConfig" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open=false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTemplate, addTemplate, updateTemplate, delTemplate } from '@/api/nocontact/report'

export default {
  name: 'ReportTemplate',
  data() {
    return {
      loading: false,
      open: false,
      title: '',
      total: 0,
      templateList: [],
      queryParams: { pageNum: 1, pageSize: 10 },
      form: {},
      rules: { templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }] }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listTemplate(this.queryParams).then(response => {
        this.templateList = response.rows || []
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
      this.form = { reportType: 'overall', sections: '概述,总体评价,指标分析,问题与建议', versionNo: 1, status: '0' }
      this.title = '新增模板'
      this.open = true
    },
    handleUpdate(row) {
      this.form = Object.assign({}, row)
      this.title = '编辑模板'
      this.open = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.templateId ? updateTemplate(this.form) : addTemplate(this.form)
        request.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除模板"' + row.templateName + '"？').then(() => delTemplate(row.templateId)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    }
  }
}
</script>
