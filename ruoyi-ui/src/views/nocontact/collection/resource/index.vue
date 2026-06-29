<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
      <el-form-item label="资源名称">
        <el-input v-model="queryParams.resourceName" clearable placeholder="请输入资源名称" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="资源类型">
        <el-select v-model="queryParams.resourceType" clearable placeholder="请选择">
          <el-option label="数据集" value="dataset" />
          <el-option label="接口" value="api" />
          <el-option label="文件" value="file" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增资源</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="resourceList">
      <el-table-column label="资源名称" prop="resourceName" min-width="180" show-overflow-tooltip />
      <el-table-column label="类型" prop="resourceType" width="100" />
      <el-table-column label="来源系统" prop="sourceSystem" min-width="160" show-overflow-tooltip />
      <el-table-column label="更新周期" prop="updateCycle" width="110" />
      <el-table-column label="负责人" prop="ownerName" width="120" />
      <el-table-column label="数据量" prop="dataCount" width="100" align="right" />
      <el-table-column label="标签" prop="tags" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="150" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="资源名称" prop="resourceName"><el-input v-model="form.resourceName" /></el-form-item>
        <el-form-item label="资源类型"><el-select v-model="form.resourceType"><el-option label="数据集" value="dataset" /><el-option label="接口" value="api" /><el-option label="文件" value="file" /></el-select></el-form-item>
        <el-form-item label="来源系统"><el-input v-model="form.sourceSystem" /></el-form-item>
        <el-form-item label="更新周期"><el-input v-model="form.updateCycle" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.ownerName" /></el-form-item>
        <el-form-item label="数据量"><el-input-number v-model="form.dataCount" :min="0" controls-position="right" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" /></el-form-item>
        <el-form-item label="权限范围"><el-input v-model="form.permissionScope" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open=false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listResource, addResource, updateResource, delResource } from '@/api/nocontact/collection/resource'

export default {
  name: 'CollectionResource',
  data() {
    return {
      loading: false,
      open: false,
      title: '',
      total: 0,
      resourceList: [],
      queryParams: { pageNum: 1, pageSize: 10, resourceName: undefined, resourceType: undefined },
      form: {},
      rules: { resourceName: [{ required: true, message: '资源名称不能为空', trigger: 'blur' }] }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listResource(this.queryParams).then(response => {
        this.resourceList = response.rows || []
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
      this.form = { resourceType: 'dataset', status: '0', dataCount: 0 }
      this.title = '新增资源'
      this.open = true
    },
    handleUpdate(row) {
      this.form = Object.assign({}, row)
      this.title = '编辑资源'
      this.open = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.resourceId ? updateResource(this.form) : addResource(this.form)
        request.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除资源"' + row.resourceName + '"？').then(() => delResource(row.resourceId)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    }
  }
}
</script>
