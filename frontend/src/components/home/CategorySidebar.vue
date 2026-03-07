<template>
  <div class="category-sidebar">
    <div class="sidebar-title">商品分类</div>
    <ul class="category-list">
      <li
        v-for="cat in categories"
        :key="cat.id"
        class="category-item"
        @mouseenter="activeCategory = cat.id"
        @mouseleave="activeCategory = null"
        @click="router.push(`/category/${cat.id}`)"
      >
        <div class="item-content">
          <el-icon class="cat-icon"><component :is="getCategoryIcon(cat.name)" /></el-icon>
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
const activeCategory = ref<number | null>(null)

function getCategoryIcon(name: string) {
  const iconMap: Record<string, string> = {
    '水果': 'Apple',
    '蔬菜': 'Food',
    '肉类': 'Chicken',
    '海鲜': 'Ship',
    '蛋奶': 'Coffee',
    '粮油': 'Bowl',
    '零食': 'IceCream',
    '饮品': 'ColdDrink'
  }
  return iconMap[name] || 'Goods'
}

async function loadCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
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
  /* height auto-fits content — no forced stretch */
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

.cat-icon {
  margin-right: 12px;
  font-size: 16px;
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
