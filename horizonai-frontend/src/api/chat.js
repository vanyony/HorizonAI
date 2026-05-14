import request from '@/utils/request'

export function sendMessage(content) {
  return request.post('/chat/send', { content })
}

export function getHistory() {
  return request.get('/chat/history')
}
