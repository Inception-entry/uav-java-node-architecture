import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { JavaClientService } from '../shared/java-client.service';
import { AnalyzeInspectionTaskDto } from './dto/analyze-inspection-task.dto';

@Controller('inspection-tasks')
export class InspectionTaskController {
  constructor(
    private readonly javaClient: JavaClientService,
  ) {}

  @Get()
  list(): Promise<unknown> {
    return this.javaClient.get('/inspection-tasks');
  }

  @Get(':taskCode')
  detail(@Param('taskCode') taskCode: string): Promise<unknown> {
    return this.javaClient.get(`/inspection-tasks/${taskCode}`);
  }

  @Post(':taskCode/start')
  start(@Param('taskCode') taskCode: string): Promise<unknown> {
    return this.javaClient.post(
      `/inspection-workflows/${taskCode}`,
      {},
    );
  }

  @Post(':taskCode/complete')
  complete(@Param('taskCode') taskCode: string): Promise<unknown> {
    return this.javaClient.post(
      `/inspection-workflows/${taskCode}/complete`,
      {},
    );
  }

  @Post(':taskCode/cancel')
  cancel(@Param('taskCode') taskCode: string): Promise<unknown> {
    return this.javaClient.post(
      `/inspection-workflows/${taskCode}/cancel`,
      {},
    );
  }

  @Post(':taskCode/analysis')
  analyze(
    @Param('taskCode') taskCode: string,
    @Body() dto: AnalyzeInspectionTaskDto,
  ): Promise<unknown> {
    return this.javaClient.post(
      `/inspection-workflows/${taskCode}/analysis`,
      dto,
      180_000,
    );
  }
}
