<template>
  <main class="task-page">
    <section class="task-panel">
      <h1>无人机巡检任务</h1>

      <div class="toolbar">
        <input
          v-model.trim="newTaskCode"
          placeholder="输入任务编号，例如 TASK-006"
        />

        <button
          :disabled="loading || !newTaskCode"
          @click="handleStart"
        >
          启动任务
        </button>

        <button :disabled="loading" @click="loadTasks">
          刷新
        </button>
      </div>

      <p v-if="errorMessage" class="error">
        {{ errorMessage }}
      </p>

      <p v-if="loading">加载中……</p>

      <table v-else>
        <thead>
          <tr>
            <th>任务编号</th>
            <th>任务名称</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="task in tasks" :key="task.taskCode">
            <td>{{ task.taskCode }}</td>
            <td>{{ task.taskName }}</td>
            <td>{{ task.status }}</td>
            <td>
              <button
                :disabled="task.status !== 'RUNNING'"
                @click="handleComplete(task.taskCode)"
              >
                完成
              </button>

              <button
                :disabled="task.status !== 'RUNNING'"
                @click="handleCancel(task.taskCode)"
              >
                取消
              </button>
            </td>
          </tr>

          <tr v-if="tasks.length === 0">
            <td colspan="4">暂无巡检任务</td>
          </tr>
        </tbody>
      </table>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  cancelInspectionTask,
  completeInspectionTask,
  getInspectionTasks,
  startInspectionTask,
  type InspectionTask,
} from '@/api/inspection-task'

const tasks = ref<InspectionTask[]>([])
const newTaskCode = ref('')
const loading = ref(false)
const errorMessage = ref('')

const delay = (milliseconds: number) =>
  new Promise(resolve => setTimeout(resolve, milliseconds))

const loadTasks = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    tasks.value = await getInspectionTasks()
  } catch (error) {
    errorMessage.value =
      error instanceof Error ? error.message : '加载失败'
  } finally {
    loading.value = false
  }
}

const handleStart = async () => {
  if (!newTaskCode.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    await startInspectionTask(newTaskCode.value)
    newTaskCode.value = ''

    // 等待 Temporal Activity 写入数据库
    await delay(500)
    await loadTasks()
  } catch (error) {
    errorMessage.value =
      error instanceof Error ? error.message : '启动失败'
    loading.value = false
  }
}

const handleComplete = async (taskCode: string) => {
  loading.value = true

  try {
    await completeInspectionTask(taskCode)
    await delay(500)
    await loadTasks()
  } catch (error) {
    errorMessage.value =
      error instanceof Error ? error.message : '完成失败'
    loading.value = false
  }
}

const handleCancel = async (taskCode: string) => {
  loading.value = true

  try {
    await cancelInspectionTask(taskCode)
    await delay(500)
    await loadTasks()
  } catch (error) {
    errorMessage.value =
      error instanceof Error ? error.message : '取消失败'
    loading.value = false
  }
}

onMounted(loadTasks)
</script>

<style scoped>
.task-page {
  min-height: 100vh;
  padding: 40px;
  box-sizing: border-box;
  background: #f4f6f8;
  color: #1f2937;
}

.task-panel {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background: white;
  border-radius: 10px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

input,
button {
  padding: 8px 12px;
}

input {
  width: 260px;
}

button {
  cursor: pointer;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.error {
  color: #dc2626;
}
</style>