import { login, logout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

const state = {
  token: getToken(),
  userInfo: null,
  roles: []
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
  },
  SET_ROLES(state, roles) {
    state.roles = roles
  },
  CLEAR_USER(state) {
    state.token = null
    state.userInfo = null
    state.roles = []
  }
}

const actions = {
  // 用户登录
  login({ commit }, loginForm) {
    const { username, password } = loginForm
    return new Promise((resolve, reject) => {
      login({ username: username.trim(), password })
        .then(response => {
          const { data } = response
          commit('SET_TOKEN', data.token)
          commit('SET_USER_INFO', {
            id: data.id,
            username: data.username,
            realName: data.realName,
            role: data.role,
            avatar: data.avatar
          })
          commit('SET_ROLES', [data.role])
          setToken(data.token)
          resolve(data)
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 获取用户信息
  getUserInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getUserInfo(state.token)
        .then(response => {
          const { data } = response
          
          if (!data) {
            reject('获取用户信息失败，请重新登录')
          }
          
          const { role, ...userInfo } = data
          commit('SET_USER_INFO', userInfo)
          commit('SET_ROLES', [role])
          resolve(data)
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 用户登出
  logout({ commit }) {
    return new Promise((resolve, reject) => {
      logout(state.token)
        .then(() => {
          commit('CLEAR_USER')
          removeToken()
          resolve()
        })
        .catch(error => {
          reject(error)
        })
    })
  },

  // 前端登出
  fedLogout({ commit }) {
    return new Promise(resolve => {
      commit('CLEAR_USER')
      removeToken()
      resolve()
    })
  },

  // 重置用户状态
  resetUser({ commit }) {
    return new Promise(resolve => {
      commit('CLEAR_USER')
      resolve()
    })
  }
}

const getters = {
  token: state => state.token,
  userInfo: state => state.userInfo,
  roles: state => state.roles,
  avatar: state => state.userInfo?.avatar,
  username: state => state.userInfo?.username,
  realName: state => state.userInfo?.realName,
  role: state => state.userInfo?.role
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}