import { createStore } from 'vuex'
import auth from './modules/auth'
import admin from './modules/admin'

export default createStore({
  modules: {
    auth,
    admin
  }
})