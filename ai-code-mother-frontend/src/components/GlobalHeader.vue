<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

type MenuItem = {
  key: string
  label: string
  path: string
}

const router = useRouter()

const menuItems = ref<MenuItem[]>([
  { key: 'home', label: '首页', path: '/' },
  { key: 'about', label: '关于', path: '/about' },
])

const selectedKeys = ref<string[]>(['home'])

const handleMenuClick = (info: { key: string }) => {
  const target = menuItems.value.find((item) => item.key === info.key)
  if (target) {
    selectedKeys.value = [target.key]
    router.push(target.path)
  }
}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="left">
      <img class="logo" src="@/assets/logo.png" alt="logo" />
      <span class="title">AI 零代码应用生成平台</span>
    </div>

    <div class="center">
      <a-menu
        mode="horizontal"
        theme="light"
        :selected-keys="selectedKeys"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </div>

    <div class="right">
      <a-button type="primary">登录</a-button>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  padding: 0 16px;
  background: #ffffff;
  color: #000000;
  position: relative;
}

.left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.logo {
  width: 32px;
  height: 32px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  white-space: nowrap;
}

.center {
  /* 绝对定位，保证菜单在整个导航栏里严格居中 */
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.right {
  flex-shrink: 0;
  margin-left: auto;
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 8px;
  }

  .center {
    position: static;
    transform: none;
    justify-content: flex-end;
  }

  .title {
    display: none;
  }
}
</style>

