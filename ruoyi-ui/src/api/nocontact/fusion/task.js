import request from '@/utils/request'

export function listTask(query) {
  return request({
    url: '/nocontact/fusion/task/list',
    method: 'get',
    params: query
  })
}

export function getTask(taskId) {
  return request({
    url: '/nocontact/fusion/task/' + taskId,
    method: 'get'
  })
}

export function getTaskSummary() {
  return request({
    url: '/nocontact/fusion/task/summary',
    method: 'get'
  })
}

export function addTask(data) {
  return request({
    url: '/nocontact/fusion/task',
    method: 'post',
    data: data
  })
}

export function updateTask(data) {
  return request({
    url: '/nocontact/fusion/task',
    method: 'put',
    data: data
  })
}

export function updateTaskStatus(taskId, status) {
  return request({
    url: '/nocontact/fusion/task/' + taskId + '/status/' + status,
    method: 'put'
  })
}

export function delTask(taskId) {
  return request({
    url: '/nocontact/fusion/task/' + taskId,
    method: 'delete'
  })
}
