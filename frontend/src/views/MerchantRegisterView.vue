<template>
  <div class="register-page merchant-register">
    <div class="register-container">
      <!-- 左侧装饰 -->
      <div class="register-banner">
        <div class="banner-content">
          <el-icon class="banner-icon"><Shop /></el-icon>
          <h1>商家入驻</h1>
          <p>加入平台，开启您的水果生鲜事业</p>
        </div>
      </div>
      
      <!-- 右侧注册表单 -->
      <div class="register-form-wrapper">
        <div class="register-form-content">
          <h2>商家注册</h2>
          <p class="subtitle">填写以下信息申请入驻，审核通过后即可使用</p>
          
          <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            size="large"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="formData.username"
                placeholder="请输入用户名"
                prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="phone">
              <el-input
                v-model="formData.phone"
                placeholder="请输入手机号"
                prefix-icon="Phone"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="formData.confirmPassword"
                type="password"
                placeholder="请确认密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                native-type="submit"
                :loading="loading"
                class="register-btn"
              >
                申请入驻
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="form-footer">
            <span>已有商家账号？</span>
            <router-link to="/merchant/login">立即登录</router-link>
          </div>
          
          <div class="switch-role">
            <router-link to="/register">消费者注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { merchantRegister } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: string, callback: Function) => {
  if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await merchantRegister({
        username: formData.username,
        phone: formData.phone,
        password: formData.password,
        role: 'MERCHANT'
      })
      ElMessage.success('注册成功，请等待管理员审核后登录')
      router.push('/merchant/login')
    } catch (error) {
      // 错误已在拦截器处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  padding: 20px;
}

.register-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.register-banner {
  flex: 1;
  background: linear-gradient(135deg, #FF8C00 0%, #FF6B00 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.banner-content {
  text-align: center;
  color: white;
}

.banner-icon {
  font-size: 80px;
  margin-bottom: 24px;
}

.banner-content h1 {
  font-size: 28px;
  margin-bottom: 12px;
  color: white;
}

.banner-content p {
  font-size: 16px;
  opacity: 0.9;
}

.register-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-form-content {
  width: 100%;
  max-width: 320px;
}

.register-form-content h2 {
  font-size: 28px;
  margin-bottom: 8px;
  color: #333;
}

.subtitle {
  color: #666;
  margin-bottom: 32px;
  font-size: 14px;
}

.register-btn {
  width: 100%;
  background: linear-gradient(135deg, #FF8C00 0%, #FF6B00 100%);
  border: none;
}

.register-btn:hover {
  background: linear-gradient(135deg, #FF6B00 0%, #E65100 100%);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  color: #666;
}

.form-footer a {
  color: #FF8C00;
  font-weight: 500;
}

.switch-role {
  text-align: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.switch-role a {
  color: #999;
  font-size: 14px;
}

.switch-role a:hover {
  color: #FF8C00;
}

@media (max-width: 768px) {
  .register-banner {
    display: none;
  }
}
</style>
