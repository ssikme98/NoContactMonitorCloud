<template>
  <div class="app-container collection-schedule">
    <el-form
      v-show="showSearch"
      ref="queryForm"
      :model="queryParams"
      size="small"
      :inline="true"
      label-width="84px"
    >
      <el-form-item label="计划名称" prop="jobName">
        <el-input
          v-model="queryParams.jobName"
          placeholder="请输入采集计划名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="采集类型" prop="jobGroup">
        <el-select v-model="queryParams.jobGroup" placeholder="请选择采集类型" clearable>
          <el-option
            v-for="dict in dict.type.sys_job_group"
            :key="dict.value"
            :label="collectionTypeLabel(dict.label)"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="启用状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择启用状态" clearable>
          <el-option label="启用中" value="0" />
          <el-option label="已暂停" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['monitor:job:add']"
        >新增采集计划</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['monitor:job:edit']"
        >编辑计划</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['monitor:job:remove']"
        >删除计划</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['monitor:job:export']"
        >导出计划</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="计划名称" min-width="190" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <a class="link-type" style="cursor:pointer" @click="handleView(scope.row)">{{ scope.row.jobName }}</a>
          <div v-if="scope.row.remark" class="plan-subtitle">{{ scope.row.remark }}</div>
        </template>
      </el-table-column>
      <el-table-column label="采集类型" width="120" align="center">
        <template slot-scope="scope">
          <el-tag size="small" effect="plain">{{ jobGroupLabel(scope.row.jobGroup) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="采集动作" min-width="150" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ actionLabel(scope.row.invokeTarget) }}
        </template>
      </el-table-column>
      <el-table-column label="执行频率" min-width="150" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          {{ frequencyLabel(scope.row.cronExpression) }}
        </template>
      </el-table-column>
      <el-table-column label="下次采集" width="170" align="center">
        <template slot-scope="scope">
          {{ nextCollectTime(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="启用状态" width="110" align="center">
        <template slot-scope="scope">
          <div class="plan-status-switch">
            <el-switch
              v-model="scope.row.status"
              active-value="0"
              inactive-value="1"
              @change="handleStatusChange(scope.row)"
            />
            <span class="plan-status-switch__text">{{ statusLabel(scope.row.status) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-video-play"
            @click="handleRun(scope.row)"
            v-hasPermi="['monitor:job:changeStatus']"
          >立即采集</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['monitor:job:edit']"
          >编辑</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-document"
            @click="handleLogs(scope.row)"
            v-hasPermi="['monitor:job:query']"
          >执行记录</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['monitor:job:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="计划名称" prop="jobName">
              <el-input v-model="form.jobName" placeholder="请输入采集计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采集类型" prop="jobGroup">
              <el-select v-model="form.jobGroup" placeholder="请选择采集类型">
                <el-option
                  v-for="dict in dict.type.sys_job_group"
                  :key="dict.value"
                  :label="collectionTypeLabel(dict.label)"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采集动作" prop="actionType">
              <el-select v-model="form.actionType" placeholder="请选择采集动作" @change="handleActionChange">
                <el-option
                  v-for="item in actionOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="执行频率" prop="schedulePreset">
              <el-select v-model="form.schedulePreset" placeholder="请选择执行频率" @change="handleScheduleChange">
                <el-option
                  v-for="item in scheduleOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="计划说明" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="2"
                placeholder="请输入这条采集计划的业务说明"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="启用状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">启用中</el-radio>
                <el-radio label="1">已暂停</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-collapse class="advanced-config">
          <el-collapse-item title="高级配置" name="advanced">
            <el-row>
              <el-col :span="24">
                <el-form-item label="后台采集动作" prop="invokeTarget">
                  <el-input v-model="form.invokeTarget" placeholder="请输入后台采集动作" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="高级频率配置" prop="cronExpression">
                  <el-input v-model="form.cronExpression" placeholder="请输入高级频率配置" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="补采策略" prop="misfirePolicy">
                  <el-radio-group v-model="form.misfirePolicy" size="small">
                    <el-radio-button label="1">立即补采</el-radio-button>
                    <el-radio-button label="2">只补一次</el-radio-button>
                    <el-radio-button label="3">跳过本次</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="并行采集" prop="concurrent">
                  <el-radio-group v-model="form.concurrent" size="small">
                    <el-radio-button label="0">允许</el-radio-button>
                    <el-radio-button label="1">禁止</el-radio-button>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="采集计划详情" :visible.sync="detailOpen" width="760px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划名称">{{ detail.jobName }}</el-descriptions-item>
        <el-descriptions-item label="采集类型">{{ jobGroupLabel(detail.jobGroup) }}</el-descriptions-item>
        <el-descriptions-item label="采集动作">{{ actionLabel(detail.invokeTarget) }}</el-descriptions-item>
        <el-descriptions-item label="执行频率">{{ frequencyLabel(detail.cronExpression) }}</el-descriptions-item>
        <el-descriptions-item label="下次采集">{{ nextCollectTime(detail) }}</el-descriptions-item>
        <el-descriptions-item label="启用状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="计划说明" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-collapse class="advanced-config">
        <el-collapse-item title="高级信息" name="detailAdvanced">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="计划编号">{{ detail.jobId }}</el-descriptions-item>
            <el-descriptions-item label="后台采集动作">{{ detail.invokeTarget || '-' }}</el-descriptions-item>
            <el-descriptions-item label="高级频率配置">{{ detail.cronExpression || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatTime(detail.updateTime) }}</el-descriptions-item>
          </el-descriptions>
        </el-collapse-item>
      </el-collapse>
    </el-dialog>

    <el-dialog :title="logTitle" :visible.sync="logOpen" width="900px" append-to-body>
      <el-form ref="logQueryForm" :model="logQueryParams" size="small" :inline="true" label-width="84px">
        <el-form-item label="执行结果" prop="status">
          <el-select v-model="logQueryParams.status" placeholder="全部结果" clearable>
            <el-option label="成功" value="0" />
            <el-option label="失败" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleLogQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetLogQuery">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="logLoading" :data="jobLogList">
        <el-table-column label="执行时间" prop="createTime" width="170" align="center">
          <template slot-scope="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="计划名称" prop="jobName" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column label="执行结果" prop="status" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === '0' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100" align="center">
          <template slot-scope="scope">
            {{ durationLabel(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="执行说明" prop="jobMessage" min-width="220" :show-overflow-tooltip="true" />
        <el-table-column label="异常信息" prop="exceptionInfo" min-width="160" :show-overflow-tooltip="true" />
      </el-table>
      <pagination
        v-show="logTotal>0"
        :total="logTotal"
        :page.sync="logQueryParams.pageNum"
        :limit.sync="logQueryParams.pageSize"
        @pagination="getLogList"
      />
    </el-dialog>
  </div>
</template>

<script>
import { listJob, getJob, delJob, addJob, updateJob, runJob, changeJobStatus } from '@/api/monitor/job'
import { listJobLog } from '@/api/monitor/jobLog'
import { parseTime } from '@/utils/ruoyi'

const DEFAULT_ACTION = 'standard'
const DEFAULT_SCHEDULE = 'every30Minutes'

export default {
  name: 'CollectionSchedule',
  dicts: ['sys_job_group'],
  data() {
    return {
      loading: true,
      logLoading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      logTotal: 0,
      jobList: [],
      jobLogList: [],
      title: '',
      logTitle: '执行记录',
      open: false,
      detailOpen: false,
      logOpen: false,
      detail: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: undefined,
        jobGroup: undefined,
        status: undefined
      },
      logQueryBase: {
        jobName: undefined,
        jobGroup: undefined
      },
      logQueryParams: {
        pageNum: 1,
        pageSize: 10,
        jobName: undefined,
        jobGroup: undefined,
        status: undefined
      },
      form: {},
      scheduleOptions: [
        { value: 'every10Minutes', label: '每 10 分钟', cron: '0 */10 * * * ?' },
        { value: 'every30Minutes', label: '每 30 分钟', cron: '0 */30 * * * ?' },
        { value: 'hourly', label: '每小时', cron: '0 0 * * * ?' },
        { value: 'daily1am', label: '每天 01:00', cron: '0 0 1 * * ?' },
        { value: 'custom', label: '自定义频率' }
      ],
      actionOptions: [
        { value: 'standard', label: '标准采集', target: 'ryTask.ryNoParams' },
        { value: 'parameter', label: '按指定条件采集', target: "ryTask.ryParams('ry')" },
        { value: 'batch', label: '批量采集', target: "ryTask.ryMultipleParams('ry', true, 2000L, 316.50D, 100)" },
        { value: 'warning', label: '预警评估采集', target: 'nocontactWarningTask.evaluateDueRules' },
        { value: 'custom', label: '自定义动作' }
      ],
      rules: {
        jobName: [
          { required: true, message: '计划名称不能为空', trigger: 'blur' }
        ],
        jobGroup: [
          { required: true, message: '采集类型不能为空', trigger: 'change' }
        ],
        schedulePreset: [
          { required: true, message: '执行频率不能为空', trigger: 'change' }
        ],
        actionType: [
          { required: true, message: '采集动作不能为空', trigger: 'change' }
        ],
        invokeTarget: [
          { required: true, message: '后台采集动作不能为空', trigger: 'blur' }
        ],
        cronExpression: [
          { required: true, message: '高级频率配置不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listJob(this.queryParams).then(response => {
        this.jobList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      })
    },
    collectionTypeLabel(label) {
      if (label === '默认') return '常规采集'
      if (label === '系统') return '系统内置'
      return label
    },
    jobGroupLabel(value) {
      const label = this.selectDictLabel(this.dict.type.sys_job_group, value)
      return this.collectionTypeLabel(label)
    },
    statusLabel(status) {
      return status === '0' ? '启用中' : '已暂停'
    },
    formatTime(time) {
      return parseTime(time) || '-'
    },
    nextCollectTime(row) {
      if (!row || row.status === '1') {
        return '暂停后不执行'
      }
      return this.formatTime(row.nextValidTime)
    },
    frequencyLabel(cronExpression) {
      const matched = this.scheduleOptions.find(item => item.cron === cronExpression)
      if (matched) return matched.label
      if (/^0\/(\d+) \* \* \* \* \?$/.test(cronExpression || '')) {
        return '每 ' + cronExpression.match(/^0\/(\d+)/)[1] + ' 秒'
      }
      if (/^0 \*\/(\d+) \* \* \* \?$/.test(cronExpression || '')) {
        return '每 ' + cronExpression.match(/^0 \*\/(\d+)/)[1] + ' 分钟'
      }
      return cronExpression ? '自定义频率' : '-'
    },
    actionLabel(invokeTarget) {
      const matched = this.actionOptions.find(item => item.target === invokeTarget)
      if (matched) return matched.label
      if (!invokeTarget) return '-'
      return '自定义动作'
    },
    schedulePresetFromCron(cronExpression) {
      const matched = this.scheduleOptions.find(item => item.cron === cronExpression)
      return matched ? matched.value : 'custom'
    },
    actionTypeFromTarget(invokeTarget) {
      const matched = this.actionOptions.find(item => item.target === invokeTarget)
      return matched ? matched.value : 'custom'
    },
    reset() {
      const action = this.actionOptions.find(item => item.value === DEFAULT_ACTION)
      const schedule = this.scheduleOptions.find(item => item.value === DEFAULT_SCHEDULE)
      this.form = {
        jobId: undefined,
        jobName: undefined,
        jobGroup: 'DEFAULT',
        actionType: action.value,
        schedulePreset: schedule.value,
        invokeTarget: action.target,
        cronExpression: schedule.cron,
        misfirePolicy: '3',
        concurrent: '1',
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    decorateForm(data) {
      const form = Object.assign({}, data)
      form.actionType = this.actionTypeFromTarget(form.invokeTarget)
      form.schedulePreset = this.schedulePresetFromCron(form.cronExpression)
      return form
    },
    buildSubmitPayload() {
      const payload = {}
      ;['jobId', 'jobName', 'jobGroup', 'invokeTarget', 'cronExpression', 'misfirePolicy', 'concurrent', 'status', 'remark'].forEach(key => {
        payload[key] = this.form[key]
      })
      return payload
    },
    handleScheduleChange(value) {
      const matched = this.scheduleOptions.find(item => item.value === value)
      if (matched && matched.cron) {
        this.form.cronExpression = matched.cron
      }
    },
    handleActionChange(value) {
      const matched = this.actionOptions.find(item => item.value === value)
      if (matched && matched.target) {
        this.form.invokeTarget = matched.target
      }
    },
    cancel() {
      this.open = false
      this.reset()
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
      this.ids = selection.map(item => item.jobId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleStatusChange(row) {
      const text = row.status === '0' ? '启用' : '暂停'
      this.$modal.confirm('确认' + text + '采集计划"' + row.jobName + '"？').then(function() {
        return changeJobStatus(row.jobId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + '成功')
      }).catch(function() {
        row.status = row.status === '0' ? '1' : '0'
      })
    },
    handleRun(row) {
      this.$modal.confirm('确认立即启动一次"' + row.jobName + '"？').then(function() {
        return runJob(row.jobId, row.jobGroup)
      }).then(() => {
        this.$modal.msgSuccess('采集已启动')
        this.handleLogs(row)
      }).catch(() => {})
    },
    handleView(row) {
      getJob(row.jobId).then(response => {
        this.detail = this.decorateForm(response.data || {})
        this.detailOpen = true
      })
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增采集计划'
    },
    handleUpdate(row) {
      this.reset()
      const jobId = row.jobId || this.ids
      getJob(jobId).then(response => {
        this.form = this.decorateForm(response.data || {})
        this.open = true
        this.title = '编辑采集计划'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const payload = this.buildSubmitPayload()
        const request = payload.jobId ? updateJob(payload) : addJob(payload)
        request.then(() => {
          this.$modal.msgSuccess(payload.jobId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const jobIds = row.jobId || this.ids
      this.$modal.confirm('确认删除采集计划编号为"' + jobIds + '"的数据项？').then(function() {
        return delJob(jobIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('schedule/job/export', {
        ...this.queryParams
      }, `collection_plan_${new Date().getTime()}.xlsx`)
    },
    handleLogs(row) {
      this.logTitle = '"' + row.jobName + '"执行记录'
      this.logQueryBase = {
        jobName: row.jobName,
        jobGroup: row.jobGroup
      }
      this.logQueryParams = {
        pageNum: 1,
        pageSize: 10,
        jobName: row.jobName,
        jobGroup: row.jobGroup,
        status: undefined
      }
      this.logOpen = true
      this.getLogList()
    },
    getLogList() {
      this.logLoading = true
      listJobLog(this.logQueryParams).then(response => {
        this.jobLogList = response.rows || []
        this.logTotal = response.total || 0
        this.logLoading = false
      })
    },
    handleLogQuery() {
      this.logQueryParams.pageNum = 1
      this.getLogList()
    },
    resetLogQuery() {
      this.logQueryParams = Object.assign({
        pageNum: 1,
        pageSize: 10,
        status: undefined
      }, this.logQueryBase)
      this.getLogList()
    },
    durationLabel(row) {
      if (!row.startTime || !row.endTime) return '-'
      const start = new Date(row.startTime).getTime()
      const end = new Date(row.endTime).getTime()
      if (Number.isNaN(start) || Number.isNaN(end) || end < start) return '-'
      return (end - start) + ' 毫秒'
    }
  }
}
</script>

<style scoped>
.collection-schedule .el-select,
.collection-schedule .el-input {
  width: 240px;
}

.plan-subtitle {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  line-height: 18px;
}

.advanced-config {
  margin-top: 8px;
}

.plan-status-switch {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  white-space: nowrap;
}

.plan-status-switch__text {
  color: #606266;
  font-size: 12px;
  line-height: 20px;
}
</style>
