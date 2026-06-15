import i18next from 'i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import zhData from './locales/zh'
import enData from './locales/en'
const callback = () => {}
i18next
  // detect user language
  // learn more: https://github.com/i18next/i18next-browser-languageDetector
  .use(LanguageDetector)
  // init i18next
  // for all options read: https://www.i18next.com/overview/configuration-options
  .init({
    debug: true,
    lng: 'zh',
    supportedLngs: ['zh', 'en'],
    fallbackLng: 'zh',
    detection: {
      order: ['cookie', 'localStorage', 'sessionStorage', 'querystring', 'navigator', 'htmlTag', 'path', 'subdomain'],
      lookupFromPathIndex: 0,
      caches: ['cookie']
    },
    interpolation: {
      escapeValue: false // 不转义 HTML 中的内容
    },
    resources: {
      en: {
        translation: enData
      },
      zh: {
        translation: zhData
      }
    },
  }, callback);

export default i18next