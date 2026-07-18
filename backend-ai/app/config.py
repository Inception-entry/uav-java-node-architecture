from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    service_name: str = "uav-backend-ai"
    ollama_base_url: str = "http://127.0.0.1:11434"
    ollama_model: str = "my-drone-expert"
    ollama_embedding_model: str = "nomic-embed-text"
    ollama_timeout_seconds: float = 120.0
    redis_url: str = "redis://127.0.0.1:6380/0"
    chat_history_turns: int = 6
    chat_session_ttl_seconds: int = 86_400
    qdrant_url: str = "http://127.0.0.1:6333"
    qdrant_collection: str = "uav_knowledge"
    knowledge_chunk_size: int = 800
    knowledge_chunk_overlap: int = 120
    knowledge_top_k: int = 4
    knowledge_score_threshold: float = 0.25
    knowledge_max_file_size_bytes: int = 10 * 1024 * 1024

    model_config = SettingsConfigDict(
        env_prefix="AI_",
        env_file=".env",
        extra="ignore",
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
