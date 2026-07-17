import { getAccessToken } from '@/auth/keycloak'

export async function authorizedFetch(
  input: RequestInfo | URL,
  init: RequestInit = {},
) {
  const headers = new Headers(init.headers)
  headers.set('Authorization', `Bearer ${await getAccessToken()}`)
  headers.set('Accept', 'application/json')

  return fetch(input, {
    ...init,
    headers,
  })
}
