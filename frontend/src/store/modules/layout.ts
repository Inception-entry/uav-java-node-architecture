// 布局相关
import { defineStore } from 'pinia'
import { store } from '@/store'
import { getLayout, setLayout, removeLayout } from '@/utils/layout';

interface LayoutState {
  layoutKey: string;
  asideStatus: boolean;
  headerStatus: boolean;
}

export const useLayoutStore = defineStore('layout', {
  state: ():LayoutState => ({
    layoutKey: 'topLeft',
    asideStatus: false,
    headerStatus: false
  }),
  getters: {
    getLayout(): string {
      return this.layoutKey || getLayout()
    },
    getAsideStatus(): boolean {
      return this.asideStatus
    },
    getHeaderStatus(): boolean {
      return this.headerStatus
    },
  },
  actions: {
    setLayout(info: string) {
      this.layoutKey = info ?? ''; // for null or undefined value
      setLayout(info);
    },
    removeLayout() {
      removeLayout();
    },
    setAsideStatus(status: boolean) {
      this.asideStatus = status ?? ''
    },
    setHeaderStatus(status: boolean) {
      this.headerStatus = status ?? ''
    }
  }
})

// Need to be used outside the setup
export const useLayoutStoreWithOut = () => {
  return useLayoutStore(store);
}