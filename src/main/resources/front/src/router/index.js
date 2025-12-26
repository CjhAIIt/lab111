import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true },
    children: [
      // 学生路由
      {
        path: 'student',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { roles: ['student'] }
      },
      // 管理员路由
      {
        path: 'admin',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { roles: ['admin'] }
      },
      // 总负责人路由
      {
        path: 'super',
        name: 'SuperDashboard',
        component: () => import('@/views/super/Dashboard.vue'),
        meta: { roles: ['super_admin'] },
        children: [
          {
            path: '',
            name: 'SystemOverview',
            component: () => import('@/views/super/Overview.vue')
          },
          {
            path: 'admin-management',
            name: 'AdminManagement',
            component: () => import('@/views/super/AdminManagement.vue')
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  
  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.roles) {
      // 这里应该从store中获取用户角色
      // 由于路由守卫在store初始化之前执行，我们需要在meta中处理角色验证
      // 实际项目中可能需要更复杂的逻辑来处理角色验证
      // 这里简化处理，在Dashboard组件中进行角色重定向
    }
  }
  
  // 如果已登录且访问登录页，重定向到dashboard
  if (token && (to.path === '/login' || to.path === '/register')) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router