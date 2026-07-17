from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    service_name: str = "uav-backend-ai"
    ollama_base_url: str = "http://127.0.0.1:11434"
    ollama_model: str = "my-drone-expert"
    ollama_timeout_seconds: float = 120.0
    redis_url: str = "redis://127.0.0.1:6380/0"
    chat_history_turns: int = 6
    chat_session_ttl_seconds: int = 86_400

    model_config = SettingsConfigDict(
        env_prefix="AI_",
        env_file=".env",
        extra="ignore",
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
