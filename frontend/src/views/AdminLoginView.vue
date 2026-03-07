<template>
  <div class="login-page admin-login">
    <div class="login-container">
      <!-- 左侧装饰 -->
      <div class="login-banner">
        <div class="banner-content">
          <el-icon class="banner-icon"><Setting /></el-icon>
          <h1>系统管理后台</h1>
          <p>安全高效的管理平台</p>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-form-wrapper">
        <div class="login-form-content">
          <h2>管理员登录</h2>
          <p class="subtitle">仅限授权管理员使用</p>
          
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
                placeholder="请输入管理员账号"
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
          
          <div class="switch-role">
            <router-link to="/login">消费者登录</router-link>
            <span class="divider">|</span>
            <router-link to="/merchant/login">商家登录</router-link>
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
import { adminLogin } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    loading.value = true
    const res = await adminLogin(formData)
    
    // 后端返回扁平结构: { token, userId, username, avatar, role }
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
    router.push('/admin')
  } catch (error) {
    // 错误已在拦截器处理
    console.error('管理员登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
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
  background: linear-gradient(135deg, #1976D2 0%, #0D47A1 100%);
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
  background: linear-gradient(135deg, #1976D2 0%, #0D47A1 100%);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #1565C0 0%, #0D47A1 100%);
}

.switch-role {
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.switch-role a {
  color: #999;
  font-size: 14px;
}

.switch-role a:hover {
  color: #1976D2;
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
