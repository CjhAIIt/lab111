<template>
  <div class="super-dashboard">
    <div class="header">
      <h1>总负责人控制台</h1>
      <div class="user-info">
        <span>欢迎，{{ userInfo.realName }}</span>
        <el-button type="primary" @click="logout">退出登录</el-button>
      </div>
    </div>
    
    <div class="menu">
      <el-menu mode="horizontal" :default-active="activeMenu" @select="handleMenuSelect">
        <el-menu-item index="overview">系统概览</el-menu-item>
        <el-menu-item index="admin-management">管理员管理</el-menu-item>
        <el-menu-item index="system-settings">系统设置</el-menu-item>
      </el-menu>
    </div>
    
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'SuperDashboard',
  setup() {
    const router = useRouter()
    
    const handleMenuSelect = (index) => {
      switch (index) {
        case 'overview':
          router.push('/dashboard/super')
          break
        case 'admin-management':
          router.push('/dashboard/super/admin-management')
          break
        case 'system-settings':
          // 系统设置功能待实现
          break
      }
    }
    
    return {
      handleMenuSelect
    }
  },
  data() {
    return {
      activeMenu: 'overview'
    }
  },
  computed: {
    ...mapState('auth', ['userInfo'])
  },
  methods: {
    ...mapActions('auth', ['logout']),
    
    async logout() {
      try {
        await this.logout()
        this.$router.push('/login')
      } catch (error) {
        console.error('退出登录失败:', error)
      }
    }
  },
  watch: {
    '$route'(to) {
      this.updateActiveMenu(to.path)
    }
  },
  created() {
    this.updateActiveMenu(this.$route.path)
  },
  methods: {
    ...mapActions('auth', ['logout']),
    
    updateActiveMenu(path) {
      if (path.includes('admin-management')) {
        this.activeMenu = 'admin-management'
      } else if (path.includes('super')) {
        this.activeMenu = 'overview'
      }
    },
    
    async logout() {
      try {
        await this.logout()
        this.$router.push('/login')
      } catch (error) {
        console.error('退出登录失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.super-dashboard {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.menu {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.content {
  padding: 20px;
}
</style>