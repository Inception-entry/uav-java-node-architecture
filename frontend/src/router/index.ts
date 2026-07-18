import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import DroneView from '../views/DroneView.vue'
import ChatView from '../views/ChatView.vue'
import KnowledgeView from '../views/KnowledgeView.vue'
import AuthorizationErrorView from '../views/AuthorizationErrorView.vue'

const routes = [
  {
    path: '/',
    component: Home
  },
  {
    path: '/drone',
    name: 'drone',
    component: DroneView
  },
  {
    path: '/chat',
    name: 'chat',
    component: ChatView
  },
  {
    path: '/knowledge',
    name: 'knowledge',
    component: KnowledgeView
  },
  {
    path: '/401',
    name: 'unauthorized',
    component: AuthorizationErrorView,
    props: { status: 401 }
  },
  {
    path: '/403',
    name: 'forbidden',
    component: AuthorizationErrorView,
    props: { status: 403 }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
