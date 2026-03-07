<template>
  <div class="product-list-page">
    <div class="header">
      <h2>商品管理</h2>
    </div>
    
    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="商品名称" clearable style="width: 200px" @keyup.enter="loadProducts" />
      <el-select v-model="query.categoryId" placeholder="商品分类" clearable style="width: 150px">
        <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadProducts">搜索</el-button>
    </div>
    
    <!-- 商品表格 -->
    <el-table :data="productList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品" min-width="280">
        <template #default="{ row }">
          <div class="product-cell">
            <img :src="row.mainImage" :alt="row.name" class="product-img" />
            <div class="product-info">
              <p class="name">{{ row.name }}</p>
              <p class="category">{{ row.categoryName }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="merchantName" label="商家" width="120" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="handleView(row)">查看</el-button>
          <el-button v-if="row.status === 1" text type="warning" @click="handleToggleStatus(row, 0)">下架</el-button>
          <el-button v-else text type="success" @click="handleToggleStatus(row, 1)">上架</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadProducts"
      />
    </div>
    
    <!-- 商品详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="商品详情" size="600px">
      <template v-if="currentProduct">
        <div class="product-detail">
          <div class="main-image">
            <img :src="currentProduct.mainImage" :alt="currentProduct.name" />
          </div>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="商品ID">{{ currentProduct.id }}</el-descriptions-item>
            <el-descriptions-item label="商品名称">{{ currentProduct.name }}</el-descriptions-item>
            <el-descriptions-item label="所属分类">{{ currentProduct.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="所属商家">{{ currentProduct.merchantName }}</el-descriptions-item>
            <el-descriptions-item label="商品价格">¥{{ currentProduct.price?.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="商品库存">{{ currentProduct.stock }} {{ currentProduct.unit }}</el-descriptions-item>
            <el-descriptions-item label="销量">{{ currentProduct.sales || 0 }}</el-descriptions-item>
            <el-descriptions-item label="产地">{{ currentProduct.origin || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentProduct.status === 1 ? 'success' : 'info'">
                {{ currentProduct.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentProduct.createTime }}</el-descriptions-item>
            <el-descriptions-item label="商品描述" :span="2">{{ currentProduct.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProductListForAdmin, approveProduct } from '@/api/admin'
import { getCategoryList } from '@/api/product'
import type { Product, Category } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const productList = ref<Product[]>([])
const categoryList = ref<Category[]>([])
const total = ref(0)
const detailDrawerVisible = ref(false)
const currentProduct = ref<Product | null>(null)

const query = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

async function loadCategories() {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch {
    categoryList.value = []
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const res = await getProductListForAdmin({
      keyword: query.keyword,
      categoryId: query.categoryId,
      status: query.status,
      pageNum: query.page,
      pageSize: query.size
    })
    productList.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch {
    productList.value = []
  } finally {
    loading.value = false
  }
}

function handleView(product: Product) {
  currentProduct.value = product
  detailDrawerVisible.value = true
}

async function handleToggleStatus(product: Product, status: number) {
  const action = status === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '确认操作')
    await approveProduct(product.id, status)
    ElMessage.success(`${action}成功`)
    loadProducts()
  } catch {}
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.product-list-page {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.header {
  margin-bottom: 24px;
}

.header h2 {
  font-size: 18px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.product-info .name {
  font-weight: 500;
  margin-bottom: 4px;
}

.product-info .category {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.product-detail .main-image {
  margin-bottom: 24px;
}

.product-detail .main-image img {
  width: 100%;
  max-height: 300px;
  object-fit: cover;
  border-radius: var(--radius-lg);
}
</style>
