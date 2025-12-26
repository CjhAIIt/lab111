<template>
  <div class="admin-delivery-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>申请管理</span>
          <el-button type="primary" @click="fetchDeliveries">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="实验室名称">
            <el-input
              v-model="searchForm.labName"
              placeholder="请输入实验室名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="学生姓名">
            <el-input
              v-model="searchForm.studentName"
              placeholder="请输入学生姓名"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="申请状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待审核" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 申请列表 -->
      <el-table v-loading="loading" :data="deliveries" stripe>
        <el-table-column prop="labName" label="实验室名称" min-width="150" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button
              size="small"
              type="success"
              :disabled="scope.row.status !== 'PENDING'"
              @click="approveApplication(scope.row)"
            >
              通过
            </el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="scope.row.status !== 'PENDING'"
              @click="rejectApplication(scope.row)"
            >
              拒绝
            </el-button>
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
    
    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="申请详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedDelivery" class="delivery-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="实验室名称">
            {{ selectedDelivery.labName }}
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(selectedDelivery.status)">
              {{ getStatusText(selectedDelivery.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学生姓名">
            {{ selectedDelivery.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ selectedDelivery.studentId }}
          </el-descriptions-item>
          <el-descriptions-item label="专业">
            {{ selectedDelivery.major }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ selectedDelivery.email }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">
            {{ formatDateTime(selectedDelivery.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ formatDateTime(selectedDelivery.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">
            <div class="reason-content">{{ selectedDelivery.reason }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="附件" :span="2" v-if="selectedDelivery.attachments && selectedDelivery.attachments.length > 0">
            <div class="attachments-list">
              <div
                v-for="(attachment, index) in selectedDelivery.attachments"
                :key="index"
                class="attachment-item"
              >
                <el-link :href="attachment.url" target="_blank" type="primary">
                  {{ attachment.name }}
                </el-link>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2" v-if="selectedDelivery.comment">
            <div class="comment-content">{{ selectedDelivery.comment }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button
            type="success"
            v-if="selectedDelivery?.status === 'PENDING'"
            @click="approveApplication(selectedDelivery)"
          >
            通过
          </el-button>
          <el-button
            type="danger"
            v-if="selectedDelivery?.status === 'PENDING'"
            @click="rejectApplication(selectedDelivery)"
          >
            拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 审核对话框 -->
    <el-dialog
      v-model="showReviewDialog"
      :title="reviewTitle"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="80px"
      >
        <el-form-item label="审核意见" prop="comment">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            :placeholder="reviewPlaceholder"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showReviewDialog = false">取消</el-button>
          <el-button type="primary" :loading="reviewing" @click="submitReview">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

export default {
  name: 'AdminDelivery',
  components: {
    Refresh
  },
  setup() {
    const router = useRouter()
    
    // 加载状态
    const loading = ref(false)
    const reviewing = ref(false)
    
    // 表单引用
    const reviewFormRef = ref(null)
    
    // 搜索表单
    const searchForm = reactive({
      labName: '',
      studentName: '',
      status: ''
    })
    
    // 分页数据
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    
    // 申请列表
    const deliveries = ref([])
    
    // 显示详情对话框
    const showDetailDialog = ref(false)
    const selectedDelivery = ref(null)
    
    // 显示审核对话框
    const showReviewDialog = ref(false)
    const reviewType = ref('') // 'approve' 或 'reject'
    
    // 审核表单
    const reviewForm = reactive({
      id: '',
      comment: ''
    })
    
    // 审核表单验证规则
    const reviewRules = {
      comment: [
        { required: true, message: '请输入审核意见', trigger: 'blur' },
        { min: 5, message: '审核意见不能少于5个字符', trigger: 'blur' }
      ]
    }
    
    // 审核对话框标题
    const reviewTitle = computed(() => {
      return reviewType.value === 'approve' ? '通过申请' : '拒绝申请'
    })
    
    // 审核意见占位符
    const reviewPlaceholder = computed(() => {
      return reviewType.value === 'approve' 
        ? '请输入通过申请的理由' 
        : '请输入拒绝申请的理由'
    })
    
    // 获取申请列表
    const fetchDeliveries = async () => {
      loading.value = true
      try {
        const params = {
          pageNum: pagination.current,
          pageSize: pagination.size,
          labName: searchForm.labName,
          realName: searchForm.studentName,
          auditStatus: searchForm.status === 'PENDING' ? 0 : 
                     searchForm.status === 'APPROVED' ? 1 : 
                     searchForm.status === 'REJECTED' ? 2 : null
        }
        
        // 清除空值参数
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null) {
            delete params[key]
          }
        })
        
        const response = await request.get('/api/delivery/list', { params })
        
        deliveries.value = (response.data.records || []).map(record => ({
          ...record,
          status: record.status === 0 ? 'PENDING' : record.status === 1 ? 'APPROVED' : record.status === 2 ? 'REJECTED' : 'UNKNOWN'
        }))
        pagination.total = response.data.total || 0
      } catch (error) {
        if (error.response?.status === 403) {
          ElMessage.error('权限不足：请使用管理员账号登录')
        } else if (error.response?.status === 401) {
          ElMessage.error('未登录或登录已过期，请重新登录')
          router.push('/login')
        } else {
          ElMessage.error('获取申请列表失败')
        }
      } finally {
        loading.value = false
      }
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.current = 1
      fetchDeliveries()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.labName = ''
      searchForm.studentName = ''
      searchForm.status = ''
      pagination.current = 1
      fetchDeliveries()
    }
    
    // 分页大小改变
    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      fetchDeliveries()
    }
    
    // 当前页改变
    const handleCurrentChange = (current) => {
      pagination.current = current
      fetchDeliveries()
    }
    
    // 查看详情
    const viewDetail = (delivery) => {
      selectedDelivery.value = delivery
      showDetailDialog.value = true
    }
    
    // 通过申请
    const approveApplication = (delivery) => {
      reviewType.value = 'approve'
      reviewForm.id = delivery.id
      reviewForm.comment = ''
      
      // 关闭详情对话框（如果打开）
      if (showDetailDialog.value) {
        showDetailDialog.value = false
      }
      
      showReviewDialog.value = true
    }
    
    // 拒绝申请
    const rejectApplication = (delivery) => {
      reviewType.value = 'reject'
      reviewForm.id = delivery.id
      reviewForm.comment = ''
      
      // 关闭详情对话框（如果打开）
      if (showDetailDialog.value) {
        showDetailDialog.value = false
      }
      
      showReviewDialog.value = true
    }
    
    // 提交审核
    const submitReview = async () => {
      if (!reviewFormRef.value) return
      
      try {
        await reviewFormRef.value.validate()
        
        reviewing.value = true
        
        const status = reviewType.value === 'approve' ? 1 : 2
        const url = `/api/delivery/audit/${reviewForm.id}`
        await request.post(url, null, {
          params: {
            auditStatus: status,
            auditRemark: reviewForm.comment
          }
        })
        
        ElMessage.success(reviewType.value === 'approve' ? '申请已通过' : '申请已拒绝')
        showReviewDialog.value = false
        
        // 刷新列表
        fetchDeliveries()
      } catch (error) {
        if (error.message) {
          console.error('审核申请失败:', error)
        }
      } finally {
        reviewing.value = false
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
      fetchDeliveries()
    })
    
    return {
      loading,
      reviewing,
      reviewFormRef,
      searchForm,
      pagination,
      deliveries,
      showDetailDialog,
      selectedDelivery,
      showReviewDialog,
      reviewType,
      reviewForm,
      reviewRules,
      reviewTitle,
      reviewPlaceholder,
      fetchDeliveries,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      viewDetail,
      approveApplication,
      rejectApplication,
      submitReview,
      getStatusType,
      getStatusText,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.admin-delivery-page {
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

.delivery-detail {
  margin-bottom: 20px;
}

.reason-content {
  white-space: pre-wrap;
  line-height: 1.6;
  word-break: break-word;
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
}

.comment-content {
  white-space: pre-wrap;
  line-height: 1.6;
  word-break: break-word;
  color: #606266;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
