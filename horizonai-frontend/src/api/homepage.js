import request from '@/utils/request'

export function getTodayOverview() {
  return request.get('/homepage/today')
}
