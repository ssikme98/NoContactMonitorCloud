import request from '@/utils/request'

export function listMessage(query) {
  return request({
    url: '/nocontact/warning/message/list',
    method: 'get',
    params: query
  })
}

export function getMessage(messageId) {
  return request({
    url: '/nocontact/warning/message/' + messageId,
    method: 'get'
  })
}

export function getWarningDashboard() {
  return request({
    url: '/nocontact/warning/message/dashboard',
    method: 'get'
  })
}

export function updateMessageStatus(messageId, status, opinion) {
  return request({
    url: '/nocontact/warning/message/' + messageId + '/status/' + status,
    method: 'put',
    params: { opinion }
  })
}

export function delMessage(messageId) {
  return request({
    url: '/nocontact/warning/message/' + messageId,
    method: 'delete'
  })
}
