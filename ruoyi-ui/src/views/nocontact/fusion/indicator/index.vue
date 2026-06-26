<template>
  <div class="app-container indicator-page">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="年份" prop="yearName">
        <el-input v-model="queryParams.yearName" placeholder="如 2026" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="一级指标" prop="firstLevel">
        <el-input v-model="queryParams.firstLevel" placeholder="请输入一级指标" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="具体指标" prop="indicatorName">
        <el-input v-model="queryParams.indicatorName" placeholder="请输入指标名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="责任单位" prop="responsibleUnit">
        <el-input v-model="queryParams.responsibleUnit" placeholder="请输入责任单位" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['fusion:indicator:add']">新增指标</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['fusion:indicator:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="indicatorList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="年份" prop="yearName" width="110" />
      <el-table-column label="一级指标" prop="firstLevel" min-width="150" show-overflow-tooltip />
      <el-table-column label="二级指标" prop="secondLevel" min-width="150" show-overflow-tooltip />
      <el-table-column label="指标目录" prop="indicatorName" min-width="220" show-overflow-tooltip />
      <el-table-column label="责任单位" prop="responsibleUnit" min-width="180" show-overflow-tooltip />
      <el-table-column label="数据来源" prop="dataSource" width="120" />
      <el-table-column label="状态" prop="status" width="80" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['fusion:indicator:edit']">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['fusion:indicator:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="年份"><el-input v-model="form.yearName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="一级指标"><el-input v-model="form.firstLevel" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="二级指标"><el-input v-model="form.secondLevel" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="具体指标" prop="indicatorName">
          <el-input v-model="form.indicatorName" maxlength="200" />
        </el-form-item>
        <el-form-item label="计分规则">
          <el-input v-model="form.scoringRule" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="责任单位"><el-input v-model="form.responsibleUnit" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数据来源"><el-input v-model="form.dataSource" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listIndicator, getIndicator, addIndicator, updateIndicator, delIndicator } from '@/api/nocontact/fusion/indicator'

export default {
  name: 'FusionIndicator',
  data() {
    return {
      loading: false,
      showSearch: true,
      ids: [],
      multiple: true,
      total: 0,
      indicatorList: [],
      open: false,
      title: '',
      queryParams: { pageNum: 1, pageSize: 10, yearName: undefined, firstLevel: undefined, indicatorName: undefined, responsibleUnit: undefined },
      form: {},
      rules: { indicatorName: [{ required: true, message: '指标名称不能为空', trigger: 'blur' }] }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listIndicator(this.queryParams).then(response => {
        this.indicatorList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    reset() {
      this.form = { indicatorId: undefined, yearName: '2026', firstLevel: '', secondLevel: '', indicatorName: '', scoringRule: '', responsibleUnit: '', dataSource: '手工维护', status: '0' }
      this.resetForm('form')
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
      this.ids = selection.map(item => item.indicatorId)
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增指标'
    },
    handleUpdate(row) {
      this.reset()
      getIndicator(row.indicatorId || this.ids[0]).then(response => {
        this.form = response.data
        this.open = true
        this.title = '编辑指标'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.indicatorId ? updateIndicator(this.form) : addIndicator(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.indicatorId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const indicatorIds = row.indicatorId || this.ids
      this.$modal.confirm('是否确认删除选中的指标？').then(function() {
        return delIndicator(indicatorIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      })
    }
  }
}
</script>
