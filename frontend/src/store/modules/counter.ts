import { defineStore } from 'pinia'
import { store } from '@/store'

export const userCounterStore = defineStore('counter', {
  state: () => ({
    count: 0
  }),
  actions: {
    increment() {
      this.count++
    }
  }
})

// Need to be used outside the setup
export const userCounterStoreWithOut = () => {
  return userCounterStore(store);
}