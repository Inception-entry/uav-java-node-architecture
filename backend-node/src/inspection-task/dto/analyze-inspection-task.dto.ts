import { IsNotEmpty, IsString, MaxLength } from 'class-validator';

export class AnalyzeInspectionTaskDto {
  @IsString()
  @IsNotEmpty()
  @MaxLength(2000)
  question!: string;
}
