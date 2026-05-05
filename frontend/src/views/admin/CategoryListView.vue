<template>
  <div class="category-list-page">
    <div class="header">
      <h2>分类管理</h2>
      <div class="header-actions">
        <el-button type="danger" :disabled="selectedCategories.length === 0" @click="handleBatchDelete">
          删除选中
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 添加分类
        </el-button>
      </div>
    </div>
    
    <!-- 分类表格 -->
    <div class="admin-table-shell is-compact">
      <el-table
        class="admin-data-table"
        :data="categoryList"
        v-loading="loading"
        :fit="false"
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="38" />
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="name" label="分类名称" width="130" class-name="admin-ellipsis" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="62" />
        <el-table-column prop="productCount" label="商品数" width="70" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="168" class-name="admin-nowrap">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="112" class-name="admin-action-column">
          <template #default="{ row }">
            <div class="admin-table-actions">
              <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @change="loadCategories"
      />
    </div>
    
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <span class="form-tip">数值越小越靠前</span>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { Category } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addCategory,
  deleteCategory,
  getCategoryListForAdmin,
  updateCategory,
  updateCategoryStatus
} from '@/api/admin'
import { formatDateTime } from '@/utils/format'
import { batchDeleteSelected } from '@/utils/batchDelete'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const categoryList = ref<Category[]>([])
const selectedCategories = ref<Category[]>([])
const editingCategory = ref<Category | null>(null)
const formRef = ref<FormInstance>()
const total = ref(0)

const query = reactive({
  page: 1,
  size: 10
})

const form = reactive({
  parentId: 0,
  name: '',
  sort: 0,
  status: 1
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

async function loadCategories() {
  loading.value = true
  try {
    const res = await getCategoryListForAdmin({ pageNum: query.page, pageSize: query.size })
    categoryList.value = sortCategories((res.data as any)?.records || (res.data as any)?.list || [])
    total.value = (res.data as any)?.total || 0
  } catch {
    ElMessage.error('获取分类列表失败')
    categoryList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingCategory.value = null
  dialogVisible.value = true
}

function handleEdit(category: Category) {
  editingCategory.value = category
  form.parentId = category.parentId || 0
  form.name = category.name
  form.sort = category.sort || 0
  form.status = category.status
  dialogVisible.value = true
}

function handleSelectionChange(selection: Category[]) {
  selectedCategories.value = selection
}

function resetForm() {
  form.parentId = 0
  form.name = ''
  form.sort = 0
  form.status = 1
  editingCategory.value = null
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  
  saving.value = true
  try {
    if (editingCategory.value) {
      await updateCategory(editingCategory.value.id, { ...form })
      ElMessage.success('分类更新成功')
    } else {
      await addCategory({ ...form })
      ElMessage.success('分类添加成功')
      query.page = 1
    }
    dialogVisible.value = false
    loadCategories()
  } catch {
    ElMessage.error(editingCategory.value ? '分类更新失败' : '分类添加失败')
  } finally {
    saving.value = false
  }
}

async function handleStatusChange(category: Category) {
  try {
    await updateCategoryStatus(category.id, category.status)
    ElMessage.success('状态更新成功')
  } catch {
    category.status = category.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

async function handleDelete(category: Category) {
  if (category.productCount && category.productCount > 0) {
    ElMessage.warning('该分类下有商品，无法删除')
    return
  }
  await ElMessageBox.confirm('确定要删除该分类吗？', '删除确认', { type: 'warning' })
  try {
    await deleteCategory(category.id)
    ElMessage.success('删除成功')
    loadCategories()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleBatchDelete() {
  const blocked = selectedCategories.value.filter(category => (category.productCount || 0) > 0)
  if (blocked.length > 0) {
    ElMessage.warning('选中的分类中有已关联商品的分类，不能批量删除')
    return
  }

  await batchDeleteSelected({
    items: selectedCategories.value,
    label: '分类',
    deleteOne: deleteCategory,
    afterDelete: loadCategories
  })
  selectedCategories.value = []
}

onMounted(() => {
  loadCategories()
})

function sortCategories(list: Category[]) {
  return [...list].sort((a, b) => {
    const sortA = a.sort ?? 0
    const sortB = b.sort ?? 0
    if (sortA !== sortB) return sortA - sortB
    return (b.id ?? 0) - (a.id ?? 0)
  })
}
</script>

<style scoped>
.category-list-page {
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

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
