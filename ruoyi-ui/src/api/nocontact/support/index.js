import request from '@/utils/request'

export function listBusinessMessage(query) {
  return request({
    url: '/nocontact/support/message/list',
    method: 'get',
    params: query
  })
}

export function markBusinessMessageRead(messageId) {
  return request({
    url: '/nocontact/support/message/' + messageId + '/read',
    method: 'put'
  })
}

export function listTodoSummary() {
  return request({
    url: '/nocontact/support/todo/summary',
    method: 'get'
  })
}

export function getSupportPublicSettings() {
  return request({
    url: '/nocontact/support/settings/public',
    method: 'get'
  })
}

export function geocodeEnterpriseAddress(regionName, address) {
  return request({
    url: '/nocontact/support/geocode/enterprise',
    method: 'get',
    params: { regionName, address }
  })
}
