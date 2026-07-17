/// <reference types="vite/client" />
interface ImportMetaEnv {
  readonly VITE_KEYCLOAK_URL?: string
  readonly VITE_KEYCLOAK_REALM?: string
  readonly VITE_KEYCLOAK_CLIENT_ID?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

export declare global {
  interface Window {
    Cesium: any,
    viewer: any,
    setTimeout: any
  }
  interface Document {
    mozFullScreenElement: any,
    webkitFullscreenElement: any,
    msFullscreenElement: any
    msExitFullscreen: any,
    mozCancelFullScreen: any,
    webkitExitFullscreen: any,
  }
}
