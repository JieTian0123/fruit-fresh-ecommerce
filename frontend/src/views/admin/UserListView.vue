<template>
  <div class="user-list-page">
    <div class="header">
      <h2>用户管理</h2>
    </div>
    
    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="用户名/手机号" clearable style="width: 200px" @keyup.enter="loadUsers" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadUsers">搜索</el-button>
    </div>
    
    <!-- 用户表格 -->
    <div class="admin-table-shell is-user">
      <el-table class="admin-data-table" :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="normalizeImageUrl(row.avatar, defaultAvatar)">{{ row.nickname?.[0] || row.username?.[0] }}</el-avatar>
              <div class="user-info">
                <p class="name">{{ row.nickname || row.username }}</p>
                <p class="username">{{ row.username }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="126" class-name="admin-nowrap" />
        <el-table-column prop="email" label="邮箱" min-width="190" class-name="admin-ellipsis" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="76">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="168" class-name="admin-nowrap">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="112" class-name="admin-action-column">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button text type="primary" @click="handleView(row)">查看</el-button>
              <el-button v-if="row.status === 1" text type="danger" @click="handleToggleStatus(row, 0)">禁用</el-button>
              <el-button v-else text type="success" @click="handleToggleStatus(row, 1)">启用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadUsers"
      />
    </div>
    
    <!-- 用户详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="用户详情" size="400px">
      <template v-if="currentUser">
        <div class="user-detail">
          <div class="avatar-section">
            <el-avatar :size="80" :src="normalizeImageUrl(currentUser.avatar, defaultAvatar)">{{ currentUser.nickname?.[0] || currentUser.username?.[0] }}</el-avatar>
            <h3>{{ currentUser.nickname || currentUser.username }}</h3>
            <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
              {{ currentUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDateTime(currentUser.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { UserInfo } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/format'
import { defaultAvatar, normalizeImageUrl } from '@/utils/image'

const loading = ref(false)
const userList = ref<UserInfo[]>([])
const total = ref(0)
const detailDrawerVisible = ref(false)
const currentUser = ref<UserInfo | null>(null)

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  page: 1,
  size: 10
})

async function loadUsers() {
  loading.value = true
  try {
    const { getUserList } = await import('@/api/admin')
    const res = await getUserList({
      userType: 0,
      status: query.status,
      pageNum: query.page,
      pageSize: query.size
    })
    userList.value = (res.data as any)?.records || (res.data as any)?.list || []
    total.value = (res.data as any)?.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleView(user: UserInfo) {
  currentUser.value = user
  detailDrawerVisible.value = true
}

async function handleToggleStatus(user: UserInfo, status: number) {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确定要${action}该用户吗？`, '确认操作')
  try {
    const { enableUser, disableUser } = await import('@/api/admin')
    if (status === 1) {
      await enableUser(user.id)
    } else {
      await disableUser(user.id)
    }
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch {}
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-list-page {
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
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-info .name {
  font-weight: 500;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info .username {
  font-size: 12px;
  color: var(--color-text-secondary);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.user-detail .avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.user-detail .avatar-section h3 {
  margin: 0;
}
</style>
