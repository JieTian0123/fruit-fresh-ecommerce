<template>
  <div class="announcement-manage-page">
    <div class="header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="handleAdd">添加公告</el-button>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="公告标题"
        clearable
        style="width: 200px"
        @keyup.enter="loadAnnouncements"
      />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已下架" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadAnnouncements">搜索</el-button>
    </div>

    <!-- Table -->
    <el-table :data="announcementList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? '' : row.type === 2 ? 'success' : 'warning'">
            {{ announcementTypeText(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="80" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'danger'">
            {{ row.status === 1 ? '已发布' : row.status === 0 ? '草稿' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status !== 1" text type="success" @click="handlePublish(row)">发布</el-button>
          <el-button v-if="row.status === 1" text type="warning" @click="handleUnpublish(row)">下架</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadAnnouncements"
      />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '添加公告'" width="700px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="系统公告" :value="1" />
            <el-option label="活动公告" :value="2" />
            <el-option label="知识科普" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入公告内容" />
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
import type { Announcement } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

import { getAnnouncementList, addAnnouncement, updateAnnouncement, publishAnnouncement, unpublishAnnouncement, deleteAnnouncement } from '@/api/admin'

const announcementList = ref<Announcement[]>([])
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

const form = reactive({
  title: '',
  type: 1,
  content: '',
  coverImage: '',
  sort: 0
})

function announcementTypeText(type: number) {
  return type === 1 ? '系统公告' : type === 2 ? '活动公告' : '知识科普'
}

async function loadAnnouncements() {
  loading.value = true
  try {
    const res = await getAnnouncementList({
      pageNum: query.page,
      pageSize: query.size,
      status: query.status,
      keyword: query.keyword
    })
    announcementList.value = (res.data as any)?.records || (res.data as any)?.list || []
    total.value = (res.data as any)?.total || 0
  } catch {
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.title = ''
  form.type = 1
  form.content = ''
  form.coverImage = ''
  form.sort = 0
}

function handleAdd() {
  isEdit.value = false
  editId.value = 0
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: Announcement) {
  isEdit.value = true
  editId.value = row.id
  form.title = row.title
  form.type = row.type
  form.content = row.content
  form.coverImage = row.coverImage || ''
  form.sort = row.sort || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateAnnouncement({ ...form, id: editId.value })
      ElMessage.success('更新成功')
    } else {
      await addAnnouncement(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadAnnouncements()
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    submitLoading.value = false
  }
}

async function handlePublish(row: Announcement) {
  try {
    await ElMessageBox.confirm('确定要发布该公告吗？', '提示', { type: 'info' })
    await publishAnnouncement(row.id)
    ElMessage.success('发布成功')
    loadAnnouncements()
  } catch {
    // cancelled or failed
  }
}

async function handleUnpublish(row: Announcement) {
  try {
    await ElMessageBox.confirm('确定要下架该公告吗？', '提示', { type: 'warning' })
    await unpublishAnnouncement(row.id)
    ElMessage.success('下架成功')
    loadAnnouncements()
  } catch {
    // cancelled or failed
  }
}

async function handleDelete(row: Announcement) {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？删除后不可恢复', '警告', { type: 'warning' })
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    loadAnnouncements()
  } catch {
    // cancelled or failed
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcement-manage-page {
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
