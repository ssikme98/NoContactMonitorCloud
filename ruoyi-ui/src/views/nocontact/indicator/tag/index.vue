<template>
  <div class="app-container">
    <el-form :model="queryParams" size="small" :inline="true" label-width="80px">
      <el-form-item label="标签名称"><el-input v-model="queryParams.tagName" clearable placeholder="请输入标签名称" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="分类"><el-input v-model="queryParams.categoryName" clearable placeholder="请输入分类" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增标签</el-button></el-col>
    </el-row>

    <el-table v-loading="loading" :data="tagList">
      <el-table-column label="标签名称" prop="tagName" min-width="140" />
      <el-table-column label="颜色" prop="tagColor" width="100"><template slot-scope="scope"><el-tag :color="scope.row.tagColor || '#409EFF'" effect="dark">{{ scope.row.tagColor || '默认' }}</el-tag></template></el-table-column>
      <el-table-column label="分类" prop="categoryName" min-width="140" />
      <el-table-column label="关联指标数" prop="relatedCount" width="110" align="right" />
      <el-table-column label="排序" prop="orderNum" width="90" align="right" />
      <el-table-column label="描述" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="140" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标签名称" prop="tagName"><el-input v-model="form.tagName" /></el-form-item>
        <el-form-item label="颜色"><el-color-picker v-model="form.tagColor" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.categoryName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" controls-position="right" /></el-form-item>
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
import { listTags, addTag, updateTag, delTag } from '@/api/nocontact/indicator/workbench'

export default {
  name: 'IndicatorTag',
  data() {
    return {
      loading: false,
      open: false,
      title: '',
      total: 0,
      tagList: [],
      queryParams: { pageNum: 1, pageSize: 10 },
      form: {},
      rules: { tagName: [{ required: true, message: '标签名称不能为空', trigger: 'blur' }] }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listTags(this.queryParams).then(response => {
        this.tagList = response.rows || []
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
      this.form = { tagColor: '#409EFF', orderNum: 1, relatedCount: 0, status: '0' }
      this.title = '新增标签'
      this.open = true
    },
    handleUpdate(row) {
      this.form = Object.assign({}, row)
      this.title = '编辑标签'
      this.open = true
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.tagId ? updateTag(this.form) : addTag(this.form)
        request.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除标签"' + row.tagName + '"？').then(() => delTag(row.tagId)).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    }
  }
}
</script>
