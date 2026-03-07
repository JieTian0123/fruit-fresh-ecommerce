<template>
  <div class="address-page">
    <div class="header">
      <h2>收货地址</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增地址
      </el-button>
    </div>
    
    <div class="address-list" v-loading="loading">
      <div 
        v-for="item in addressList" 
        :key="item.id" 
        class="address-card"
        :class="{ default: item.isDefault }"
      >
        <div class="address-info">
          <div class="name-phone">
            <span class="name">{{ item.receiverName }}</span>
            <span class="phone">{{ item.receiverPhone }}</span>
            <el-tag v-if="item.isDefault" type="success" size="small">默认</el-tag>
          </div>
          <p class="full-address">
            {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detailAddress }}
          </p>
        </div>
        <div class="address-actions">
          <el-button text @click="handleEdit(item)">编辑</el-button>
          <el-button v-if="!item.isDefault" text @click="handleSetDefault(item)">设为默认</el-button>
          <el-button text type="danger" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
      
      <el-empty v-if="!loading && addressList.length === 0" description="暂无收货地址" />
    </div>
    
    <!-- 地址编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editingAddress ? '编辑地址' : '新增地址'"
      width="500px"
      @close="resetForm"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="80px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        
        <el-form-item label="所在地区" prop="selectedOptions">
          <el-cascader
            :key="cascaderKey"
            v-model="form.selectedOptions"
            :options="regionData"
            placeholder="请选择省/市/区"
            style="width: 100%"
            @change="handleRegionChange"
          />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input 
            v-model="form.detailAddress" 
            type="textarea" 
            :rows="3"
            placeholder="请输入详细地址，如街道、门牌号等" 
          />
        </el-form-item>
        
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
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
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'
import type { Address } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { regionData, codeToText } from 'element-china-area-data'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const addressList = ref<Address[]>([])
const editingAddress = ref<Address | null>(null)

const formRef = ref<FormInstance>()
const cascaderKey = ref(0)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false,
  selectedOptions: [] as string[]
})

const rules: FormRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  selectedOptions: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

async function loadAddressList() {
  loading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
  } catch {
    addressList.value = []
  } finally {
    loading.value = false
  }
}



function getCodesFromText(province: string, city: string, district: string) {
  let codes: string[] = []
  // 这里类型 any 因为 regionData 类型定义可能比较宽松，或者为了简单
  const pNode = (regionData as any[]).find(item => item.label === province || item.value === province) // 兼容
  if (pNode) {
    if (province) codes.push(pNode.value)
    if (city && pNode.children) {
      const cNode = pNode.children.find((item: any) => item.label === city)
      if (cNode) {
        codes.push(cNode.value)
        if (district && cNode.children) {
          const dNode = cNode.children.find((item: any) => item.label === district)
          if (dNode) {
            codes.push(dNode.value)
          }
        }
      }
    }
  }
  return codes
}

function handleEdit(address: Address) {
  editingAddress.value = address
  form.receiverName = address.receiverName
  form.receiverPhone = address.receiverPhone
  form.province = address.province
  form.city = address.city
  form.district = address.district
  form.detailAddress = address.detailAddress
  form.isDefault = Boolean(address.isDefault)
  
  // 回显地区选择
  if (address.province && address.city && address.district) {
      const codes = getCodesFromText(address.province, address.city, address.district)
      if (codes.length === 3) {
          form.selectedOptions = codes
      } else {
          form.selectedOptions = []
      }
  } else {
      form.selectedOptions = []
  }

  dialogVisible.value = true
}

function resetForm() {
  form.receiverName = ''
  form.receiverPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detailAddress = ''
  form.isDefault = false
  form.selectedOptions = []
  cascaderKey.value++ // 强制刷新级联选择器
  // 移除 resetFields，防止恢复到第一次打开时的值
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

function handleAdd() {
  editingAddress.value = null
  // 确保表单已重置
  resetForm()
  dialogVisible.value = true
}

function handleRegionChange(value: string[]) {
  if (value && value.length === 3) {
    form.province = (codeToText as Record<string, string>)[value[0]!] || ''
    form.city = (codeToText as Record<string, string>)[value[1]!] || ''
    form.district = (codeToText as Record<string, string>)[value[2]!] || ''
  } else {
    form.province = ''
    form.city = ''
    form.district = ''
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  
  saving.value = true
  try {
    // API 层会自动转换 isDefault: boolean → number
    if (editingAddress.value) {
      await updateAddress(editingAddress.value.id, form)
      ElMessage.success('地址更新成功')
    } else {
      await addAddress(form)
      ElMessage.success('地址添加成功')
    }
    dialogVisible.value = false
    loadAddressList()
  } finally {
    saving.value = false
  }
}

async function handleSetDefault(address: Address) {
  try {
    await setDefaultAddress(address.id)
    ElMessage.success('已设为默认地址')
    loadAddressList()
  } catch {}
}

async function handleDelete(address: Address) {
  await ElMessageBox.confirm('确定要删除这个地址吗？', '删除地址', { type: 'warning' })
  try {
    await deleteAddress(address.id)
    ElMessage.success('地址已删除')
    loadAddressList()
  } catch {}
}

onMounted(() => {
  loadAddressList()
})
</script>

<style scoped>
.address-page {
  max-width: 800px;
  margin: 0 auto;
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

.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  border: 2px solid transparent;
  transition: border-color 0.3s;
}

.address-card.default {
  border-color: var(--color-primary);
}

.name-phone {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name {
  font-weight: 600;
  font-size: 16px;
}

.phone {
  color: var(--color-text-secondary);
}

.full-address {
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 8px;
}

.region-select {
  display: flex;
  gap: 8px;
}

.region-select .el-input {
  flex: 1;
}
</style>
