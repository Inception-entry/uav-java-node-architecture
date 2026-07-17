import {
  IsNotEmpty,
  IsOptional,
  IsString,
  Matches,
  MaxLength,
} from 'class-validator';

export class AnalyzeInspectionTaskDto {
  @IsOptional()
  @IsString()
  @MaxLength(64)
  @Matches(/^[A-Za-z0-9_-]+$/)
  sessionId?: string;

  @IsString()
  @IsNotEmpty()
  @MaxLength(2000)
  question!: string;
}
