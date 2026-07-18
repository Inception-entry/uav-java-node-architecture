import asyncio
from types import SimpleNamespace
from unittest.mock import AsyncMock

from app.config import Settings
from app.knowledge_base import KnowledgeBase


class FakeEmbeddings:
    async def aembed_documents(self, texts: list[str]) -> list[list[float]]:
        return [[1.0, float(index), 0.5] for index, _ in enumerate(texts)]

    async def aembed_query(self, text: str) -> list[float]:
        return [1.0, float("返航" in text), 0.5]


def test_imports_utf8_document_as_qdrant_points() -> None:
    client = SimpleNamespace(
        collection_exists=AsyncMock(return_value=False),
        create_collection=AsyncMock(),
        delete=AsyncMock(),
        upsert=AsyncMock(),
    )
    knowledge_base = KnowledgeBase(
        Settings(knowledge_chunk_size=20, knowledge_chunk_overlap=4),
        client=client,
        embeddings=FakeEmbeddings(),
    )

    document = asyncio.run(
        knowledge_base.import_document(
            "flight-manual.md",
            "text/markdown",
            "低电量告警后应确认返航点。\n\n返航前检查航线和剩余电量。".encode(),
        )
    )

    assert document.filename == "flight-manual.md"
    assert document.chunk_count >= 1
    client.create_collection.assert_awaited_once()
    client.delete.assert_awaited_once()
    client.upsert.assert_awaited_once()
    points = client.upsert.await_args.kwargs["points"]
    assert len(points) == document.chunk_count
    assert points[0].payload["document_id"] == document.document_id
    assert points[0].payload["filename"] == "flight-manual.md"


def test_search_maps_qdrant_payload_to_traceable_result() -> None:
    client = SimpleNamespace(
        collection_exists=AsyncMock(return_value=True),
        query_points=AsyncMock(
            return_value=SimpleNamespace(
                points=[
                    SimpleNamespace(
                        score=0.91,
                        payload={
                            "document_id": "doc-001",
                            "filename": "fault-guide.pdf",
                            "text": "图传中断后先保持航向并检查链路。",
                            "page": 8,
                            "chunk_index": 3,
                        },
                    )
                ]
            )
        ),
    )
    knowledge_base = KnowledgeBase(
        Settings(),
        client=client,
        embeddings=FakeEmbeddings(),
    )

    results = asyncio.run(knowledge_base.search("图传中断如何返航？", top_k=3))

    assert len(results) == 1
    assert results[0].document_id == "doc-001"
    assert results[0].filename == "fault-guide.pdf"
    assert results[0].page == 8
    assert results[0].score == 0.91
    assert client.query_points.await_args.kwargs["limit"] == 3
