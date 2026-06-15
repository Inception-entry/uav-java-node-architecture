import { IsNotEmpty, IsOptional, IsString } from 'class-validator';

export class CreateAlarmDto {
  @IsString()
  @IsNotEmpty()
  deviceCode!: string;

  @IsString()
  @IsOptional()
  taskCode?: string;

  @IsString()
  @IsNotEmpty()
  eventType!: string;

  @IsString()
  @IsOptional()
  weaponType?: string;

  confidence?: number;
  latitude?: number;
  longitude?: number;
  imageUrl?: string;
  videoUrl?: string;
  eventTime?: string;
}
