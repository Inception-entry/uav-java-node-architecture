import type { IncomingHttpHeaders } from 'node:http';
import type { RealtimeUser } from './keycloak-jwt.service';

export interface AuthenticatedHttpRequest {
  headers: IncomingHttpHeaders;
  ip?: string;
  socket?: {
    remoteAddress?: string;
  };
  authenticatedUser?: RealtimeUser;
  accessToken?: string;
}
