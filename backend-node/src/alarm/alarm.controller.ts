import { Body, Controller, Get, Post } from '@nestjs/common';
import { JavaClientService } from '../shared/java-client.service';
import { AlarmRealtimeGateway } from '../realtime/alarm-realtime.gateway';
import { CreateAlarmDto } from './dto/create-alarm.dto';

@Controller('alarms')
export class AlarmController {
  constructor(
    private readonly javaClient: JavaClientService,
    private readonly alarmGateway: AlarmRealtimeGateway,
  ) {}

  @Get('latest')
  latest() {
    return this.javaClient.get('/alarms/latest');
  }

  @Post()
  async create(@Body() dto: CreateAlarmDto) {
    const payload = {
      ...dto,
      eventTime: dto.eventTime ?? new Date().toISOString(),
    };
    const result = await this.javaClient.post('/alarms', payload);
    this.alarmGateway.broadcastAlarm(result);
    return result;
  }
}
