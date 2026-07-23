import {
  CanActivate,
  ExecutionContext,
  HttpException,
  Injectable,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import {
  KeycloakJwtService,
  RealtimeAuthorizationError,
} from './keycloak-jwt.service';
import { PUBLIC_ROUTE } from './http-auth.decorators';
import type { AuthenticatedHttpRequest } from './http-auth.types';

@Injectable()
export class HttpJwtAuthGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly jwtService: KeycloakJwtService,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    if (context.getType() !== 'http') {
      return true;
    }
    const isPublic = this.reflector.getAllAndOverride<boolean>(
      PUBLIC_ROUTE,
      [context.getHandler(), context.getClass()],
    );
    if (isPublic) {
      return true;
    }

    const request = context.switchToHttp()
      .getRequest<AuthenticatedHttpRequest>();
    try {
      const token = this.extractBearerToken(
        request.headers.authorization,
      );
      request.authenticatedUser =
        await this.jwtService.verifyAccessToken(token);
      request.accessToken = token;
      return true;
    } catch (error) {
      const authError = error instanceof RealtimeAuthorizationError
        ? error
        : new RealtimeAuthorizationError(
          401,
          'UNAUTHORIZED',
          '身份认证失败',
        );
      throw new HttpException(
        {
          success: false,
          code: authError.code,
          message: authError.message,
          data: null,
          timestamp: new Date().toISOString(),
        },
        authError.status,
      );
    }
  }

  private extractBearerToken(value: string | undefined): string {
    if (!value?.startsWith('Bearer ')) {
      throw new RealtimeAuthorizationError(
        401,
        'UNAUTHORIZED',
        '请求缺少 Bearer Token',
      );
    }
    return value.slice(7).trim();
  }
}
