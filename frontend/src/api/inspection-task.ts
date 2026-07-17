import { authorizedFetch } from '@/api/http'

export interface InspectionTask {
  taskCode: string
  taskName: string
  deviceCode: string | null
  status: string
  planStartTime: string | null
  planEndTime: string | null
  createdAt: string
  updatedAt: string
}

export interface InspectionTaskDetailsInput {
  taskName: string
  deviceCode: string
  planStartTime: string
  planEndTime: string
}

export interface CreateInspectionTaskInput
  extends InspectionTaskDetailsInput {
  taskCode: string
}

export interface InspectionAnalysis {
  taskCode: string
  workflowId: string
  analysis: string
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
  const response = await authorizedFetch(url, options)
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
    `/api/inspection-tasks/${encodeURIComponent(taskCode)}`,
  )
}

export function createInspectionTask(
  input: CreateInspectionTaskInput,
) {
  return request<InspectionTask>('/api/inspection-tasks', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(input),
  })
}

export function updateInspectionTask(
  taskCode: string,
  input: InspectionTaskDetailsInput,
) {
  return request<InspectionTask>(
    `/api/inspection-tasks/${encodeURIComponent(taskCode)}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(input),
    },
  )
}

export function startInspectionTask(taskCode: string) {
  return request<{ workflowId: string; runId: string }>(
    `/api/inspection-tasks/${encodeURIComponent(taskCode)}/start`,
    { method: 'POST' },
  )
}

export function completeInspectionTask(taskCode: string) {
  return request<void>(
    `/api/inspection-tasks/${encodeURIComponent(taskCode)}/complete`,
    { method: 'POST' },
  )
}

export function cancelInspectionTask(taskCode: string) {
  return request<void>(
    `/api/inspection-tasks/${encodeURIComponent(taskCode)}/cancel`,
    { method: 'POST' },
  )
}

export function analyzeInspectionTask(
  taskCode: string,
  sessionId: string,
  question: string,
) {
  return request<InspectionAnalysis>(
    `/api/inspection-tasks/${encodeURIComponent(taskCode)}/analysis`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ sessionId, question }),
    },
  )
}
