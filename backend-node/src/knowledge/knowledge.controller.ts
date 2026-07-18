import {
  BadRequestException,
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { JavaClientService } from '../shared/java-client.service';
import { SearchKnowledgeDto } from './dto/search-knowledge.dto';

interface UploadedKnowledgeFile {
  buffer: Buffer;
  originalname: string;
  mimetype: string;
}

@Controller('knowledge')
export class KnowledgeController {
  constructor(private readonly javaClient: JavaClientService) {}

  @Get('documents')
  listDocuments(): Promise<unknown> {
    return this.javaClient.get('/knowledge/documents');
  }

  @Post('documents')
  @UseInterceptors(
    FileInterceptor('file', {
      limits: { fileSize: 10 * 1024 * 1024 },
    }),
  )
  uploadDocument(
    @UploadedFile() file?: UploadedKnowledgeFile,
  ): Promise<unknown> {
    if (!file) {
      throw new BadRequestException('请选择需要上传的文档');
    }
    return this.javaClient.postMultipart(
      '/knowledge/documents',
      file,
    );
  }

  @Post('search')
  search(@Body() dto: SearchKnowledgeDto): Promise<unknown> {
    return this.javaClient.post('/knowledge/search', dto, 120_000);
  }

  @Delete('documents/:documentId')
  deleteDocument(
    @Param('documentId') documentId: string,
  ): Promise<unknown> {
    return this.javaClient.delete(
      `/knowledge/documents/${encodeURIComponent(documentId)}`,
      30_000,
    );
  }
}
