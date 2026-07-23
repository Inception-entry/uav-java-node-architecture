import asyncio
import logging
from collections.abc import AsyncIterator
from time import perf_counter
from typing import Any

from app.errors import AiServiceError, classify_model_exception
from app.observability import log_event


async def invoke_model_with_retry(
    llm: Any,
    messages: list[Any],
    *,
    timeout_seconds: float,
    max_attempts: int,
    initial_backoff_seconds: float,
    max_backoff_seconds: float,
    logger: logging.Logger,
    request_id: str,
    session_id: str,
) -> Any:
    backoff = initial_backoff_seconds
    for attempt in range(1, max_attempts + 1):
        started_at = perf_counter()
        try:
            async with asyncio.timeout(timeout_seconds):
                response = await llm.ainvoke(messages)
            log_event(
                logger,
                logging.INFO,
                "ai_model_call_success",
                request_id=request_id,
                operation="chat",
                session_id=session_id,
                attempt=attempt,
                duration_ms=_elapsed_ms(started_at),
            )
            return response
        except asyncio.CancelledError:
            raise
        except Exception as exception:
            error = classify_model_exception(exception)
            if not error.retryable or attempt >= max_attempts:
                _log_failure(
                    logger,
                    "chat",
                    request_id,
                    session_id,
                    attempt,
                    started_at,
                    error,
                    exception,
                    stream_started=False,
                )
                raise error from exception

            log_event(
                logger,
                logging.WARNING,
                "ai_model_call_retry",
                request_id=request_id,
                operation="chat",
                session_id=session_id,
                attempt=attempt,
                next_attempt=attempt + 1,
                duration_ms=_elapsed_ms(started_at),
                backoff_ms=round(backoff * 1000),
                error_code=error.code,
                retryable=error.retryable,
                exception_type=type(exception).__name__,
                stream_started=False,
            )
            await asyncio.sleep(backoff)
            backoff = min(backoff * 2, max_backoff_seconds)

    raise RuntimeError("unreachable retry state")


async def stream_model_with_retry(
    llm: Any,
    messages: list[Any],
    *,
    timeout_seconds: float,
    max_attempts: int,
    initial_backoff_seconds: float,
    max_backoff_seconds: float,
    logger: logging.Logger,
    request_id: str,
    session_id: str,
) -> AsyncIterator[Any]:
    backoff = initial_backoff_seconds
    for attempt in range(1, max_attempts + 1):
        started_at = perf_counter()
        stream_started = False
        try:
            async with asyncio.timeout(timeout_seconds):
                async for chunk in llm.astream(messages):
                    stream_started = True
                    yield chunk
            log_event(
                logger,
                logging.INFO,
                "ai_model_call_success",
                request_id=request_id,
                operation="chat_stream",
                session_id=session_id,
                attempt=attempt,
                duration_ms=_elapsed_ms(started_at),
                stream_started=stream_started,
            )
            return
        except asyncio.CancelledError:
            raise
        except Exception as exception:
            error = classify_model_exception(exception)
            can_retry = (
                error.retryable
                and not stream_started
                and attempt < max_attempts
            )
            if not can_retry:
                _log_failure(
                    logger,
                    "chat_stream",
                    request_id,
                    session_id,
                    attempt,
                    started_at,
                    error,
                    exception,
                    stream_started=stream_started,
                )
                raise error from exception

            log_event(
                logger,
                logging.WARNING,
                "ai_model_call_retry",
                request_id=request_id,
                operation="chat_stream",
                session_id=session_id,
                attempt=attempt,
                next_attempt=attempt + 1,
                duration_ms=_elapsed_ms(started_at),
                backoff_ms=round(backoff * 1000),
                error_code=error.code,
                retryable=error.retryable,
                exception_type=type(exception).__name__,
                stream_started=False,
            )
            await asyncio.sleep(backoff)
            backoff = min(backoff * 2, max_backoff_seconds)


def _log_failure(
    logger: logging.Logger,
    operation: str,
    request_id: str,
    session_id: str,
    attempt: int,
    started_at: float,
    error: AiServiceError,
    exception: Exception,
    *,
    stream_started: bool,
) -> None:
    log_event(
        logger,
        logging.ERROR,
        "ai_model_call_failed",
        request_id=request_id,
        operation=operation,
        session_id=session_id,
        attempt=attempt,
        duration_ms=_elapsed_ms(started_at),
        error_code=error.code,
        retryable=error.retryable,
        exception_type=type(exception).__name__,
        stream_started=stream_started,
    )


def _elapsed_ms(started_at: float) -> int:
    return round((perf_counter() - started_at) * 1000)
