<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 左侧装饰 -->
      <div class="register-banner">
        <div class="banner-content">
          <el-icon class="banner-icon"><Apple /></el-icon>
          <h1>水果生鲜电商平台</h1>
          <p>加入我们，享受新鲜健康生活</p>
        </div>
      </div>
      
      <!-- 右侧注册表单 -->
      <div class="register-form-wrapper">
        <div class="register-form-content">
          <h2>创建账号</h2>
          <p class="subtitle">请填写以下信息完成注册</p>
          
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
                注册
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="form-footer">
            <span>已有账号？</span>
            <router-link to="/login">立即登录</router-link>
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
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'CONSUMER' as const
})

const validateConfirmPassword = (rule: any, value: string, callback: Function) => {
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
    { min: 8, max: 20, message: '密码长度为8-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
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
      await register({
        username: formData.username,
        phone: formData.phone,
        password: formData.password,
        role: 'CONSUMER'
      })
      ElMessage.success('注册成功，请登录')
      router.push('/login')
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
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  padding: 20px;
}

.register-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.register-banner {
  flex: 1;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
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
  color: var(--color-text-primary);
}

.subtitle {
  color: var(--color-text-secondary);
  margin-bottom: 32px;
}

.role-group {
  display: flex;
  gap: 20px;
}

.role-group :deep(.el-radio) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.register-btn {
  width: 100%;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  color: var(--color-text-secondary);
}

.form-footer a {
  color: var(--color-primary);
  font-weight: 500;
}

@media (max-width: 768px) {
  .register-banner {
    display: none;
  }
}
</style>
