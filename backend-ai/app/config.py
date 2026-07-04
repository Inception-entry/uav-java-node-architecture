from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    service_name: str = "uav-backend-ai"
    ollama_base_url: str = "http://127.0.0.1:11434"
    ollama_model: str = "my-drone-expert"
    ollama_timeout_seconds: float = 120.0

    model_config = SettingsConfigDict(
        env_prefix="AI_",
        env_file=".env",
        extra="ignore",
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
