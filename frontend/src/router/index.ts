import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import DroneView from '../views/DroneView.vue'
import ChatView from '../views/ChatView.vue'

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
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
