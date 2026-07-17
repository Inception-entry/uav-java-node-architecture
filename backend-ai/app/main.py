from contextlib import asynccontextmanager

import httpx
from fastapi import FastAPI, HTTPException, Request, status
from langchain_core.messages import HumanMessage
from langchain_ollama import ChatOllama
from redis.asyncio import Redis
from redis.exceptions import RedisError

from app.chat_history import RedisChatHistory
from app.config import get_settings
from app.schemas import (
    ChatRequest,
    ChatResponse,
    HealthResponse,
    ServiceInfoResponse,
)

settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.llm = ChatOllama(
        base_url=settings.ollama_base_url,
        model=settings.ollama_model,
        temperature=0.3,
        num_ctx=8192,
        client_kwargs={"timeout": settings.ollama_timeout_seconds},
    )
    app.state.redis = Redis.from_url(
        settings.redis_url,
        decode_responses=True,
    )
    app.state.chat_history = RedisChatHistory(
        redis=app.state.redis,
        history_turns=settings.chat_history_turns,
        ttl_seconds=settings.chat_session_ttl_seconds,
    )
    yield
    await app.state.redis.aclose()


app = FastAPI(
    title="UAV AI Service",
    version="0.1.0",
    lifespan=lifespan,
)


@app.get("/", response_model=ServiceInfoResponse)
async def service_info() -> ServiceInfoResponse:
    return ServiceInfoResponse(
        service=settings.service_name,
        status="running",
        docs="/docs",
        health="/health",
        chat="POST /api/chat",
    )


@app.get("/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    try:
        async with httpx.AsyncClient(
            timeout=settings.ollama_timeout_seconds
        ) as client:
            response = await client.get(
                f"{settings.ollama_base_url}/api/tags"
            )
            response.raise_for_status()
            model_names = {
                model["name"].removesuffix(":latest")
                for model in response.json().get("models", [])
            }
        await app.state.redis.ping()
    except (httpx.HTTPError, RedisError, ValueError, KeyError) as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"AI dependency unavailable: {exc}",
        ) from exc

    model_status = (
        "available"
        if settings.ollama_model in model_names
        else "missing"
    )
    return HealthResponse(
        status="ok" if model_status == "available" else "degraded",
        service=settings.service_name,
        ollama="connected",
        redis="connected",
        model=model_status,
    )


@app.post("/api/chat", response_model=ChatResponse)
async def chat(payload: ChatRequest, request: Request) -> ChatResponse:
    try:
        history = await request.app.state.chat_history.get_messages(
            payload.session_id
        )
        response = await request.app.state.llm.ainvoke(
            [*history, HumanMessage(content=payload.message)]
        )
        answer = (
            response.content
            if isinstance(response.content, str)
            else str(response.content)
        )
        await request.app.state.chat_history.append_exchange(
            payload.session_id,
            payload.message,
            answer,
        )
    except Exception as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"Model invocation failed: {exc}",
        ) from exc

    return ChatResponse(model=settings.ollama_model, answer=answer)
