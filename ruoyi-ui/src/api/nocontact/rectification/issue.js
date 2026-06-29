import request from '@/utils/request'

export function listIssue(query) {
  return request({
    url: '/nocontact/rectification/issue/list',
    method: 'get',
    params: query
  })
}

export function getIssue(issueId) {
  return request({
    url: '/nocontact/rectification/issue/' + issueId,
    method: 'get'
  })
}

export function getIssueDashboard() {
  return request({
    url: '/nocontact/rectification/issue/dashboard',
    method: 'get'
  })
}

export function addIssue(data) {
  return request({
    url: '/nocontact/rectification/issue',
    method: 'post',
    data
  })
}

export function updateIssue(data) {
  return request({
    url: '/nocontact/rectification/issue',
    method: 'put',
    data
  })
}

export function dispatchIssue(issueId, data) {
  return request({
    url: '/nocontact/rectification/issue/' + issueId + '/dispatch',
    method: 'put',
    data
  })
}

export function submitIssue(issueId, data) {
  return request({
    url: '/nocontact/rectification/issue/' + issueId + '/submit',
    method: 'put',
    data
  })
}

export function reviewIssue(issueId, approved, data) {
  return request({
    url: '/nocontact/rectification/issue/' + issueId + '/review/' + approved,
    method: 'put',
    data
  })
}

export function archiveIssue(issueId) {
  return request({
    url: '/nocontact/rectification/issue/' + issueId + '/archive',
    method: 'put'
  })
}

export function delIssue(issueId) {
  return request({
    url: '/nocontact/rectification/issue/' + issueId,
    method: 'delete'
  })
}
