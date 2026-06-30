import request from '@/utils/request'

export function listResource(query) {
  return request({
    url: '/nocontact/fusion/resource/list',
    method: 'get',
    params: query
  })
}

export function getResource(resourceId) {
  return request({
    url: '/nocontact/fusion/resource/' + resourceId,
    method: 'get'
  })
}

export function addResource(data) {
  return request({
    url: '/nocontact/fusion/resource',
    method: 'post',
    data
  })
}

export function updateResource(data) {
  return request({
    url: '/nocontact/fusion/resource',
    method: 'put',
    data
  })
}

export function delResource(resourceId) {
  return request({
    url: '/nocontact/fusion/resource/' + resourceId,
    method: 'delete'
  })
}
