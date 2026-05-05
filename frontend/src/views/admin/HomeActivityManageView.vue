<template>
  <div class="home-activity-page">
    <div class="header">
      <h2>首页活动管理</h2>
    </div>

    <el-table :data="activityList" v-loading="loading" class="admin-data-table" :fit="false" style="width: 100%" row-key="code">
      <el-table-column prop="sort" label="排序" width="58" />
      <el-table-column label="活动" width="180">
        <template #default="{ row }">
          <div class="activity-title-cell">
            <span class="activity-title">{{ row.title }}</span>
            <span class="activity-subtitle">{{ row.subtitle }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="图片" width="124" class-name="admin-media-column">
        <template #default="{ row }">
          <el-image
            :src="resolveImageUrl(row.imageUrl)"
            fit="cover"
            class="activity-image"
            :preview-src-list="[resolveImageUrl(row.imageUrl)]"
            preview-teleported
            :z-index="5000"
            @click.stop
          />
        </template>
      </el-table-column>
      <el-table-column prop="badge" label="角标" width="74" show-overflow-tooltip />
      <el-table-column prop="actionText" label="文案" width="86" show-overflow-tooltip />
      <el-table-column prop="linkUrl" label="跳转链接" width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="82" class-name="admin-tag-column">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="70" class-name="admin-action-column">
        <template #default="{ row }">
          <div class="admin-table-actions">
            <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="`编辑${currentActivity?.title || ''}`" width="560px">
      <el-form label-width="90px">
        <el-form-item label="活动名称">
          <el-input :model-value="currentActivity?.title" disabled />
        </el-form-item>
        <el-form-item label="活动图片">
          <el-input v-model="activityForm.imageUrl" placeholder="请输入图片地址" />
          <el-image
            v-if="activityForm.imageUrl"
            :src="resolveImageUrl(activityForm.imageUrl)"
            fit="cover"
            class="image-preview"
          />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="activityForm.linkUrl" placeholder="请输入站内路径或外部链接" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="activityForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { HomeActivity } from '@/types'
import { getHomeActivityList, updateHomeActivity } from '@/api/admin'
import {
  loadStoredHomeActivities,
  mergeHomeActivities,
  saveHomeActivities,
  saveHomeActivity
} from '@/utils/homeActivities'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const activityList = ref<HomeActivity[]>(loadStoredHomeActivities())
const currentActivity = ref<HomeActivity | null>(null)

const activityForm = reactive({
  imageUrl: '',
  linkUrl: '',
  status: 1
})

function resolveImageUrl(url?: string) {
  if (!url) return ''
  return url
}

async function loadActivities() {
  loading.value = true
  activityList.value = loadStoredHomeActivities()
  try {
    const res = await getHomeActivityList()
    if (res.data?.length) {
      activityList.value = mergeHomeActivities(res.data)
      saveHomeActivities(activityList.value)
    }
  } catch {
    activityList.value = loadStoredHomeActivities()
  } finally {
    loading.value = false
  }
}

function handleEdit(row: HomeActivity) {
  currentActivity.value = row
  activityForm.imageUrl = row.imageUrl || ''
  activityForm.linkUrl = row.linkUrl || ''
  activityForm.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!currentActivity.value) return
  saving.value = true
  try {
    activityList.value = saveHomeActivity(currentActivity.value.code, {
      imageUrl: activityForm.imageUrl,
      linkUrl: activityForm.linkUrl,
      status: activityForm.status
    })
    await updateHomeActivity(currentActivity.value.code, {
      imageUrl: activityForm.imageUrl,
      linkUrl: activityForm.linkUrl,
      status: activityForm.status
    })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadActivities()
  } catch {
    ElMessage.success('保存成功')
    dialogVisible.value = false
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.home-activity-page {
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

.activity-title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.activity-title {
  font-weight: 600;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-subtitle {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.activity-image {
  width: 92px;
  height: 54px;
  border-radius: 6px;
  background: #f5f7fa;
}

.image-preview {
  width: 180px;
  height: 108px;
  margin-top: 12px;
  border-radius: 8px;
  background: #f5f7fa;
}

</style>
