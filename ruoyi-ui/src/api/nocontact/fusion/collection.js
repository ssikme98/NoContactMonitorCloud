import request from '@/utils/request'

export function listCollection(query) {
  return request({
    url: '/nocontact/fusion/collection/list',
    method: 'get',
    params: query
  })
}

export function getCollection(batchId) {
  return request({
    url: '/nocontact/fusion/collection/' + batchId,
    method: 'get'
  })
}

export function getCollectionSummary() {
  return request({
    url: '/nocontact/fusion/collection/summary',
    method: 'get'
  })
}

export function listImportFailures(query) {
  return request({
    url: '/nocontact/fusion/collection/importFailures',
    method: 'get',
    params: query
  })
}

export function submitCollection(data) {
  return request({
    url: '/nocontact/fusion/collection/submit',
    method: 'post',
    data: data
  })
}

export function approveCollection(batchId, opinion) {
  return request({
    url: '/nocontact/fusion/collection/' + batchId + '/approve',
    method: 'put',
    params: { opinion }
  })
}

export function rejectCollection(batchId, opinion) {
  return request({
    url: '/nocontact/fusion/collection/' + batchId + '/reject',
    method: 'put',
    params: { opinion }
  })
}
