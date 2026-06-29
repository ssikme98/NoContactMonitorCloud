<template>
  <div class="app-container collection-monitor">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :lg="6" v-for="card in cards" :key="card.label">
        <div class="monitor-card">
          <div class="monitor-card__label">{{ card.label }}</div>
          <div class="monitor-card__value">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="monitor-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never">
          <div slot="header">任务状态</div>
          <el-table :data="taskStats" size="small" border>
            <el-table-column label="状态">
              <template slot-scope="scope">{{ taskStatusText(scope.row.stat_name) }}</template>
            </el-table-column>
            <el-table-column label="数量" prop="count_value" width="100" align="right" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never">
          <div slot="header">采集批次状态</div>
          <el-table :data="batchStats" size="small" border>
            <el-table-column label="状态">
              <template slot-scope="scope">{{ batchStatusText(scope.row.stat_name) }}</template>
            </el-table-column>
            <el-table-column label="数量" prop="count_value" width="100" align="right" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getTaskSummary } from '@/api/nocontact/fusion/task'
import { getCollectionSummary } from '@/api/nocontact/fusion/collection'

export default {
  name: 'CollectionMonitor',
  data() {
    return {
      taskStats: [],
      batchStats: []
    }
  },
  computed: {
    cards() {
      const taskTotal = this.taskStats.reduce((sum, item) => sum + Number(item.count_value || 0), 0)
      const batchTotal = this.batchStats.reduce((sum, item) => sum + Number(item.count_value || 0), 0)
      const pending = this.batchStats.filter(item => item.stat_name === 'pending_audit').reduce((sum, item) => sum + Number(item.count_value || 0), 0)
      const approved = this.batchStats.filter(item => item.stat_name === 'approved').reduce((sum, item) => sum + Number(item.count_value || 0), 0)
      return [
        { label: '采集任务总数', value: taskTotal },
        { label: '采集批次总数', value: batchTotal },
        { label: '待审核批次', value: pending },
        { label: '已通过批次', value: approved }
      ]
    }
  },
  created() {
    this.getData()
  },
  methods: {
    getData() {
      getTaskSummary().then(response => {
        this.taskStats = (response.data && response.data.statusStats) || []
      })
      getCollectionSummary().then(response => {
        this.batchStats = (response.data && response.data.statusStats) || []
      })
    },
    taskStatusText(status) {
      const map = {
        draft: '草稿',
        published: '已发布',
        running: '执行中',
        done: '已完成',
        error: '异常'
      }
      return map[status] || status || '-'
    },
    batchStatusText(status) {
      const map = {
        draft: '草稿',
        pending_audit: '待审核',
        approved: '已审核',
        rejected: '已驳回'
      }
      return map[status] || status || '-'
    }
  }
}
</script>

<style scoped>
.monitor-card {
  min-height: 96px;
  padding: 18px;
  margin-bottom: 16px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  background: #fff;
}
.monitor-card__label {
  color: #606266;
  font-size: 13px;
}
.monitor-card__value {
  margin-top: 10px;
  color: #1f2d3d;
  font-size: 28px;
  font-weight: 600;
}
.monitor-row {
  margin-top: 4px;
}
</style>
