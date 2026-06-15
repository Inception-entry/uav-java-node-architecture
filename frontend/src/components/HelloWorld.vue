

<template>
  <div class="hello_world_wrapper">
    <h1>{{ msg }}</h1>

    <div class="card">
      <a-button type="primary" @click="count++">count is {{ count }}</a-button>
    </div>
    <p class="read-the-docs">{{ $t('desc') }}</p>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
defineProps<{ msg: string }>()
const count = ref(0)
</script>
<style scoped lang="scss">
.hello_world_wrapper {
  text-align: center;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translateX(-50%) translateY(-50%);

  a {
    font-weight: 500;
    color: #646cff;
    text-decoration: inherit;
  }
  a:hover {
    color: #535bf2;
  }
  h1 {
    font-size: 3.2em;
    line-height: 1.1;
  }

  button {
    border-radius: 8px;
    border: 1px solid transparent;
    font-size: 1em;
    font-weight: 500;
    font-family: inherit;
    cursor: pointer;
    transition: border-color 0.25s;
  }
  button:hover {
    border-color: #646cff;
  }
  button:focus,
  button:focus-visible {
    outline: 4px auto -webkit-focus-ring-color;
  }

  .card {
    padding: 2em;
  }
  .read-the-docs {
    color: variables.$primary-color;
  }
}
</style>