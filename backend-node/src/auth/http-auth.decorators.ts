import { SetMetadata } from '@nestjs/common';

export const PUBLIC_ROUTE = 'uav.public-route';
export const REQUIRED_ROLES = 'uav.required-roles';

export const PublicRoute = () => SetMetadata(PUBLIC_ROUTE, true);

export const Roles = (...roles: Array<'ADMIN' | 'OPERATOR' | 'VIEWER'>) =>
  SetMetadata(REQUIRED_ROLES, roles);
