<template>
  <div class="app-container fusion-task-page">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="任务名称" prop="taskName">
        <el-input v-model="queryParams.taskName" placeholder="请输入任务名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="任务类型" prop="taskType">
        <el-select v-model="queryParams.taskType" placeholder="全部" clearable>
          <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="taskStatus">
        <el-select v-model="queryParams.taskStatus" placeholder="全部" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nocontact:fusion:task:add']">新增采集任务</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nocontact:fusion:task:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务名称" prop="taskName" min-width="180" show-overflow-tooltip />
      <el-table-column label="任务类型" prop="taskType" width="120">
        <template slot-scope="scope">{{ optionText(taskTypeOptions, scope.row.taskType) }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="taskStatus" width="100">
        <template slot-scope="scope">
          <el-tag size="mini" :type="statusTag(scope.row.taskStatus)">{{ optionText(statusOptions, scope.row.taskStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="监测项" prop="indicatorName" min-width="180" show-overflow-tooltip />
      <el-table-column label="数据源" prop="sourceType" width="110">
        <template slot-scope="scope">{{ optionText(sourceTypeOptions, scope.row.sourceType) }}</template>
      </el-table-column>
      <el-table-column label="填报周期" prop="fillCycle" width="100">
        <template slot-scope="scope">{{ optionText(cycleOptions, scope.row.fillCycle) }}</template>
      </el-table-column>
      <el-table-column label="审核" prop="auditEnabled" width="80" align="center">
        <template slot-scope="scope">{{ scope.row.auditEnabled === '1' ? '启用' : '关闭' }}</template>
      </el-table-column>
      <el-table-column label="数据量" prop="dataCount" width="100" align="right" />
      <el-table-column label="最后执行时间" width="180">
        <template slot-scope="scope">{{ formatFriendlyDateTime(scope.row.lastRunTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nocontact:fusion:task:edit']">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-video-play" @click="changeStatus(scope.row, 'running')" v-hasPermi="['nocontact:fusion:task:edit']">执行</el-button>
          <el-button size="mini" type="text" icon="el-icon-check" @click="changeStatus(scope.row, 'done')" v-hasPermi="['nocontact:fusion:task:edit']">完成</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nocontact:fusion:task:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" maxlength="120" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务类型" prop="taskType">
              <el-select v-model="form.taskType" placeholder="请选择">
                <el-option v-for="item in taskTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="目标层级" prop="targetLevel">
              <el-select v-model="form.targetLevel">
                <el-option label="省级" value="province" />
                <el-option label="市州" value="city" />
                <el-option label="区县" value="county" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="监测项" prop="indicatorId">
              <el-select v-model="form.indicatorId" filterable clearable placeholder="请选择指标" @change="syncIndicatorName">
                <el-option v-for="item in indicatorOptions" :key="item.indicatorId" :label="item.indicatorName" :value="item.indicatorId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="数据靶点">
          <el-input v-model="form.dataTargets" placeholder="例如：14个市州、省直责任单位" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="数据源类型" prop="sourceType">
              <el-select v-model="form.sourceType">
                <el-option v-for="item in sourceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="填报周期">
              <el-select v-model="form.fillCycle">
                <el-option v-for="item in cycleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="执行方式">
              <el-select v-model="form.scheduleMode">
                <el-option label="手动" value="manual" />
                <el-option label="定时" value="timed" />
                <el-option label="触发式" value="trigger" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Cron表达式">
              <el-input v-model="form.cronExpression" placeholder="定时任务可填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="双层审核">
              <el-switch v-model="form.auditEnabled" active-value="1" inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="form.ownerName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { listTask, getTask, addTask, updateTask, updateTaskStatus, delTask } from '@/api/nocontact/fusion/task'
import { listIndicatorOptions } from '@/api/nocontact/fusion/indicator'
import { formatFriendlyDateTime } from '@/utils/nocontactDisplay'

export default {
  name: 'FusionTask',
  data() {
    return {
      loading: false,
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      taskList: [],
      indicatorOptions: [],
      open: false,
      title: '',
      queryParams: { pageNum: 1, pageSize: 10, taskName: undefined, taskType: undefined, taskStatus: undefined },
      form: {},
      taskTypeOptions: [
        { label: '填报任务', value: 'fill' },
        { label: '自动采集任务', value: 'auto' },
        { label: '接口对接任务', value: 'api' }
      ],
      statusOptions: [
        { label: '草稿', value: 'draft' },
        { label: '已发布', value: 'published' },
        { label: '执行中', value: 'running' },
        { label: '已完成', value: 'done' },
        { label: '异常', value: 'error' }
      ],
      sourceTypeOptions: [
        { label: '数据库', value: 'database' },
        { label: 'Excel模板', value: 'excel' },
        { label: 'API接口', value: 'api' },
        { label: '在线填报', value: 'form' }
      ],
      cycleOptions: [
        { label: '每月', value: 'month' },
        { label: '每季度', value: 'quarter' },
        { label: '每年', value: 'year' }
      ],
      rules: {
        taskName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
        taskType: [{ required: true, message: '任务类型不能为空', trigger: 'change' }],
        sourceType: [{ required: true, message: '数据源类型不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
    this.getIndicators()
  },
  methods: {
    formatFriendlyDateTime,
    getList() {
      this.loading = true
      listTask(this.queryParams).then(response => {
        this.taskList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getIndicators() {
      listIndicatorOptions({ status: '0' }).then(response => {
        this.indicatorOptions = response.data || []
      })
    },
    optionText(options, value) {
      const item = options.find(item => item.value === value)
      return item ? item.label : value
    },
    statusTag(status) {
      return status === 'done' ? 'success' : status === 'running' ? 'warning' : status === 'error' ? 'danger' : 'info'
    },
    syncIndicatorName(value) {
      const item = this.indicatorOptions.find(item => item.indicatorId === value)
      this.form.indicatorName = item ? item.indicatorName : ''
    },
    reset() {
      this.form = {
        taskId: undefined,
        taskName: undefined,
        taskType: 'fill',
        taskStatus: 'draft',
        targetLevel: 'province',
        indicatorId: undefined,
        indicatorName: '',
        dataTargets: '',
        sourceType: 'form',
        fillCycle: 'quarter',
        scheduleMode: 'manual',
        cronExpression: '',
        auditEnabled: '1',
        ownerName: '',
        remark: ''
      }
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
      this.ids = selection.map(item => item.taskId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增采集任务'
    },
    handleUpdate(row) {
      this.reset()
      getTask(row.taskId || this.ids[0]).then(response => {
        this.form = response.data
        this.open = true
        this.title = '编辑采集任务'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.taskId ? updateTask(this.form) : addTask(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.taskId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    changeStatus(row, status) {
      updateTaskStatus(row.taskId, status).then(() => {
        this.$modal.msgSuccess('状态已更新')
        this.getList()
      })
    },
    handleDelete(row) {
      const taskIds = row.taskId || this.ids
      this.$modal.confirm('是否确认删除选中的采集任务？').then(function() {
        return delTask(taskIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      })
    }
  }
}
</script>
