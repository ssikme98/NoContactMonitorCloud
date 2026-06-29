import request from '@/utils/request'

export function listAlgorithms() {
  return request({
    url: '/nocontact/fusion/indicator/workbench/algorithms',
    method: 'get'
  })
}

export function simulateAlgorithm(data) {
  return request({
    url: '/nocontact/fusion/indicator/workbench/simulate',
    method: 'post',
    data
  })
}

export function listTags(query) {
  return request({
    url: '/nocontact/fusion/indicator/workbench/tag/list',
    method: 'get',
    params: query
  })
}

export function addTag(data) {
  return request({
    url: '/nocontact/fusion/indicator/workbench/tag',
    method: 'post',
    data
  })
}

export function updateTag(data) {
  return request({
    url: '/nocontact/fusion/indicator/workbench/tag',
    method: 'put',
    data
  })
}

export function delTag(tagId) {
  return request({
    url: '/nocontact/fusion/indicator/workbench/tag/' + tagId,
    method: 'delete'
  })
}
