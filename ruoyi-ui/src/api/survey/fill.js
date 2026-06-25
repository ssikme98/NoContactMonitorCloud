import request from '@/utils/request'

// 获取公开填报问卷
export function getFill(token) {
  return request({
    url: '/survey/public/fill/' + token,
    method: 'get',
    headers: { isToken: false }
  })
}

// 提交公开填报答案
export function submitFill(token, data) {
  return request({
    url: '/survey/public/fill/' + token,
    method: 'post',
    headers: { isToken: false },
    data: data
  })
}
