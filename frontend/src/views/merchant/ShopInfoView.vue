<template>
  <div class="shop-info-page">
    <div class="header"><h2>店铺信息</h2></div>

    <div class="shop-form-wrapper" v-loading="loading">
      <el-form :model="form" label-width="100px" style="max-width: 600px">
        <el-form-item label="店铺名称" required>
          <el-input v-model="form.shopName" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="店铺Logo">
          <el-input v-model="form.logo" placeholder="请输入Logo URL" />
        </el-form-item>
        <el-form-item label="店铺描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入店铺描述" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县">
          <el-input v-model="form.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业执照">
          <el-input v-model="form.businessLicense" placeholder="请输入营业执照URL" />
        </el-form-item>

        <!-- Show status (read-only) if shop exists -->
        <el-form-item v-if="hasShop" label="审核状态">
          <el-tag :type="shopStatus === 1 ? 'success' : shopStatus === 2 ? 'warning' : 'danger'">
            {{ shopStatus === 1 ? '已通过' : shopStatus === 2 ? '待审核' : '未通过' }}
          </el-tag>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">
            {{ hasShop ? '保存修改' : '创建店铺' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { ShopDTO } from '@/types'
import { ElMessage } from 'element-plus'

import { getShopInfo, createShop, updateShop } from '@/api/merchant'

const loading = ref(false)
const saving = ref(false)
const hasShop = ref(false)
const shopStatus = ref(0)

const form = reactive<ShopDTO>({
  shopName: '',
  logo: '',
  description: '',
  province: '',
  city: '',
  district: '',
  address: '',
  contactPhone: '',
  businessLicense: ''
})

async function loadShopInfo() {
  loading.value = true
  try {
    const res = await getShopInfo()
    if (res.data) {
      const shop = res.data
      form.shopName = shop.shopName ?? ''
      form.logo = shop.logo ?? ''
      form.description = shop.description ?? ''
      form.province = shop.province ?? ''
      form.city = shop.city ?? ''
      form.district = shop.district ?? ''
      form.address = shop.address ?? ''
      form.contactPhone = shop.contactPhone ?? ''
      form.businessLicense = shop.businessLicense ?? ''
      shopStatus.value = shop.status ?? 0
      hasShop.value = true
    }
  } catch {
    ElMessage.error('获取店铺信息失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!form.shopName.trim()) {
    ElMessage.warning('请输入店铺名称')
    return
  }
  if (!form.contactPhone.trim()) {
    ElMessage.warning('请输入联系电话')
    return
  }

  saving.value = true
  try {
    const payload: ShopDTO = { ...form }
    if (hasShop.value) {
      await updateShop(payload)
      ElMessage.success('店铺信息已更新')
    } else {
      await createShop(payload)
      ElMessage.success('店铺创建成功')
      hasShop.value = true
    }
    await loadShopInfo()
  } catch {
    ElMessage.error(hasShop.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadShopInfo()
})
</script>

<style scoped>
.shop-info-page {
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
.shop-form-wrapper {
  padding: 20px 0;
}
</style>
