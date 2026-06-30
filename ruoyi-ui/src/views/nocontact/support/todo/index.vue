<template>
  <div class="app-container">
    <el-row :gutter="12">
      <el-col v-for="item in todoList" :key="item.todoType" :span="8">
        <div class="todo-card" :class="{ 'is-active': activeFocus === 'fill' && item.todoType === '待填报' }" @click="handleOpen(item)">
          <span>{{ item.todoLabel }}</span>
          <strong>{{ item.todoCount }}</strong>
        </div>
      </el-col>
    </el-row>
    <div v-if="activeFocus === 'fill'" class="todo-action-panel">
      <div class="todo-action-title">待填报处理入口</div>
      <div class="todo-action-grid">
        <button type="button" class="todo-action-card" @click="openAction('/collection/task')">
          <strong>采集任务列表</strong>
          <span>进入数据融合采集任务列表</span>
        </button>
        <button type="button" class="todo-action-card" @click="openAction('/nocontact/tracking')">
          <strong>问卷填报追踪</strong>
          <span>进入问卷待填报追踪页面</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { listTodoSummary } from '@/api/nocontact/support'

export default {
  name: 'SupportTodo',
  data() {
    return {
      todoList: []
    }
  },
  computed: {
    activeFocus() {
      return this.$route.query.focus || ''
    }
  },
  created() {
    listTodoSummary().then(response => {
      this.todoList = response.data || []
    })
  },
  methods: {
    handleOpen(item) {
      if (item.jumpTarget) {
        this.$router.push(item.jumpTarget)
      }
    },
    openAction(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.todo-card {
  padding: 18px;
  border: 1px solid #e4e7ed;
  background: #fff;
  cursor: pointer;
}

.todo-card.is-active {
  border-color: #409eff;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.1);
}

.todo-card span {
  display: block;
  color: #606266;
}

.todo-card strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
  color: #303133;
}

.todo-action-panel {
  margin-top: 16px;
  padding: 18px;
  border: 1px solid #ebeef5;
  background: #fff;
}

.todo-action-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.todo-action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.todo-action-card {
  padding: 16px;
  border: 1px solid #e4e7ed;
  background: #f8fafc;
  text-align: left;
  cursor: pointer;
}

.todo-action-card strong,
.todo-action-card span {
  display: block;
}

.todo-action-card strong {
  font-size: 15px;
  color: #303133;
}

.todo-action-card span {
  margin-top: 6px;
  color: #606266;
  line-height: 1.5;
}
</style>
