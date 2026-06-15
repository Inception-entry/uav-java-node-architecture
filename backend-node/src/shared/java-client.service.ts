import { HttpService } from '@nestjs/axios';
import { Injectable } from '@nestjs/common';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class JavaClientService {
  private readonly baseUrl = process.env.JAVA_BASE_URL ?? 'http://localhost:8080';

  constructor(private readonly httpService: HttpService) {}

  async get<T>(path: string): Promise<T> {
    const response = await firstValueFrom(this.httpService.get<T>(`${this.baseUrl}/api${path}`));
    return response.data;
  }

  async post<T>(path: string, body: unknown): Promise<T> {
    const response = await firstValueFrom(this.httpService.post<T>(`${this.baseUrl}/api${path}`, body));
    return response.data;
  }
}
