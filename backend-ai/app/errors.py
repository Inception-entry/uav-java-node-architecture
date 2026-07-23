import asyncio
from http import HTTPStatus
from typing import Any

import httpx


class AiServiceError(Exception):
    def __init__(
        self,
        code: str,
        message: str,
        *,
        retryable: bool,
        http_status: int,
    ) -> None:
        super().__init__(message)
        self.code = code
        self.safe_message = message
        self.retryable = retryable
        self.http_status = http_status

    def payload(self, request_id: str) -> dict[str, Any]:
        return {
            "code": self.code,
            "message": self.safe_message,
            "retryable": self.retryable,
            "requestId": request_id,
        }


def classify_model_exception(exception: BaseException) -> AiServiceError:
    chain = _exception_chain(exception)

    if any(
        isinstance(
            item,
            (
                asyncio.TimeoutError,
                TimeoutError,
                httpx.TimeoutException,
            ),
        )
        for item in chain
    ):
        return AiServiceError(
            "AI_TIMEOUT",
            "模型响应超时，请稍后重试",
            retryable=True,
            http_status=HTTPStatus.GATEWAY_TIMEOUT,
        )

    if any(
        isinstance(
            item,
            (
                httpx.ConnectError,
                ConnectionError,
            ),
        )
        for item in chain
    ):
        return AiServiceError(
            "AI_UNAVAILABLE",
            "暂时无法连接本地模型，请稍后重试",
            retryable=True,
            http_status=HTTPStatus.SERVICE_UNAVAILABLE,
        )

    upstream_status = _upstream_status(chain)
    if upstream_status == HTTPStatus.TOO_MANY_REQUESTS:
        return AiServiceError(
            "AI_RATE_LIMITED",
            "模型请求过于频繁，请稍后重试",
            retryable=True,
            http_status=HTTPStatus.TOO_MANY_REQUESTS,
        )
    if upstream_status in {
        HTTPStatus.REQUEST_TIMEOUT,
        HTTPStatus.GATEWAY_TIMEOUT,
    }:
        return AiServiceError(
            "AI_TIMEOUT",
            "模型响应超时，请稍后重试",
            retryable=True,
            http_status=HTTPStatus.GATEWAY_TIMEOUT,
        )
    if upstream_status is not None and upstream_status >= 500:
        return AiServiceError(
            "AI_UPSTREAM_ERROR",
            "本地模型暂时不可用，请稍后重试",
            retryable=True,
            http_status=HTTPStatus.BAD_GATEWAY,
        )
    if upstream_status is not None and upstream_status >= 400:
        return AiServiceError(
            "AI_REQUEST_REJECTED",
            "本地模型拒绝了本次请求",
            retryable=False,
            http_status=HTTPStatus.BAD_GATEWAY,
        )

    return AiServiceError(
        "AI_INVOCATION_FAILED",
        "模型调用失败，请稍后重试",
        retryable=False,
        http_status=HTTPStatus.BAD_GATEWAY,
    )


def context_service_error() -> AiServiceError:
    return AiServiceError(
        "AI_CONTEXT_UNAVAILABLE",
        "AI 上下文服务暂时不可用，请稍后重试",
        retryable=True,
        http_status=HTTPStatus.SERVICE_UNAVAILABLE,
    )


def _exception_chain(exception: BaseException) -> list[BaseException]:
    result: list[BaseException] = []
    current: BaseException | None = exception
    visited: set[int] = set()
    while current is not None and id(current) not in visited:
        result.append(current)
        visited.add(id(current))
        current = current.__cause__ or current.__context__
    return result


def _upstream_status(
    chain: list[BaseException],
) -> int | None:
    for item in chain:
        if isinstance(item, httpx.HTTPStatusError):
            return item.response.status_code
        value = getattr(item, "status_code", None)
        if isinstance(value, int):
            return value
    return None
