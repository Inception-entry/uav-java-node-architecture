import {
  CanActivate,
  ExecutionContext,
  ForbiddenException,
  Injectable,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { REQUIRED_ROLES } from './http-auth.decorators';
import type { AuthenticatedHttpRequest } from './http-auth.types';

@Injectable()
export class HttpRolesGuard implements CanActivate {
  constructor(private readonly reflector: Reflector) {}

  canActivate(context: ExecutionContext): boolean {
    if (context.getType() !== 'http') {
      return true;
    }
    const requiredRoles = this.reflector.getAllAndOverride<string[]>(
      REQUIRED_ROLES,
      [context.getHandler(), context.getClass()],
    );
    if (!requiredRoles?.length) {
      return true;
    }

    const user = context.switchToHttp()
      .getRequest<AuthenticatedHttpRequest>()
      .authenticatedUser;
    if (user && requiredRoles.some(role => user.roles.includes(role))) {
      return true;
    }
    throw new ForbiddenException({
      success: false,
      code: 'FORBIDDEN',
      message: '当前账号没有执行此操作的权限',
      data: null,
      timestamp: new Date().toISOString(),
    });
  }
}
