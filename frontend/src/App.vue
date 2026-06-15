<template>
  <a-config-provider
    :theme="currentTheme"
  >
    <router-view />
  </a-config-provider>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/store/modules/theme'
;
const themeStore = useThemeStore()
const currentTheme = computed(() => themeStore.getThemeValue);

</script>
