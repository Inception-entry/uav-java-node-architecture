// 语言相关
import { defineStore } from 'pinia'
import { store } from '@/store'
import { getLang, setLang, removeLang } from '@/utils/lang';

interface ThemeState {
  langKey: string;
}

export const useLangStore = defineStore('lang', {
  state: ():ThemeState => ({
    langKey: '',
  }),
  getters: {
    getLang(): string {
      return this.langKey || getLang()
    }
  },
  actions: {
    setLang(info: string) {
      this.langKey = info ?? ''; // for null or undefined value
      setLang(info);
    },
    removeLang() {
      removeLang();
    }
  }
})

// Need to be used outside the setup
export const useLangStoreWithOut = () => {
  return useLangStore(store);
}