<template>
  <div class="app-container survey-task-page">
    <div v-if="routeTitle" class="entry-title">{{ routeTitle }}</div>
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" size="small" :inline="true">
      <el-form-item label="任务名称" prop="taskName">
        <el-input v-model="queryParams.taskName" placeholder="请输入任务名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable>
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col v-if="showAddAction" :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['survey:task:add']">{{ addButtonText }}</el-button>
      </el-col>
      <el-col v-if="showDeleteAction" :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['survey:task:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务名称" prop="taskName" min-width="200" show-overflow-tooltip />
      <el-table-column label="绑定问卷" prop="questionnaireName" min-width="180" show-overflow-tooltip />
      <el-table-column label="对象池" prop="sampleSource" width="100">
        <template slot-scope="scope">{{ sourceText(scope.row.sampleSource) }}</template>
      </el-table-column>
      <el-table-column label="抽样方式" prop="samplingMethod" width="110">
        <template slot-scope="scope">{{ samplingText(scope.row.samplingMethod) }}</template>
      </el-table-column>
      <el-table-column label="样本数" prop="sampleSize" width="90" align="center" />
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发卷时间" prop="dispatchTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.dispatchTime) }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="230">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="openDetail(scope.row)" v-hasPermi="['survey:task:query']">{{ detailButtonText }}</el-button>
          <el-button v-if="showDispatchAction && scope.row.status === '1'" size="mini" type="text" icon="el-icon-s-promotion" @click="handleDispatch(scope.row)" v-hasPermi="['survey:task:dispatch']">发卷</el-button>
          <el-button v-if="showDeleteAction" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['survey:task:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="118px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="可用问卷" prop="questionnaireId">
          <el-select v-model="form.questionnaireId" placeholder="请选择可用问卷" filterable>
            <el-option v-for="item in questionnaireOptions" :key="item.questionnaireId" :label="item.questionnaireName + ' v' + (item.versionNo || 1)" :value="item.questionnaireId" />
          </el-select>
        </el-form-item>
        <el-form-item label="对象池来源" prop="sampleSource">
          <el-radio-group v-model="form.sampleSource" @change="handleSourceChange">
            <el-radio-button v-for="item in SOURCE_OPTIONS" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.sampleSource === 'group'" label="企业分组" prop="groupId">
          <treeselect v-model="form.groupId" :options="groupOptions" :show-count="true" placeholder="请选择企业分组" />
        </el-form-item>
        <el-form-item v-if="form.sampleSource === 'enterprise'" label="指定企业" prop="enterpriseIds">
          <el-select v-model="form.enterpriseIds" multiple filterable collapse-tags placeholder="请选择企业">
            <el-option v-for="item in enterpriseOptions" :key="item.enterpriseId" :label="item.enterpriseName" :value="item.enterpriseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="抽样方式" prop="samplingMethod">
          <el-select v-model="form.samplingMethod" :disabled="form.sampleSource === 'enterprise'" placeholder="请选择抽样方式">
            <el-option v-for="item in SAMPLING_METHODS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.samplingMethod !== 'specified' && form.sampleSource !== 'enterprise'" label="样本数量" prop="sampleSize">
          <el-input-number v-model="form.sampleSize" :min="1" :max="10000" controls-position="right" />
        </el-form-item>
        <el-form-item label="模拟渠道">
          <el-checkbox-group v-model="form.sendChannels">
            <el-checkbox label="sms">短信记录</el-checkbox>
            <el-checkbox label="site">站内信记录</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">生成任务样本</el-button>
      </div>
    </el-dialog>

    <el-dialog title="调研任务详情" :visible.sync="detailOpen" width="1180px" append-to-body>
      <el-descriptions v-if="detail.taskId" :column="3" border size="small">
        <el-descriptions-item label="任务名称">{{ detail.taskName }}</el-descriptions-item>
        <el-descriptions-item label="问卷">{{ detail.questionnaireName }}</el-descriptions-item>
        <el-descriptions-item label="发卷时间">{{ parseTime(detail.dispatchTime) }}</el-descriptions-item>
        <el-descriptions-item label="对象池">{{ sourceText(detail.sampleSource) }}</el-descriptions-item>
        <el-descriptions-item label="抽样方式">{{ samplingText(detail.samplingMethod) }}</el-descriptions-item>
        <el-descriptions-item label="样本数量">{{ detail.sampleSize || 0 }}</el-descriptions-item>
        <el-descriptions-item label="抽样批次">{{ detail.samplingBatchNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="抽样时间">{{ parseTime(detail.samplingBatchTime) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <div class="tracking-summary">
        <div class="tracking-metric">
          <span>总发放数</span>
          <strong>{{ trackingSummary.totalCount || 0 }}</strong>
        </div>
        <div class="tracking-metric">
          <span>已填报数</span>
          <strong>{{ trackingSummary.submittedCount || 0 }}</strong>
        </div>
        <div class="tracking-metric">
          <span>未填报数</span>
          <strong>{{ trackingSummary.unsubmittedCount || 0 }}</strong>
        </div>
        <div class="tracking-metric">
          <span>填报率</span>
          <strong>{{ formatRate(trackingSummary.responseRate) }}</strong>
        </div>
      </div>

      <el-tabs v-model="activeDetailTab" class="detail-tabs">
        <el-tab-pane label="填报明细" name="tracking-detail">
          <el-form :model="trackingQuery" size="small" :inline="true" class="tracking-filter">
            <el-form-item label="企业名称">
              <el-input v-model="trackingQuery.enterpriseName" clearable placeholder="企业名称" />
            </el-form-item>
            <el-form-item label="城市">
              <el-select v-model="trackingQuery.regionName" clearable filterable placeholder="请选择城市">
                <el-option v-for="item in HUNAN_CITY_OPTIONS" :key="item.code" :label="item.name" :value="item.name" />
              </el-select>
            </el-form-item>
            <el-form-item label="行业">
              <el-input v-model="trackingQuery.industryCategory" clearable placeholder="行业" />
            </el-form-item>
            <el-form-item label="规模">
              <el-input v-model="trackingQuery.enterpriseScale" clearable placeholder="规模" />
            </el-form-item>
            <el-form-item label="填报状态">
              <el-select v-model="trackingQuery.submitStatus" clearable placeholder="全部">
                <el-option label="未填报" value="0" />
                <el-option label="已填报" value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="el-icon-search" @click="handleTrackingQuery">搜索</el-button>
              <el-button icon="el-icon-refresh" @click="resetTrackingQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="trackingDetails" max-height="360">
            <el-table-column label="企业名称" prop="enterpriseName" min-width="190" show-overflow-tooltip />
            <el-table-column label="城市" prop="regionName" width="130" show-overflow-tooltip />
            <el-table-column label="行业" prop="industryCategory" width="150" show-overflow-tooltip />
            <el-table-column label="规模" prop="enterpriseScale" width="90" />
            <el-table-column label="发送状态" prop="sendStatus" width="90">
              <template slot-scope="scope">{{ sendStatusText(scope.row.sendStatus) }}</template>
            </el-table-column>
            <el-table-column label="填报状态" prop="submitStatus" width="90">
              <template slot-scope="scope">{{ submitStatusText(scope.row.submitStatus) }}</template>
            </el-table-column>
            <el-table-column label="发送时间" prop="sendTime" width="160">
              <template slot-scope="scope">{{ parseTime(scope.row.sendTime) }}</template>
            </el-table-column>
            <el-table-column label="提交时间" prop="submitTime" width="160">
              <template slot-scope="scope">{{ parseTime(scope.row.submitTime) }}</template>
            </el-table-column>
          </el-table>
          <pagination v-show="trackingDetailTotal > 0" :total="trackingDetailTotal" :page.sync="trackingQuery.pageNum" :limit.sync="trackingQuery.pageSize" @pagination="getTrackingDetails" />
        </el-tab-pane>
        <el-tab-pane label="城市统计" name="tracking-region">
          <el-table :data="trackingRegions" max-height="360">
            <el-table-column label="城市" prop="regionName" min-width="180" />
            <el-table-column label="发放数" prop="totalCount" width="120" align="center" />
            <el-table-column label="已填报数" prop="submittedCount" width="120" align="center" />
            <el-table-column label="填报率" prop="responseRate" width="120" align="center">
              <template slot-scope="scope">{{ formatRate(scope.row.responseRate) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="填报趋势" name="tracking-trend">
          <el-table :data="trackingTrend" max-height="320">
            <el-table-column label="提交日期" prop="submitDate" min-width="160" />
            <el-table-column label="填报数" prop="submittedCount" width="120" align="center" />
            <el-table-column label="趋势" min-width="260">
              <template slot-scope="scope">
                <div class="trend-bar">
                  <span :style="{ width: trendWidth(scope.row.submittedCount) }"></span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="满意度分析" name="analytics">
          <div class="satisfaction-head">
            <div class="tracking-metric">
              <span>有效答卷数</span>
              <strong>{{ satisfactionAnalytics.responseCount || 0 }}</strong>
            </div>
            <div class="tracking-metric">
              <span>总体满意度</span>
              <strong>{{ formatScore(satisfactionAnalytics.overallScore) }}</strong>
            </div>
            <el-button type="primary" icon="el-icon-download" size="small" @click="exportSatisfactionReport">导出Word报告</el-button>
          </div>
          <el-row :gutter="12">
            <el-col :span="12">
              <div class="analysis-panel">
                <div class="analysis-title">题目维度</div>
                <div v-for="item in satisfactionAnalytics.dimensionStats || []" :key="'dimension-' + item.statName" class="analysis-bar">
                  <span>{{ item.statName }}</span>
                  <div><em :style="{ width: scoreWidth(item.score) }"></em></div>
                  <strong>{{ formatScore(item.score) }}</strong>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="analysis-panel">
                <div class="analysis-title">城市</div>
                <div v-for="item in satisfactionAnalytics.regionStats || []" :key="'region-' + item.statName" class="analysis-bar">
                  <span>{{ item.statName }}</span>
                  <div><em :style="{ width: scoreWidth(item.score) }"></em></div>
                  <strong>{{ formatScore(item.score) }}</strong>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="12" class="analysis-row">
            <el-col :span="12">
              <div class="analysis-panel">
                <div class="analysis-title">行业</div>
                <div v-for="item in satisfactionAnalytics.industryStats || []" :key="'industry-' + item.statName" class="analysis-bar">
                  <span>{{ item.statName }}</span>
                  <div><em :style="{ width: scoreWidth(item.score) }"></em></div>
                  <strong>{{ formatScore(item.score) }}</strong>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="analysis-panel">
                <div class="analysis-title">企业规模</div>
                <div v-for="item in satisfactionAnalytics.scaleStats || []" :key="'scale-' + item.statName" class="analysis-bar">
                  <span>{{ item.statName }}</span>
                  <div><em :style="{ width: scoreWidth(item.score) }"></em></div>
                  <strong>{{ formatScore(item.score) }}</strong>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-table :data="distributionRows" class="distribution-table" max-height="300">
            <el-table-column label="题目" prop="questionTitle" min-width="220" show-overflow-tooltip />
            <el-table-column label="选项/状态" prop="itemLabel" min-width="140" />
            <el-table-column label="数量" prop="count" width="90" align="center" />
            <el-table-column label="占比" prop="percent" width="100" align="center">
              <template slot-scope="scope">{{ formatRate(scope.row.percent) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="任务样本" name="samples">
          <el-table :data="detail.samples || []" max-height="360">
            <el-table-column label="企业名称" prop="enterpriseName" min-width="180" show-overflow-tooltip />
            <el-table-column label="城市" prop="regionName" width="130" />
            <el-table-column label="行业" prop="industryCategory" width="150" />
            <el-table-column label="规模" prop="enterpriseScale" width="100" />
            <el-table-column label="状态" prop="status" width="80">
              <template slot-scope="scope">{{ sampleStatusText(scope.row.status) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-download" @click="downloadQrCode(scope.row)">二维码</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="发送记录" name="dispatch-records">
          <el-table :data="detail.sendRecords || []" max-height="360">
            <el-table-column label="渠道" prop="channel" width="90">
              <template slot-scope="scope">{{ channelText(scope.row.channel) }}</template>
            </el-table-column>
            <el-table-column label="接收人" prop="receiver" width="150" show-overflow-tooltip />
            <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
            <el-table-column label="发送状态" prop="sendStatus" width="100">
              <template slot-scope="scope">{{ sendStatusText(scope.row.sendStatus) }}</template>
            </el-table-column>
            <el-table-column label="填报状态" prop="submitStatus" width="100">
              <template slot-scope="scope">{{ submitStatusText(scope.row.submitStatus) }}</template>
            </el-table-column>
            <el-table-column label="填报时间" prop="recoveryTime" width="170">
              <template slot-scope="scope">{{ parseTime(scope.row.recoveryTime) }}</template>
            </el-table-column>
            <el-table-column label="生成时间" prop="createTime" width="170">
              <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { saveAs } from 'file-saver'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { listQuestionnaire } from '@/api/survey/questionnaire'
import { listEnterprise, groupTreeSelect } from '@/api/survey/enterprise'
import {
  listTask,
  getTask,
  getTaskTrackingSummary,
  listTaskTrackingRegions,
  listTaskTrackingDetails,
  listTaskTrackingTrend,
  getTaskSatisfactionAnalytics,
  exportTaskSatisfactionReport,
  addTask,
  dispatchTask,
  delTask,
  getTaskQrCode
} from '@/api/survey/task'

const HUNAN_CITY_OPTIONS = [
  { code: '430100', name: '长沙市' },
  { code: '430200', name: '株洲市' },
  { code: '430300', name: '湘潭市' },
  { code: '430400', name: '衡阳市' },
  { code: '430500', name: '邵阳市' },
  { code: '430600', name: '岳阳市' },
  { code: '430700', name: '常德市' },
  { code: '430800', name: '张家界市' },
  { code: '430900', name: '益阳市' },
  { code: '431000', name: '郴州市' },
  { code: '431100', name: '永州市' },
  { code: '431200', name: '怀化市' },
  { code: '431300', name: '娄底市' },
  { code: '433100', name: '湘西土家族苗族自治州' }
]

const SOURCE_OPTIONS = [
  { label: '全部企业', value: 'all' },
  { label: '企业分组', value: 'group' },
  { label: '指定企业', value: 'enterprise' }
]

const SAMPLING_METHODS = [
  { label: '随机抽样', value: 'random' },
  { label: '分层抽样', value: 'stratified' },
  { label: '指定企业', value: 'specified' }
]

export default {
  name: 'SurveyTask',
  props: {
    entryMode: {
      type: String,
      default: 'manage'
    }
  },
  components: { Treeselect },
  data() {
    return {
      SOURCE_OPTIONS,
      SAMPLING_METHODS,
      HUNAN_CITY_OPTIONS,
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      taskList: [],
      entryBootstrapped: false,
      questionnaireOptions: [],
      enterpriseOptions: [],
      groupOptions: [],
      open: false,
      detailOpen: false,
      activeDetailTab: 'tracking-detail',
      title: '',
      detail: {},
      trackingSummary: {},
      trackingRegions: [],
      trackingTrend: [],
      trackingDetails: [],
      trackingDetailTotal: 0,
      trackingQuery: this.emptyTrackingQuery(),
      satisfactionAnalytics: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskName: undefined,
        status: undefined
      },
      statusOptions: [
        { label: '已抽样', value: '1' },
        { label: '已发卷', value: '2' },
        { label: '已结束', value: '3' }
      ],
      form: this.emptyForm(),
      rules: {
        taskName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
        questionnaireId: [{ required: true, message: '可用问卷不能为空', trigger: 'change' }],
        sampleSource: [{ required: true, message: '对象池来源不能为空', trigger: 'change' }],
        samplingMethod: [{ required: true, message: '抽样方式不能为空', trigger: 'change' }]
      }
    }
  },
  computed: {
    distributionRows() {
      const rows = []
      const questions = this.satisfactionAnalytics.questionDistributions || []
      questions.forEach(question => {
        const items = question.items || []
        items.forEach(item => {
          rows.push({
            questionTitle: question.questionTitle,
            itemLabel: item.itemLabel,
            count: item.count,
            percent: item.percent
          })
        })
      })
      return rows
    },
    isSampleEntry() {
      return this.entryMode === 'sample'
    },
    isTrackingEntry() {
      return this.entryMode === 'tracking'
    },
    isAnalyticsEntry() {
      return this.entryMode === 'analytics'
    },
    routeTitle() {
      if (this.isSampleEntry) return '样本抽取与发卷'
      if (this.isTrackingEntry) return '填报追踪'
      if (this.isAnalyticsEntry) return '满意度统计分析'
      return ''
    },
    showAddAction() {
      return !this.isTrackingEntry && !this.isAnalyticsEntry
    },
    showDeleteAction() {
      return !this.isTrackingEntry && !this.isAnalyticsEntry
    },
    showDispatchAction() {
      return !this.isTrackingEntry && !this.isAnalyticsEntry
    },
    addButtonText() {
      return this.isSampleEntry ? '新增抽样任务' : '新增'
    },
    detailButtonText() {
      if (this.isTrackingEntry) return '追踪'
      if (this.isAnalyticsEntry) return '分析'
      return '详情'
    }
  },
  created() {
    this.getList()
    this.loadOptions()
  },
  methods: {
    getList() {
      this.loading = true
      listTask(this.queryParams).then(response => {
        this.taskList = response.rows || []
        this.total = response.total || 0
        this.loading = false
        this.bootstrapEntryMode()
      })
    },
    bootstrapEntryMode() {
      if (this.entryBootstrapped) {
        return
      }
      if (this.isSampleEntry) {
        this.entryBootstrapped = true
        this.handleAdd()
        return
      }
      if ((this.isTrackingEntry || this.isAnalyticsEntry) && this.routeTaskId()) {
        this.entryBootstrapped = true
        this.openDetailByTaskId(this.routeTaskId())
        return
      }
      if ((this.isTrackingEntry || this.isAnalyticsEntry) && this.taskList.length) {
        this.entryBootstrapped = true
        this.openDetail(this.taskList[0])
        return
      }
      if (!this.isTrackingEntry && !this.isAnalyticsEntry) {
        this.entryBootstrapped = true
      }
    },
    loadOptions() {
      Promise.all([
        listQuestionnaire({ status: '1', pageNum: 1, pageSize: 200 }),
        listQuestionnaire({ status: '3', pageNum: 1, pageSize: 200 })
      ]).then(([publishedResponse, collectingResponse]) => {
        const merged = [...(publishedResponse.rows || []), ...(collectingResponse.rows || [])]
        const seen = new Set()
        this.questionnaireOptions = merged.filter(item => {
          if (seen.has(item.questionnaireId)) {
            return false
          }
          seen.add(item.questionnaireId)
          return true
        })
      })
      listEnterprise({ pageNum: 1, pageSize: 500, status: '0' }).then(response => {
        this.enterpriseOptions = response.rows || []
      })
      groupTreeSelect().then(response => {
        this.groupOptions = response.data || []
      })
    },
    routeTaskId() {
      const value = Number(this.$route.query.taskId)
      return Number.isInteger(value) && value > 0 ? value : null
    },
    openDetailByTaskId(taskId) {
      this.activeDetailTab = this.isAnalyticsEntry ? 'analytics' : 'tracking-detail'
      getTask(taskId).then(response => {
        const detail = response.data || {}
        if (!detail.taskId) {
          this.openFirstEntryTask()
          return
        }
        this.detail = detail
        this.loadTracking(taskId)
        this.detailOpen = true
      }).catch(() => {
        this.openFirstEntryTask()
      })
    },
    openFirstEntryTask() {
      if (this.taskList.length) {
        this.openDetail(this.taskList[0])
      }
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
      this.ids = selection.map(item => item.taskId)
      this.multiple = !selection.length
    },
    handleAdd() {
      this.form = this.emptyForm()
      this.title = this.isSampleEntry ? '样本抽取任务' : '新增调研任务'
      this.open = true
    },
    openDetail(row) {
      this.activeDetailTab = this.isAnalyticsEntry ? 'analytics' : 'tracking-detail'
      this.handleDetail(row)
    },
    handleSourceChange(value) {
      if (value === 'enterprise') {
        this.form.samplingMethod = 'specified'
        this.form.sampleSize = undefined
      } else {
        this.form.enterpriseIds = []
        if (this.form.samplingMethod === 'specified') {
          this.form.samplingMethod = 'random'
        }
      }
      if (value !== 'group') {
        this.form.groupId = undefined
      }
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        addTask(this.form).then(() => {
          this.$modal.msgSuccess('任务样本已生成')
          this.open = false
          this.getList()
        })
      })
    },
    handleDetail(row) {
      getTask(row.taskId).then(response => {
        this.detail = response.data || {}
        this.loadTracking(row.taskId)
        this.detailOpen = true
      })
    },
    loadTracking(taskId) {
      this.trackingSummary = {}
      this.trackingRegions = []
      this.trackingTrend = []
      this.trackingDetails = []
      this.trackingDetailTotal = 0
      this.trackingQuery = this.emptyTrackingQuery()
      getTaskTrackingSummary(taskId).then(response => {
        this.trackingSummary = response.data || {}
      })
      listTaskTrackingRegions(taskId).then(response => {
        this.trackingRegions = response.data || []
      })
      listTaskTrackingTrend(taskId).then(response => {
        this.trackingTrend = response.data || []
      })
      this.loadSatisfaction(taskId)
      this.getTrackingDetails()
    },
    loadSatisfaction(taskId) {
      this.satisfactionAnalytics = {}
      getTaskSatisfactionAnalytics(taskId).then(response => {
        this.satisfactionAnalytics = response.data || {}
      })
    },
    getTrackingDetails() {
      if (!this.detail.taskId) {
        return
      }
      listTaskTrackingDetails(this.detail.taskId, this.trackingQuery).then(response => {
        this.trackingDetails = response.rows || []
        this.trackingDetailTotal = response.total || 0
      })
    },
    handleTrackingQuery() {
      this.trackingQuery.pageNum = 1
      this.getTrackingDetails()
    },
    resetTrackingQuery() {
      this.trackingQuery = this.emptyTrackingQuery()
      this.getTrackingDetails()
    },
    handleDispatch(row) {
      this.$modal.confirm('确认生成短信和站内信发送记录？').then(() => {
        return dispatchTask(row.taskId)
      }).then(() => {
        this.$modal.msgSuccess('发送记录已生成')
        this.getList()
      })
    },
    handleDelete(row) {
      const taskIds = row.taskId || this.ids
      this.$modal.confirm('确认删除选中的调研任务？').then(() => {
        return delTask(taskIds)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    },
    downloadQrCode(row) {
      getTaskQrCode(row.sampleId).then(data => {
        const blob = new Blob([data], { type: 'image/png' })
        saveAs(blob, 'survey-qrcode-' + row.sampleId + '.png')
      })
    },
    exportSatisfactionReport() {
      if (!this.detail.taskId) {
        return
      }
      exportTaskSatisfactionReport(this.detail.taskId).then(data => {
        const blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' })
        saveAs(blob, '满意度统计分析报告-' + this.detail.taskId + '.docx')
      })
    },
    emptyForm() {
      return {
        taskName: '',
        questionnaireId: undefined,
        sampleSource: 'all',
        samplingMethod: 'random',
        sampleSize: 10,
        groupId: undefined,
        enterpriseIds: [],
        sendChannels: ['sms', 'site'],
        remark: ''
      }
    },
    emptyTrackingQuery() {
      return {
        pageNum: 1,
        pageSize: 10,
        enterpriseName: undefined,
        regionName: undefined,
        industryCategory: undefined,
        enterpriseScale: undefined,
        submitStatus: undefined
      }
    },
    sourceText(value) {
      const item = SOURCE_OPTIONS.find(option => option.value === value)
      return item ? item.label : value
    },
    samplingText(value) {
      const item = SAMPLING_METHODS.find(option => option.value === value)
      return item ? item.label : value
    },
    statusText(value) {
      const item = this.statusOptions.find(option => option.value === value)
      return item ? item.label : value
    },
    statusTag(value) {
      return value === '2' ? 'success' : value === '3' ? 'info' : 'warning'
    },
    sampleStatusText(value) {
      return value === '1' ? '已发送' : value === '2' ? '已填报' : value === '3' ? '已失效' : '待发送'
    },
    sendStatusText(value) {
      return value === '0' ? '已生成' : value === '1' ? '已发送' : value || '-'
    },
    submitStatusText(value) {
      return value === '1' ? '已填报' : '未填报'
    },
    formatRate(value) {
      return `${Number(value || 0).toFixed(2)}%`
    },
    formatScore(value) {
      return Number(value || 0).toFixed(2)
    },
    scoreWidth(value) {
      return `${Math.max(4, Math.min(100, Number(value || 0)))}%`
    },
    trendWidth(value) {
      const max = Math.max(...this.trackingTrend.map(item => Number(item.submittedCount || 0)), 0)
      if (!max) {
        return '0'
      }
      return `${Math.max(8, Number(value || 0) * 100 / max)}%`
    },
    channelText(value) {
      return value === 'sms' ? '短信' : value === 'site' ? '站内信' : value
    }
  }
}
</script>

<style scoped>
.entry-title {
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.survey-task-page .form-suffix {
  color: #606266;
  margin-left: 8px;
}

.detail-tabs {
  margin-top: 14px;
}

.tracking-summary {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 12px;
}

.tracking-metric {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
}

.tracking-metric span {
  color: #909399;
  display: block;
  font-size: 12px;
  margin-bottom: 6px;
}

.tracking-metric strong {
  color: #303133;
  display: block;
  font-size: 22px;
  line-height: 28px;
}

.tracking-filter {
  margin-bottom: 10px;
}

.trend-bar {
  background: #f2f6fc;
  border-radius: 4px;
  height: 10px;
  overflow: hidden;
}

.trend-bar span {
  background: #409eff;
  display: block;
  height: 100%;
}

.satisfaction-head {
  align-items: stretch;
  display: grid;
  gap: 10px;
  grid-template-columns: 180px 180px 160px;
  margin-bottom: 12px;
}

.satisfaction-head .el-button {
  align-self: center;
}

.analysis-row {
  margin-top: 12px;
}

.analysis-panel {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 150px;
  padding: 12px;
}

.analysis-title {
  color: #303133;
  font-weight: 600;
  margin-bottom: 10px;
}

.analysis-bar {
  align-items: center;
  display: grid;
  gap: 8px;
  grid-template-columns: minmax(90px, 150px) 1fr 62px;
  margin: 8px 0;
}

.analysis-bar span {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.analysis-bar div {
  background: #f2f6fc;
  border-radius: 4px;
  height: 10px;
  overflow: hidden;
}

.analysis-bar em {
  background: #67c23a;
  display: block;
  height: 100%;
}

.analysis-bar strong {
  color: #303133;
  font-weight: 600;
  text-align: right;
}

.distribution-table {
  margin-top: 12px;
}
</style>
