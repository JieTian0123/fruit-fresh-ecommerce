<template>
  <div class="follow-shops-view">
    <div class="page-header">
      <h2>我关注的店铺</h2>
      <p>共关注 {{ shops.length }} 家店铺</p>
    </div>

    <el-empty v-if="shops.length === 0" description="您还没有关注任何店铺" />

    <div v-else class="shops-grid">
      <el-card 
        v-for="shop in shops" 
        :key="shop.id" 
        class="shop-card"
        shadow="hover"
      >
        <div class="shop-cover">
          <el-image :src="shop.logo || '/default-shop.jpg'" fit="cover" />
        </div>
        <div class="shop-info">
          <h3 class="shop-name">{{ shop.shopName }}</h3>
          <p class="shop-desc" v-if="shop.description">{{ shop.description }}</p>
          <div class="shop-stats">
            <span class="stat-item">
              <el-icon><Box /></el-icon>
              {{ shop.productCount || 0 }} 商品
            </span>
            <span class="stat-item">
              <el-icon><Star /></el-icon>
              {{ shop.rating || 5.0 }} 评分
            </span>
          </div>
          <div class="shop-actions">
            <el-button type="primary" @click="visitShop(shop.id)">进入店铺</el-button>
            <el-button @click="handleUnfollow(shop.id)">取消关注</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, Star } from '@element-plus/icons-vue'
import { getMyFollowShops, toggleFollowShop } from '@/api/shop'

const router = useRouter()
const shops = ref<any[]>([])

// 加载关注的店铺
const loadShops = async () => {
  try {
    const res = await getMyFollowShops()
    if (res.code === 200) {
      shops.value = res.data || []
    }
  } catch (error) {
    console.error('加载店铺失败:', error)
  }
}

// 访问店铺
const visitShop = (shopId: number) => {
  router.push(`/shop/${shopId}`)
}

// 取消关注
const handleUnfollow = async (shopId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消关注这家店铺吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await toggleFollowShop(shopId)
    if (res.code === 200) {
      ElMessage.success('已取消关注')
      loadShops()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

onMounted(() => {
  loadShops()
})
</script>

<style scoped>
.follow-shops-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .page-header {
    margin-bottom: 30px;

    h2 {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: #999;
    }
  }

  .shops-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }

  .shop-card {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
    }

    .shop-cover {
      width: 100%;
      height: 180px;
      border-radius: 8px;
      overflow: hidden;
      margin-bottom: 16px;

      .el-image {
        width: 100%;
        height: 100%;
      }
    }

    .shop-info {
      .shop-name {
        font-size: 18px;
        font-weight: bold;
        color: #333;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .shop-desc {
        font-size: 14px;
        color: #666;
        margin-bottom: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        min-height: 40px;
      }

      .shop-stats {
        display: flex;
        gap: 20px;
        margin-bottom: 16px;

        .stat-item {
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

      .shop-actions {
        display: flex;
        gap: 10px;

        .el-button {
          flex: 1;
        }
      }
    }
  }
}
</style>
