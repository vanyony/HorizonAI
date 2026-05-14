import request from '@/utils/request'

export function getPublicArticles(params) {
  return request.get('/articles', { params })
}

export function getPublicArticle(id) {
  return request.get(`/articles/${id}`)
}
