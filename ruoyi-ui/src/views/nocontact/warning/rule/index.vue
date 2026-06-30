<template>
  <div class="app-container warning-rule-page">
    <div class="entry-title">{{ routeTitle }}</div>
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="queryParams.ruleName" placeholder="请输入规则名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="规则状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警级别" prop="warningLevel">
        <el-select v-model="queryParams.warningLevel" placeholder="全部" clearable>
          <el-option v-for="item in levelOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['nocontact:warning:rule:add']">{{ createButtonText }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['nocontact:warning:rule:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="ruleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="规则名称" prop="ruleName" min-width="180" show-overflow-tooltip />
      <el-table-column label="关联指标" prop="indicatorName" min-width="180" show-overflow-tooltip />
      <el-table-column label="责任单位" prop="responsibleUnitName" min-width="130" show-overflow-tooltip />
      <el-table-column label="地区" prop="regionName" min-width="100" show-overflow-tooltip />
      <el-table-column label="级别" prop="warningLevel" width="90">
        <template slot-scope="scope"><el-tag size="mini" :type="levelTag(scope.row.warningLevel)">{{ levelText(scope.row.warningLevel) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="阈值类型" prop="thresholdType" width="130">
        <template slot-scope="scope">{{ optionText(thresholdOptions, scope.row.thresholdType) }}</template>
      </el-table-column>
      <el-table-column label="阈值" width="130" align="right">
        <template slot-scope="scope">{{ thresholdText(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="触发条件" prop="triggerCondition" width="110">
        <template slot-scope="scope">{{ optionText(conditionOptions, scope.row.triggerCondition) }}</template>
      </el-table-column>
      <el-table-column label="推送渠道" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">{{ channelText(scope.row.pushChannels) }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template slot-scope="scope"><el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="changeStatus(scope.row)" v-hasPermi="['nocontact:warning:rule:edit']" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['nocontact:warning:rule:edit']">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['nocontact:warning:rule:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="820px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="116px">
        <el-form-item label="规则名称" prop="ruleName"><el-input v-model="form.ruleName" maxlength="120" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="关联指标" prop="indicatorId">
              <el-select v-model="form.indicatorId" filterable clearable placeholder="请选择指标" @change="syncIndicatorName">
                <el-option v-for="item in indicatorOptions" :key="item.indicatorId" :label="item.indicatorName" :value="item.indicatorId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警级别" prop="warningLevel">
              <el-radio-group v-model="form.warningLevel">
                <el-radio-button v-for="item in levelOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="6"><el-form-item label="责任单位ID"><el-input-number v-model="form.responsibleUnitId" :min="1" :precision="0" controls-position="right" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="责任单位"><el-input v-model="form.responsibleUnitName" placeholder="可选" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="地区编码"><el-input v-model="form.regionCode" placeholder="可选" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="地区名称"><el-input v-model="form.regionName" placeholder="可选" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="form.triggerCondition === 'outside_range' ? 6 : 8"><el-form-item label="阈值类型"><el-select v-model="form.thresholdType"><el-option v-for="item in thresholdOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="form.triggerCondition === 'outside_range' ? 6 : 8"><el-form-item label="阈值下限" prop="thresholdValue"><el-input-number v-model="form.thresholdValue" :precision="2" controls-position="right" /></el-form-item></el-col>
          <el-col v-if="form.triggerCondition === 'outside_range'" :span="6"><el-form-item label="阈值上限" prop="thresholdValueMax"><el-input-number v-model="form.thresholdValueMax" :precision="2" controls-position="right" /></el-form-item></el-col>
          <el-col :span="form.triggerCondition === 'outside_range' ? 6 : 8"><el-form-item label="触发条件"><el-select v-model="form.triggerCondition"><el-option v-for="item in conditionOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="周期类型"><el-select v-model="form.periodType" clearable placeholder="不限"><el-option label="月度" value="month" /><el-option label="季度" value="quarter" /><el-option label="年度" value="year" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="触发频率"><el-select v-model="form.triggerFrequency"><el-option label="实时" value="realtime" /><el-option label="每日" value="daily" /><el-option label="每周" value="weekly" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="推送渠道"><el-checkbox-group v-model="pushChannelArray"><el-checkbox label="site">站内消息</el-checkbox><el-checkbox label="sms">短信</el-checkbox><el-checkbox label="email">邮件</el-checkbox></el-checkbox-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="推送对象"><el-input v-model="form.pushTargets" placeholder="监测项负责人、管理人员或指定用户" /></el-form-item>
        <el-form-item label="推送模板"><el-input v-model="form.contentTemplate" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio v-for="item in statusOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio></el-radio-group></el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRule, getRule, addRule, updateRule, updateRuleStatus, delRule } from '@/api/nocontact/warning/rule'
import { listIndicatorOptions } from '@/api/nocontact/fusion/indicator'
import { BINARY_STATUS_OPTIONS, channelText, WARNING_LEVEL_COLOR_OPTIONS, warningLevelColorText } from '@/utils/nocontactDisplay'

export default {
  name: 'WarningRule',
  props: {
    entryMode: {
      type: String,
      default: 'list'
    }
  },
  data() {
    return {
      loading: false,
      entryBootstrapped: false,
      showSearch: true,
      ids: [],
      multiple: true,
      total: 0,
      ruleList: [],
      indicatorOptions: [],
      open: false,
      title: '',
      pushChannelArray: [],
      queryParams: { pageNum: 1, pageSize: 10, ruleName: undefined, status: undefined, warningLevel: undefined },
      form: {},
      levelOptions: WARNING_LEVEL_COLOR_OPTIONS,
      statusOptions: BINARY_STATUS_OPTIONS,
      thresholdOptions: [{ label: '目标值对比', value: 'target' }, { label: '国内普遍值对比', value: 'common' }, { label: '自定义阈值', value: 'custom' }, { label: '同比环比', value: 'trend' }],
      conditionOptions: [{ label: '大于', value: 'gt' }, { label: '大于等于', value: 'gte' }, { label: '小于', value: 'lt' }, { label: '小于等于', value: 'lte' }, { label: '等于', value: 'eq' }, { label: '不等于', value: 'ne' }, { label: '区间外', value: 'outside_range' }, { label: '缺失', value: 'missing' }, { label: '逾期', value: 'overdue' }],
      rules: {
        ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
        indicatorId: [{ required: true, message: '关联指标不能为空', trigger: 'change' }],
        thresholdValue: [{ required: true, message: '阈值不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    listIndicatorOptions({ status: '0' }).then(response => { this.indicatorOptions = response.data || [] })
  },
  mounted() {
    this.bootstrapEntryMode()
  },
  computed: {
    isEditorEntry() {
      return this.entryMode === 'editor'
    },
    isListEntry() {
      return this.entryMode === 'list'
    },
    routeTitle() {
      return this.isEditorEntry ? '新增/编辑预警规则' : '预警规则列表'
    },
    createButtonText() {
      return this.isEditorEntry ? '新建预警规则' : '新增预警规则'
    }
  },
  methods: {
    channelText,
    bootstrapEntryMode() {
      if (this.entryBootstrapped) {
        return
      }
      this.entryBootstrapped = true
      if (this.isEditorEntry) {
        this.handleAdd()
      }
    },
    getList() {
      this.loading = true
      listRule(this.queryParams).then(response => {
        this.ruleList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    optionText(options, value) {
      const item = options.find(item => item.value === value)
      return item ? item.label : value
    },
    levelText(value) {
      return warningLevelColorText(value)
    },
    levelTag(value) {
      return value === '1' ? 'danger' : value === '2' ? 'warning' : 'info'
    },
    thresholdText(row) {
      if (row.triggerCondition === 'outside_range') {
        return `${row.thresholdValue} - ${row.thresholdValueMax}`
      }
      return row.thresholdValue
    },
    syncIndicatorName(value) {
      const item = this.indicatorOptions.find(item => item.indicatorId === value)
      this.form.indicatorName = item ? item.indicatorName : ''
    },
    reset() {
      this.form = { ruleId: undefined, ruleName: '', indicatorId: undefined, indicatorName: '', responsibleUnitId: undefined, responsibleUnitName: '', regionCode: '', regionName: '', periodType: undefined, warningLevel: '3', thresholdType: 'target', thresholdValue: 80, thresholdValueMax: 90, triggerCondition: 'lt', triggerFrequency: 'daily', pushChannels: 'site', pushTargets: '监测项负责人', contentTemplate: '{{指标名称}}触发预警，当前值{{当前值}}，阈值{{阈值}}。', effectiveMode: 'now', status: '0' }
      this.pushChannelArray = ['site']
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
      this.ids = selection.map(item => item.ruleId)
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = this.isEditorEntry ? '新增/编辑预警规则' : '新增预警规则'
    },
    handleUpdate(row) {
      this.reset()
      getRule(row.ruleId || this.ids[0]).then(response => {
        this.form = response.data
        this.pushChannelArray = this.form.pushChannels ? this.form.pushChannels.split(',') : []
        this.open = true
        this.title = this.isEditorEntry ? '新增/编辑预警规则' : '编辑预警规则'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        if (!this.validateRuleForm()) return
        this.form.pushChannels = this.pushChannelArray.join(',')
        const request = this.form.ruleId ? updateRule(this.form) : addRule(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.ruleId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    validateRuleForm() {
      if (this.form.triggerCondition === 'outside_range') {
        if (this.form.thresholdValueMax === undefined || this.form.thresholdValueMax === null || this.form.thresholdValueMax === '') {
          this.$modal.msgError('区间规则必须填写阈值上限')
          return false
        }
        if (Number(this.form.thresholdValueMax) < Number(this.form.thresholdValue)) {
          this.$modal.msgError('阈值上限不能小于阈值下限')
          return false
        }
      }
      if ((this.form.triggerCondition === 'missing' || this.form.triggerCondition === 'overdue') && !this.form.responsibleUnitId) {
        this.$modal.msgError('缺报或逾期规则必须绑定责任单位ID')
        return false
      }
      if ((this.form.triggerCondition === 'missing' || this.form.triggerCondition === 'overdue') && !this.form.periodType) {
        this.$modal.msgError('缺报或逾期规则必须设置周期类型')
        return false
      }
      return true
    },
    changeStatus(row) {
      updateRuleStatus(row.ruleId, row.status).then(() => this.$modal.msgSuccess('状态已更新'))
    },
    handleDelete(row) {
      const ruleIds = row.ruleId || this.ids
      this.$modal.confirm('是否确认删除选中的预警规则？').then(function() {
        return delRule(ruleIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      })
    }
  }
}
</script>

<style scoped>
.entry-title {
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
</style>
