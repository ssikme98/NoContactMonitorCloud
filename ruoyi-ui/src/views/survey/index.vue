<template>
  <div class="app-container survey-overview">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="metric-label">模块状态</div>
          <div class="metric-value">{{ overview.status || '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="metric-label">服务名称</div>
          <div class="metric-value">{{ overview.serviceName || '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="metric-label">模块名称</div>
          <div class="metric-value">{{ overview.moduleName || '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="metric-label">服务端口</div>
          <div class="metric-value">{{ overview.port || '-' }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getSurveyOverview } from '@/api/survey/overview'

export default {
  name: 'SurveyOverview',
  data() {
    return {
      overview: {}
    }
  },
  created() {
    this.loadOverview()
  },
  methods: {
    loadOverview() {
      getSurveyOverview().then(response => {
        this.overview = response.data || {}
      })
    }
  }
}
</script>

<style scoped>
.survey-overview .metric-label {
  color: #606266;
  font-size: 13px;
  line-height: 20px;
}

.survey-overview .metric-value {
  color: #303133;
  font-size: 20px;
  font-weight: 600;
  line-height: 32px;
  margin-top: 8px;
  word-break: break-all;
}
</style>
