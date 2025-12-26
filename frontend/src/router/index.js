import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

// 导入组件
import StudentLayout from '@/layouts/StudentLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'

// 学生页面
import Labs from '@/views/student/Labs.vue'
import Delivery from '@/views/student/Delivery.vue'
import Profile from '@/views/student/Profile.vue'

// 管理员页面
import Dashboard from '@/views/admin/Dashboard.vue'
import AdminLabs from '@/views/admin/Labs.vue'
import AdminDelivery from '@/views/admin/Delivery.vue'
import Students from '@/views/admin/Students.vue'
import AdminManagement from '@/views/admin/AdminManagement.vue'
import LabAdminManagement from '@/views/admin/LabAdminManagement.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/student',
    component: StudentLayout,
    meta: { requiresAuth: true, role: 'student' },
    children: [
      {
        path: '',
        redirect: '/student/labs'
      },
      {
        path: 'labs',
        name: 'StudentLabs',
        component: Labs,
        meta: { title: '实验室列表' }
      },
      {
        path: 'delivery',
        name: 'StudentDelivery',
        component: Delivery,
        meta: { title: '投递记录' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: Profile,
        meta: { title: '个人信息' }
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: ['admin', 'super_admin'] },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: Dashboard,
        meta: { title: '控制台' }
      },
      {
        path: 'labs',
        name: 'AdminLabs',
        component: AdminLabs,
        meta: { title: '实验室管理' }
      },
      {
        path: 'delivery',
        name: 'AdminDelivery',
        component: AdminDelivery,
        meta: { title: '投递管理' }
      },
      {
        path: 'students',
        name: 'AdminStudents',
        component: Students,
        meta: { title: '学生管理' }
      },
      {
        path: 'admin-management',
        name: 'AdminManagement',
        component: AdminManagement,
        meta: { title: '管理员管理', role: 'super_admin' }
      },
      {
        path: 'lab-admin-management',
        name: 'LabAdminManagement',
        component: LabAdminManagement,
        meta: { title: '实验室管理员管理', role: 'super_admin' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 实验室招募系统` : '实验室招募系统'

  const token = getToken()
  const userRole = localStorage.getItem('userRole')

  // 已登录用户访问登录/注册/根路径时，自动跳转到各自首页
  if (token && (to.path === '/' || to.path === '/login' || to.path === '/register')) {
    if (userRole === 'student') {
      next('/student/labs')
    } else if (userRole === 'admin' || userRole === 'super_admin') {
      next('/admin/dashboard')
    } else {
      next('/login')
    }
    return
  }
  
  // 检查是否需要登录
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  if (requiresAuth) {
    if (!token) {
      next('/login')
      return
    }
    
    // 检查当前路由和所有父路由的角色要求
    const matchedRecords = to.matched
    let hasPermission = true
    
    for (const record of matchedRecords) {
      const requiredRole = record.meta.role
      
      if (requiredRole) {
        // 如果requiredRole是数组，检查用户角色是否在其中
        if (Array.isArray(requiredRole)) {
          if (!requiredRole.includes(userRole)) {
            hasPermission = false
            break
          }
        } 
        // 如果requiredRole是字符串，检查用户角色是否匹配
        else if (userRole !== requiredRole) {
          hasPermission = false
          break
        }
      }
    }
    
    if (!hasPermission) {
      // 如果是学生但想访问管理员页面，重定向到学生首页
      if (userRole === 'student') {
        next('/student/labs')
      } 
      // 如果是管理员但想访问学生页面，重定向到管理员首页
      else if (userRole === 'admin' || userRole === 'super_admin') {
        next('/admin/dashboard')
      }
      // 其他情况重定向到登录页
      else {
        next('/login')
      }
      return
    }
  }
  
  next()
})

export default router