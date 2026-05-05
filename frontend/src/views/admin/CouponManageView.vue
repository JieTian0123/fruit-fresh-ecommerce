<template>
  <div class="coupon-manage-page">
    <div class="header">
      <h2>优惠券管理</h2>
      <div class="header-actions">
        <el-button type="danger" :disabled="selectedCoupons.length === 0" @click="handleBatchDelete">
          删除选中
        </el-button>
        <el-button type="primary" @click="handleAdd">添加优惠券</el-button>
      </div>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="优惠券名称"
        clearable
        style="width: 200px"
        @keyup.enter="loadCoupons"
      />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
        <el-option label="已结束" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadCoupons">搜索</el-button>
    </div>

    <!-- Table -->
    <div class="admin-table-shell is-wide">
      <el-table
        class="admin-data-table"
        :data="couponList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="38" />
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="title" label="名称" min-width="210" class-name="admin-ellipsis" show-overflow-tooltip />
        <el-table-column label="类型" width="98" class-name="admin-tag-column">
          <template #default="{ row }">
            <el-tag
              :type="row.couponType === 1 ? 'danger' : row.couponType === 2 ? 'warning' : 'success'"
            >
              {{ couponTypeText(row.couponType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠" width="76">
          <template #default="{ row }">
            {{ row.couponType === 2 ? (row.discountRate * 10) + '折' : '¥' + row.discountAmount }}
          </template>
        </el-table-column>
        <el-table-column label="门槛" width="84">
          <template #default="{ row }">
            {{ row.minimumAmount ? '满¥' + row.minimumAmount : '无门槛' }}
          </template>
        </el-table-column>
        <el-table-column label="领取/总量" width="86">
          <template #default="{ row }">
            {{ row.receivedQuantity }} / {{ row.totalQuantity }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="82" class-name="admin-tag-column">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : row.status === 0 ? 'danger' : 'info'"
            >
              {{ row.status === 1 ? '启用' : row.status === 0 ? '禁用' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="168" class-name="admin-nowrap">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="172" class-name="admin-action-column">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 0" text type="success" @click="handleToggleStatus(row, 1)">启用</el-button>
              <el-button v-if="row.status === 1" text type="warning" @click="handleToggleStatus(row, 0)">禁用</el-button>
              <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
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
        @change="loadCoupons"
      />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑优惠券' : '添加优惠券'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.title" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.couponType" style="width: 100%">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="无门槛券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.couponType !== 2" label="优惠金额">
          <el-input-number v-model="form.discountAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item v-if="form.couponType === 2" label="折扣率">
          <el-input-number v-model="form.discountRate" :min="0.1" :max="0.99" :step="0.1" :precision="2" />
        </el-form-item>
        <el-form-item label="最低消费">
          <el-input-number v-model="form.minimumAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="发行总量" required>
          <el-input-number v-model="form.totalQuantity" :min="1" />
        </el-form-item>
        <el-form-item label="每人限领">
          <el-input-number v-model="form.perUserLimit" :min="1" />
        </el-form-item>
        <el-form-item label="使用说明">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>

        <!-- 有效期设置 -->
        <el-divider content-position="left">有效期设置</el-divider>
        <el-form-item label="有效期类型">
          <el-radio-group v-model="validityType">
            <el-radio label="fixed">固定时间</el-radio>
            <el-radio label="days">领取后N天</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="validityType === 'fixed'" label="有效期" required>
          <el-date-picker
            v-model="validDateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="validityType === 'days'" label="有效天数" required>
          <el-input-number v-model="form.validDays" :min="1" :max="365" />
          <span style="margin-left: 8px; color: #909399">天（从领取时开始计算）</span>
        </el-form-item>

        <!-- 积分兑换设置 -->
        <el-divider content-position="left">领取限制</el-divider>
        <el-form-item label="兑换积分">
          <el-input-number v-model="form.pointsPrice" :min="0" placeholder="0表示免费领取" />
          <span style="margin-left: 8px; color: #909399">大于0时普通用户需积分兑换</span>
        </el-form-item>
        <el-form-item label="VIP免积分">
          <el-switch
            v-model="form.vipFreeReceive"
            :active-value="1"
            :inactive-value="0"
            active-text="是"
            inactive-text="否"
          />
          <span style="margin-left: 8px; color: #909399">开启后VIP会员可免积分领取一次</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { Coupon } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/format'

import { getCouponList, addCoupon, updateCoupon, deleteCoupon, updateCouponStatus } from '@/api/admin'
import { batchDeleteSelected } from '@/utils/batchDelete'

const couponList = ref<Coupon[]>([])
const selectedCoupons = ref<Coupon[]>([])
const loading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)
const submitLoading = ref(false)

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

// 有效期类型：fixed=固定时间，days=领取后N天
const validityType = ref<'fixed' | 'days'>('fixed')
const validDateRange = ref<[string, string] | null>(null)

const form = reactive({
  title: '',
  couponType: 1,
  discountAmount: 0,
  discountRate: 0.9,
  minimumAmount: 0,
  totalQuantity: 100,
  perUserLimit: 1,
  description: '',
  // 有效期相关
  validFrom: '' as string,
  validUntil: '' as string,
  validDays: null as number | null,
  // 积分兑换相关
  pointsPrice: null as number | null,
  vipFreeReceive: 0  // 0-否 1-是
})

function couponTypeText(type: number) {
  return type === 1 ? '满减券' : type === 2 ? '折扣券' : '无门槛券'
}

function resetForm() {
  form.title = ''
  form.couponType = 1
  form.discountAmount = 0
  form.discountRate = 0.9
  form.minimumAmount = 0
  form.totalQuantity = 100
  form.perUserLimit = 1
  form.description = ''
  form.validFrom = ''
  form.validUntil = ''
  form.validDays = null
  form.pointsPrice = null
  form.vipFreeReceive = 0
  validityType.value = 'fixed'
  validDateRange.value = null
}

async function loadCoupons() {
  loading.value = true
  try {
    const res = await getCouponList({
      pageNum: query.page,
      pageSize: query.size,
      status: query.status,
      keyword: query.keyword
    })
    couponList.value = (res.data as any)?.records || (res.data as any)?.list || []
    total.value = (res.data as any)?.total || 0
  } catch {
    ElMessage.error('获取优惠券列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  resetForm()
  isEdit.value = false
  editId.value = 0
  dialogVisible.value = true
}

function handleEdit(row: Coupon) {
  isEdit.value = true
  editId.value = row.id
  form.title = row.title
  form.couponType = row.couponType
  form.discountAmount = row.discountAmount ?? 0
  form.discountRate = row.discountRate ?? 0
  form.minimumAmount = row.minimumAmount ?? 0
  form.totalQuantity = row.totalQuantity
  form.perUserLimit = row.perUserLimit
  form.description = row.description || ''
  form.pointsPrice = (row as any).pointsPrice ?? null
  form.vipFreeReceive = (row as any).vipFreeReceive ?? 0

  // 处理有效期
  const validFrom = (row as any).validFrom
  const validUntil = (row as any).validUntil
  const validDays = (row as any).validDays

  if (validDays && validDays > 0) {
    validityType.value = 'days'
    form.validDays = validDays
    validDateRange.value = null
  } else if (validFrom && validUntil) {
    validityType.value = 'fixed'
    form.validFrom = validFrom
    form.validUntil = validUntil
    validDateRange.value = [validFrom, validUntil]
  } else {
    validityType.value = 'fixed'
    validDateRange.value = null
  }

  dialogVisible.value = true
}

function handleSelectionChange(selection: Coupon[]) {
  selectedCoupons.value = selection
}

async function handleSubmit() {
  if (!form.title) {
    ElMessage.warning('请输入优惠券名称')
    return
  }
  if (!form.totalQuantity) {
    ElMessage.warning('请输入发行总量')
    return
  }

  // 验证有效期
  if (validityType.value === 'fixed') {
    if (!validDateRange.value || !validDateRange.value[0] || !validDateRange.value[1]) {
      ElMessage.warning('请选择有效期时间范围')
      return
    }
    form.validFrom = validDateRange.value[0]
    form.validUntil = validDateRange.value[1]
    form.validDays = null
  } else {
    if (!form.validDays || form.validDays <= 0) {
      ElMessage.warning('请输入有效天数')
      return
    }
    form.validFrom = ''
    form.validUntil = ''
  }

  submitLoading.value = true
  try {
    const submitData = {
      ...form,
      validFrom: form.validFrom || null,
      validUntil: form.validUntil || null
    }
    if (isEdit.value) {
      await updateCoupon({ ...submitData, id: editId.value })
      ElMessage.success('更新成功')
    } else {
      await addCoupon(submitData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await loadCoupons()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row: Coupon, status: number) {
  const label = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}优惠券「${row.title}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateCouponStatus(row.id, status)
    ElMessage.success(`${label}成功`)
    await loadCoupons()
  } catch {
    // cancelled
  }
}

async function handleDelete(row: Coupon) {
  try {
    await ElMessageBox.confirm(`确定删除优惠券「${row.title}」吗？此操作不可撤销。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCoupon(row.id)
    ElMessage.success('删除成功')
    await loadCoupons()
  } catch {
    // cancelled
  }
}

async function handleBatchDelete() {
  await batchDeleteSelected({
    items: selectedCoupons.value,
    label: '优惠券',
    deleteOne: deleteCoupon,
    afterDelete: loadCoupons
  })
  selectedCoupons.value = []
}

onMounted(() => {
  loadCoupons()
})
</script>

<style scoped>
.coupon-manage-page {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
