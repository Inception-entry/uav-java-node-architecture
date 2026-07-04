# UAV AI Service

本地 AI 服务，通过 LangChain 调用 Ollama 中的无人机模型。

## 启动

```bash
cd backend-ai
uv sync
uv run uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## 验证

```bash
curl http://localhost:8000/

curl http://localhost:8000/health

curl -X POST http://localhost:8000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"无人机失联后应该如何处理？"}'
```

## Docker 启动

在项目根目录执行：

```bash
./scripts/uav.sh rebuild backend-ai
./scripts/uav.sh logs backend-ai
```
