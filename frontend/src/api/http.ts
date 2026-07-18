import { getAccessToken } from '@/auth/keycloak'
import { redirectToAuthorizationPage } from '@/auth/authorization-navigation'

export async function authorizedFetch(
  input: RequestInfo | URL,
  init: RequestInit = {},
) {
  const request = new Request(input, init)
  let response: Response

  try {
    response = await sendAuthorizedRequest(request, false)
    if (response.status === 401) {
      response = await sendAuthorizedRequest(request, true)
    }
  } catch (error) {
    redirectToAuthorizationPage(401)
    throw error
  }

  if (response.status === 401 || response.status === 403) {
    redirectToAuthorizationPage(response.status)
  }
  return response
}

async function sendAuthorizedRequest(
  request: Request,
  forceRefresh: boolean,
) {
  const headers = new Headers(request.headers)
  headers.set(
    'Authorization',
    `Bearer ${await getAccessToken(forceRefresh)}`,
  )
  headers.set('Accept', 'application/json')

  return fetch(request.clone(), {
    headers,
  })
}
