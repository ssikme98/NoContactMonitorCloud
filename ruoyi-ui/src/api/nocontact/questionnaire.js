import request from '@/utils/request'

// 查询问卷列表
export function listQuestionnaire(query) {
  return request({
    url: '/nocontact/questionnaire/list',
    method: 'get',
    params: query
  })
}

// 查询问卷详细
export function getQuestionnaire(questionnaireId) {
  return request({
    url: '/nocontact/questionnaire/' + questionnaireId,
    method: 'get'
  })
}

// 新增问卷
export function addQuestionnaire(data) {
  return request({
    url: '/nocontact/questionnaire',
    method: 'post',
    data: data
  })
}

// 修改问卷
export function updateQuestionnaire(data) {
  return request({
    url: '/nocontact/questionnaire',
    method: 'put',
    data: data
  })
}

// 删除问卷
export function delQuestionnaire(questionnaireId) {
  return request({
    url: '/nocontact/questionnaire/' + questionnaireId,
    method: 'delete'
  })
}

// 创建新版草稿
export function createDraftQuestionnaire(questionnaireId) {
  return request({
    url: '/nocontact/questionnaire/' + questionnaireId + '/draft',
    method: 'post'
  })
}

// 发布问卷
export function publishQuestionnaire(questionnaireId) {
  return request({
    url: '/nocontact/questionnaire/' + questionnaireId + '/publish',
    method: 'post'
  })
}

// 结束问卷
export function endQuestionnaire(questionnaireId) {
  return request({
    url: '/nocontact/questionnaire/' + questionnaireId + '/end',
    method: 'post'
  })
}
