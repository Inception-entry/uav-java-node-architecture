import { HttpService } from '@nestjs/axios';
import { HttpException, Injectable } from '@nestjs/common';
import { isAxiosError } from 'axios';
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
