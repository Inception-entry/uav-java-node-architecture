#!/usr/bin/env python3

"""Minimal Ollama API used only by the Docker integration test."""

import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


HOST = os.getenv("OLLAMA_MOCK_HOST", "127.0.0.1")
PORT = int(os.getenv("OLLAMA_MOCK_PORT", "11434"))
CHAT_MODEL = os.getenv("AI_OLLAMA_MODEL", "my-drone-expert")
EMBEDDING_MODEL = os.getenv(
    "AI_OLLAMA_EMBEDDING_MODEL",
    "nomic-embed-text",
)


class OllamaMockHandler(BaseHTTPRequestHandler):
    def do_GET(self) -> None:
        if self.path == "/api/tags":
            self._json_response(
                200,
                {
                    "models": [
                        {"name": f"{CHAT_MODEL}:latest"},
                        {"name": f"{EMBEDDING_MODEL}:latest"},
                    ]
                },
            )
            return
        if self.path == "/api/version":
            self._json_response(200, {"version": "ci-mock"})
            return
        self._json_response(404, {"error": "not found"})

    def log_message(self, format: str, *args: object) -> None:
        print(
            f"ollama-mock client={self.client_address[0]} "
            f"message={format % args}",
            flush=True,
        )

    def _json_response(self, status: int, payload: dict[str, object]) -> None:
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


if __name__ == "__main__":
    print(f"ollama-mock listening on http://{HOST}:{PORT}", flush=True)
    ThreadingHTTPServer((HOST, PORT), OllamaMockHandler).serve_forever()
