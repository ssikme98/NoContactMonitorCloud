import request from '@/utils/request'

// 查询问卷模块概览
export function getSurveyOverview() {
  return request({
    url: '/nocontact/overview',
    method: 'get'
  })
}
