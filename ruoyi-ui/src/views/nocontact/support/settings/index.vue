<template>
  <div class="app-container">
    <el-descriptions title="业务设置" :column="2" border>
      <el-descriptions-item label="地图展示配置">{{ configState(form.amapFrontendKey) }}</el-descriptions-item>
      <el-descriptions-item label="地图安全校验">{{ configState(form.amapSecurityJsCode) }}</el-descriptions-item>
      <el-descriptions-item label="地址解析服务">仅后端使用，已单独保管</el-descriptions-item>
      <el-descriptions-item label="文件存储位置">{{ form.fileBasePath || '-' }}</el-descriptions-item>
      <el-descriptions-item label="预警推送">{{ form.warningPushEnabled === '1' ? '开启' : '关闭' }}</el-descriptions-item>
      <el-descriptions-item label="默认报告周期">{{ periodText(form.reportDefaultPeriod) }}</el-descriptions-item>
      <el-descriptions-item label="外部同步开关">{{ form.integrationGlobalEnabled === '1' ? '开启' : '关闭' }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script>
import { getSupportPublicSettings } from '@/api/nocontact/support'

export default {
  name: 'SupportSettings',
  data() {
    return {
      form: {}
    }
  },
  created() {
    getSupportPublicSettings().then(response => {
      this.form = response.data || {}
    })
  },
  methods: {
    configState(value) {
      return value ? '已配置' : '未配置'
    },
    periodText(value) {
      const map = { month: '月度', quarter: '季度', year: '年度' }
      return map[value] || value || '-'
    }
  }
}
</script>
