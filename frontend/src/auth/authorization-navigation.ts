export type AuthorizationStatus = 401 | 403

export function redirectToAuthorizationPage(
  status: AuthorizationStatus,
) {
  if (window.location.pathname === `/${status}`) {
    return
  }

  const redirect = [
    window.location.pathname,
    window.location.search,
    window.location.hash,
  ].join('')
  window.location.assign(
    `/${status}?redirect=${encodeURIComponent(redirect)}`,
  )
}

export function resolveSafeRedirect(
  value: unknown,
  fallback = '/',
) {
  if (typeof value !== 'string'
    || !value.startsWith('/')
    || value.startsWith('//')
    || value.startsWith('/401')
    || value.startsWith('/403')) {
    return fallback
  }
  return value
}
