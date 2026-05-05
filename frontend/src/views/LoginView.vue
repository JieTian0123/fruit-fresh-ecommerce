<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧装饰 -->
      <div class="login-banner">
        <div class="banner-content">
          <el-icon class="banner-icon"><Apple /></el-icon>
          <h1>水果生鲜电商平台</h1>
          <p>新鲜直达，健康生活</p>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <div class="login-form-content">
          <h2>欢迎登录</h2>
          <p class="subtitle">请输入您的账号密码</p>
          
          <el-form
            ref="formRef"
            :model="formData"
            :rules="rules"
            size="large"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="formData.username"
                placeholder="请输入用户名"
                prefix-icon="User"
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

            <el-form-item prop="captchaCode">
              <div style="display: flex; gap: 12px; width: 100%;">
                <el-input
                  v-model="formData.captchaCode"
                  placeholder="请输入验证码"
                  prefix-icon="Key"
                  style="flex: 1;"
                  @keyup.enter="handleLogin"
                />
                <img
                  :src="captchaImage"
                  alt="验证码"
                  style="height: 40px; cursor: pointer; border-radius: 4px; border: 1px solid #dcdfe6;"
                  title="点击刷新验证码"
                  @click="refreshCaptcha"
                />
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                native-type="submit"
                :loading="loading"
                class="login-btn"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="form-footer">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
          </div>
          
          <div class="switch-role">
            <router-link to="/merchant/login">商家登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'

interface CaptchaResponse {
  uuid: string
  image: string
}

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')

const formData = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaUuid: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

async function refreshCaptcha() {
  try {
    const res = await request.get<{ data: CaptchaResponse }>('/captcha')
    captchaImage.value = res.data.image
    formData.captchaUuid = res.data.uuid
    formData.captchaCode = ''
  } catch {
    captchaImage.value = ''
  }
}

async function handleLogin() {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true
    const success = await userStore.login(
      formData.username,
      formData.password,
      formData.captchaCode,
      formData.captchaUuid
    )
    await refreshCaptcha()

    if (!success) {
      return
    }
  } catch {
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  padding: 20px;
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.login-banner {
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

.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-form-content {
  width: 100%;
  max-width: 320px;
}

.login-form-content h2 {
  font-size: 28px;
  margin-bottom: 8px;
  color: var(--color-text-primary);
}

.subtitle {
  color: var(--color-text-secondary);
  margin-bottom: 32px;
}

.login-btn {
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
  color: var(--color-primary);
}

.switch-role .divider {
  margin: 0 12px;
  color: #ddd;
}

@media (max-width: 768px) {
  .login-banner {
    display: none;
  }
}
</style>
