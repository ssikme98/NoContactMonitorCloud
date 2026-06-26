import request from '@/utils/request'

export function listRule(query) {
  return request({
    url: '/nocontact/warning/rule/list',
    method: 'get',
    params: query
  })
}

export function getRule(ruleId) {
  return request({
    url: '/nocontact/warning/rule/' + ruleId,
    method: 'get'
  })
}

export function addRule(data) {
  return request({
    url: '/nocontact/warning/rule',
    method: 'post',
    data: data
  })
}

export function updateRule(data) {
  return request({
    url: '/nocontact/warning/rule',
    method: 'put',
    data: data
  })
}

export function updateRuleStatus(ruleId, status) {
  return request({
    url: '/nocontact/warning/rule/' + ruleId + '/status/' + status,
    method: 'put'
  })
}

export function delRule(ruleId) {
  return request({
    url: '/nocontact/warning/rule/' + ruleId,
    method: 'delete'
  })
}
