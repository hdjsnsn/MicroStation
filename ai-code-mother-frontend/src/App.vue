<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'

import { hello } from '@/api/helloController.ts'

hello().then((res) => {
  console.log(res)
})


</script>

<template>
  <BasicLayout />
</template>

