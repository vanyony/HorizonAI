import request from '@/utils/request'

export function getPipelineTasks(params) {
  return request.get('/admin/pipeline/tasks', { params })
}

export function getTraceTasks(traceId) {
  return request.get(`/admin/pipeline/traces/${traceId}`)
}

export function syncSource(source) {
  return request.post(`/admin/pipeline/sync/${source}`)
}

export function retryTask(taskId) {
  return request.post(`/admin/pipeline/tasks/${taskId}/retry`)
}
