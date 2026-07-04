from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=10_000)


class ChatResponse(BaseModel):
    model: str
    answer: str


class HealthResponse(BaseModel):
    status: str
    service: str
    ollama: str
    model: str


class ServiceInfoResponse(BaseModel):
    service: str
    status: str
    docs: str
    health: str
    chat: str
