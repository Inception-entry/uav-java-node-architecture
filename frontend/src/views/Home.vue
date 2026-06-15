<template>
  <div class="h-screen bg-green-200">
    <overlay v-if="cesiumLoaded">
      <router-view></router-view>
    </overlay>
    <pe-cesium-vue @loaded="loaded" :depthTestAgainstTerrain="true" />
    <HelloWorld msg="Vite + Vue" />
    <pe-tool-header />
    <pe-menu-aside />
  </div>
</template>

<script setup lang="ts">
import overlay from '@/components/pe-overlay/index.vue'
import peCesiumVue from '@/components/pe-cesium-vue/index.vue'
import HelloWorld from '@/components/HelloWorld.vue'
import PeToolHeader from '@/components/pe-tool-header/index.vue'
import PeMenuAside from '@/components/pe-menu-aside/index.vue'
import { ref } from 'vue'

const cesiumLoaded = ref<boolean>(false)
const loaded = (): void => {
  cesiumLoaded.value = true
}
</script>
<style lang="scss" scoped>
.h-screen.bg-green-200 {
  position: relative;
  // width: 100%;
  // height: 100vh;
}
</style>
