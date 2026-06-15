import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import DroneView from '../views/DroneView.vue'

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
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router