from contextlib import asynccontextmanager

import httpx
from fastapi import FastAPI, File, HTTPException, Request, UploadFile, status
from langchain_core.messages import HumanMessage, SystemMessage
from langchain_ollama import ChatOllama
from qdrant_client.http.exceptions import UnexpectedResponse
from redis.asyncio import Redis

from app.chat_history import RedisChatHistory
from app.config import get_settings
from app.knowledge_base import KnowledgeBase
from app.schemas import (
    ChatRequest,
    ChatResponse,
    HealthResponse,
    KnowledgeDeleteResponse,
    KnowledgeDocumentResponse,
    KnowledgeSearchRequest,
    KnowledgeSearchResult,
    KnowledgeSource,
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
    app.state.knowledge_base = KnowledgeBase(settings)
    yield
    await app.state.redis.aclose()
    await app.state.knowledge_base.close()


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
        knowledge="/api/knowledge/documents",
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
        await app.state.knowledge_base.health()
    except Exception as exc:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"AI dependency unavailable: {exc}",
        ) from exc

    model_status = "available" if _has_model(model_names, settings.ollama_model) else "missing"
    embedding_status = (
        "available"
        if _has_model(model_names, settings.ollama_embedding_model)
        else "missing"
    )
    return HealthResponse(
        status=(
            "ok"
            if model_status == "available" and embedding_status == "available"
            else "degraded"
        ),
        service=settings.service_name,
        ollama="connected",
        redis="connected",
        qdrant="connected",
        model=model_status,
        embeddingModel=embedding_status,
    )


@app.post("/api/chat", response_model=ChatResponse)
async def chat(payload: ChatRequest, request: Request) -> ChatResponse:
    try:
        history = await request.app.state.chat_history.get_messages(
            payload.session_id
        )
        sources = await request.app.state.knowledge_base.search(
            payload.knowledge_query or payload.message
        )
        messages = [*history]
        if sources:
            messages.append(SystemMessage(content=_rag_system_prompt(sources)))
        messages.append(HumanMessage(content=payload.message))
        response = await request.app.state.llm.ainvoke(
            messages
        )
        answer = (
            response.content
            if isinstance(response.content, str)
            else str(response.content)
        )
        answer = _append_source_summary(answer, sources)
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

    return ChatResponse(
        model=settings.ollama_model,
        answer=answer,
        sources=[
            KnowledgeSource(
                documentId=item.document_id,
                filename=item.filename,
                page=item.page,
                score=item.score,
            )
            for item in sources
        ],
    )


@app.post(
    "/api/knowledge/documents",
    response_model=KnowledgeDocumentResponse,
    status_code=status.HTTP_201_CREATED,
)
async def upload_knowledge_document(
    request: Request,
    file: UploadFile = File(...),
) -> KnowledgeDocumentResponse:
    content = await file.read(settings.knowledge_max_file_size_bytes + 1)
    try:
        return await request.app.state.knowledge_base.import_document(
            file.filename or "unnamed.txt",
            file.content_type or "application/octet-stream",
            content,
        )
    except ValueError as exc:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"知识库入库失败: {exc}",
        ) from exc
    finally:
        await file.close()


@app.get(
    "/api/knowledge/documents",
    response_model=list[KnowledgeDocumentResponse],
)
async def list_knowledge_documents(request: Request) -> list[KnowledgeDocumentResponse]:
    return await request.app.state.knowledge_base.list_documents()


@app.post(
    "/api/knowledge/search",
    response_model=list[KnowledgeSearchResult],
)
async def search_knowledge(
    payload: KnowledgeSearchRequest,
    request: Request,
) -> list[KnowledgeSearchResult]:
    try:
        return await request.app.state.knowledge_base.search(
            payload.query,
            payload.top_k,
        )
    except Exception as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"知识库检索失败: {exc}",
        ) from exc


@app.delete(
    "/api/knowledge/documents/{document_id}",
    response_model=KnowledgeDeleteResponse,
)
async def delete_knowledge_document(
    document_id: str,
    request: Request,
) -> KnowledgeDeleteResponse:
    try:
        deleted_chunks = await request.app.state.knowledge_base.delete_document(
            document_id
        )
    except UnexpectedResponse as exc:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=f"知识库删除失败: {exc}",
        ) from exc
    if deleted_chunks == 0:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="知识库文档不存在",
        )
    return KnowledgeDeleteResponse(
        documentId=document_id,
        deletedChunks=deleted_chunks,
    )


def _has_model(model_names: set[str], expected: str) -> bool:
    normalized = {name.removesuffix(":latest") for name in model_names}
    return expected.removesuffix(":latest") in normalized


def _rag_system_prompt(sources: list[KnowledgeSearchResult]) -> str:
    context = "\n\n".join(
        f"[资料{index}] 文件：{item.filename}"
        f"{'，第 ' + str(item.page) + ' 页' if item.page else ''}\n"
        f"{item.content}"
        for index, item in enumerate(sources, start=1)
    )
    return f"""
你是无人机巡检知识助手。下面是从内部知识库检索到的资料。
只把资料作为专业知识依据；业务任务事实仍以用户消息中的 MySQL 数据为准。
引用资料时使用 [资料1]、[资料2] 标记。若资料不足，请明确说明，不要编造。

【知识库检索结果】
{context}
""".strip()


def _append_source_summary(
    answer: str,
    sources: list[KnowledgeSearchResult],
) -> str:
    if not sources:
        return answer
    seen: set[tuple[str, int | None]] = set()
    lines = []
    for item in sources:
        key = (item.filename, item.page)
        if key in seen:
            continue
        seen.add(key)
        page = f"（第 {item.page} 页）" if item.page else ""
        lines.append(f"- {item.filename}{page}")
    return f"{answer.rstrip()}\n\n参考资料：\n" + "\n".join(lines)
