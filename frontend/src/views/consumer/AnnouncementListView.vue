<template>
  <div class="announcement-list">
    <div class="announcement-header">
      <h2>平台公告</h2>
      <el-tabs v-model="activeType">
        <el-tab-pane label="全部" name="0"></el-tab-pane>
        <el-tab-pane label="系统公告" name="1"></el-tab-pane>
        <el-tab-pane label="活动公告" name="2"></el-tab-pane>
        <el-tab-pane label="知识科普" name="3"></el-tab-pane>
      </el-tabs>
    </div>

    <el-empty v-if="announcements.length === 0" description="暂无公告" />
    
    <div v-else class="announcement-items">
      <div 
        v-for="item in announcements" 
        :key="item.id" 
        class="announcement-item"
        @click="goToDetail(item.id)"
      >
        <div class="announcement-cover" v-if="item.coverImage">
          <el-image :src="item.coverImage" fit="cover" />
        </div>
        <div class="announcement-content">
          <div class="announcement-top">
            <el-tag :type="getTypeTag(item.type)" size="small">
              {{ getTypeName(item.type) }}
            </el-tag>
            <span class="announcement-time">{{ formatDate(item.publishTime) }}</span>
          </div>
          <h3 class="announcement-title">{{ item.title }}</h3>
          <p class="announcement-desc">{{ item.content }}</p>
          <div class="announcement-footer">
            <span class="view-count">
              <el-icon><View /></el-icon>
              {{ item.viewCount }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadAnnouncements"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { View } from '@element-plus/icons-vue'
import { getAnnouncementList } from '@/api/announcement'

const router = useRouter()
const activeType = ref('0')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const announcements = ref<any[]>([])

// 加载公告列表
const loadAnnouncements = async () => {
  try {
    const params: any = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (activeType.value !== '0') {
      params.type = Number(activeType.value)
    }
    const res = await getAnnouncementList(params)
    if (res.code === 200) {
      announcements.value = res.data.records || res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

// 切换类型
watch(activeType, () => {
  currentPage.value = 1
  loadAnnouncements()
})

// 跳转到详情页
const goToDetail = (id: number) => {
  router.push(`/announcement/${id}`)
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
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcement-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .announcement-header {
    margin-bottom: 30px;
    
    h2 {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: 20px;
    }
  }

  .announcement-items {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .announcement-item {
    display: flex;
    background: white;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .announcement-cover {
      width: 240px;
      height: 180px;
      flex-shrink: 0;

      .el-image {
        width: 100%;
        height: 100%;
      }
    }

    .announcement-content {
      flex: 1;
      padding: 20px;
      display: flex;
      flex-direction: column;

      .announcement-top {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;

        .announcement-time {
          font-size: 14px;
          color: #999;
        }
      }

      .announcement-title {
        font-size: 20px;
        font-weight: bold;
        color: #333;
        margin-bottom: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
      }

      .announcement-desc {
        font-size: 14px;
        color: #666;
        line-height: 1.6;
        margin-bottom: 16px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        flex: 1;
      }

      .announcement-footer {
        display: flex;
        align-items: center;
        justify-content: flex-end;

        .view-count {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 14px;
          color: #999;

          .el-icon {
            font-size: 16px;
          }
        }
      }
    }
  }

  .pagination {
    margin-top: 30px;
    display: flex;
    justify-content: center;
  }
}
</style>
