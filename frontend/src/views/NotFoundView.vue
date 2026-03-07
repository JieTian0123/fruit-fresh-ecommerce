<template>
  <div class="not-found-page">
    <div class="content">
      <h1>404</h1>
      <p class="title">页面不存在</p>
      <p class="description">抱歉，您访问的页面不存在或已被移除</p>
      <el-button type="primary" size="large" @click="router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.content {
  text-align: center;
}

.content h1 {
  font-size: 120px;
  font-weight: 700;
  color: var(--color-primary);
  margin: 0;
  line-height: 1;
}

.title {
  font-size: 24px;
  color: var(--color-text-primary);
  margin: 16px 0 8px;
}

.description {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin-bottom: 32px;
}
</style>
