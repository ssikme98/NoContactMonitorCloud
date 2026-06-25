import request from '@/utils/request'

// 查询调研任务列表
export function listTask(query) {
  return request({
    url: '/survey/task/list',
    method: 'get',
    params: query
  })
}

// 查询调研任务详细
export function getTask(taskId) {
  return request({
    url: '/survey/task/' + taskId,
    method: 'get'
  })
}

// 查询调研任务填报汇总
export function getTaskTrackingSummary(taskId) {
  return request({
    url: '/survey/task/' + taskId + '/track/summary',
    method: 'get'
  })
}

// 查询调研任务地区回收统计
export function listTaskTrackingRegions(taskId) {
  return request({
    url: '/survey/task/' + taskId + '/track/regions',
    method: 'get'
  })
}

// 查询调研任务企业级填报明细
export function listTaskTrackingDetails(taskId, query) {
  return request({
    url: '/survey/task/' + taskId + '/track/details',
    method: 'get',
    params: query
  })
}

// 查询调研任务回收趋势
export function listTaskTrackingTrend(taskId) {
  return request({
    url: '/survey/task/' + taskId + '/track/trend',
    method: 'get'
  })
}

// 查询满意度统计分析
export function getTaskSatisfactionAnalytics(taskId) {
  return request({
    url: '/survey/task/' + taskId + '/analytics/satisfaction',
    method: 'get'
  })
}

// 导出满意度Word报告
export function exportTaskSatisfactionReport(taskId) {
  return request({
    url: '/survey/task/' + taskId + '/analytics/report',
    method: 'get',
    responseType: 'blob'
  })
}

// 新增调研任务并生成样本
export function addTask(data) {
  return request({
    url: '/survey/task',
    method: 'post',
    data: data
  })
}

// 模拟发卷
export function dispatchTask(taskId) {
  return request({
    url: '/survey/task/' + taskId + '/dispatch',
    method: 'post'
  })
}

// 删除调研任务
export function delTask(taskId) {
  return request({
    url: '/survey/task/' + taskId,
    method: 'delete'
  })
}

// 下载样本二维码
export function getTaskQrCode(sampleId) {
  return request({
    url: '/survey/task/sample/' + sampleId + '/qrcode',
    method: 'get',
    responseType: 'blob'
  })
}
