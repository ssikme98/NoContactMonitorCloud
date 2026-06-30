<template>
  <div class="app-container warning-dashboard-page">
    <div class="page-title">分级预警看板</div>
    <el-row :gutter="12" class="summary-row">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <div class="summary-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="24">
        <div class="panel map-panel">
          <div class="panel-title">湖南省地图热力图</div>
          <nocontact-risk-map :regions="mapRegions" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="stats-row">
      <el-col :span="8">
        <div class="panel">
          <div class="panel-title">级别分布</div>
          <div v-for="item in data.levelStats || []" :key="'level-' + statName(item)" class="bar-row">
            <span>{{ levelText(statName(item)) }}</span>
            <div><em :style="{ width: statWidth(item) }"></em></div>
            <strong>{{ statCount(item) }}</strong>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panel">
          <div class="panel-title">地区分布</div>
          <div v-for="item in data.regionStats || []" :key="'region-' + statName(item)" class="bar-row">
            <span>{{ statName(item) }}</span>
            <div><em :style="{ width: statWidth(item) }"></em></div>
            <strong>{{ statCount(item) }}</strong>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="panel">
          <div class="panel-title">指标TOP10</div>
          <div v-for="item in data.indicatorStats || []" :key="'indicator-' + statName(item)" class="bar-row">
            <span>{{ statName(item) }}</span>
            <div><em :style="{ width: statWidth(item) }"></em></div>
            <strong>{{ statCount(item) }}</strong>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="panel trend-panel">
      <div class="panel-title">近30天预警趋势</div>
      <el-table :data="data.trendStats || []">
        <el-table-column label="日期" min-width="160">
          <template slot-scope="scope">{{ statName(scope.row) }}</template>
        </el-table-column>
        <el-table-column label="预警数" width="120" align="right">
          <template slot-scope="scope">{{ statCount(scope.row) }}</template>
        </el-table-column>
        <el-table-column label="趋势">
          <template slot-scope="scope"><div class="trend-line"><span :style="{ width: statWidth(scope.row) }"></span></div></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { getWarningDashboard } from '@/api/nocontact/warning/message'
import NocontactRiskMap from '@/components/amap/NocontactRiskMap'
import { warningLevelColorText } from '@/utils/nocontactDisplay'

export default {
  name: 'WarningDashboard',
  components: { NocontactRiskMap },
  data() {
    return {
      data: { summary: {}, levelStats: [], regionStats: [], indicatorStats: [], trendStats: [] }
    }
  },
  computed: {
    cards() {
      const s = this.data.summary || {}
      return [
        { label: '当前预警总数', value: this.valueOf(s, 'total_count') },
        { label: '今日新增预警数', value: this.valueOf(s, 'today_count') },
        { label: '已处理数', value: this.valueOf(s, 'handled_count') },
        { label: '待处理数', value: this.valueOf(s, 'pending_count') }
      ]
    },
    maxCount() {
      const rows = [].concat(this.data.levelStats || [], this.data.regionStats || [], this.data.indicatorStats || [], this.data.trendStats || [])
      return rows.reduce((max, row) => Math.max(max, Number(this.statCount(row)) || 0), 1)
    },
    mapRegions() {
      return (this.data.regionStats || []).map(item => ({
        name: this.statName(item),
        value: this.statCount(item)
      }))
    }
  },
  created() {
    this.getDashboard()
  },
  methods: {
    getDashboard() {
      getWarningDashboard().then(response => { this.data = response.data || this.data })
    },
    valueOf(row, key) {
      return row[key] || row[key.toUpperCase()] || 0
    },
    statName(row) {
      return row.stat_name || row.STAT_NAME || row.statName || ''
    },
    statCount(row) {
      return row.count_value || row.COUNT_VALUE || row.countValue || 0
    },
    statWidth(row) {
      return Math.max(8, Number(this.statCount(row)) * 100 / this.maxCount) + '%'
    },
    levelText(value) {
      return warningLevelColorText(value)
    }
  }
}
</script>

<style scoped>
.summary-row {
  margin-bottom: 12px;
}
.page-title {
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.summary-card,
.panel {
  border: 1px solid #e6ebf5;
  border-radius: 4px;
  background: #fff;
}
.summary-card {
  padding: 16px;
}
.summary-card span {
  display: block;
  color: #606266;
  font-size: 13px;
}
.summary-card strong {
  display: block;
  margin-top: 8px;
  color: #303133;
  font-size: 24px;
  line-height: 1.2;
}
.panel {
  padding: 14px;
  min-height: 260px;
}
.trend-panel {
  margin-top: 12px;
}
.stats-row {
  margin-top: 12px;
}
.panel-title {
  margin-bottom: 12px;
  font-weight: 600;
  color: #303133;
}
.map-panel {
  min-height: 360px;
}
.bar-row {
  display: grid;
  grid-template-columns: minmax(96px, 1fr) 2fr 42px;
  gap: 8px;
  align-items: center;
  min-height: 32px;
  font-size: 13px;
}
.bar-row span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bar-row div,
.trend-line {
  height: 8px;
  background: #edf2f7;
  border-radius: 4px;
  overflow: hidden;
}
.bar-row em,
.trend-line span {
  display: block;
  height: 100%;
  background: #409eff;
}
</style>
