<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="messageList">
      <el-table-column label="消息标题" prop="title" min-width="220" />
      <el-table-column label="业务类型" prop="businessType" width="120" />
      <el-table-column label="业务ID" prop="businessId" width="120" />
      <el-table-column label="已读状态" width="100">
        <template slot-scope="scope">{{ scope.row.readStatus === '1' ? '已读' : '未读' }}</template>
      </el-table-column>
      <el-table-column label="事件时间" width="180">
        <template slot-scope="scope">{{ formatFriendlyDateTime(scope.row.eventTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleOpen(scope.row)">打开</el-button>
          <el-button v-if="scope.row.readStatus !== '1'" size="mini" type="text" @click="handleRead(scope.row)">标记已读</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listBusinessMessage, markBusinessMessageRead } from '@/api/nocontact/support'
import { formatFriendlyDateTime } from '@/utils/nocontactDisplay'

export default {
  name: 'SupportMessage',
  data() {
    return {
      loading: false,
      total: 0,
      messageList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    formatFriendlyDateTime,
    getList() {
      this.loading = true
      listBusinessMessage(this.queryParams).then(response => {
        this.messageList = response.rows || []
        this.total = response.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleRead(row) {
      markBusinessMessageRead(row.messageId).then(() => {
        this.$modal.msgSuccess('已标记为已读')
        this.getList()
      })
    },
    handleOpen(row) {
      if (row.jumpTarget) {
        this.$router.push(row.jumpTarget.replace('/nocontact', ''))
      }
    }
  }
}
</script>
