import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { HttpModule } from '@nestjs/axios';
import { HealthController } from './health/health.controller';
import { JavaClientService } from './shared/java-client.service';
import { AlarmController } from './alarm/alarm.controller';
import { InspectionTaskController } from './inspection-task/inspection-task.controller';
import { AlarmRealtimeGateway } from './realtime/alarm-realtime.gateway';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    HttpModule.register({ timeout: 5000, maxRedirects: 3 }),
  ],
  controllers: [
    HealthController,
    AlarmController,
    InspectionTaskController
  ],
  providers: [JavaClientService, AlarmRealtimeGateway],
})
export class AppModule {}
