declare module 'cesium' {
  import('cesium')
  import { ImageryLayer, Viewer } from 'cesium'
  import CesiumLibs from '@/libs/cesium/cesium-libs'
  import ImageryLayerCoordinateTransform from '@/libs/cesium/libs/imagery-layer-coordinate-transform/ImageryLayerCoordinateTransform'

  interface ImageryLayer {
    name: string
    uuid: string
    coordinateTransform: ?ImageryLayerCoordinateTransform
  }

  interface Viewer {
    pe?: CesiumLibs
  }

  interface Model {
    name: string
    uuid: string
    _PriManagFlag: string
  }

  interface Cesium3DTileset {
    name: string
    uuid: string
    _PriManagFlag: string
  }
}
