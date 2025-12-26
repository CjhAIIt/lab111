import { defineStore } from 'pinia'
import { getToken, setToken as setAuthToken, removeToken, getUserInfo, setUserInfo as setAuthUserInfo, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: getUserInfo(),
    token: getToken()
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    userRole: (state) => state.userInfo?.role,
    userName: (state) => state.userInfo?.username,
    realName: (state) => state.userInfo?.realName
  },
  
  actions: {
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      // 保存用户角色到localStorage以便路由守卫使用
      if (userInfo && userInfo.role) {
        localStorage.setItem('userRole', userInfo.role)
      }
      // 保存用户信息到localStorage
      setAuthUserInfo(userInfo)
    },
    
    setToken(token) {
      this.token = token
      // 保存到localStorage
      setAuthToken(token)
    },
    
    clearUserInfo() {
      this.userInfo = null
      this.token = null
      // 清除localStorage
      clearAuth()
      localStorage.removeItem('userRole')
    }
  }
})