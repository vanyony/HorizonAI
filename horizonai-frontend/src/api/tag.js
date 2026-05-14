import request from '@/utils/request'

export function getTags() {
  return request.get('/tags')
}

export function createTag(name, description) {
  return request.post('/tags', null, { params: { name, description } })
}

export function deleteTag(id) {
  return request.delete(`/tags/${id}`)
}
