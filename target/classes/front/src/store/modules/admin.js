import { getAdminList, createAdmin, updateAdmin, deleteAdmin, getAdminDetail } from '@/api/admin'

const state = {
  adminList: [],
  currentAdmin: null,
  loading: false
}

const mutations = {
  SET_ADMIN_LIST(state, list) {
    state.adminList = list
  },
  SET_CURRENT_ADMIN(state, admin) {
    state.currentAdmin = admin
  },
  SET_LOADING(state, loading) {
    state.loading = loading
  },
  ADD_ADMIN(state, admin) {
    state.adminList.unshift(admin)
  },
  UPDATE_ADMIN(state, updatedAdmin) {
    const index = state.adminList.findIndex(admin => admin.id === updatedAdmin.id)
    if (index !== -1) {
      state.adminList.splice(index, 1, updatedAdmin)
    }
  },
  REMOVE_ADMIN(state, adminId) {
    const index = state.adminList.findIndex(admin => admin.id === adminId)
    if (index !== -1) {
      state.adminList.splice(index, 1)
    }
  }
}

const actions = {
  // 获取管理员列表
  async fetchAdminList({ commit }) {
    commit('SET_LOADING', true)
    try {
      const response = await getAdminList()
      commit('SET_ADMIN_LIST', response.data)
      return response.data
    } catch (error) {
      console.error('获取管理员列表失败:', error)
      throw error
    } finally {
      commit('SET_LOADING', false)
    }
  },

  // 创建管理员
  async createAdmin({ commit }, adminData) {
    try {
      const response = await createAdmin(adminData)
      commit('ADD_ADMIN', response.data)
      return response.data
    } catch (error) {
      console.error('创建管理员失败:', error)
      throw error
    }
  },

  // 更新管理员
  async updateAdmin({ commit }, { id, data }) {
    try {
      const response = await updateAdmin(id, data)
      commit('UPDATE_ADMIN', response.data)
      return response.data
    } catch (error) {
      console.error('更新管理员失败:', error)
      throw error
    }
  },

  // 删除管理员
  async deleteAdmin({ commit }, adminId) {
    try {
      await deleteAdmin(adminId)
      commit('REMOVE_ADMIN', adminId)
    } catch (error) {
      console.error('删除管理员失败:', error)
      throw error
    }
  },

  // 获取管理员详情
  async fetchAdminDetail({ commit }, adminId) {
    try {
      const response = await getAdminDetail(adminId)
      commit('SET_CURRENT_ADMIN', response.data)
      return response.data
    } catch (error) {
      console.error('获取管理员详情失败:', error)
      throw error
    }
  }
}

const getters = {
  adminList: state => state.adminList,
  currentAdmin: state => state.currentAdmin,
  loading: state => state.loading
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}