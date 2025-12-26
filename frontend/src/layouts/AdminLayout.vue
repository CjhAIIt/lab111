<template>
  <div class="admin-layout">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h2>实验室招募系统 - 管理端</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar || ''">{{ (userInfo.realName || userInfo.username || 'A').charAt(0) }}</el-avatar>
              <span class="username">{{ userInfo.realName || userInfo.username || '管理员' }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="250px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            router
            text-color="#333"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/admin/dashboard">
              <el-icon><monitor /></el-icon>
              <span>控制台</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/labs">
              <el-icon><house /></el-icon>
              <span>实验室管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/delivery">
              <el-icon><document /></el-icon>
              <span>投递管理</span>
            </el-menu-item>
            
            <el-menu-item index="/admin/students">
              <el-icon><user /></el-icon>
              <span>学生管理</span>
            </el-menu-item>
            
            <el-menu-item 
              index="/admin/admin-management" 
              v-if="userInfo.role === 'super_admin'"
            >
              <el-icon><user-filled /></el-icon>
              <span>管理员管理</span>
            </el-menu-item>
            
            <el-menu-item 
              index="/admin/lab-admin-management" 
              v-if="userInfo.role === 'super_admin'"
            >
              <el-icon><connection /></el-icon>
              <span>实验室管理员绑定</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Monitor, House, Document, User, UserFilled, Connection } from '@element-plus/icons-vue'
import { getUserInfo, clearAuth } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const userInfo = ref({})

// 计算当前激活的菜单项
const activeMenu = computed(() => route.path)

// 获取用户信息
const fetchUserInfo = () => {
  const user = getUserInfo()
  if (user) {
    userInfo.value = user
  }
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      clearAuth()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
    .catch(() => {
      // 用户取消
    })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  color: #1890ff;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  margin: 0 8px;
  color: #333;
}

.sidebar {
  background-color: #fff;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.08);
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  border-right: 3px solid #409EFF;
}

.main-content {
  background-color: #f5f5f5;
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
}
</style>