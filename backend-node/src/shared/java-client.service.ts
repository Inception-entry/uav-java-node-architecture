import { HttpService } from '@nestjs/axios';
import { HttpException, Injectable } from '@nestjs/common';
import { isAxiosError } from 'axios';
import { Readable } from 'node:stream';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class JavaClientService {
  private readonly baseUrl = process.env.JAVA_BASE_URL ?? 'http://localhost:8081';

  constructor(private readonly httpService: HttpService) {}

  async get<T>(path: string): Promise<T> {
    try {
      const response = await firstValueFrom(
        this.httpService.get<T>(`${this.baseUrl}/api${path}`),
      );
      return response.data;
    } catch (error) {
      this.rethrowUpstreamError(error);
    }
  }

  async post<T>(
    path: string,
    body: unknown,
    timeout = 5000,
  ): Promise<T> {
    try {
      const response = await firstValueFrom(
        this.httpService.post<T>(
          `${this.baseUrl}/api${path}`,
          body,
          { timeout },
        ),
      );
      return response.data;
    } catch (error) {
      this.rethrowUpstreamError(error);
    }
  }

  async postStream(
    path: string,
    body: unknown,
    timeout = 300_000,
  ): Promise<Readable> {
    try {
      const response = await firstValueFrom(
        this.httpService.post<Readable>(
          `${this.baseUrl}/api${path}`,
          body,
          {
            timeout,
            responseType: 'stream',
            headers: {
              Accept: 'text/event-stream',
              'Content-Type': 'application/json',
            },
          },
        ),
      );
      return response.data;
    } catch (error) {
      if (isAxiosError(error) && error.response) {
        throw new HttpException(
          'Java 流式分析服务请求失败',
          error.response.status,
        );
      }
      throw error;
    }
  }

  async put<T>(
    path: string,
    body: unknown,
    timeout = 5000,
  ): Promise<T> {
    try {
      const response = await firstValueFrom(
        this.httpService.put<T>(
          `${this.baseUrl}/api${path}`,
          body,
          { timeout },
        ),
      );
      return response.data;
    } catch (error) {
      this.rethrowUpstreamError(error);
    }
  }

  async postMultipart<T>(
    path: string,
    file: {
      buffer: Buffer;
      originalname: string;
      mimetype: string;
    },
    timeout = 180_000,
  ): Promise<T> {
    const formData = new FormData();
    formData.append(
      'file',
      new Blob([new Uint8Array(file.buffer)], {
        type: file.mimetype || 'application/octet-stream',
      }),
      file.originalname,
    );

    try {
      const response = await firstValueFrom(
        this.httpService.post<T>(
          `${this.baseUrl}/api${path}`,
          formData,
          { timeout },
        ),
      );
      return response.data;
    } catch (error) {
      this.rethrowUpstreamError(error);
    }
  }

  async delete<T>(path: string, timeout = 5000): Promise<T> {
    try {
      const response = await firstValueFrom(
        this.httpService.delete<T>(`${this.baseUrl}/api${path}`, {
          timeout,
        }),
      );
      return response.data;
    } catch (error) {
      this.rethrowUpstreamError(error);
    }
  }

  private rethrowUpstreamError(error: unknown): never {
    if (isAxiosError(error) && error.response) {
      throw new HttpException(
        error.response.data ?? 'Java 服务请求失败',
        error.response.status,
      );
    }
    throw error;
  }
}
