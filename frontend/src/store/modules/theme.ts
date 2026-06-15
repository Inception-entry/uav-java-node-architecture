// 主题相关
import { defineStore } from 'pinia'
import { store } from '@/store'
import { getTheme, setTheme, removeTheme } from '@/utils/theme';

interface ThemeState {
  theme: object;
  themeKey: string;
}

export const useThemeStore = defineStore('theme', {
  state: ():ThemeState => ({
    theme: {},
    themeKey: '',
  }),
  getters: {
    getTheme(): string {
      return this.themeKey || getTheme()
    },
    getThemeValue(): any {
      return this.theme || {}
    }
  },
  actions: {
    setTheme(info: string) {
      this.themeKey = info ?? ''; // for null or undefined value
      setTheme(info);
    },
    setThemeValue(config: any) {
      this.theme = config ?? '';
    },
    removeTheme() {
      removeTheme();
    }
  }
})

// Need to be used outside the setup
export const useThemeStoreWithOut = () => {
  return useThemeStore(store);
}