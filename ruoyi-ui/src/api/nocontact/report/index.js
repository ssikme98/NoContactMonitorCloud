import request from '@/utils/request'

export function listTemplate(query) {
  return request({
    url: '/nocontact/report/template/list',
    method: 'get',
    params: query
  })
}

export function addTemplate(data) {
  return request({
    url: '/nocontact/report/template',
    method: 'post',
    data
  })
}

export function updateTemplate(data) {
  return request({
    url: '/nocontact/report/template',
    method: 'put',
    data
  })
}

export function delTemplate(templateId) {
  return request({
    url: '/nocontact/report/template/' + templateId,
    method: 'delete'
  })
}

export function listReportTask(query) {
  return request({
    url: '/nocontact/report/task/list',
    method: 'get',
    params: query
  })
}

export function addReportTask(data) {
  return request({
    url: '/nocontact/report/task',
    method: 'post',
    data
  })
}

export function generateReportTask(taskId) {
  return request({
    url: '/nocontact/report/task/' + taskId + '/generate',
    method: 'put'
  })
}

export function listReportSnapshot(taskId) {
  return request({
    url: '/nocontact/report/task/' + taskId + '/snapshot/list',
    method: 'get'
  })
}

export function reportTaskDownloadUrl(taskId, fileType) {
  return '/nocontact/report/task/' + taskId + '/download/' + fileType
}

export function reportSnapshotDownloadUrl(snapshotId, fileType) {
  return '/nocontact/report/task/snapshot/' + snapshotId + '/download/' + fileType
}
