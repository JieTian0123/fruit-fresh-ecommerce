<template>
  <div class="login-page merchant-login">
    <div class="login-container">
      <!-- 左侧装饰 -->
      <div class="login-banner">
        <div class="banner-content">
          <el-icon class="banner-icon"><Shop /></el-icon>
          <h1>商家管理中心</h1>
          <p>高效管理，轻松运营</p>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <div class="login-form-content">
          <h2>商家登录</h2>
          <p class="subtitle">请输入您的商家账号密码</p>
          
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
                placeholder="请输入商家账号"
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
            <span>还没有商家账号？</span>
            <router-link to="/merchant/register">申请入驻</router-link>
          </div>
          
          <div class="switch-role">
            <router-link to="/login">消费者登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { merchantLogin } from '@/api/user'
import request from '@/api/request'

interface CaptchaResponse {
  uuid: string
  image: string
}

const router = useRouter()
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
    { required: true, message: '请输入商家账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为3-20个字符', trigger: 'blur' }
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
    const res = await merchantLogin(formData)
    await refreshCaptcha()

    // 后端返回扁平结构: { token, userId, username, avatar, role, shopId, shopName }
    const data = res.data
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify({
      id: data.userId,
      username: data.username,
      phone: '',
      email: '',
      avatar: data.avatar || '',
      role: data.role,
      status: 1,
      createTime: ''
    }))
    
    ElMessage.success('登录成功')
    router.push('/merchant')
  } catch (error) {
    await refreshCaptcha()
    // 错误已在拦截器处理
    console.error('商家登录失败:', error)
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
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  padding: 20px;
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.login-banner {
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
  color: #333;
}

.subtitle {
  color: #666;
  margin-bottom: 32px;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #FF8C00 0%, #FF6B00 100%);
  border: none;
}

.login-btn:hover {
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
