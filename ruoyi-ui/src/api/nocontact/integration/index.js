import request from '@/utils/request'

export function listConfig(query) {
  return request({
    url: '/nocontact/integration/config/list',
    method: 'get',
    params: query
  })
}

export function getConfig(configId) {
  return request({
    url: '/nocontact/integration/config/' + configId,
    method: 'get'
  })
}

export function addConfig(data) {
  return request({
    url: '/nocontact/integration/config',
    method: 'post',
    data
  })
}

export function updateConfig(data) {
  return request({
    url: '/nocontact/integration/config',
    method: 'put',
    data
  })
}

export function delConfig(configId) {
  return request({
    url: '/nocontact/integration/config/' + configId,
    method: 'delete'
  })
}

export function testConfig(configId) {
  return request({
    url: '/nocontact/integration/config/' + configId + '/test',
    method: 'post'
  })
}

export function syncConfig(configId) {
  return request({
    url: '/nocontact/integration/config/' + configId + '/sync',
    method: 'post'
  })
}

export function listSyncLog(query) {
  return request({
    url: '/nocontact/integration/log/list',
    method: 'get',
    params: query
  })
}
