/// <reference types="vite/client" />
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
