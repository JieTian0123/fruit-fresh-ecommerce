<template>
  <div class="announcement-detail">
    <div class="detail-container" v-if="announcement">
      <div class="detail-header">
        <el-tag :type="getTypeTag(announcement.type)" size="large">
          {{ getTypeName(announcement.type) }}
        </el-tag>
        <h1 class="detail-title">{{ announcement.title }}</h1>
        <div class="detail-meta">
          <span class="meta-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(announcement.publishTime) }}
          </span>
          <span class="meta-item">
            <el-icon><View /></el-icon>
            {{ announcement.viewCount }} 次浏览
          </span>
        </div>
      </div>

      <div class="detail-cover" v-if="announcement.coverImage">
        <el-image :src="announcement.coverImage" fit="cover" />
      </div>

      <div class="detail-content" v-html="announcement.content"></div>

      <div class="detail-footer">
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </div>

    <el-empty v-else description="公告不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Calendar, View } from '@element-plus/icons-vue'
import { getAnnouncementDetail } from '@/api/announcement'

const route = useRoute()
const router = useRouter()
const announcement = ref<any>(null)

// 加载公告详情
const loadDetail = async () => {
  try {
    const id = Number(route.params.id)
    const res = await getAnnouncementDetail(id)
    if (res.code === 200) {
      announcement.value = res.data
    }
  } catch (error) {
    console.error('加载公告详情失败:', error)
  }
}

// 返回列表
const goBack = () => {
  router.back()
}

// 获取类型名称
const getTypeName = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '系统公告',
    2: '活动公告',
    3: '知识科普'
  }
  return typeMap[type] || '公告'
}

// 获取类型标签
const getTypeTag = (type: number) => {
  const tagMap: Record<number, string> = {
    1: 'warning',
    2: 'danger',
    3: 'success'
  }
  return tagMap[type] || ''
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.announcement-detail {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;

  .detail-container {
    background: white;
    border-radius: 8px;
    padding: 40px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .detail-header {
      text-align: center;
      margin-bottom: 40px;

      .el-tag {
        margin-bottom: 20px;
      }

      .detail-title {
        font-size: 32px;
        font-weight: bold;
        color: #333;
        margin-bottom: 20px;
        line-height: 1.4;
      }

      .detail-meta {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 30px;
        font-size: 14px;
        color: #999;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 6px;

          .el-icon {
            font-size: 16px;
          }
        }
      }
    }

    .detail-cover {
      margin-bottom: 40px;
      border-radius: 8px;
      overflow: hidden;

      .el-image {
        width: 100%;
        max-height: 500px;
      }
    }

    .detail-content {
      font-size: 16px;
      line-height: 2;
      color: #333;
      margin-bottom: 40px;

      :deep(p) {
        margin-bottom: 16px;
      }

      :deep(img) {
        max-width: 100%;
        border-radius: 4px;
        margin: 20px 0;
      }

      :deep(h2), :deep(h3) {
        margin-top: 32px;
        margin-bottom: 16px;
        font-weight: bold;
      }

      :deep(h2) {
        font-size: 24px;
      }

      :deep(h3) {
        font-size: 20px;
      }

      :deep(ul), :deep(ol) {
        padding-left: 24px;
        margin-bottom: 16px;
      }

      :deep(li) {
        margin-bottom: 8px;
      }
    }

    .detail-footer {
      text-align: center;
      padding-top: 20px;
      border-top: 1px solid #eee;
    }
  }
}
</style>
