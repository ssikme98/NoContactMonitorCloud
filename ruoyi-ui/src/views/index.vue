<template>
  <div class="app-container workbench-page">
    <div class="hero">
      <div>
        <div class="hero__eyebrow">业务总览</div>
        <h1>营商环境无感监测子系统</h1>
        <p>围绕数据采集、指标体系、预警处理、整改闭环、问卷调研和报告生成，提供统一的业务入口。</p>
      </div>
      <div class="hero__meta">
        <div class="hero__meta-item">
          <span>企业样本库</span>
          <strong>{{ enterpriseTotal }}</strong>
        </div>
        <div class="hero__meta-item">
          <span>预警待处理</span>
          <strong>{{ warningPending }}</strong>
        </div>
        <div class="hero__meta-item">
          <span>待办总量</span>
          <strong>{{ todoTotal }}</strong>
        </div>
      </div>
    </div>

    <el-row :gutter="12" class="summary-row">
      <el-col v-for="item in summaryCards" :key="item.label" :xs="24" :sm="12" :lg="6">
        <div class="summary-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <em>{{ item.desc }}</em>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="section-row">
      <el-col :xs="24" :lg="14">
        <div class="panel">
          <div class="panel__header">
            <span>统一待办</span>
            <el-button type="text" @click="$router.push('/support/todo')">查看全部</el-button>
          </div>
          <div class="todo-grid">
            <button
              v-for="item in todoList"
              :key="item.todoType"
              type="button"
              class="todo-card"
              @click="openTarget(item.jumpTarget)"
            >
              <span>{{ item.todoLabel }}</span>
              <strong>{{ item.todoCount }}</strong>
            </button>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="10">
        <div class="panel">
          <div class="panel__header">
            <span>重点入口</span>
          </div>
          <div class="entry-list">
            <button v-for="item in entryList" :key="item.title" type="button" class="entry-item" @click="openTarget(item.path)">
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </button>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="section-row">
      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel__header">
            <span>采集任务状态</span>
            <el-button type="text" @click="$router.push('/collection/task')">进入任务列表</el-button>
          </div>
          <div v-if="taskStatusStats.length" class="stat-list">
            <div v-for="item in taskStatusStats" :key="'status-' + item.name" class="stat-row">
              <span>{{ taskStatusLabel(item.name) }}</span>
              <div><em :style="{ width: barWidth(item.count) }"></em></div>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
          <el-empty v-else description="暂无采集任务数据" :image-size="72" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel__header">
            <span>预警级别分布</span>
            <el-button type="text" @click="$router.push('/warning/dashboard')">进入预警看板</el-button>
          </div>
          <div v-if="warningLevelStats.length" class="stat-list">
            <div v-for="item in warningLevelStats" :key="'warning-' + item.name" class="stat-row">
              <span>{{ warningLevelLabel(item.name) }}</span>
              <div><em :style="{ width: barWidth(item.count) }"></em></div>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
          <el-empty v-else description="暂无预警数据" :image-size="72" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="section-row">
      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel__header">
            <span>地区预警分布</span>
            <el-button type="text" @click="$router.push('/warning/message')">查看预警消息</el-button>
          </div>
          <div v-if="warningRegionStats.length" class="stat-list">
            <div v-for="item in warningRegionStats" :key="'region-' + item.name" class="stat-row">
              <span>{{ item.name }}</span>
              <div><em :style="{ width: barWidth(item.count) }"></em></div>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
          <el-empty v-else description="暂无地区预警数据" :image-size="72" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel__header">
            <span>系统状态</span>
          </div>
          <div class="status-list">
            <div class="status-item">
              <span>报告任务</span>
              <strong>{{ reportTotal }}</strong>
            </div>
            <div class="status-item">
              <span>整改问题</span>
              <strong>{{ rectificationTotal }}</strong>
            </div>
            <div class="status-item">
              <span>消息中心</span>
              <strong>{{ messageTotal }}</strong>
            </div>
            <div class="status-item">
              <span>地图配置</span>
              <strong>{{ amapConfigured ? '已配置' : '待配置' }}</strong>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listTodoSummary, getSupportPublicSettings, listBusinessMessage } from '@/api/nocontact/support'
import { getTaskSummary } from '@/api/nocontact/fusion/task'
import { getWarningDashboard } from '@/api/nocontact/warning/message'
import { listEnterprise } from '@/api/nocontact/enterprise'
import { getIssueDashboard } from '@/api/nocontact/rectification/issue'
import { listReportTask } from '@/api/nocontact/report'

function normalizeStats(rows = []) {
  return rows.map(item => ({
    name: item.stat_name || item.STAT_NAME || item.statName || '',
    count: Number(item.count_value || item.COUNT_VALUE || item.countValue || 0)
  }))
}

export default {
  name: 'BusinessWorkbench',
  data() {
    return {
      todoList: [],
      todoTotal: 0,
      enterpriseTotal: 0,
      warningPending: 0,
      warningTotal: 0,
      reportTotal: 0,
      rectificationTotal: 0,
      messageTotal: 0,
      amapConfigured: false,
      taskStatusStats: [],
      warningLevelStats: [],
      warningRegionStats: [],
      entryList: [
        { title: '营商数据融合', desc: '采集任务、填报审核、采集监控', path: '/collection/task' },
        { title: '指标体系管理', desc: '指标体系、算法与标签分类', path: '/indicator-system/list' },
        { title: '检测预警中心', desc: '预警规则、消息管理、分级看板', path: '/warning/dashboard' },
        { title: '公共支撑', desc: '统一待办、消息中心、业务设置', path: '/support/todo' }
      ]
    }
  },
  computed: {
    summaryCards() {
      return [
        { label: '采集任务总量', value: this.taskTotal, desc: '当前业务链路中的采集任务' },
        { label: '预警总数', value: this.warningTotal, desc: '已生成的预警消息' },
        { label: '企业样本数', value: this.enterpriseTotal, desc: '问卷调研企业库记录' },
        { label: '报告任务数', value: this.reportTotal, desc: '报告自动生成任务' }
      ]
    },
    taskTotal() {
      return this.taskStatusStats.reduce((sum, item) => sum + item.count, 0)
    },
    maxBarValue() {
      const all = [].concat(this.taskStatusStats, this.warningLevelStats, this.warningRegionStats)
      return all.reduce((max, item) => Math.max(max, item.count || 0), 1)
    }
  },
  created() {
    this.loadWorkbench()
  },
  methods: {
    loadWorkbench() {
      this.loadTodo()
      this.loadWarning()
      this.loadTasks()
      this.loadEnterprises()
      this.loadRectification()
      this.loadReports()
      this.loadSettings()
      this.loadMessages()
    },
    loadTodo() {
      listTodoSummary().then(response => {
        this.todoList = response.data || []
        this.todoTotal = this.todoList.reduce((sum, item) => sum + Number(item.todoCount || 0), 0)
      })
    },
    loadWarning() {
      getWarningDashboard().then(response => {
        const data = response.data || {}
        const summary = data.summary || {}
        this.warningPending = Number(summary.pending_count || summary.PENDING_COUNT || 0)
        this.warningTotal = Number(summary.total_count || summary.TOTAL_COUNT || 0)
        this.warningLevelStats = normalizeStats(data.levelStats)
        this.warningRegionStats = normalizeStats(data.regionStats)
      })
    },
    loadTasks() {
      getTaskSummary().then(response => {
        const data = response.data || {}
        this.taskStatusStats = normalizeStats(data.statusStats)
      })
    },
    loadEnterprises() {
      listEnterprise({ pageNum: 1, pageSize: 1 }).then(response => {
        this.enterpriseTotal = Number(response.total || 0)
      })
    },
    loadRectification() {
      getIssueDashboard().then(response => {
        const data = response.data || {}
        this.rectificationTotal = normalizeStats(data.statusStats).reduce((sum, item) => sum + item.count, 0)
      })
    },
    loadReports() {
      listReportTask({ pageNum: 1, pageSize: 1 }).then(response => {
        this.reportTotal = Number(response.total || 0)
      })
    },
    loadSettings() {
      getSupportPublicSettings().then(response => {
        const data = response.data || {}
        this.amapConfigured = Boolean(data.amapFrontendKey || data.amapSecurityJsCode || data.amapGeocodeKey)
      })
    },
    loadMessages() {
      listBusinessMessage({ pageNum: 1, pageSize: 1 }).then(response => {
        this.messageTotal = Number(response.total || 0)
      })
    },
    openTarget(path) {
      if (path) {
        this.$router.push(path)
      }
    },
    taskStatusLabel(value) {
      const map = {
        draft: '草稿',
        published: '已发布',
        running: '执行中',
        done: '已完成',
        rejected: '已退回',
        failed: '执行异常'
      }
      return map[value] || value || '-'
    },
    warningLevelLabel(value) {
      const map = {
        '1': '一级红色',
        '2': '二级橙色',
        '3': '三级黄色'
      }
      return map[value] || value || '-'
    },
    barWidth(value) {
      return `${Math.max(10, value * 100 / this.maxBarValue)}%`
    }
  }
}
</script>

<style scoped>
.workbench-page {
  background: #f5f7fb;
  min-height: calc(100vh - 84px);
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 24px;
  margin-bottom: 12px;
  border: 1px solid #e6ebf5;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffffff 0%, #f7fbff 100%);
}

.hero__eyebrow {
  margin-bottom: 8px;
  color: #409eff;
  font-size: 13px;
  font-weight: 600;
}

.hero h1 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
  color: #1f2d3d;
}

.hero p {
  margin: 12px 0 0;
  max-width: 760px;
  color: #5b6b82;
  line-height: 1.7;
}

.hero__meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(110px, 1fr));
  gap: 12px;
  min-width: 360px;
}

.hero__meta-item,
.summary-card,
.panel,
.todo-card,
.entry-item,
.status-item {
  border: 1px solid #e6ebf5;
  border-radius: 8px;
  background: #fff;
}

.hero__meta-item {
  padding: 16px;
}

.hero__meta-item span,
.summary-card span,
.status-item span {
  display: block;
  color: #6b778c;
  font-size: 13px;
}

.hero__meta-item strong,
.summary-card strong,
.status-item strong {
  display: block;
  margin-top: 8px;
  color: #1f2d3d;
  font-size: 24px;
  line-height: 1.2;
}

.summary-row,
.section-row {
  margin-bottom: 12px;
}

.summary-card {
  padding: 18px;
  min-height: 122px;
}

.summary-card em {
  display: block;
  margin-top: 12px;
  color: #8a94a6;
  font-style: normal;
  line-height: 1.6;
}

.panel {
  padding: 16px;
  min-height: 260px;
}

.panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: #1f2d3d;
  font-weight: 600;
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.todo-card,
.entry-item {
  width: 100%;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: border-color .15s ease, transform .15s ease, box-shadow .15s ease;
}

.todo-card:hover,
.entry-item:hover {
  border-color: #bfdcff;
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.12);
  transform: translateY(-1px);
}

.todo-card span,
.entry-item span {
  display: block;
  color: #6b778c;
}

.todo-card strong,
.entry-item strong {
  display: block;
  margin-top: 8px;
  color: #1f2d3d;
  font-size: 22px;
  line-height: 1.2;
}

.entry-list {
  display: grid;
  gap: 12px;
}

.entry-item strong {
  font-size: 16px;
}

.entry-item span {
  margin-top: 8px;
  line-height: 1.6;
}

.stat-list {
  display: grid;
  gap: 12px;
}

.stat-row {
  display: grid;
  grid-template-columns: minmax(96px, 1fr) 2fr 42px;
  gap: 10px;
  align-items: center;
  min-height: 32px;
  font-size: 13px;
}

.stat-row span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #475569;
}

.stat-row div {
  height: 8px;
  background: #edf2f7;
  border-radius: 999px;
  overflow: hidden;
}

.stat-row em {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #409eff 0%, #67c23a 100%);
}

.stat-row strong {
  color: #1f2d3d;
  text-align: right;
}

.status-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.status-item {
  padding: 16px;
  min-height: 98px;
}

@media (max-width: 1200px) {
  .hero {
    flex-direction: column;
  }

  .hero__meta {
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .hero__meta,
  .todo-grid,
  .status-list {
    grid-template-columns: 1fr;
  }

  .summary-card,
  .panel,
  .todo-card,
  .entry-item,
  .status-item {
    border-radius: 6px;
  }
}
</style>
