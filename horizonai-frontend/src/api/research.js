import request from '@/utils/request'

export function askResearch(question, sessionId = null) {
  return request.post('/research/ask', { question, sessionId })
}

export function getResearchSessions() {
  return request.get('/research/sessions')
}

export function getResearchMessages(sessionId) {
  return request.get(`/research/sessions/${sessionId}/messages`)
}

export function getResearchTools(sessionId) {
  return request.get(`/research/sessions/${sessionId}/tools`)
}
