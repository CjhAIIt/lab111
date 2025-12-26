<template>
  <div class="admin-students-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <el-button type="primary" @click="fetchStudents">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="学生姓名">
            <el-input
              v-model="searchForm.realName"
              placeholder="请输入学生姓名"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="学号">
            <el-input
              v-model="searchForm.studentId"
              placeholder="请输入学号"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="专业">
            <el-input
              v-model="searchForm.major"
              placeholder="请输入专业"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 学生列表 -->
      <el-table v-loading="loading" :data="students" stripe>
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="申请统计" width="150">
          <template #default="scope">
            <div class="application-stats">
              <el-tag size="small" type="warning">待审核: {{ scope.row.pendingCount || 0 }}</el-tag>
              <el-tag size="small" type="success" style="margin-left: 5px">已通过: {{ scope.row.approvedCount || 0 }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewStudent(scope.row)">查看</el-button>
            <el-button size="small" type="primary" @click="viewApplications(scope.row)">申请记录</el-button>
            <el-button size="small" type="danger" @click="deleteStudent(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="学生详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedStudent" class="student-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">
            {{ selectedStudent.realName }}
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ selectedStudent.studentId }}
          </el-descriptions-item>
          <el-descriptions-item label="专业">
            {{ selectedStudent.major }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ selectedStudent.email }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ selectedStudent.username }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDateTime(selectedStudent.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(selectedStudent.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="申请统计">
            <div class="application-stats">
              <el-tag size="small" type="warning">待审核: {{ selectedStudent.pendingCount || 0 }}</el-tag>
              <el-tag size="small" type="success" style="margin-left: 5px">已通过: {{ selectedStudent.approvedCount || 0 }}</el-tag>
              <el-tag size="small" type="danger" style="margin-left: 5px">已拒绝: {{ selectedStudent.rejectedCount || 0 }}</el-tag>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button type="primary" @click="viewApplications(selectedStudent)">申请记录</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 学生申请记录对话框 -->
    <el-dialog
      v-model="showApplicationsDialog"
      title="学生申请记录"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedStudent" class="student-applications">
        <el-table v-loading="applicationsLoading" :data="studentApplications" stripe>
          <el-table-column prop="labName" label="实验室名称" min-width="150" />
          <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="审核意见" min-width="150" show-overflow-tooltip />
          <el-table-column prop="createTime" label="申请时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.updateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="viewApplicationDetail(scope.row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 申请记录分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="applicationsPagination.current"
            v-model:page-size="applicationsPagination.size"
            :page-sizes="[10, 20, 50]"
            :total="applicationsPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleApplicationsSizeChange"
            @current-change="handleApplicationsCurrentChange"
          />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showApplicationsDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

export default {
  name: 'AdminStudents',
  components: {
    Refresh
  },
  setup() {
    const router = useRouter()
    
    // 加载状态
    const loading = ref(false)
    const applicationsLoading = ref(false)
    
    // 搜索表单
    const searchForm = reactive({
      realName: '',
      studentId: '',
      major: ''
    })
    
    // 分页数据
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    
    // 申请记录分页数据
    const applicationsPagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    
    // 学生列表
    const students = ref([])
    
    // 显示详情对话框
    const showDetailDialog = ref(false)
    const selectedStudent = ref(null)
    
    // 显示申请记录对话框
    const showApplicationsDialog = ref(false)
    
    // 学生申请记录
    const studentApplications = ref([])
    
    // 获取学生列表
    const fetchStudents = async () => {
      loading.value = true
      try {
        const params = {
          pageNum: pagination.current,
          pageSize: pagination.size,
          ...searchForm
        }
        
        // 清除空值参数
        Object.keys(params).forEach(key => {
          if (params[key] === '') {
            delete params[key]
          }
        })
        
        const response = await request.get('/api/admin/users', { params })
        
        students.value = response.data.records || []
        pagination.total = response.data.total || 0
      } catch (error) {
        console.error('获取学生列表失败:', error)
        ElMessage.error('获取学生列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 获取学生申请记录
    const fetchStudentApplications = async (studentId) => {
      applicationsLoading.value = true
      try {
        const params = {
          pageNum: applicationsPagination.current,
          pageSize: applicationsPagination.size,
          studentId
        }
        
        const response = await request.get('/api/delivery/list', { params })
        
        studentApplications.value = response.data.records || []
        applicationsPagination.total = response.data.total || 0
      } catch (error) {
        console.error('获取学生申请记录失败:', error)
        ElMessage.error('获取学生申请记录失败')
      } finally {
        applicationsLoading.value = false
      }
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.current = 1
      fetchStudents()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.realName = ''
      searchForm.studentId = ''
      searchForm.major = ''
      pagination.current = 1
      fetchStudents()
    }
    
    // 分页大小改变
    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      fetchStudents()
    }
    
    // 当前页改变
    const handleCurrentChange = (current) => {
      pagination.current = current
      fetchStudents()
    }
    
    // 申请记录分页大小改变
    const handleApplicationsSizeChange = (size) => {
      applicationsPagination.size = size
      applicationsPagination.current = 1
      if (selectedStudent.value) {
        fetchStudentApplications(selectedStudent.value.studentId)
      }
    }
    
    // 申请记录当前页改变
    const handleApplicationsCurrentChange = (current) => {
      applicationsPagination.current = current
      if (selectedStudent.value) {
        fetchStudentApplications(selectedStudent.value.studentId)
      }
    }
    
    // 查看学生详情
    const viewStudent = (student) => {
      selectedStudent.value = student
      showDetailDialog.value = true
    }
    
    // 查看学生申请记录
    const viewApplications = (student) => {
      selectedStudent.value = student
      applicationsPagination.current = 1
      fetchStudentApplications(student.studentId)
      
      // 关闭详情对话框（如果打开）
      if (showDetailDialog.value) {
        showDetailDialog.value = false
      }
      
      showApplicationsDialog.value = true
    }
    
    // 查看申请详情
    const viewApplicationDetail = (application) => {
      // 关闭申请记录对话框
      showApplicationsDialog.value = false
      
      // 跳转到申请管理页面并指定申请ID
      router.push(`/admin/delivery?id=${application.id}`)
    }
    
    // 删除学生
    const deleteStudent = async (student) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除学生"${student.realName}"吗？删除后将无法恢复。`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        await request.delete(`/api/admin/users/${student.id}`)
        ElMessage.success('学生删除成功')
        
        // 刷新列表
        fetchStudents()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除学生失败:', error)
          ElMessage.error('删除学生失败')
        }
      }
    }
    
    // 获取状态类型
    const getStatusType = (status) => {
      const statusMap = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
      }
      return statusMap[status] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝'
      }
      return statusMap[status] || '未知'
    }
    
    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchStudents()
    })
    
    return {
      loading,
      applicationsLoading,
      searchForm,
      pagination,
      applicationsPagination,
      students,
      showDetailDialog,
      selectedStudent,
      showApplicationsDialog,
      studentApplications,
      fetchStudents,
      fetchStudentApplications,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleApplicationsSizeChange,
      handleApplicationsCurrentChange,
      viewStudent,
      viewApplications,
      viewApplicationDetail,
      deleteStudent,
      getStatusType,
      getStatusText,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.admin-students-page {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.student-detail {
  margin-bottom: 20px;
}

.application-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.student-applications {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>