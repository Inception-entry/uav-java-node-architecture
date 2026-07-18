import { authorizedFetch } from '@/api/http'

export interface KnowledgeDocument {
  documentId: string
  filename: string
  contentType: string
  chunkCount: number
  uploadedAt: string
}

export interface KnowledgeSearchResult {
  documentId: string
  filename: string
  content: string
  page: number | null
  chunkIndex: number
  score: number
}

interface KnowledgeDeleteResult {
  documentId: string
  deletedChunks: number
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
    throw new Error(result.message || '知识库请求失败')
  }
  return result.data
}

export function getKnowledgeDocuments() {
  return request<KnowledgeDocument[]>('/api/knowledge/documents')
}

export function uploadKnowledgeDocument(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request<KnowledgeDocument>('/api/knowledge/documents', {
    method: 'POST',
    body: formData,
  })
}

export function searchKnowledge(query: string, topK = 6) {
  return request<KnowledgeSearchResult[]>('/api/knowledge/search', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, topK }),
  })
}

export function deleteKnowledgeDocument(documentId: string) {
  return request<KnowledgeDeleteResult>(
    `/api/knowledge/documents/${encodeURIComponent(documentId)}`,
    { method: 'DELETE' },
  )
}
