<template>
  <div class="admin-dashboard">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎回来，{{ currentUser?.realName || '管理员' }}！</h2>
              <p>今天是 {{ currentDate }}，系统运行正常。</p>
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
              <el-icon :size="40" color="#67C23A"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalStudents }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40" color="#E6A23C"><Files /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalApplications }}</div>
              <div class="stat-label">申请总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40" color="#F56C6C"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingApplications }}</div>
              <div class="stat-label">待审核申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 最新申请 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新申请</span>
              <el-button type="primary" text @click="$router.push('/admin/delivery')">
                查看更多
              </el-button>
            </div>
          </template>
          <div v-loading="loadingApplications">
            <div
              v-for="application in latestApplications"
              :key="application.id"
              class="application-item"
              @click="viewApplicationDetail(application.id)"
            >
              <div class="application-title">{{ application.labName }}</div>
              <div class="application-info">
                <span class="application-student">{{ application.studentName }}</span>
                <el-tag size="small" :type="getStatusType(application.status)">
                  {{ getStatusText(application.status) }}
                </el-tag>
                <span class="application-date">{{ formatDate(application.createTime) }}</span>
              </div>
            </div>
            <div v-if="latestApplications.length === 0" class="empty-state">
              暂无申请记录
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 最新注册学生 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新注册学生</span>
              <el-button type="primary" text @click="$router.push('/admin/students')">
                查看更多
              </el-button>
            </div>
          </template>
          <div v-loading="loadingStudents">
            <div
              v-for="student in latestStudents"
              :key="student.id"
              class="student-item"
              @click="viewStudentDetail(student.id)"
            >
              <div class="student-info">
                <el-avatar :size="40" :src="student.avatar">
                  {{ student.realName?.charAt(0) }}
                </el-avatar>
                <div class="student-details">
                  <div class="student-name">{{ student.realName }}</div>
                  <div class="student-meta">
                    <span>{{ student.studentId }}</span>
                    <span>{{ student.major }}</span>
                  </div>
                </div>
              </div>
              <div class="student-date">{{ formatDate(student.createTime) }}</div>
            </div>
            <div v-if="latestStudents.length === 0" class="empty-state">
              暂无学生注册记录
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, User, Files, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

export default {
  name: 'AdminDashboard',
  components: {
    Document,
    User,
    Files,
    Clock
  },
  setup() {
    const userStore = useUserStore()
    const router = useRouter()
    
    // 加载状态
    const loadingApplications = ref(false)
    const loadingStudents = ref(false)
    
    // 当前用户信息
    const currentUser = computed(() => userStore.userInfo)
    
    // 用户头像
    const userAvatar = computed(() => {
      return currentUser.value?.avatar || ''
    })
    
    // 用户名首字
    const userInitial = computed(() => {
      const name = currentUser.value?.realName || '管'
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
      totalStudents: 0,
      totalApplications: 0,
      pendingApplications: 0
    })
    
    // 最新申请
    const latestApplications = ref([])
    
    // 最新注册学生
    const latestStudents = ref([])
    
    // 获取统计数据
    const fetchStats = async () => {
      try {
        // 获取实验室总数
        const labsResponse = await request.get('/api/labs/list', {
          params: { pageNum: 1, pageSize: 1 }
        })
        stats.value.totalLabs = labsResponse.data.total || 0
        
        // 获取学生总数
        const studentsResponse = await request.get('/api/admin/users', {
          params: { pageNum: 1, pageSize: 1 }
        })
        stats.value.totalStudents = studentsResponse.data.total || 0
        
        // 获取申请总数
        const deliveryResponse = await request.get('/api/delivery/list', {
          params: { pageNum: 1, pageSize: 1 }
        })
        const totalApplications = deliveryResponse.data.total || 0
        stats.value.totalApplications = totalApplications
        
        // 获取待审核申请数
        const pendingResponse = await request.get('/api/delivery/list', {
          params: { pageNum: 1, pageSize: 1, auditStatus: 0 }
        })
        stats.value.pendingApplications = pendingResponse.data.total || 0
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    }
    
    // 获取最新申请
    const fetchLatestApplications = async () => {
      loadingApplications.value = true
      try {
        const response = await request.get('/api/delivery/list', {
          params: { pageNum: 1, pageSize: 5 }
        })
        latestApplications.value = response.data.records || []
      } catch (error) {
        console.error('获取最新申请失败:', error)
      } finally {
        loadingApplications.value = false
      }
    }
    
    // 获取最新注册学生
    const fetchLatestStudents = async () => {
      loadingStudents.value = true
      try {
        const response = await request.get('/api/admin/users', {
          params: { pageNum: 1, pageSize: 5 }
        })
        latestStudents.value = response.data.records || []
      } catch (error) {
        console.error('获取最新注册学生失败:', error)
      } finally {
        loadingStudents.value = false
      }
    }
    
    // 查看申请详情
    const viewApplicationDetail = (applicationId) => {
      router.push(`/admin/delivery?id=${applicationId}`)
    }
    
    // 查看学生详情
    const viewStudentDetail = (studentId) => {
      router.push(`/admin/students?id=${studentId}`)
    }
    
    // 获取状态类型
    const getStatusType = (status) => {
      const statusMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return statusMap[status] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝'
      }
      return statusMap[status] || '未知'
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
      fetchLatestApplications()
      fetchLatestStudents()
    })
    
    return {
      currentUser,
      userAvatar,
      userInitial,
      currentDate,
      stats,
      latestApplications,
      latestStudents,
      loadingApplications,
      loadingStudents,
      viewApplicationDetail,
      viewStudentDetail,
      getStatusType,
      getStatusText,
      formatDate
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
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

.application-item, .student-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
}

.application-item:hover, .student-item:hover {
  background-color: #f5f7fa;
  padding-left: 10px;
  margin: 0 -10px;
}

.application-item:last-child, .student-item:last-child {
  border-bottom: none;
}

.application-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 500;
}

.application-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.application-student {
  font-size: 14px;
  color: #606266;
}

.application-date {
  font-size: 12px;
  color: #909399;
}

.student-info {
  display: flex;
  align-items: center;
}

.student-details {
  margin-left: 12px;
}

.student-name {
  font-size: 16px;
  color: #303133;
  margin-bottom: 5px;
}

.student-meta {
  font-size: 12px;
  color: #909399;
}

.student-meta span {
  margin-right: 10px;
}

.student-date {
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