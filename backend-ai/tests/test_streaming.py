from app.main import _message_content, _sse_event


def test_serializes_unicode_sse_event() -> None:
    event = _sse_event("token", {"content": "正在返航"})

    assert event == (
        'event: token\n'
        'data: {"content":"正在返航"}\n\n'
    )


def test_extracts_text_from_langchain_content_blocks() -> None:
    content = _message_content([
        {"type": "text", "text": "低电量"},
        {"type": "tool_call", "name": "ignored"},
        {"type": "text", "text": "立即返航"},
    ])

    assert content == "低电量立即返航"
