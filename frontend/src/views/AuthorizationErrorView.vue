<template>
  <main class="authorization-page">
    <section class="authorization-card">
      <div class="status-code">{{ status }}</div>
      <p class="eyebrow">访问控制</p>
      <h1>{{ title }}</h1>
      <p class="description">{{ description }}</p>

      <div class="identity">
        <span>当前用户</span>
        <strong>{{ authenticationState.username || '未知用户' }}</strong>
        <span>当前角色</span>
        <strong>{{ displayedRoles || '无业务角色' }}</strong>
      </div>

      <div class="actions">
        <a-button type="primary" @click="handlePrimaryAction">
          {{ status === 401 ? '重新登录' : '返回巡检任务' }}
        </a-button>
        <a-button @click="goHome">返回首页</a-button>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  authenticationState,
  reauthenticate,
} from '@/auth/keycloak'
import { resolveSafeRedirect } from '@/auth/authorization-navigation'

const props = defineProps<{
  status: 401 | 403
}>()
const route = useRoute()
const router = useRouter()

const title = computed(() => props.status === 401
  ? '登录状态已失效'
  : '没有访问权限')
const description = computed(() => props.status === 401
  ? '访问令牌缺失、失效或无法通过身份校验，请重新登录。'
  : '当前账号已登录，但没有执行该操作所需的业务角色。')
const displayedRoles = computed(() => authenticationState.roles
  .filter((role) => ['ADMIN', 'OPERATOR', 'VIEWER'].includes(role))
  .join(', '))
const redirectPath = computed(() => resolveSafeRedirect(
  route.query.redirect,
  '/drone',
))

const handlePrimaryAction = () => {
  if (props.status === 401) {
    void reauthenticate(redirectPath.value)
    return
  }
  void router.push(redirectPath.value)
}

const goHome = () => {
  void router.push('/')
}
</script>

<style scoped>
.authorization-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  color: #172033;
  background:
    radial-gradient(circle at top right, #dbeafe 0, transparent 36%),
    linear-gradient(145deg, #f8fafc, #e2e8f0);
}

.authorization-card {
  width: min(560px, 100%);
  padding: 44px;
  background: rgb(255 255 255 / 92%);
  border: 1px solid #dbe3ef;
  border-radius: 18px;
  box-shadow: 0 24px 70px rgb(15 23 42 / 14%);
}

.status-code {
  color: #1677ff;
  font-size: clamp(72px, 18vw, 132px);
  font-weight: 800;
  line-height: 0.85;
  letter-spacing: -8px;
}

.eyebrow {
  margin: 30px 0 8px;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

h1 {
  margin: 0;
  font-size: 30px;
}

.description {
  margin: 14px 0 24px;
  color: #64748b;
  line-height: 1.7;
}

.identity {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 10px 16px;
  padding: 18px;
  background: #f8fafc;
  border-radius: 10px;
}

.identity span {
  color: #64748b;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
}

@media (max-width: 560px) {
  .authorization-card {
    padding: 30px 24px;
  }

  .identity {
    grid-template-columns: 1fr;
  }
}
</style>
