<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" @click="editProfile">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
            </div>
          </template>
          
          <div class="profile-info">
            <div class="avatar-section">
              <el-avatar :size="100" :src="userInfo.avatar">
                {{ userInitial }}
              </el-avatar>
              <div class="username">{{ userInfo.realName }}</div>
              <div class="user-id">学号: {{ userInfo.studentId }}</div>
              <el-button 
                type="text" 
                size="small" 
                @click="showAvatarUpload = true"
                style="margin-top: 10px;"
              >
                更换头像
              </el-button>
            </div>
            
            <div class="info-section">
              <div class="info-item">
                <div class="info-label">用户名:</div>
                <div class="info-value">{{ userInfo.username }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">邮箱:</div>
                <div class="info-value">{{ userInfo.email }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">专业:</div>
                <div class="info-value">{{ userInfo.major }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">注册时间:</div>
                <div class="info-value">{{ formatDate(userInfo.createTime) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 修改密码卡片 -->
      <el-col :span="16">
        <el-card class="password-card">
          <template #header>
            <span>修改密码</span>
          </template>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="password-form"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="changePassword">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 编辑个人信息对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑个人信息"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="80px"
      >
        <el-form-item label="学号">
          <el-input v-model="profileForm.studentId" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="profileForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="个人简历">
          <FileUpload
            v-model="resumeFiles"
            action="/api/file/upload"
            accept=".pdf,.doc,.docx"
            :limit="1"
            :tip="'支持PDF、DOC、DOCX格式，单个文件不超过10MB'"
            @success="handleResumeUpload"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" :loading="profileLoading" @click="updateProfile">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 头像上传对话框 -->
    <el-dialog
      v-model="showAvatarUpload"
      title="更换头像"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="avatar-upload-container">
        <FileUpload
          v-model="avatarFiles"
          action="/api/file/upload"
          accept="image/*"
          :limit="1"
          :tip="'支持JPG、PNG格式，单个文件不超过10MB'"
          :drag="false"
          :list-type="'text'"
          @success="handleAvatarUpload"
        />
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarUpload = false">取消</el-button>
          <el-button type="primary" :loading="avatarLoading" @click="saveAvatar">
            保存头像
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import request from '@/utils/request'
import FileUpload from '@/components/FileUpload.vue'

export default {
  name: 'StudentProfile',
  components: {
    Edit,
    FileUpload
  },
  setup() {
    const store = useUserStore()
    
    // 加载状态
    const passwordLoading = ref(false)
    const profileLoading = ref(false)
    const avatarLoading = ref(false)
    
    // 表单引用
    const passwordFormRef = ref(null)
    const profileFormRef = ref(null)
    
    // 显示对话框
    const showEditDialog = ref(false)
    const showAvatarUpload = ref(false)
    
    // 文件上传
    const avatarFiles = ref([])
    const resumeFiles = ref([])
    const tempAvatarUrl = ref('')
    
    // 用户信息
    const userInfo = ref({})
    
    // 用户名首字
    const userInitial = computed(() => {
      if (!userInfo.value || !userInfo.value.realName) {
        return '用'
      }
      return userInfo.value.realName.charAt(0)
    })
    
    // 修改密码表单
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 个人信息表单
    const profileForm = reactive({
      studentId: '',
      realName: '',
      major: '',
      email: '',
      resume: ''
    })
    
    // 修改密码表单验证规则
    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    // 个人信息表单验证规则
    const profileRules = {
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      major: [
        { required: true, message: '请输入专业', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ]
    }
    
    // 获取用户信息
    const fetchUserInfo = async () => {
      try {
        const response = await request.get('/api/user/info')
        userInfo.value = response.data
        
        // 更新store中的用户信息
        store.setUserInfo(userInfo.value)
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败')
      }
    }
    
    // 编辑个人信息
    const editProfile = () => {
      // 确保userInfo存在
      if (!userInfo.value) {
        ElMessage.error('用户信息未加载')
        return
      }
      
      // 填充表单数据
      profileForm.studentId = userInfo.value.studentId || ''
      profileForm.realName = userInfo.value.realName || ''
      profileForm.major = userInfo.value.major || ''
      profileForm.email = userInfo.value.email || ''
      profileForm.resume = userInfo.value.resume || ''
      
      // 设置简历文件
      if (userInfo.value.resume) {
        resumeFiles.value = [{
          name: '个人简历',
          url: userInfo.value.resume
        }]
      }
      
      showEditDialog.value = true
    }
    
    // 更新个人信息
    const updateProfile = async () => {
      if (!profileFormRef.value) return
      
      try {
        if (!profileFormRef.value) {
          ElMessage.error('表单未初始化')
          return
        }
        await profileFormRef.value.validate()
        
        profileLoading.value = true
        
        const response = await request.put('/api/user/info', {
          realName: profileForm.realName,
          major: profileForm.major,
          email: profileForm.email,
          resume: profileForm.resume
        })
        
        ElMessage.success('个人信息更新成功')
        showEditDialog.value = false
        
        // 重新获取用户信息
        fetchUserInfo()
      } catch (error) {
        if (error.message) {
          console.error('更新个人信息失败:', error)
        }
      } finally {
        profileLoading.value = false
      }
    }
    
    // 修改密码
    const changePassword = async () => {
      if (!passwordFormRef.value) return
      
      try {
        if (!passwordFormRef.value) {
          ElMessage.error('表单未初始化')
          return
        }
        await passwordFormRef.value.validate()
        
        passwordLoading.value = true
        
        const response = await request.put('/api/user/password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        
        ElMessage.success('密码修改成功')
        
        // 重置表单
        resetPasswordForm()
      } catch (error) {
        if (error.message) {
          console.error('修改密码失败:', error)
        }
      } finally {
        passwordLoading.value = false
      }
    }
    
    // 重置密码表单
    const resetPasswordForm = () => {
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      
      // 清除验证状态
      if (passwordFormRef.value) {
        passwordFormRef.value.clearValidate()
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }
    
    // 处理头像上传成功
    const handleAvatarUpload = (response, file) => {
      tempAvatarUrl.value = response.data.url || response.data.path
    }
    
    // 保存头像
    const saveAvatar = async () => {
      if (!tempAvatarUrl.value) {
        ElMessage.warning('请先选择头像')
        return
      }
      
      try {
        avatarLoading.value = true
        
        await request.put('/api/user/avatar', {
          avatar: tempAvatarUrl.value
        })
        
        ElMessage.success('头像更新成功')
        showAvatarUpload.value = false
        tempAvatarUrl.value = ''
        avatarFiles.value = []
        
        // 重新获取用户信息
        fetchUserInfo()
      } catch (error) {
        console.error('更新头像失败:', error)
        ElMessage.error('更新头像失败')
      } finally {
        avatarLoading.value = false
      }
    }
    
    // 处理简历上传成功
    const handleResumeUpload = (response, file) => {
      profileForm.resume = response.data.url || response.data.path
    }
    
    // 组件挂载时获取用户信息
    onMounted(() => {
      fetchUserInfo()
    })
    
    return {
      passwordLoading,
      profileLoading,
      avatarLoading,
      passwordFormRef,
      profileFormRef,
      showEditDialog,
      showAvatarUpload,
      userInfo,
      userInitial,
      passwordForm,
      profileForm,
      passwordRules,
      profileRules,
      avatarFiles,
      resumeFiles,
      fetchUserInfo,
      editProfile,
      updateProfile,
      changePassword,
      resetPasswordForm,
      formatDate,
      handleAvatarUpload,
      saveAvatar,
      handleResumeUpload
    }
  }
}
</script>

<style scoped>
.profile-page {
  padding: 0;
}

.profile-card, .password-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.username {
  font-size: 18px;
  font-weight: bold;
  margin-top: 15px;
  color: #303133;
}

.user-id {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.info-section {
  width: 100%;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
  border-bottom: none;
  padding-bottom: 0;
}

.info-label {
  width: 80px;
  color: #909399;
  font-size: 14px;
}

.info-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
}

.password-form {
  max-width: 500px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.avatar-upload-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
}
</style>