<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :lg="6" v-for="card in cards" :key="card.label">
        <div class="rect-card">
          <div class="rect-card__label">{{ card.label }}</div>
          <div class="rect-card__value">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>
    <el-card shadow="never">
      <div slot="header">地区整改排名</div>
      <el-table :data="regionStats" size="small" border>
        <el-table-column label="地区" prop="stat_name" />
        <el-table-column label="问题数" prop="count_value" width="120" align="right" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getIssueDashboard } from '@/api/nocontact/rectification/issue'

export default {
  name: 'RectificationDashboard',
  data() {
    return {
      statusStats: [],
      regionStats: []
    }
  },
  computed: {
    cards() {
      const total = this.statusStats.reduce((sum, item) => sum + Number(item.count_value || 0), 0)
      return [
        { label: '总问题数', value: total },
        { label: '待整改', value: this.valueOf('pending_rectification') + this.valueOf('rework') },
        { label: '整改中', value: this.valueOf('rectifying') },
        { label: '待审核', value: this.valueOf('pending_review') },
        { label: '已关闭', value: this.valueOf('closed') + this.valueOf('review_passed') + this.valueOf('archived') }
      ]
    }
  },
  created() {
    getIssueDashboard().then(response => {
      this.statusStats = (response.data && response.data.statusStats) || []
      this.regionStats = (response.data && response.data.regionStats) || []
    })
  },
  methods: {
    valueOf(status) {
      const row = this.statusStats.find(item => item.stat_name === status)
      return row ? row.count_value : 0
    }
  }
}
</script>

<style scoped>
.rect-card {
  min-height: 96px;
  padding: 18px;
  margin-bottom: 16px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  background: #fff;
}
.rect-card__label {
  color: #606266;
  font-size: 13px;
}
.rect-card__value {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 600;
}
</style>
