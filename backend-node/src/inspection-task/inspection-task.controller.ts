import {
  Body,
  Controller,
  Get,
  Param,
  Post,
  Res,
  Put,
} from '@nestjs/common';
import { JavaClientService } from '../shared/java-client.service';
import { AnalyzeInspectionTaskDto } from './dto/analyze-inspection-task.dto';
import {
  CreateInspectionTaskDto,
  UpdateInspectionTaskDto,
} from './dto/save-inspection-task.dto';

interface StreamResponse extends NodeJS.WritableStream {
  statusCode: number;
  headersSent: boolean;
  writableEnded: boolean;
  setHeader(name: string, value: string): void;
  on(event: 'close', listener: () => void): this;
  once(event: 'finish', listener: () => void): this;
  end(): this;
}

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

  @Get(':taskCode/analyses')
  analyses(@Param('taskCode') taskCode: string): Promise<unknown> {
    return this.javaClient.get(
      `/inspection-tasks/${taskCode}/analyses`,
    );
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

  @Post(':taskCode/analysis/stream')
  async streamAnalysis(
    @Param('taskCode') taskCode: string,
    @Body() dto: AnalyzeInspectionTaskDto,
    @Res() response: StreamResponse,
  ): Promise<void> {
    const stream = await this.javaClient.postStream(
      `/inspection-workflows/${taskCode}/analysis/stream`,
      dto,
    );

    response.statusCode = 200;
    response.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
    response.setHeader('Cache-Control', 'no-cache, no-transform');
    response.setHeader('X-Accel-Buffering', 'no');

    await new Promise<void>((resolve) => {
      let finished = false;
      const finish = () => {
        if (finished) return;
        finished = true;
        resolve();
      };

      response.once('finish', finish);
      response.on('close', () => {
        if (!response.writableEnded && !stream.destroyed) {
          stream.destroy();
        }
        finish();
      });
      stream.once('error', (error) => {
        if (!response.writableEnded) {
          const data = JSON.stringify({
            message: `流式代理中断: ${error.message}`,
          });
          response.write(`event: error\ndata: ${data}\n\n`);
          response.end();
        }
        finish();
      });
      stream.pipe(response);
    });
  }
}
