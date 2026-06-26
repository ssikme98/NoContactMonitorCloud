<template>
  <div class="app-container warning-rule-page">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="queryParams.ruleName" placeholder="请输入规则名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="规则状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['warning:rule:add']">新增预警规则</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['warning:rule:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="ruleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="规则名称" prop="ruleName" min-width="180" show-overflow-tooltip />
      <el-table-column label="关联指标" prop="indicatorName" min-width="180" show-overflow-tooltip />
      <el-table-column label="级别" prop="warningLevel" width="90">
        <template slot-scope="scope"><el-tag size="mini" :type="levelTag(scope.row.warningLevel)">{{ levelText(scope.row.warningLevel) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="阈值类型" prop="thresholdType" width="130">
        <template slot-scope="scope">{{ optionText(thresholdOptions, scope.row.thresholdType) }}</template>
      </el-table-column>
      <el-table-column label="阈值" prop="thresholdValue" width="100" align="right" />
      <el-table-column label="触发条件" prop="triggerCondition" width="100">
        <template slot-scope="scope">{{ optionText(conditionOptions, scope.row.triggerCondition) }}</template>
      </el-table-column>
      <el-table-column label="推送渠道" prop="pushChannels" width="130" />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template slot-scope="scope"><el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="changeStatus(scope.row)" v-hasPermi="['warning:rule:edit']" /></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['warning:rule:edit']">编辑</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['warning:rule:remove']">删除</el-button>
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
          <el-col :span="8"><el-form-item label="阈值类型"><el-select v-model="form.thresholdType"><el-option v-for="item in thresholdOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="阈值数值" prop="thresholdValue"><el-input-number v-model="form.thresholdValue" :precision="2" controls-position="right" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="触发条件"><el-select v-model="form.triggerCondition"><el-option v-for="item in conditionOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="触发频率"><el-select v-model="form.triggerFrequency"><el-option label="实时" value="realtime" /><el-option label="每日" value="daily" /><el-option label="每周" value="weekly" /></el-select></el-form-item></el-col>
          <el-col :span="16"><el-form-item label="推送渠道"><el-checkbox-group v-model="pushChannelArray"><el-checkbox label="site">平台消息</el-checkbox><el-checkbox label="sms">短信</el-checkbox><el-checkbox label="email">邮件</el-checkbox></el-checkbox-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="推送对象"><el-input v-model="form.pushTargets" placeholder="监测项负责人、管理人员或指定用户" /></el-form-item>
        <el-form-item label="推送模板"><el-input v-model="form.contentTemplate" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio label="0">启用</el-radio><el-radio label="1">停用</el-radio></el-radio-group></el-form-item>
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

export default {
  name: 'WarningRule',
  data() {
    return {
      loading: false,
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
      levelOptions: [{ label: '一级红色', value: '1' }, { label: '二级橙色', value: '2' }, { label: '三级黄色', value: '3' }],
      thresholdOptions: [{ label: '目标值对比', value: 'target' }, { label: '国内普遍值对比', value: 'common' }, { label: '自定义阈值', value: 'custom' }, { label: '同比环比', value: 'trend' }],
      conditionOptions: [{ label: '大于', value: 'gt' }, { label: '大于等于', value: 'gte' }, { label: '小于', value: 'lt' }, { label: '小于等于', value: 'lte' }, { label: '等于', value: 'eq' }, { label: '不等于', value: 'ne' }, { label: '缺失', value: 'missing' }, { label: '逾期', value: 'overdue' }],
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
  methods: {
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
      return this.optionText(this.levelOptions, value)
    },
    levelTag(value) {
      return value === '1' ? 'danger' : value === '2' ? 'warning' : 'info'
    },
    syncIndicatorName(value) {
      const item = this.indicatorOptions.find(item => item.indicatorId === value)
      this.form.indicatorName = item ? item.indicatorName : ''
    },
    reset() {
      this.form = { ruleId: undefined, ruleName: '', indicatorId: undefined, indicatorName: '', warningLevel: '3', thresholdType: 'target', thresholdValue: 80, triggerCondition: 'lt', triggerFrequency: 'daily', pushChannels: 'site', pushTargets: '监测项负责人', contentTemplate: '{{指标名称}}触发预警，当前值{{当前值}}，阈值{{阈值}}。', effectiveMode: 'now', status: '0' }
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
      this.title = '新增预警规则'
    },
    handleUpdate(row) {
      this.reset()
      getRule(row.ruleId || this.ids[0]).then(response => {
        this.form = response.data
        this.pushChannelArray = this.form.pushChannels ? this.form.pushChannels.split(',') : []
        this.open = true
        this.title = '编辑预警规则'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.form.pushChannels = this.pushChannelArray.join(',')
        const request = this.form.ruleId ? updateRule(this.form) : addRule(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.ruleId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
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
