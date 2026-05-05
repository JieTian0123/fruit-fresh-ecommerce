<template>
  <div class="product-list-page">
    <div class="header">
      <h2>商品管理</h2>
      <div class="header-actions">
        <el-button type="danger" :disabled="selectedProducts.length === 0" @click="handleBatchDelete">
          删除选中
        </el-button>
        <el-button type="primary" @click="$router.push('/merchant/products/add')">
          <el-icon><Plus /></el-icon> 添加商品
        </el-button>
      </div>
    </div>
    
    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索商品名称" clearable style="width: 240px" @keyup.enter="loadProducts" />
      <el-select v-model="query.status" placeholder="商品状态" clearable style="width: 120px" @change="loadProducts">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadProducts">搜索</el-button>
    </div>
    
    <!-- 商品表格 -->
    <el-table
      class="admin-data-table"
      :data="productList"
      v-loading="loading"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="38" />
      <el-table-column label="商品" min-width="260">
        <template #default="{ row }">
          <div class="product-cell">
            <img :src="row.mainImage" :alt="row.name" class="product-img" @error="(e: Event) => (e.target as HTMLImageElement).src = defaultImage" />
            <div class="product-info">
              <p class="name">{{ row.name }}</p>
              <p class="category">{{ row.categoryName }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="86">
        <template #default="{ row }">
          <span class="price">¥{{ row.price?.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="68" />
      <el-table-column prop="sales" label="销量" width="68" />
      <el-table-column prop="status" label="状态" width="76">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="172" class-name="admin-action-column">
        <template #default="{ row }">
          <div class="admin-table-actions">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" text type="warning" size="small" @click="handleToggleStatus(row, 0)">下架</el-button>
            <el-button v-else text type="success" size="small" @click="handleToggleStatus(row, 1)">上架</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMerchantProductList, onSaleProduct, offSaleProduct } from '@/api/merchant'
import { deleteProduct } from '@/api/product'
import type { Product } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { batchDeleteSelected } from '@/utils/batchDelete'

const defaultImage = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'%3E%3Crect fill='%23f5f5f5' width='200' height='200'/%3E%3Ctext x='100' y='110' text-anchor='middle' font-size='48'%3E🍊%3C/text%3E%3C/svg%3E"

const router = useRouter()

const loading = ref(false)
const productList = ref<Product[]>([])
const selectedProducts = ref<Product[]>([])
const total = ref(0)
const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

async function loadProducts() {
  loading.value = true
  try {
    const res = await getMerchantProductList({
      keyword: query.keyword,
      status: query.status,
      pageNum: query.page,
      pageSize: query.size
    })
    productList.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    productList.value = []
  } finally {
    loading.value = false
  }
}

function handleEdit(product: Product) {
  router.push(`/merchant/products/edit/${product.id}`)
}

function handleSelectionChange(selection: Product[]) {
  selectedProducts.value = selection
}

async function handleToggleStatus(product: Product, status: number) {
  const action = status === 1 ? '上架' : '下架'
  await ElMessageBox.confirm(`确定要${action}该商品吗？`, '确认操作')
  try {
    if (status === 1) {
      await onSaleProduct(product.id)
    } else {
      await offSaleProduct(product.id)
    }
    ElMessage.success(`${action}成功`)
    loadProducts()
  } catch {}
}

async function handleDelete(product: Product) {
  await ElMessageBox.confirm('确定要删除该商品吗？此操作不可恢复', '删除商品', { type: 'warning' })
  try {
    await deleteProduct(product.id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch {}
}

async function handleBatchDelete() {
  await batchDeleteSelected({
    items: selectedProducts.value,
    label: '商品',
    deleteOne: deleteProduct,
    afterDelete: loadProducts
  })
  selectedProducts.value = []
}

onMounted(() => {
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.header h2 {
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-info {
  flex: 1;
  min-width: 0;
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
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-info .category {
  font-size: 12px;
  color: var(--color-text-secondary);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  font-weight: 600;
  color: var(--color-secondary);
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

</style>
