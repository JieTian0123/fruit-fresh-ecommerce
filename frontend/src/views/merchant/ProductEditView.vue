<template>
  <div class="product-edit-page">
    <div class="header">
      <h2>{{ isEdit ? '编辑商品' : '添加商品' }}</h2>
    </div>
    
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="100" show-word-limit />
      </el-form-item>
      
      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
          <el-option
            v-for="cat in categoryList"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="商品主图" prop="mainImage">
        <el-upload
          class="image-uploader"
          action=""
          :show-file-list="false"
          :before-upload="handleImageUpload"
        >
          <img v-if="form.mainImage && !mainImageError" :src="form.mainImage" class="main-image" @error="mainImageError = true" />
          <el-icon v-else class="upload-icon"><Plus /></el-icon>
        </el-upload>
        <p class="upload-tip">建议尺寸 800x800，支持 JPG/PNG</p>
      </el-form-item>
      
      <el-form-item label="商品图册">
        <el-upload
          multiple
          action=""
          list-type="picture-card"
          :file-list="imageList"
          :before-upload="handleGalleryUpload"
          :on-remove="handleRemoveImage"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      
      <el-form-item label="商品价格" prop="price">
        <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.1" />
        <span class="unit">元</span>
      </el-form-item>
      
      <el-form-item label="商品库存" prop="stock">
        <el-input-number v-model="form.stock" :min="0" />
        <span class="unit">件</span>
      </el-form-item>
      
      <el-form-item label="计量单位" prop="unit">
        <el-input v-model="form.unit" placeholder="如：斤、个、箱" style="width: 200px" />
      </el-form-item>
      
      <el-form-item label="商品产地">
        <el-input v-model="form.origin" placeholder="如：新疆、海南" style="width: 200px" />
      </el-form-item>
      
      <el-form-item label="保质期">
        <el-input-number v-model="form.shelfLifeDays" :min="1" :max="365" placeholder="天" />
        <span class="unit">天</span>
      </el-form-item>

      <el-form-item label="采摘日期">
        <el-date-picker
          v-model="form.productionDate"
          type="date"
          placeholder="选择生产/采摘日期"
          value-format="YYYY-MM-DD"
          style="width: 200px"
        />
      </el-form-item>

      <el-form-item label="存储条件">
        <el-input v-model="form.storageCondition" placeholder="如：0-4℃冷藏" style="width: 300px" />
      </el-form-item>

      <el-form-item label="品质等级">
        <el-select v-model="form.qualityGrade" placeholder="请选择品质等级" style="width: 200px" clearable>
          <el-option label="A级 - 优质" value="A" />
          <el-option label="B级 - 良好" value="B" />
          <el-option label="C级 - 普通" value="C" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="商品描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入商品描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="商品状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">上架</el-radio>
          <el-radio :value="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <!-- 溯源记录管理（仅编辑模式） -->
      <template v-if="isEdit">
        <el-divider content-position="left">溯源记录管理</el-divider>
        
        <el-form-item label="溯源记录">
          <div class="traceability-section">
            <el-button type="primary" size="small" @click="showTraceDialog = true">
              添加溯源节点
            </el-button>
            
            <div class="trace-list" v-if="traceabilityList.length > 0">
              <div class="trace-item" v-for="item in traceabilityList" :key="item.id">
                <div class="trace-item-info">
                  <el-tag size="small">{{ getNodeTypeText(item.nodeType) }}</el-tag>
                  <span class="trace-item-name">{{ item.nodeName }}</span>
                  <span class="trace-item-time">{{ item.occurredTime }}</span>
                  <span v-if="item.location" class="trace-item-loc">📍{{ item.location }}</span>
                </div>
                <el-button type="danger" size="small" text @click="handleDeleteTrace(item.id)">
                  删除
                </el-button>
              </div>
            </div>
            <el-empty v-else description="暂无溯源记录" :image-size="60" />
          </div>
        </el-form-item>
      </template>
      
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="saving">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>

    <!-- 添加溯源记录对话框 -->
    <el-dialog v-model="showTraceDialog" title="添加溯源节点" width="500px">
      <el-form :model="traceForm" label-width="80px">
        <el-form-item label="节点类型" required>
          <el-select v-model="traceForm.nodeType" placeholder="请选择">
            <el-option label="采摘" :value="1" />
            <el-option label="质检" :value="2" />
            <el-option label="入库" :value="3" />
            <el-option label="出库" :value="4" />
            <el-option label="配送" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="节点名称" required>
          <el-input v-model="traceForm.nodeName" placeholder="如：果园采摘" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="traceForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="traceForm.location" placeholder="如：海南三亚" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="traceForm.operator" />
        </el-form-item>
        <el-form-item label="温度(℃)">
          <el-input-number v-model="traceForm.temperature" :precision="1" />
        </el-form-item>
        <el-form-item label="湿度(%)">
          <el-input-number v-model="traceForm.humidity" :precision="1" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="发生时间" required>
          <el-date-picker
            v-model="traceForm.occurredTime"
            type="datetime"
            placeholder="选择时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTraceDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddTrace" :loading="traceSaving">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, addProduct, updateProduct } from '@/api/product'
import { getCategoryList } from '@/api/product'
import { addTraceability, deleteTraceability, getProductTraceabilityList } from '@/api/merchant'
import type { Category } from '@/types'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const categoryList = ref<Category[]>([])
const imageList = ref<UploadFile[]>([])
const mainImageError = ref(false)

const traceabilityList = ref<any[]>([])
const showTraceDialog = ref(false)
const traceSaving = ref(false)
const traceForm = reactive({
  nodeType: undefined as number | undefined,
  nodeName: '',
  description: '',
  location: '',
  operator: '',
  temperature: undefined as number | undefined,
  humidity: undefined as number | undefined,
  occurredTime: ''
})

const form = reactive({
  name: '',
  categoryId: undefined as number | undefined,
  mainImage: '',
  images: '',
  price: 0,
  stock: 0,
  unit: '',
  origin: '',
  shelfLifeDays: undefined as number | undefined,
  productionDate: '',
  storageCondition: '',
  qualityGrade: '',
  description: '',
  status: 1
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  mainImage: [
    { required: true, message: '请上传商品主图', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请输入计量单位', trigger: 'blur' }
  ]
}

async function loadCategories() {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch {}
}

async function loadProduct() {
  if (!route.params.id) return
  loading.value = true
  try {
    const res = await getProductDetail(Number(route.params.id))
    const product = res.data
    if (product) {
      form.name = product.name
      form.categoryId = product.categoryId
      form.mainImage = product.mainImage || ''
      form.images = product.images || ''
      form.price = product.price
      form.stock = product.stock
      form.unit = product.unit
      form.origin = product.origin || ''
      form.shelfLifeDays = product.shelfLifeDays
      form.productionDate = product.productionDate || ''
      form.storageCondition = product.storageCondition || ''
      form.qualityGrade = product.qualityGrade || ''
      form.description = product.description || ''
      form.status = product.status
      
      if (product.images) {
        imageList.value = product.images.split(',').map((url: string, idx: number) => ({
          name: `image-${idx}`,
          url
        })) as UploadFile[]
      }
    }
  } finally {
    loading.value = false
  }
}

async function handleImageUpload(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res: any = await request.post('/common/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.mainImage = res.data
    mainImageError.value = false
  } catch {
    ElMessage.error('图片上传失败')
  }
  return false
}

async function handleGalleryUpload(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res: any = await request.post('/common/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    imageList.value.push({
      name: file.name,
      url: res.data
    } as UploadFile)
    updateImagesField()
  } catch {
    ElMessage.error('图片上传失败')
  }
  return false
}

function handleRemoveImage(file: UploadFile) {
  const index = imageList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    imageList.value.splice(index, 1)
    updateImagesField()
  }
}

function updateImagesField() {
  form.images = imageList.value.map(f => f.url).join(',')
}

function getNodeTypeText(type: number): string {
  const map: Record<number, string> = { 1: '采摘', 2: '质检', 3: '入库', 4: '出库', 5: '配送' }
  return map[type] || '其他'
}

async function loadTraceability() {
  if (!route.params.id) return
  try {
    const res = await getProductTraceabilityList(Number(route.params.id))
    traceabilityList.value = res.data || []
  } catch {
    traceabilityList.value = []
  }
}

async function handleAddTrace() {
  if (!traceForm.nodeType || !traceForm.nodeName || !traceForm.occurredTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  traceSaving.value = true
  try {
    await addTraceability({
      ...traceForm,
      productId: Number(route.params.id)
    })
    ElMessage.success('溯源记录添加成功')
    showTraceDialog.value = false
    // Reset form
    traceForm.nodeType = undefined
    traceForm.nodeName = ''
    traceForm.description = ''
    traceForm.location = ''
    traceForm.operator = ''
    traceForm.temperature = undefined
    traceForm.humidity = undefined
    traceForm.occurredTime = ''
    await loadTraceability()
  } finally {
    traceSaving.value = false
  }
}

async function handleDeleteTrace(id: number) {
  try {
    await deleteTraceability(id)
    ElMessage.success('删除成功')
    await loadTraceability()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  
  saving.value = true
  try {
    if (isEdit.value) {
      await updateProduct(Number(route.params.id), form)
      ElMessage.success('商品更新成功')
    } else {
      await addProduct(form)
      ElMessage.success('商品添加成功')
    }
    router.push('/merchant/products')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) {
    loadProduct()
    loadTraceability()
  }
})
</script>

<style scoped>
.product-edit-page {
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
  max-width: 800px;
}

.header {
  margin-bottom: 32px;
}

.header h2 {
  font-size: 18px;
}

.image-uploader {
  display: block;
}

.image-uploader :deep(.el-upload) {
  width: 160px;
  height: 160px;
  border: 2px dashed #ddd;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.image-uploader :deep(.el-upload:hover) {
  border-color: var(--color-primary);
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.upload-icon {
  font-size: 32px;
  color: #ccc;
}

.upload-tip {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-top: 8px;
}

.unit {
  margin-left: 8px;
  color: var(--color-text-secondary);
}

.traceability-section {
  width: 100%;
}

.trace-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.trace-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.trace-item-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.trace-item-name {
  font-weight: 500;
  color: #333;
}

.trace-item-time {
  font-size: 12px;
  color: #999;
}

.trace-item-loc {
  font-size: 12px;
  color: #999;
}
</style>
