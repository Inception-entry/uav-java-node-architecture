import { authorizedFetch } from '@/api/http'

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface AdminOverview {
  totalTasks: number
  createdTasks: number
  runningTasks: number
  completedTasks: number
  cancelledTasks: number
  totalAnalyses: number
  totalAuditEvents: number
  failedAuditEventsLast24Hours: number
}

export interface AuditLog {
  id: number
  requestId: string
  actorId: string
  username: string
  roles: string
  action: string
  resourceType: string
  resourceId: string | null
  httpMethod: string
  requestPath: string
  statusCode: number
  outcome: 'SUCCESS' | 'FAILURE'
  clientIp: string
  durationMs: number
  errorType: string | null
  createdAt: string
}

export interface AuditLogPage {
  content: AuditLog[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface AuditLogQuery {
  page?: number
  size?: number
  action?: string
  outcome?: '' | 'SUCCESS' | 'FAILURE'
  username?: string
}

async function request<T>(url: string): Promise<T> {
  const response = await authorizedFetch(url)
  const result = await response.json() as ApiResponse<T>
  if (!response.ok || !result.success) {
    throw new Error(result.message || '后台管理请求失败')
  }
  return result.data
}

export function getAdminOverview() {
  return request<AdminOverview>('/api/admin/overview')
}

export function getAuditLogs(query: AuditLogQuery = {}) {
  const parameters = new URLSearchParams({
    page: String(query.page ?? 0),
    size: String(query.size ?? 20),
  })
  if (query.action) parameters.set('action', query.action)
  if (query.outcome) parameters.set('outcome', query.outcome)
  if (query.username?.trim()) {
    parameters.set('username', query.username.trim())
  }
  return request<AuditLogPage>(
    `/api/admin/audit-logs?${parameters.toString()}`,
  )
}
