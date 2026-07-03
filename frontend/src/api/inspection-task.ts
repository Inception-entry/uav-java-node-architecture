export interface InspectionTask {
  taskCode: string
  taskName: string
  status: string
}

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

async function request<T>(
  url: string,
  options?: RequestInit,
): Promise<T> {
  const response = await fetch(url, options)
  const result: ApiResponse<T> = await response.json()

  if (!response.ok || !result.success) {
    throw new Error(result.message || '请求失败')
  }

  return result.data
}

export function getInspectionTasks() {
  return request<InspectionTask[]>('/api/inspection-tasks')
}

export function getInspectionTask(taskCode: string) {
  return request<InspectionTask>(
    `/api/inspection-tasks/${taskCode}`,
  )
}

export function startInspectionTask(taskCode: string) {
  return request<{ workflowId: string; runId: string }>(
    `/api/inspection-tasks/${taskCode}/start`,
    { method: 'POST' },
  )
}

export function completeInspectionTask(taskCode: string) {
  return request<void>(
    `/api/inspection-tasks/${taskCode}/complete`,
    { method: 'POST' },
  )
}

export function cancelInspectionTask(taskCode: string) {
  return request<void>(
    `/api/inspection-tasks/${taskCode}/cancel`,
    { method: 'POST' },
  )
}