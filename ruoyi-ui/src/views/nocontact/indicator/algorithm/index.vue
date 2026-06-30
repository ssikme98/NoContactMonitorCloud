<template>
  <div class="app-container algorithm-page">
    <el-row :gutter="16">
      <el-col :xs="24" :md="12" :lg="8" v-for="item in algorithms" :key="item.algorithmType">
        <el-card shadow="never" class="algorithm-card">
          <div class="algorithm-title">{{ item.algorithmName }}</div>
          <div class="algorithm-desc">{{ item.description }}</div>
          <el-button type="text" icon="el-icon-cpu" @click="selectAlgorithm(item)">模拟计算</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="simulator">
      <div slot="header">算法模拟器</div>
      <el-form :model="form" size="small" :inline="true">
        <el-form-item label="算法">
          <el-select v-model="form.algorithmType" style="width: 180px">
            <el-option v-for="item in algorithms" :key="item.algorithmType" :label="item.algorithmName" :value="item.algorithmType" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前值"><el-input-number v-model="form.currentValue" controls-position="right" /></el-form-item>
        <el-form-item label="目标值"><el-input-number v-model="form.targetValue" controls-position="right" /></el-form-item>
        <el-form-item label="基准值"><el-input-number v-model="form.baselineValue" controls-position="right" /></el-form-item>
        <el-form-item label="满分"><el-input-number v-model="form.fullScore" controls-position="right" /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-data-analysis" @click="simulate">计算</el-button></el-form-item>
      </el-form>
      <el-alert v-if="result" :title="'模拟得分：' + result.score" type="success" show-icon :closable="false" />
    </el-card>
  </div>
</template>

<script>
import { listAlgorithms, simulateAlgorithm } from '@/api/nocontact/indicator/workbench'

export default {
  name: 'IndicatorAlgorithm',
  data() {
    return {
      algorithms: [],
      result: null,
      form: { algorithmType: 'deduct', currentValue: 80, targetValue: 100, baselineValue: 0, fullScore: 100 }
    }
  },
  created() {
    listAlgorithms().then(response => {
      this.algorithms = response.data || []
      if (this.algorithms.length) this.form.algorithmType = this.algorithms[0].algorithmType
    })
  },
  methods: {
    selectAlgorithm(item) {
      this.form.algorithmType = item.algorithmType
      this.simulate()
    },
    simulate() {
      simulateAlgorithm(this.form).then(response => {
        this.result = response.data
      })
    }
  }
}
</script>

<style scoped>
.algorithm-card {
  margin-bottom: 16px;
  min-height: 130px;
}
.algorithm-title {
  font-weight: 600;
  margin-bottom: 10px;
}
.algorithm-desc {
  min-height: 44px;
  color: #606266;
  line-height: 22px;
}
.simulator {
  margin-top: 4px;
}
</style>
