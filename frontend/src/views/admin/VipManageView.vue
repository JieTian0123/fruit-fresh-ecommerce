<template>
  <div class="vip-manage-page">
    <div class="header">
      <h2>VIP会员管理</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- Tab 1: VIP套餐管理 -->
      <el-tab-pane label="VIP套餐管理" name="plans">
        <div class="filter-bar">
          <el-button type="primary" @click="handleAddPlan">添加套餐</el-button>
        </div>

        <el-table :data="planList" v-loading="planLoading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="套餐名称" min-width="120" />
          <el-table-column label="价格" width="100">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="durationDays" label="有效天数" width="100" />
          <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="handleEditPlan(row)">编辑</el-button>
              <el-button v-if="row.status === 0" text type="success" @click="handleTogglePlanStatus(row, 1)">启用</el-button>
              <el-button v-if="row.status === 1" text type="warning" @click="handleTogglePlanStatus(row, 0)">禁用</el-button>
              <el-button text type="danger" @click="handleDeletePlan(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab 2: VIP用户列表 -->
      <el-tab-pane label="VIP用户列表" name="users">
        <el-table :data="userList" v-loading="userLoading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="头像" width="80">
            <template #default="{ row }">
              <el-avatar :size="36" :src="row.avatar" />
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="vipExpireTime" label="VIP到期时间" width="160" />
          <el-table-column prop="completedOrders" label="完成订单数" width="110" />
          <el-table-column prop="points" label="积分" width="100" />
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="userQuery.page"
            v-model:page-size="userQuery.size"
            :total="userTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @change="loadVipUsers"
          />
        </div>
      </el-tab-pane>

      <!-- Tab 3: VIP订单记录 -->
      <el-tab-pane label="VIP订单记录" name="orders">
        <el-table :data="orderList" v-loading="orderLoading" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column label="金额" width="100">
            <template #default="{ row }">
              ¥{{ row.amount }}
            </template>
          </el-table-column>
          <el-table-column label="来源" width="120">
            <template #default="{ row }">
              <el-tag :type="row.source === 1 ? 'primary' : 'success'">
                {{ row.source === 1 ? '购买' : '订单达标' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '已付款' : '未付款' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="vipStartTime" label="VIP开始时间" width="160" />
          <el-table-column prop="vipEndTime" label="VIP结束时间" width="160" />
          <el-table-column prop="createTime" label="创建时间" width="160" />
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="orderQuery.page"
            v-model:page-size="orderQuery.size"
            :total="orderTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @change="loadVipOrders"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Add/Edit Plan Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑VIP套餐' : '添加VIP套餐'" width="600px">
      <el-form :model="planForm" label-width="100px">
        <el-form-item label="套餐名称" required>
          <el-input v-model="planForm.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="planForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效天数" required>
          <el-input-number v-model="planForm.durationDays" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="planForm.description" type="textarea" :rows="3" placeholder="请输入套餐描述" />
        </el-form-item>
        <el-form-item label="权益说明">
          <el-input v-model="planForm.benefits" type="textarea" :rows="3" placeholder="请输入权益说明" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="planForm.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="planForm.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPlan" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { VipPlan, VipOrder } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminVipPlans,
  addVipPlan,
  updateVipPlan,
  deleteVipPlan,
  updateVipPlanStatus,
  getVipUsers,
  getVipOrders
} from '@/api/vip'

// Active tab
const activeTab = ref('plans')

// ========== Tab 1: VIP套餐管理 ==========
const planList = ref<VipPlan[]>([])
const planLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)
const submitLoading = ref(false)

const planForm = reactive({
  name: '',
  price: 0,
  durationDays: 30,
  description: '',
  benefits: '',
  sort: 0,
  status: 1
})

function resetPlanForm() {
  planForm.name = ''
  planForm.price = 0
  planForm.durationDays = 30
  planForm.description = ''
  planForm.benefits = ''
  planForm.sort = 0
  planForm.status = 1
}

async function loadVipPlans() {
  planLoading.value = true
  try {
    const res = await getAdminVipPlans(1, 100)
    if (res.code === 200) {
      const data = res.data
      planList.value = data.records || []
    }
  } catch {
    ElMessage.error('获取VIP套餐列表失败')
  } finally {
    planLoading.value = false
  }
}

function handleAddPlan() {
  resetPlanForm()
  isEdit.value = false
  editId.value = 0
  dialogVisible.value = true
}

function handleEditPlan(row: VipPlan) {
  isEdit.value = true
  editId.value = row.id
  planForm.name = row.name
  planForm.price = row.price
  planForm.durationDays = row.durationDays
  planForm.description = row.description || ''
  planForm.benefits = row.benefits || ''
  planForm.sort = row.sort
  planForm.status = row.status
  dialogVisible.value = true
}

async function handleSubmitPlan() {
  if (!planForm.name) {
    ElMessage.warning('请输入套餐名称')
    return
  }
  if (!planForm.price) {
    ElMessage.warning('请输入价格')
    return
  }
  if (!planForm.durationDays) {
    ElMessage.warning('请输入有效天数')
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateVipPlan({ ...planForm, id: editId.value })
      ElMessage.success('更新成功')
    } else {
      await addVipPlan(planForm)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    await loadVipPlans()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleTogglePlanStatus(row: VipPlan, status: number) {
  const label = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}套餐「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateVipPlanStatus(row.id, status)
    ElMessage.success(`${label}成功`)
    await loadVipPlans()
  } catch {
    // cancelled
  }
}

async function handleDeletePlan(row: VipPlan) {
  try {
    await ElMessageBox.confirm(`确定删除套餐「${row.name}」吗？此操作不可撤销。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteVipPlan(row.id)
    ElMessage.success('删除成功')
    await loadVipPlans()
  } catch {
    // cancelled
  }
}

// ========== Tab 2: VIP用户列表 ==========
interface VipUser {
  id: number
  username: string
  nickname: string
  phone: string
  avatar: string
  vipExpireTime: string
  completedOrders: number
  points: number
}

const userList = ref<VipUser[]>([])
const userLoading = ref(false)
const userTotal = ref(0)
const userQuery = reactive({
  page: 1,
  size: 10
})

async function loadVipUsers() {
  userLoading.value = true
  try {
    const res = await getVipUsers(userQuery.page, userQuery.size)
    if (res.code === 200) {
      const data = res.data
      userList.value = data.records || []
      userTotal.value = data.total || 0
    }
  } catch {
    ElMessage.error('获取VIP用户列表失败')
  } finally {
    userLoading.value = false
  }
}

// ========== Tab 3: VIP订单记录 ==========
const orderList = ref<VipOrder[]>([])
const orderLoading = ref(false)
const orderTotal = ref(0)
const orderQuery = reactive({
  page: 1,
  size: 10
})

async function loadVipOrders() {
  orderLoading.value = true
  try {
    const res = await getVipOrders(orderQuery.page, orderQuery.size)
    if (res.code === 200) {
      const data = res.data
      orderList.value = data.records || []
      orderTotal.value = data.total || 0
    }
  } catch {
    ElMessage.error('获取VIP订单列表失败')
  } finally {
    orderLoading.value = false
  }
}

// ========== Tab切换 ==========
function handleTabChange(tab: string) {
  if (tab === 'plans') {
    loadVipPlans()
  } else if (tab === 'users') {
    loadVipUsers()
  } else if (tab === 'orders') {
    loadVipOrders()
  }
}

onMounted(() => {
  loadVipPlans()
})
</script>

<style scoped>
.vip-manage-page {
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
