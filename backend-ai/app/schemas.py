from pydantic import BaseModel, ConfigDict, Field


class ChatRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    session_id: str = Field(
        alias="sessionId",
        min_length=8,
        max_length=64,
        pattern=r"^[A-Za-z0-9_-]+$",
    )
    message: str = Field(min_length=1, max_length=10_000)


class ChatResponse(BaseModel):
    model: str
    answer: str


class HealthResponse(BaseModel):
    status: str
    service: str
    ollama: str
    redis: str
    model: str


class ServiceInfoResponse(BaseModel):
    service: str
    status: str
    docs: str
    health: str
    chat: str
