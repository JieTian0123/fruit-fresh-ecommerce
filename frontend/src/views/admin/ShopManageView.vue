<template>
  <div class="shop-manage-page">
    <div class="header"><h2>店铺管理</h2></div>

    <!-- Filter bar -->
    <div class="filter-bar">
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="待审核" :value="2" />
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadShops">搜索</el-button>
    </div>

    <!-- Table -->
    <div class="admin-table-shell is-medium">
      <el-table class="admin-data-table" :data="shopList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column label="店铺" min-width="200">
          <template #default="{ row }">
            <div class="shop-cell">
              <el-avatar :size="36" :src="normalizeImageUrl(row.logo)">{{ row.shopName?.[0] }}</el-avatar>
              <div class="shop-info">
                <p class="name">{{ row.shopName }}</p>
                <p class="phone">{{ row.contactPhone }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="地区" min-width="140" class-name="admin-ellipsis" show-overflow-tooltip>
          <template #default="{ row }">
            {{ [row.province, row.city, row.district].filter(Boolean).join(' ') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="92" class-name="admin-tag-column">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : row.status === 2 ? '待审核' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="168" class-name="admin-nowrap">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="168" class-name="admin-action-column">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button text type="primary" @click="handleView(row)">查看</el-button>
              <el-button v-if="row.status === 2" text type="success" @click="handleApprove(row, 1)">通过</el-button>
              <el-button v-if="row.status === 2" text type="danger" @click="handleApprove(row, 0)">拒绝</el-button>
              <el-button v-if="row.status === 1" text type="danger" @click="handleApprove(row, 0)">禁用</el-button>
              <el-button v-if="row.status === 0" text type="success" @click="handleApprove(row, 1)">启用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadShops"
      />
    </div>

    <!-- Detail Drawer -->
    <el-drawer v-model="detailDrawerVisible" title="店铺详情" size="450px">
      <template v-if="currentShop">
        <div class="shop-detail">
          <div class="avatar-section">
            <el-avatar :size="80" :src="normalizeImageUrl(currentShop.logo)">{{ currentShop.shopName?.[0] }}</el-avatar>
            <h3>{{ currentShop.shopName }}</h3>
            <el-tag :type="currentShop.status === 1 ? 'success' : currentShop.status === 2 ? 'warning' : 'danger'">
              {{ currentShop.status === 1 ? '启用' : currentShop.status === 2 ? '待审核' : '禁用' }}
            </el-tag>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="店铺ID">{{ currentShop.id }}</el-descriptions-item>
            <el-descriptions-item label="商家ID">{{ currentShop.merchantId }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentShop.contactPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="地区">{{ [currentShop.province, currentShop.city, currentShop.district].filter(Boolean).join(' ') || '-' }}</el-descriptions-item>
            <el-descriptions-item label="详细地址">{{ currentShop.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="店铺描述">{{ currentShop.description || '-' }}</el-descriptions-item>
            <el-descriptions-item label="营业执照">{{ currentShop.businessLicense || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(currentShop.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { MerchantShop } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getShopList, getShopDetail, approveShop } from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import { normalizeImageUrl } from '@/utils/image'

const shopList = ref<MerchantShop[]>([])
const loading = ref(false)
const total = ref(0)
const detailDrawerVisible = ref(false)
const currentShop = ref<MerchantShop | null>(null)

const query = reactive({
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

async function loadShops() {
  loading.value = true
  try {
    const res = await getShopList({
      status: query.status,
      pageNum: query.page,
      pageSize: query.size
    })
    shopList.value = (res.data as any)?.records || (res.data as any)?.list || []
    total.value = (res.data as any)?.total || 0
  } catch {
    ElMessage.error('获取店铺列表失败')
  } finally {
    loading.value = false
  }
}

async function handleView(row: MerchantShop) {
  try {
    const res = await getShopDetail(row.id)
    currentShop.value = res.data || row
  } catch {
    currentShop.value = row
  }
  detailDrawerVisible.value = true
}

async function handleApprove(row: MerchantShop, status: number) {
  const actionMap: Record<number, string> = {
    1: row.status === 2 ? '通过该店铺审核' : '启用该店铺',
    0: row.status === 2 ? '拒绝该店铺申请' : '禁用该店铺'
  }
  try {
    await ElMessageBox.confirm(`确定${actionMap[status]}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await approveShop(row.id, status)
    ElMessage.success('操作成功')
    await loadShops()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadShops()
})
</script>

<style scoped>
.shop-manage-page { background: white; border-radius: var(--radius-lg); padding: 24px; }
.header { margin-bottom: 24px; }
.header h2 { font-size: 18px; }
.filter-bar { display: flex; gap: 16px; margin-bottom: 24px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 24px; }
.shop-cell { display: flex; align-items: center; gap: 12px; }
.shop-info { flex: 1; min-width: 0; }
.shop-info .name,
.shop-info .phone {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.shop-info .name { font-weight: 500; }
.shop-info .phone { font-size: 12px; color: var(--color-text-secondary); }
.shop-detail .avatar-section { display: flex; flex-direction: column; align-items: center; gap: 12px; margin-bottom: 24px; }
.shop-detail .avatar-section h3 { margin: 0; }
</style>
