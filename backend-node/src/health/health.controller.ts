import { Controller, Get } from '@nestjs/common';

@Controller('health')
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
