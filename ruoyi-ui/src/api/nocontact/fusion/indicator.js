import request from '@/utils/request'

export function listIndicator(query) {
  return request({
    url: '/nocontact/fusion/indicator/list',
    method: 'get',
    params: query
  })
}

export function listIndicatorOptions(query) {
  return request({
    url: '/nocontact/fusion/indicator/options',
    method: 'get',
    params: query
  })
}

export function getIndicator(indicatorId) {
  return request({
    url: '/nocontact/fusion/indicator/' + indicatorId,
    method: 'get'
  })
}

export function addIndicator(data) {
  return request({
    url: '/nocontact/fusion/indicator',
    method: 'post',
    data: data
  })
}

export function updateIndicator(data) {
  return request({
    url: '/nocontact/fusion/indicator',
    method: 'put',
    data: data
  })
}

export function copyIndicatorDraft(indicatorId) {
  return request({
    url: '/nocontact/fusion/indicator/' + indicatorId + '/copyDraft',
    method: 'post'
  })
}

export function delIndicator(indicatorId) {
  return request({
    url: '/nocontact/fusion/indicator/' + indicatorId,
    method: 'delete'
  })
}
