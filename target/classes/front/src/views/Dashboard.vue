<template>
  <div class="dashboard">
    <router-view />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 根据用户角色重定向到相应的控制台
    const redirectToRoleDashboard = (role) => {
      switch (role) {
        case 'student':
          router.replace('/dashboard/student')
          break
        case 'admin':
          router.replace('/dashboard/admin')
          break
        case 'super_admin':
          router.replace('/dashboard/super')
          break
        default:
          router.replace('/login')
      }
    }
    
    return {
      redirectToRoleDashboard
    }
  },
  computed: {
    ...mapState('auth', ['userInfo', 'token'])
  },
  watch: {
    userInfo: {
      handler(newUserInfo) {
        if (newUserInfo && newUserInfo.role) {
          // 如果当前路径是dashboard根路径，则重定向到角色对应的控制台
          if (this.$route.path === '/dashboard') {
            this.redirectToRoleDashboard(newUserInfo.role)
          }
        }
      },
      immediate: true
    }
  },
  created() {
    // 如果用户信息已加载且当前在dashboard根路径，则重定向
    if (this.userInfo && this.userInfo.role && this.$route.path === '/dashboard') {
      this.redirectToRoleDashboard(this.userInfo.role)
    }
  }
}
</script>

<style scoped>
.dashboard {
  width: 100%;
  height: 100%;
}
</style>