<template>
  <div class="banner-manage-page">
    <div class="header">
      <h2>轮播图管理</h2>
      <el-button type="primary" @click="handleAdd">添加轮播图</el-button>
    </div>

    <!-- 筛选区 -->
    <div class="filter-bar">
      <el-button type="primary" @click="loadBanners">搜索</el-button>
    </div>

    <!-- 轮播图表格 -->
    <el-table :data="bannerList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column label="图片" width="120">
        <template #default="{ row }">
          <el-image :src="row.imageUrl" style="width: 80px; height: 45px" fit="cover" :preview-src-list="[row.imageUrl]" />
        </template>
      </el-table-column>
      <el-table-column prop="linkUrl" label="链接" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="(val: boolean) => handleToggleStatus(row, val ? 1 : 0)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
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
        @change="loadBanners"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑轮播图' : '添加轮播图'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片URL" required>
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="链接URL">
          <el-input v-model="form.linkUrl" placeholder="请输入链接URL" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import type { Banner } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const bannerList = ref<Banner[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)

const query = reactive({
  page: 1,
  size: 10
})

const form = reactive({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1
})

async function loadBanners() {
  loading.value = true
  try {
    const { getBannerList } = await import('@/api/admin')
    const res = await getBannerList({
      pageNum: query.page,
      pageSize: query.size
    })
    bannerList.value = (res.data as any)?.records || (res.data as any)?.list || []
    total.value = (res.data as any)?.total || 0
  } catch (error) {
    console.error('加载轮播图列表失败:', error)
    bannerList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editId.value = 0
  form.title = ''
  form.imageUrl = ''
  form.linkUrl = ''
  form.sort = 0
  form.status = 1
  dialogVisible.value = true
}

function handleEdit(row: Banner) {
  isEdit.value = true
  editId.value = row.id
  form.title = row.title
  form.imageUrl = row.imageUrl
  form.linkUrl = row.linkUrl
  form.sort = row.sort
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      const { updateBanner } = await import('@/api/admin')
      await updateBanner(editId.value, { ...form })
      ElMessage.success('编辑成功')
    } else {
      const { addBanner } = await import('@/api/admin')
      await addBanner({ ...form })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadBanners()
  } catch {
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: Banner) {
  await ElMessageBox.confirm('确定要删除该轮播图吗？', '确认操作')
  try {
    const { deleteBanner } = await import('@/api/admin')
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    loadBanners()
  } catch {}
}

async function handleToggleStatus(row: Banner, status: number) {
  const action = status === 1 ? '启用' : '禁用'
  try {
    const { updateBannerStatus } = await import('@/api/admin')
    await updateBannerStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    loadBanners()
  } catch {}
}

onMounted(() => {
  loadBanners()
})
</script>

<style scoped>
.banner-manage-page {
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
