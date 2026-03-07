<template>
  <div class="category-list-page">
    <div class="header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 添加分类
      </el-button>
    </div>
    
    <!-- 分类表格 -->
    <el-table :data="categoryList" v-loading="loading" style="width: 100%" row-key="id">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="分类图标" width="100">
        <template #default="{ row }">
          <img v-if="row.icon" :src="row.icon" class="category-icon" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="分类名称" min-width="150" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="productCount" label="商品数" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
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
        
        <el-form-item label="分类图标">
          <el-upload
            class="icon-uploader"
            action=""
            :show-file-list="false"
            :before-upload="handleIconUpload"
          >
            <img v-if="form.icon" :src="form.icon" class="icon-preview" />
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
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

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const categoryList = ref<Category[]>([])
const editingCategory = ref<Category | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  icon: '',
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
    // TODO: 调用接口获取分类列表
    categoryList.value = [
      { id: 1, name: '新鲜水果', icon: '', sort: 1, status: 1, productCount: 120, createTime: '2024-01-01 10:00' },
      { id: 2, name: '时令蔬菜', icon: '', sort: 2, status: 1, productCount: 85, createTime: '2024-01-01 10:00' },
      { id: 3, name: '海鲜水产', icon: '', sort: 3, status: 1, productCount: 56, createTime: '2024-01-01 10:00' },
      { id: 4, name: '肉禽蛋品', icon: '', sort: 4, status: 1, productCount: 42, createTime: '2024-01-01 10:00' },
      { id: 5, name: '乳品烘焙', icon: '', sort: 5, status: 0, productCount: 28, createTime: '2024-01-01 10:00' }
    ] as Category[]
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
  form.name = category.name
  form.icon = category.icon || ''
  form.sort = category.sort || 0
  form.status = category.status
  dialogVisible.value = true
}

function resetForm() {
  form.name = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
  editingCategory.value = null
  formRef.value?.resetFields()
}

function handleIconUpload(file: File) {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.icon = e.target?.result as string
  }
  reader.readAsDataURL(file)
  return false
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  
  saving.value = true
  try {
    if (editingCategory.value) {
      // TODO: 调用更新接口
      ElMessage.success('分类更新成功')
    } else {
      // TODO: 调用添加接口
      ElMessage.success('分类添加成功')
    }
    dialogVisible.value = false
    loadCategories()
  } finally {
    saving.value = false
  }
}

async function handleStatusChange(category: Category) {
  try {
    // TODO: 调用修改状态接口
    ElMessage.success('状态更新成功')
  } catch {
    category.status = category.status === 1 ? 0 : 1
  }
}

async function handleDelete(category: Category) {
  if (category.productCount && category.productCount > 0) {
    ElMessage.warning('该分类下有商品，无法删除')
    return
  }
  await ElMessageBox.confirm('确定要删除该分类吗？', '删除确认', { type: 'warning' })
  try {
    // TODO: 调用删除接口
    ElMessage.success('删除成功')
    loadCategories()
  } catch {}
}

onMounted(() => {
  loadCategories()
})
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

.category-icon {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.icon-uploader :deep(.el-upload) {
  width: 80px;
  height: 80px;
  border: 2px dashed #ddd;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.icon-uploader :deep(.el-upload:hover) {
  border-color: var(--color-primary);
}

.icon-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.upload-icon {
  font-size: 24px;
  color: #ccc;
}

.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
