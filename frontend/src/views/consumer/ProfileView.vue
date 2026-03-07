<template>
  <div class="profile-page">
    <div class="profile-card">
      <div class="card-header">
        <h2><el-icon><User /></el-icon> 个人信息</h2>
        <el-button v-if="!isEditing" type="primary" text @click="isEditing = true">
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
      </div>
      
      <!-- 查看模式 -->
      <div v-if="!isEditing" class="info-display">
        <div class="info-item">
          <span class="label">头像</span>
          <el-avatar :size="60" :src="getAvatarUrl(userStore.userInfo?.avatar)" />
        </div>
        <div class="info-item">
          <span class="label">用户名</span>
          <span class="value">{{ userStore.userInfo?.username }}</span>
        </div>
        <div class="info-item">
          <span class="label">昵称</span>
          <span class="value">{{ userStore.userInfo?.nickname || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">手机号</span>
          <span class="value">{{ userStore.userInfo?.phone || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">邮箱</span>
          <span class="value">{{ userStore.userInfo?.email || '-' }}</span>
        </div>
      </div>
      
      <!-- 编辑模式 -->
      <el-form 
        v-else
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        v-loading="loading"
      >
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="getAvatarUrl(form.avatar)" />
            <el-upload
              action=""
              :show-file-list="false"
              :before-upload="handleAvatarUpload"
            >
              <el-button type="primary" text>更换头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
        
        <el-form-item label="用户名">
          <el-input :value="userStore.userInfo?.username" disabled />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="saving">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="profile-card">
      <div class="card-header">
        <h2><el-icon><Lock /></el-icon> 安全设置</h2>
      </div>
      
      <div class="security-item" @click="showPasswordDialog = true">
        <div class="security-info">
          <span class="title">修改密码</span>
          <span class="desc">建议定期更换密码，保障账户安全</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-width="80px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changingPwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, updatePassword, getCurrentUser } from '@/api/user'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取完整的头像URL
function getAvatarUrl(avatar: string | undefined | null): string {
  if (!avatar) return defaultAvatar
  // 如果是相对路径，加上/api前缀
  if (avatar.startsWith('/uploads/')) {
    return '/api' + avatar
  }
  // 如果已经是完整URL或其他情况
  return avatar
}

const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const changingPwd = ref(false)
const isEditing = ref(false)
const showPasswordDialog = ref(false)

const form = reactive({
  nickname: '',
  phone: '',
  email: '',
  avatar: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const pwdRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 从本地 store 加载用户信息
function loadUserInfo() {
  const userInfo = userStore.userInfo
  if (userInfo) {
    form.nickname = userInfo.nickname || ''
    form.phone = userInfo.phone || ''
    form.email = userInfo.email || ''
    form.avatar = userInfo.avatar || ''
  }
}

// 从 API 获取最新用户信息
async function fetchUserInfo() {
  loading.value = true
  try {
    const res = await getCurrentUser()
    const userInfo = res.data
    if (userInfo) {
      // 更新本地 store
      userStore.userInfo = userInfo
      // 更新表单
      form.nickname = userInfo.nickname || ''
      form.phone = userInfo.phone || ''
      form.email = userInfo.email || ''
      form.avatar = userInfo.avatar || ''
    }
  } catch {
    // API 失败时使用本地数据
    loadUserInfo()
  } finally {
    loading.value = false
  }
}

function cancelEdit() {
  isEditing.value = false
  loadUserInfo() // 恢复原数据
}

async function handleAvatarUpload(file: File) {
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('只能上传图片文件')
    return false
  }
  
  // 检查文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return false
  }
  
  // 创建 FormData 并上传
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await fetch('/api/common/upload/image', {
      method: 'POST',
      headers: {
        'Authorization': localStorage.getItem('token') || ''
      },
      body: formData
    })
    const result = await response.json()
    if (result.code === 200) {
      form.avatar = result.data // 使用返回的 URL 路径
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(result.message || '上传失败')
    }
  } catch {
    ElMessage.error('上传失败，请重试')
  }
  
  return false // 阻止 el-upload 默认行为
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  
  saving.value = true
  try {
    // 调用更新用户信息接口 - 展开 reactive 对象
    await updateUserInfo({
      nickname: form.nickname,
      phone: form.phone,
      email: form.email,
      avatar: form.avatar
    })
    ElMessage.success('保存成功')
    // 更新本地用户信息
    if (userStore.userInfo) {
      userStore.userInfo = {
        ...userStore.userInfo,
        nickname: form.nickname,
        phone: form.phone,
        email: form.email,
        avatar: form.avatar
      }
    }
    isEditing.value = false
  } catch {
    // 错误已由拦截器处理
  } finally {
    saving.value = false
  }
}

async function handleChangePassword() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate()
  
  changingPwd.value = true
  try {
    // 调用修改密码接口
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false
    pwdFormRef.value.resetFields()
    // 退出登录并跳转到登录页
    userStore.logout()
    router.push('/login')
  } catch {
    // 错误已由拦截器处理
  } finally {
    changingPwd.value = false
  }
}

onMounted(() => {
  // 从 API 获取最新用户信息
  fetchUserInfo()
})
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  margin: 0;
}

.card-header h2 .el-icon {
  color: var(--color-primary);
}

/* 查看模式 */
.info-display {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-item .label {
  width: 80px;
  color: var(--color-text-secondary);
}

.info-item .value {
  color: var(--color-text-primary);
}

/* 编辑模式 */
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.el-form {
  max-width: 400px;
}

/* 安全设置 */
.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 0.3s;
}

.security-item:hover {
  background: var(--color-background);
}

.security-info .title {
  font-weight: 500;
  display: block;
}

.security-info .desc {
  color: var(--color-text-light);
  font-size: 13px;
  margin-top: 4px;
  display: block;
}
</style>
