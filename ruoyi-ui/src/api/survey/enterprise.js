import request from '@/utils/request'

// 查询企业库列表
export function listEnterprise(query) {
  return request({
    url: '/survey/enterprise/list',
    method: 'get',
    params: query
  })
}

// 查询企业库详细
export function getEnterprise(enterpriseId) {
  return request({
    url: '/survey/enterprise/' + enterpriseId,
    method: 'get'
  })
}

// 新增企业
export function addEnterprise(data) {
  return request({
    url: '/survey/enterprise',
    method: 'post',
    data: data
  })
}

// 修改企业
export function updateEnterprise(data) {
  return request({
    url: '/survey/enterprise',
    method: 'put',
    data: data
  })
}

// 删除企业
export function delEnterprise(enterpriseId) {
  return request({
    url: '/survey/enterprise/' + enterpriseId,
    method: 'delete'
  })
}

// 查询企业分组列表
export function listEnterpriseGroup(query) {
  return request({
    url: '/survey/enterprise/group/list',
    method: 'get',
    params: query
  })
}

// 查询企业分组下拉树
export function groupTreeSelect() {
  return request({
    url: '/survey/enterprise/group/treeselect',
    method: 'get'
  })
}

// 查询企业分组详细
export function getEnterpriseGroup(groupId) {
  return request({
    url: '/survey/enterprise/group/' + groupId,
    method: 'get'
  })
}

// 新增企业分组
export function addEnterpriseGroup(data) {
  return request({
    url: '/survey/enterprise/group',
    method: 'post',
    data: data
  })
}

// 修改企业分组
export function updateEnterpriseGroup(data) {
  return request({
    url: '/survey/enterprise/group',
    method: 'put',
    data: data
  })
}

// 删除企业分组
export function delEnterpriseGroup(groupId) {
  return request({
    url: '/survey/enterprise/group/' + groupId,
    method: 'delete'
  })
}
