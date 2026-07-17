import {
  Body,
  Controller,
  Get,
  Param,
  Post,
  Put,
} from '@nestjs/common';
import { JavaClientService } from '../shared/java-client.service';
import { AnalyzeInspectionTaskDto } from './dto/analyze-inspection-task.dto';
import {
  CreateInspectionTaskDto,
  UpdateInspectionTaskDto,
} from './dto/save-inspection-task.dto';

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

  @Post()
  create(@Body() dto: CreateInspectionTaskDto): Promise<unknown> {
    return this.javaClient.post('/inspection-tasks', dto);
  }

  @Put(':taskCode')
  update(
    @Param('taskCode') taskCode: string,
    @Body() dto: UpdateInspectionTaskDto,
  ): Promise<unknown> {
    return this.javaClient.put(
      `/inspection-tasks/${taskCode}`,
      dto,
    );
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
