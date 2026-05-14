import request from '@/utils/request'

export function getProfile() {
  return request.get('/user/profile')
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}

export function getInterests() {
  return request.get('/user/interests')
}

export function updateInterests(tagIds) {
  return request.put('/user/interests', { tagIds })
}

export function getHistory() {
  return request.get('/user/history')
}

export function recordBrowse(articleId) {
  return request.post(`/user/history/${articleId}`)
}
