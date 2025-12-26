<template>
  <div class="system-overview">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon student">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.studentCount }}</div>
              <div class="stat-label">学生总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon admin">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.adminCount }}</div>
              <div class="stat-label">管理员总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon lab">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.labCount }}</div>
              <div class="stat-label">实验室总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon recruitment">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.recruitmentCount }}</div>
              <div class="stat-label">招募总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新管理员</span>
            </div>
          </template>
          <el-table :data="recentAdmins" style="width: 100%">
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="realName" label="真实姓名" />
            <el-table-column prop="labName" label="负责实验室" />
            <el-table-column prop="createTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统公告</span>
            </div>
          </template>
          <div class="announcement-list">
            <div v-for="announcement in announcements" :key="announcement.id" class="announcement-item">
              <div class="announcement-title">{{ announcement.title }}</div>
              <div class="announcement-time">{{ announcement.time }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { User, UserFilled, OfficeBuilding, Document } from '@element-plus/icons-vue'
import { mapActions } from 'vuex'

export default {
  name: 'SystemOverview',
  components: {
    User,
    UserFilled,
    OfficeBuilding,
    Document
  },
  data() {
    return {
      stats: {
        studentCount: 0,
        adminCount: 0,
        labCount: 0,
        recruitmentCount: 0
      },
      recentAdmins: [],
      announcements: [
        {
          id: 1,
          title: '系统升级通知：新增管理员管理功能',
          time: '2023-12-20'
        },
        {
          id: 2,
          title: '请各管理员及时更新实验室信息',
          time: '2023-12-18'
        },
        {
          id: 3,
          title: '新学期实验室招募即将开始',
          time: '2023-12-15'
        }
      ]
    }
  },
  created() {
    this.fetchSystemStats()
    this.fetchRecentAdmins()
  },
  methods: {
    ...mapActions('admin', ['fetchAdminList']),
    
    async fetchSystemStats() {
      try {
        // 这里应该调用获取系统统计数据的API
        // 暂时使用模拟数据
        this.stats = {
          studentCount: 1250,
          adminCount: 15,
          labCount: 8,
          recruitmentCount: 320
        }
      } catch (error) {
        console.error('获取系统统计数据失败:', error)
      }
    },
    
    async fetchRecentAdmins() {
      try {
        await this.fetchAdminList()
        // 获取最近添加的管理员
        this.recentAdmins = [
          {
            username: 'admin001',
            realName: '张三',
            labName: '计算机实验室',
            createTime: '2023-12-20'
          },
          {
            username: 'admin002',
            realName: '李四',
            labName: '物理实验室',
            createTime: '2023-12-18'
          }
        ]
      } catch (error) {
        console.error('获取最新管理员失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.system-overview {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.student {
  background-color: #409EFF;
}

.stat-icon.admin {
  background-color: #67C23A;
}

.stat-icon.lab {
  background-color: #E6A23C;
}

.stat-icon.recruitment {
  background-color: #F56C6C;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-list {
  max-height: 300px;
  overflow-y: auto;
}

.announcement-item {
  padding: 10px 0;
  border-bottom: 1px solid #EBEEF5;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
}

.announcement-time {
  font-size: 12px;
  color: #909399;
}
</style>