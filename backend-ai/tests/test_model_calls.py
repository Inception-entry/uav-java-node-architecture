import asyncio
import logging
from types import SimpleNamespace

import httpx
import pytest

from app.errors import AiServiceError
from app.model_calls import (
    invoke_model_with_retry,
    stream_model_with_retry,
)


class InvokeFailsOnce:
    def __init__(self) -> None:
        self.calls = 0

    async def ainvoke(self, messages):
        self.calls += 1
        if self.calls == 1:
            raise httpx.ConnectError("connection failed")
        return SimpleNamespace(content="已恢复")


class StreamFailsBeforeFirstChunk:
    def __init__(self) -> None:
        self.calls = 0

    async def astream(self, messages):
        self.calls += 1
        if self.calls == 1:
            raise httpx.ConnectError("connection failed")
        yield SimpleNamespace(content="返航")


class StreamFailsAfterFirstChunk:
    def __init__(self) -> None:
        self.calls = 0

    async def astream(self, messages):
        self.calls += 1
        yield SimpleNamespace(content="部分答案")
        raise httpx.ConnectError("connection failed")


def test_retries_transient_synchronous_model_failure() -> None:
    llm = InvokeFailsOnce()

    response = asyncio.run(
        invoke_model_with_retry(
            llm,
            [],
            timeout_seconds=1,
            max_attempts=2,
            initial_backoff_seconds=0,
            max_backoff_seconds=0,
            logger=logging.getLogger("test"),
            request_id="request-1",
            session_id="session-1",
        )
    )

    assert response.content == "已恢复"
    assert llm.calls == 2


def test_retries_stream_only_before_first_chunk() -> None:
    llm = StreamFailsBeforeFirstChunk()

    async def collect():
        return [
            chunk.content
            async for chunk in stream_model_with_retry(
                llm,
                [],
                timeout_seconds=1,
                max_attempts=2,
                initial_backoff_seconds=0,
                max_backoff_seconds=0,
                logger=logging.getLogger("test"),
                request_id="request-2",
                session_id="session-2",
            )
        ]

    assert asyncio.run(collect()) == ["返航"]
    assert llm.calls == 2


def test_does_not_retry_stream_after_first_chunk() -> None:
    llm = StreamFailsAfterFirstChunk()

    async def collect():
        return [
            chunk.content
            async for chunk in stream_model_with_retry(
                llm,
                [],
                timeout_seconds=1,
                max_attempts=2,
                initial_backoff_seconds=0,
                max_backoff_seconds=0,
                logger=logging.getLogger("test"),
                request_id="request-3",
                session_id="session-3",
            )
        ]

    with pytest.raises(AiServiceError) as captured:
        asyncio.run(collect())

    assert captured.value.code == "AI_UNAVAILABLE"
    assert captured.value.retryable is True
    assert llm.calls == 1
