import { Controller, Get } from '@nestjs/common';
import { PublicRoute } from '../auth/http-auth.decorators';

@Controller('health')
@PublicRoute()
export class HealthController {
  @Get()
  health() {
    return {
      success: true,
      message: 'success',
      data: { service: 'backend-node', status: 'UP' },
      timestamp: new Date().toISOString(),
    };
  }
}
