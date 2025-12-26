<template>
  <div class="student-dashboard">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎回来，{{ currentUser?.realName || '同学' }}！</h2>
              <p>今天是 {{ currentDate }}，祝您学习愉快！</p>
            </div>
            <div class="welcome-avatar">
              <el-avatar :size="80" :src="userAvatar">
                {{ userInitial }}
              </el-avatar>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40" color="#409EFF"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalLabs }}</div>
              <div class="stat-label">实验室总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40" color="#67C23A"><Files /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.myApplications }}</div>
              <div class="stat-label">我的申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40" color="#E6A23C"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingApplications }}</div>
              <div class="stat-label">待审核申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40" color="#F56C6C"><Select /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.approvedApplications }}</div>
              <div class="stat-label">已通过申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 最新实验室 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新实验室</span>
              <el-button type="primary" text @click="$router.push('/student/labs')">
                查看更多
              </el-button>
            </div>
          </template>
          <div v-loading="loadingLabs">
            <div
              v-for="lab in latestLabs"
              :key="lab.id"
              class="lab-item"
              @click="viewLabDetail(lab.id)"
            >
              <div class="lab-title">{{ lab.name }}</div>
              <div class="lab-info">
                <el-tag size="small" :type="getStatusType(lab.status, 'lab')">
                  {{ getStatusText(lab.status, 'lab') }}
                </el-tag>
                <span class="lab-date">{{ formatDate(lab.createTime) }}</span>
              </div>
            </div>
            <div v-if="latestLabs.length === 0" class="empty-state">
              暂无实验室信息
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 我的申请状态 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的申请状态</span>
              <el-button type="primary" text @click="$router.push('/student/delivery')">
                查看全部
              </el-button>
            </div>
          </template>
          <div v-loading="loadingApplications">
            <div
              v-for="application in recentApplications"
              :key="application.id"
              class="application-item"
            >
              <div class="application-title">{{ application.labName }}</div>
              <div class="application-info">
                <el-tag size="small" :type="getStatusType(application.status, 'application')">
                  {{ getStatusText(application.status, 'application') }}
                </el-tag>
                <span class="application-date">{{ formatDate(application.createTime) }}</span>
              </div>
            </div>
            <div v-if="recentApplications.length === 0" class="empty-state">
              暂无申请记录
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { Document, Files, Clock, Select } from '@element-plus/icons-vue'
import request from '@/utils/request'

export default {
  name: 'StudentDashboard',
  components: {
    Document,
    Files,
    Clock,
    Select
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    
    // 加载状态
    const loadingLabs = ref(false)
    const loadingApplications = ref(false)
    
    // 当前用户信息
    const currentUser = computed(() => store.getters.currentUser)
    
    // 用户头像
    const userAvatar = computed(() => {
      return currentUser.value?.avatar || ''
    })
    
    // 用户名首字
    const userInitial = computed(() => {
      const name = currentUser.value?.realName || '用'
      return name.charAt(0)
    })
    
    // 当前日期
    const currentDate = computed(() => {
      const date = new Date()
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      const weekDay = weekDays[date.getDay()]
      return `${year}年${month}月${day}日 ${weekDay}`
    })
    
    // 统计数据
    const stats = ref({
      totalLabs: 0,
      myApplications: 0,
      pendingApplications: 0,
      approvedApplications: 0
    })
    
    // 最新实验室
    const latestLabs = ref([])
    
    // 最近申请
    const recentApplications = ref([])
    
    // 获取统计数据
    const fetchStats = async () => {
      try {
        // 获取实验室总数
        const labsResponse = await request.get('/api/labs/list', {
          params: { pageNum: 1, pageSize: 1 }
        })
        stats.value.totalLabs = labsResponse.data.data.total || 0
        
        // 获取申请统计
        const deliveryResponse = await request.get('/api/delivery/list', {
          params: { pageNum: 1, pageSize: 1 }
        })
        const totalApplications = deliveryResponse.data.data.total || 0
        stats.value.myApplications = totalApplications
        
        // 获取待审核申请数
        const pendingResponse = await request.get('/api/delivery/list', {
          params: { pageNum: 1, pageSize: 1, auditStatus: 0 }
        })
        stats.value.pendingApplications = pendingResponse.data.data.total || 0
        
        // 获取已通过申请数
        const approvedResponse = await request.get('/api/delivery/list', {
          params: { pageNum: 1, pageSize: 1, auditStatus: 1 }
        })
        stats.value.approvedApplications = approvedResponse.data.data.total || 0
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    }
    
    // 获取最新实验室
    const fetchLatestLabs = async () => {
      loadingLabs.value = true
      try {
        const response = await request.get('/api/labs/list', {
          params: { pageNum: 1, pageSize: 5 }
        })
        latestLabs.value = response.data.data.records || []
      } catch (error) {
        console.error('获取最新实验室失败:', error)
      } finally {
        loadingLabs.value = false
      }
    }
    
    // 获取最近申请
    const fetchRecentApplications = async () => {
      loadingApplications.value = true
      try {
        const response = await request.get('/api/delivery/list', {
          params: { pageNum: 1, pageSize: 5 }
        })
        recentApplications.value = response.data.data.records || []
      } catch (error) {
        console.error('获取最近申请失败:', error)
      } finally {
        loadingApplications.value = false
      }
    }
    
    // 查看实验室详情
    const viewLabDetail = (labId) => {
      router.push(`/student/labs?id=${labId}`)
    }
    
    // 获取状态类型
    const getStatusType = (status, type) => {
      if (type === 'lab') {
        // 实验室状态
        const statusMap = {
          1: 'success',   // 招募中
          2: 'danger'     // 已关闭
        }
        return statusMap[status] || 'info'
      } else {
        // 申请状态
        const statusMap = {
          0: 'warning',   // 待审核
          1: 'success',   // 已通过
          2: 'danger'     // 已拒绝
        }
        return statusMap[status] || 'info'
      }
    }
    
    // 获取状态文本
    const getStatusText = (status, type) => {
      if (type === 'lab') {
        // 实验室状态
        const statusMap = {
          1: '招募中',
          2: '已关闭'
        }
        return statusMap[status] || '未知'
      } else {
        // 申请状态
        const statusMap = {
          0: '待审核',
          1: '已通过',
          2: '已拒绝'
        }
        return statusMap[status] || '未知'
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchStats()
      fetchLatestLabs()
      fetchRecentApplications()
    })
    
    return {
      currentUser,
      userAvatar,
      userInitial,
      currentDate,
      stats,
      latestLabs,
      recentApplications,
      loadingLabs,
      loadingApplications,
      viewLabDetail,
      getStatusType,
      getStatusText,
      formatDate
    }
  }
}
</script>

<style scoped>
.student-dashboard {
  padding: 0;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.welcome-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lab-item, .application-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.lab-item:hover, .application-item:hover {
  background-color: #f5f7fa;
  padding-left: 10px;
  margin: 0 -10px;
}

.lab-item:last-child, .application-item:last-child {
  border-bottom: none;
}

.lab-title, .application-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 500;
}

.lab-info, .application-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lab-date, .application-date {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  text-align: center;
  padding: 20px 0;
  color: #909399;
  font-size: 14px;
}
</style>