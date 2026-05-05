<template>
  <div class="category-sidebar">
    <div class="sidebar-title">商品分类</div>
    <ul class="category-list">
      <li
        v-for="cat in categories"
        :key="cat.id"
        class="category-item"
        @click="router.push(`/category/${cat.id}`)"
      >
        <div class="item-content">
          <span class="cat-name">{{ cat.name }}</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryList } from '@/api/product'
import type { Category } from '@/types'

const router = useRouter()
const categories = ref<Category[]>([])

async function loadCategories() {
  try {
    const res = await getCategoryList()
    categories.value = (res.data || [])
      .filter(cat => cat.status === undefined || cat.status === 1)
      .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
  } catch {
    categories.value = []
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-sidebar {
  background: white;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  height: 100%;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  padding: 12px 20px;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-primary);
  background: linear-gradient(to right, #fcfcfc, #fff);
  border-bottom: 1px solid #eee;
}

.category-list {
  list-style: none;
  padding: 8px 0;
  margin: 0;
  background: #fafbfc;
  flex: 1;
  overflow-y: auto;
}

.category-item {
  padding: 0 16px;
  height: 42px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s;
}

.category-item:hover {
  background-color: #E6F7F0; /* Very light primary */
  color: var(--color-primary);
}

.item-content {
  display: flex;
  align-items: center;
  width: 100%;
  font-size: 14px;
}

.cat-name {
  flex: 1;
}

.arrow-icon {
  font-size: 12px;
  color: #ccc;
}

.category-item:hover .arrow-icon {
  color: var(--color-primary);
}
</style>
