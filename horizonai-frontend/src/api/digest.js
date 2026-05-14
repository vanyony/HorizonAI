import request from '@/utils/request'

export function getDigests(params) {
  return request.get('/digests', { params })
}

export function getDigestByDate(date) {
  return request.get(`/digests/${date}`)
}

export function getTodayDigest() {
  return request.get('/digests/today')
}
