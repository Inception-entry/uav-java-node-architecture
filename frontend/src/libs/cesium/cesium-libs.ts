import * as Cesium from 'cesium'

// cesium 工具类
class CesiumLibs {
  protected viewer: Cesium.Viewer
  constructor(viewer: Cesium.Viewer) {
    this.viewer = viewer
  }
}

export default CesiumLibs
