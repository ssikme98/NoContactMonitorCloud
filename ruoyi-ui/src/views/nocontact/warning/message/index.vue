<template>
  <div class="app-container warning-message-page">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="规则名称"><el-input v-model="queryParams.ruleName" clearable placeholder="规则名称" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="预警级别">
        <el-select v-model="queryParams.warningLevel" clearable placeholder="全部"><el-option label="一级红色" value="1" /><el-option label="二级橙色" value="2" /><el-option label="三级黄色" value="3" /></el-select>
      </el-form-item>
      <el-form-item label="消息状态">
        <el-select v-model="queryParams.messageStatus" clearable placeholder="全部"><el-option label="待处理" value="pending" /><el-option label="处理中" value="processing" /><el-option label="已关闭" value="closed" /><el-option label="已忽略" value="ignored" /></el-select>
      </el-form-item>
      <el-form-item label="地区"><el-input v-model="queryParams.regionName" clearable placeholder="地区" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['warning:message:remove']">删除</el-button></el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="规则名称" prop="ruleName" min-width="180" show-overflow-tooltip />
      <el-table-column label="指标" prop="indicatorName" min-width="180" show-overflow-tooltip />
      <el-table-column label="级别" prop="warningLevel" width="90"><template slot-scope="scope"><el-tag size="mini" :type="levelTag(scope.row.warningLevel)">{{ levelText(scope.row.warningLevel) }}</el-tag></template></el-table-column>
      <el-table-column label="责任单位" prop="responsibleUnitName" min-width="130" show-overflow-tooltip />
      <el-table-column label="地区" prop="regionName" width="110" />
      <el-table-column label="期间" prop="periodKey" width="100" />
      <el-table-column label="当前值" prop="currentValue" width="100" align="right" />
      <el-table-column label="阈值" prop="thresholdValue" width="100" align="right" />
      <el-table-column label="命中" prop="hitCount" width="80" align="right" />
      <el-table-column label="渠道" prop="pushChannels" width="110" />
      <el-table-column label="状态" prop="messageStatus" width="90"><template slot-scope="scope">{{ statusText(scope.row.messageStatus) }}</template></el-table-column>
      <el-table-column label="触发时间" prop="triggerTime" width="160" />
      <el-table-column label="操作" align="center" width="190">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)" v-hasPermi="['warning:message:query']">详情</el-button>
          <el-button v-if="scope.row.messageStatus === 'pending'" size="mini" type="text" icon="el-icon-loading" @click="changeStatus(scope.row, 'processing')" v-hasPermi="['warning:message:edit']">处理</el-button>
          <el-button v-if="scope.row.messageStatus !== 'closed' && scope.row.messageStatus !== 'ignored'" size="mini" type="text" icon="el-icon-check" @click="changeStatus(scope.row, 'closed')" v-hasPermi="['warning:message:edit']">关闭</el-button>
          <el-button v-if="scope.row.messageStatus !== 'closed' && scope.row.messageStatus !== 'ignored'" size="mini" type="text" icon="el-icon-close" @click="changeStatus(scope.row, 'ignored')" v-hasPermi="['warning:message:edit']">忽略</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="预警消息详情" :visible.sync="detailOpen" width="680px" append-to-body>
      <el-descriptions v-if="detail.messageId" :column="2" border size="small">
        <el-descriptions-item label="规则">{{ detail.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="指标">{{ detail.indicatorName }}</el-descriptions-item>
        <el-descriptions-item label="级别">{{ levelText(detail.warningLevel) }}</el-descriptions-item>
        <el-descriptions-item label="责任单位">{{ detail.responsibleUnitName }}</el-descriptions-item>
        <el-descriptions-item label="地区">{{ detail.regionName }}</el-descriptions-item>
        <el-descriptions-item label="期间">{{ detail.periodKey }}</el-descriptions-item>
        <el-descriptions-item label="当前值">{{ detail.currentValue }}</el-descriptions-item>
        <el-descriptions-item label="阈值">{{ detail.thresholdValue }}</el-descriptions-item>
        <el-descriptions-item label="命中次数">{{ detail.hitCount }}</el-descriptions-item>
        <el-descriptions-item label="渠道">{{ detail.pushChannels }}</el-descriptions-item>
        <el-descriptions-item label="接收人">{{ detail.receivers }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">处理日志</el-divider>
      <el-table :data="detail.handleLogs || []" size="small" border>
        <el-table-column label="原状态" prop="fromStatus" width="110"><template slot-scope="scope">{{ statusText(scope.row.fromStatus) }}</template></el-table-column>
        <el-table-column label="目标状态" prop="toStatus" width="110"><template slot-scope="scope">{{ statusText(scope.row.toStatus) }}</template></el-table-column>
        <el-table-column label="处理人" prop="handleBy" width="110" />
        <el-table-column label="处理时间" prop="handleTime" width="160" />
        <el-table-column label="意见" prop="handleOpinion" min-width="180" show-overflow-tooltip />
      </el-table>
      <div slot="footer" class="dialog-footer"><el-button @click="detailOpen = false">关 闭</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { listMessage, getMessage, updateMessageStatus, delMessage } from '@/api/nocontact/warning/message'

export default {
  name: 'WarningMessage',
  data() {
    return {
      loading: false,
      showSearch: true,
      ids: [],
      multiple: true,
      total: 0,
      messageList: [],
      detailOpen: false,
      detail: {},
      queryParams: { pageNum: 1, pageSize: 10, ruleName: undefined, warningLevel: undefined, messageStatus: undefined, regionName: undefined }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listMessage(this.queryParams).then(response => {
        this.messageList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    levelText(value) {
      return value === '1' ? '一级红色' : value === '2' ? '二级橙色' : '三级黄色'
    },
    levelTag(value) {
      return value === '1' ? 'danger' : value === '2' ? 'warning' : 'info'
    },
    statusText(value) {
      const map = { pending: '待处理', processing: '处理中', closed: '已关闭', ignored: '已忽略' }
      return map[value] || value
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
      this.ids = selection.map(item => item.messageId)
      this.multiple = !selection.length
    },
    handleDetail(row) {
      getMessage(row.messageId).then(response => {
        this.detail = response.data
        this.detailOpen = true
      })
    },
    changeStatus(row, status) {
      const title = status === 'processing' ? '处理预警' : status === 'closed' ? '关闭预警' : '忽略预警'
      this.$prompt('请输入处理意见', title, { inputValue: status === 'processing' ? '开始处理' : '' }).then(({ value }) => {
        return updateMessageStatus(row.messageId, status, value)
      }).then(() => {
        this.$modal.msgSuccess('状态已更新')
        this.getList()
      })
    },
    handleDelete(row) {
      const messageIds = row.messageId || this.ids
      this.$modal.confirm('是否确认删除选中的预警消息？').then(function() {
        return delMessage(messageIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      })
    }
  }
}
</script>
