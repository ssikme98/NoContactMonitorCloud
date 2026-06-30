<template>
  <div class="app-container tree-sidebar-manage-wrap">
    <tree-panel title="企业分组" :tree-data="groupOptions" search-placeholder="请输入分组名称" storage-key="survey-enterprise-group-width" :defaultExpandAll="true" @node-click="handleGroupNodeClick" @refresh="getGroupTree" ref="groupTreeRef">
      <template slot="actions">
        <el-tooltip content="新增分组" placement="right">
          <i class="tree-action-icon el-icon-plus" @click.stop="handleGroupAdd()" />
        </el-tooltip>
        <el-tooltip content="修改分组" placement="right">
          <i class="tree-action-icon el-icon-edit" @click.stop="handleGroupUpdate()" />
        </el-tooltip>
        <el-tooltip content="删除分组" placement="right">
          <i class="tree-action-icon el-icon-delete" @click.stop="handleGroupDelete()" />
        </el-tooltip>
      </template>
      <template slot="node" slot-scope="{ node, data }">
        <span class="tree-node survey-group-node">
          <i :class="data.children && data.children.length ? 'el-icon-folder' : 'el-icon-document'" class="node-icon" />
          <span class="node-label" :title="node.label">{{ node.label }}</span>
          <span class="survey-group-actions">
            <i class="group-action-icon el-icon-plus" @click.stop="handleGroupAdd(data)" />
            <i class="group-action-icon el-icon-edit" @click.stop="handleGroupUpdate(data)" />
            <i class="group-action-icon el-icon-delete" @click.stop="handleGroupDelete(data)" />
          </span>
        </span>
      </template>
    </tree-panel>

    <div class="tree-sidebar-content">
      <div class="content-inner">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="96px">
          <el-form-item label="企业名称" prop="enterpriseName">
            <el-input v-model="queryParams.enterpriseName" placeholder="请输入企业名称" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="信用代码" prop="creditCode">
            <el-input v-model="queryParams.creditCode" placeholder="请输入统一社会信用代码" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="行业分类" prop="industryCategory">
            <el-input v-model="queryParams.industryCategory" placeholder="请输入行业分类" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="城市" prop="regionName">
            <el-select v-model="queryParams.regionName" placeholder="请选择城市" clearable filterable @change="handleQuery">
              <el-option v-for="item in HUNAN_CITY_OPTIONS" :key="item.code" :label="item.name" :value="item.name" />
            </el-select>
          </el-form-item>
          <el-form-item label="企业规模" prop="enterpriseScale">
            <el-select v-model="queryParams.enterpriseScale" placeholder="请选择企业规模" clearable>
              <el-option label="大型" value="大型" />
              <el-option label="中型" value="中型" />
              <el-option label="小型" value="小型" />
              <el-option label="微型" value="微型" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['survey:enterprise:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['survey:enterprise:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['survey:enterprise:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['survey:enterprise:import']">导入</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['survey:enterprise:export']">导出</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="enterpriseList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="企业名称" align="center" prop="enterpriseName" min-width="180" show-overflow-tooltip />
          <el-table-column label="统一社会信用代码" align="center" prop="creditCode" width="190">
            <template slot-scope="scope">{{ maskCreditCode(scope.row.creditCode) }}</template>
          </el-table-column>
          <el-table-column label="行业分类" align="center" prop="industryCategory" width="130" />
          <el-table-column label="城市" align="center" prop="regionName" width="130" />
          <el-table-column label="企业规模" align="center" prop="enterpriseScale" width="100" />
          <el-table-column label="联系人" align="center" prop="contactName" width="100" />
          <el-table-column label="联系电话" align="center" prop="contactPhone" width="130">
            <template slot-scope="scope">{{ maskPhone(scope.row.contactPhone) }}</template>
          </el-table-column>
          <el-table-column label="详细地址" align="center" prop="address" min-width="220" show-overflow-tooltip />
          <el-table-column label="经度" align="center" prop="longitude" width="110" />
          <el-table-column label="纬度" align="center" prop="latitude" width="110" />
          <el-table-column label="状态" align="center" prop="status" width="80">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.status === '0'" size="mini">正常</el-tag>
              <el-tag v-else type="info" size="mini">停用</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['survey:enterprise:edit']">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['survey:enterprise:remove']">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <el-dialog :title="title" :visible.sync="open" width="640px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="企业名称" prop="enterpriseName">
          <el-input v-model="form.enterpriseName" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="信用代码" prop="creditCode">
          <el-input v-model="form.creditCode" placeholder="请输入18位统一社会信用代码" maxlength="18" />
        </el-form-item>
        <el-form-item label="所属分组" prop="groupIds">
          <treeselect v-model="form.groupIds" :multiple="true" :options="enabledGroupOptions" :show-count="true" placeholder="请选择所属分组" />
        </el-form-item>
        <el-form-item label="行业分类" prop="industryCategory">
          <el-input v-model="form.industryCategory" placeholder="请输入行业分类" />
        </el-form-item>
        <el-form-item label="城市" prop="regionName">
          <el-select v-model="form.regionName" placeholder="请选择城市" filterable clearable @change="handleCityChange">
            <el-option v-for="item in HUNAN_CITY_OPTIONS" :key="item.code" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="企业规模" prop="enterpriseScale">
          <el-select v-model="form.enterpriseScale" placeholder="请选择企业规模" clearable>
            <el-option label="大型" value="大型" />
            <el-option label="中型" value="中型" />
            <el-option label="小型" value="小型" />
            <el-option label="微型" value="微型" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model="form.longitude" placeholder="请输入经度" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model="form.latitude" placeholder="请输入纬度" />
        </el-form-item>
        <el-form-item label="坐标操作">
          <el-button size="mini" type="primary" @click="handleGeocode">地址解析</el-button>
          <span class="location-tip">{{ form.geocodeMessage || '支持地址解析，失败后可手工维护坐标' }}</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="位置预览">
          <enterprise-location-map
            :name="form.enterpriseName"
            :longitude="form.longitude"
            :latitude="form.latitude"
            @manual="handleManualLocation"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="groupTitle" :visible.sync="groupOpen" width="520px" append-to-body>
      <el-form ref="groupForm" :model="groupForm" :rules="groupRules" label-width="100px">
        <el-form-item label="上级分组" prop="parentId">
          <treeselect v-model="groupForm.parentId" :options="groupTreeWithRoot" :show-count="true" placeholder="请选择上级分组" />
        </el-form-item>
        <el-form-item label="分组名称" prop="groupName">
          <el-input v-model="groupForm.groupName" placeholder="请输入分组名称" maxlength="80" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="groupForm.orderNum" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="groupForm.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="groupForm.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitGroupForm">确 定</el-button>
        <el-button @click="groupCancel">取 消</el-button>
      </div>
    </el-dialog>

    <excel-import-dialog ref="importEnterpriseRef" title="企业导入" action="/nocontact/enterprise/importData" template-action="/nocontact/enterprise/importTemplate" template-file-name="enterprise_template" update-support-label="是否更新统一社会信用代码已存在的数据" @success="getList" />
  </div>
</template>

<script>
import {
  listEnterprise,
  getEnterprise,
  delEnterprise,
  addEnterprise,
  updateEnterprise,
  geocodeEnterpriseLocation,
  groupTreeSelect,
  getEnterpriseGroup,
  addEnterpriseGroup,
  updateEnterpriseGroup,
  delEnterpriseGroup
} from '@/api/nocontact/enterprise'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import TreePanel from '@/components/TreePanel'
import ExcelImportDialog from '@/components/ExcelImportDialog'
import EnterpriseLocationMap from '@/components/amap/EnterpriseLocationMap'
import { geocodeWithAmapJs } from '@/config/amap'

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

export default {
  name: 'SurveyEnterprise',
  components: { Treeselect, TreePanel, ExcelImportDialog, EnterpriseLocationMap },
  data() {
    return {
      HUNAN_CITY_OPTIONS,
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      enterpriseList: [],
      groupOptions: [],
      enabledGroupOptions: [],
      currentGroupId: undefined,
      currentGroupName: '',
      title: '',
      open: false,
      groupTitle: '',
      groupOpen: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        groupId: undefined,
        enterpriseName: undefined,
        creditCode: undefined,
        industryCategory: undefined,
        regionName: undefined,
        enterpriseScale: undefined
      },
      form: {},
      groupForm: {},
      rules: {
        enterpriseName: [
          { required: true, message: '企业名称不能为空', trigger: 'blur' }
        ],
        creditCode: [
          { required: true, message: '统一社会信用代码不能为空', trigger: 'blur' },
          { pattern: /^[0-9A-Z]{18}$/, message: '统一社会信用代码必须为18位数字或大写字母', trigger: 'blur' }
        ]
      },
      groupRules: {
        parentId: [
          { required: true, message: '上级分组不能为空', trigger: 'change' }
        ],
        groupName: [
          { required: true, message: '分组名称不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    groupTreeWithRoot() {
      return [
        {
          id: 0,
          label: '根分组',
          children: this.enabledGroupOptions
        }
      ]
    }
  },
  created() {
    this.getList()
    this.getGroupTree()
  },
  methods: {
    getList() {
      this.loading = true
      listEnterprise(this.queryParams).then(response => {
        this.enterpriseList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getGroupTree() {
      groupTreeSelect().then(response => {
        const data = response.data || []
        this.groupOptions = data
        this.enabledGroupOptions = this.filterDisabledGroup(JSON.parse(JSON.stringify(data)))
      })
    },
    filterDisabledGroup(groupList) {
      return groupList.filter(group => {
        if (group.disabled) {
          return false
        }
        if (group.children && group.children.length) {
          group.children = this.filterDisabledGroup(group.children)
        }
        return true
      })
    },
    handleGroupNodeClick(data) {
      this.currentGroupId = data.id
      this.currentGroupName = data.label
      this.queryParams.groupId = data.id === 0 ? undefined : data.id
      this.handleQuery()
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        enterpriseId: undefined,
        enterpriseName: undefined,
        creditCode: undefined,
        groupIds: [],
        industryCategory: undefined,
        regionCode: undefined,
        regionName: undefined,
        enterpriseScale: undefined,
        contactName: undefined,
        contactPhone: undefined,
        address: undefined,
        longitude: undefined,
        latitude: undefined,
        geocodeStatus: undefined,
        geocodeMessage: undefined,
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    resetGroup() {
      this.groupForm = {
        groupId: undefined,
        parentId: 0,
        groupName: undefined,
        orderNum: 0,
        status: '0',
        remark: undefined
      }
      this.resetForm('groupForm')
    },
    groupCancel() {
      this.groupOpen = false
      this.resetGroup()
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.queryParams.groupId = undefined
      this.currentGroupId = undefined
      this.currentGroupName = ''
      if (this.$refs.groupTreeRef) {
        this.$refs.groupTreeRef.setCurrentKey(null)
      }
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.enterpriseId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      if (this.currentGroupId !== undefined) {
        this.form.groupIds = [this.currentGroupId]
      }
      this.open = true
      this.title = '新增企业'
    },
    handleUpdate(row) {
      this.reset()
      const enterpriseId = row.enterpriseId || this.ids[0]
      getEnterprise(enterpriseId).then(response => {
        this.form = response.data
        if (!this.form.groupIds) {
          this.$set(this.form, 'groupIds', [])
        }
        this.open = true
        this.title = '修改企业'
      })
    },
    handleCityChange(regionName) {
      const city = HUNAN_CITY_OPTIONS.find(item => item.name === regionName)
      this.form.regionCode = city ? city.code : undefined
    },
    handleGeocode() {
      if (!this.form.regionName || !this.form.address) {
        this.$modal.msgWarning('请先填写城市和详细地址')
        return
      }
      geocodeEnterpriseLocation(this.form.regionName, this.form.address).then(response => {
        const data = response.data || {}
        if (response.code === 200 && data.longitude && data.latitude) {
          this.form.longitude = data.longitude
          this.form.latitude = data.latitude
          this.form.geocodeStatus = 'success'
          this.form.geocodeMessage = '地址解析成功'
          return
        }
        return this.handleGeocodeFallback(response.msg)
      }).catch(error => {
        return this.handleGeocodeFallback((error && error.message) || '地址解析失败，请手工维护坐标')
      })
    },
    handleGeocodeFallback(message) {
      return geocodeWithAmapJs(this.form.regionName, this.form.address).then(result => {
        this.form.longitude = result.longitude
        this.form.latitude = result.latitude
        this.form.geocodeStatus = 'success'
        this.form.geocodeMessage = '地址解析成功（浏览器地图服务）'
      }).catch(() => {
        this.form.geocodeStatus = 'manual'
        this.form.geocodeMessage = message || '地址解析失败，请手工维护坐标'
      })
    },
    handleManualLocation() {
      this.form.geocodeStatus = 'manual'
      this.form.geocodeMessage = '已切换为手工维护坐标'
    },
    maskCreditCode(value) {
      if (!value) {
        return ''
      }
      const text = String(value)
      return text.length <= 8 ? text.replace(/.(?=.{2})/g, '*') : text.slice(0, 4) + '**********' + text.slice(-4)
    },
    maskPhone(value) {
      if (!value) {
        return ''
      }
      const text = String(value)
      return text.length >= 7 ? text.slice(0, 3) + '****' + text.slice(-4) : text.replace(/.(?=.{2})/g, '*')
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        if (this.form.enterpriseId !== undefined) {
          updateEnterprise(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
            this.getGroupTree()
          })
        } else {
          addEnterprise(this.form).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.open = false
            this.getList()
            this.getGroupTree()
          })
        }
      })
    },
    handleDelete(row) {
      const enterpriseIds = row.enterpriseId || this.ids
      this.$modal.confirm('是否确认删除选中的企业数据？').then(function() {
        return delEnterprise(enterpriseIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('nocontact/enterprise/export', {
        ...this.queryParams
      }, `enterprise_${new Date().getTime()}.xlsx`)
    },
    handleImport() {
      this.$refs.importEnterpriseRef.open()
    },
    handleGroupAdd(data) {
      this.resetGroup()
      this.groupForm.parentId = data && data.id !== undefined ? data.id : (this.currentGroupId || 0)
      this.groupOpen = true
      this.groupTitle = '新增分组'
    },
    handleGroupUpdate(data) {
      const groupId = data && data.id !== undefined ? data.id : this.currentGroupId
      if (groupId === undefined) {
        this.$modal.msgError('请选择分组')
        return
      }
      this.resetGroup()
      getEnterpriseGroup(groupId).then(response => {
        this.groupForm = response.data
        this.groupOpen = true
        this.groupTitle = '修改分组'
      })
    },
    submitGroupForm() {
      this.$refs['groupForm'].validate(valid => {
        if (!valid) {
          return
        }
        if (this.groupForm.groupId !== undefined && this.groupForm.parentId === this.groupForm.groupId) {
          this.$modal.msgError('上级分组不能是自己')
          return
        }
        if (this.groupForm.groupId !== undefined) {
          updateEnterpriseGroup(this.groupForm).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.groupOpen = false
            this.getGroupTree()
          })
        } else {
          addEnterpriseGroup(this.groupForm).then(() => {
            this.$modal.msgSuccess('新增成功')
            this.groupOpen = false
            this.getGroupTree()
          })
        }
      })
    },
    handleGroupDelete(data) {
      const groupId = data && data.id !== undefined ? data.id : this.currentGroupId
      const groupName = data && data.label ? data.label : this.currentGroupName
      if (groupId === undefined) {
        this.$modal.msgError('请选择分组')
        return
      }
      this.$modal.confirm('是否确认删除分组"' + groupName + '"？').then(function() {
        return delEnterpriseGroup(groupId)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        if (this.currentGroupId === groupId) {
          this.queryParams.groupId = undefined
          this.currentGroupId = undefined
          this.currentGroupName = ''
        }
        this.getGroupTree()
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.survey-group-node {
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 0;
}

.survey-group-node .node-label {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.survey-group-actions {
  display: inline-flex;
  gap: 4px;
  opacity: 0;
}

.survey-group-node:hover .survey-group-actions {
  opacity: 1;
}

.group-action-icon {
  padding: 2px;
  color: #606266;
  font-size: 13px;
}

.group-action-icon:hover {
  color: #409eff;
}

.location-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}
</style>
