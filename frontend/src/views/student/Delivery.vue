<template>
  <div class="delivery-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的申请</span>
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
          <el-form-item label="申请状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待审核" :value="0" />
              <el-option label="已通过" :value="1" />
              <el-option label="已拒绝" :value="2" />
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
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="scope.row.status !== 'PENDING'"
              @click="cancelApplication(scope.row)"
            >
              撤销
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
            type="danger"
            v-if="selectedDelivery?.status === 0"
            @click="cancelApplication(selectedDelivery)"
          >
            撤销申请
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

export default {
  name: 'StudentDelivery',
  components: {
    Refresh
  },
  setup() {
    // 加载状态
    const loading = ref(false)
    
    // 搜索表单
    const searchForm = reactive({
      labName: '',
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
    
    // 获取申请列表
    const fetchDeliveries = async () => {
      loading.value = true
      try {
        const params = {
          pageNum: pagination.current,
          pageSize: pagination.size,
          labName: searchForm.labName,
          auditStatus: searchForm.status
        }
        
        // 清除空值参数
        Object.keys(params).forEach(key => {
          if (params[key] === '') {
            delete params[key]
          }
        })
        
        console.log('发送请求:', '/api/delivery/my', params)
        const response = await request.get('/api/delivery/my', { params })
        console.log('收到响应:', response)
        
        deliveries.value = (response.data.records || []).map(record => ({
          ...record,
          status: record.status === 0 ? 0 : record.status === 1 ? 1 : record.status === 2 ? 2 : -1
        }))
        pagination.total = response.data.total || 0
      } catch (error) {
        console.error('获取申请列表失败:', error)
        console.error('错误详情:', {
          message: error.message,
          response: error.response ? {
            status: error.response.status,
            statusText: error.response.statusText,
            data: error.response.data
          } : '无响应数据'
        })
        ElMessage.error('获取申请列表失败')
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
    
    // 撤销申请
    const cancelApplication = async (delivery) => {
      try {
        await ElMessageBox.confirm(
          '确定要撤销该申请吗？撤销后将无法恢复。',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        await request.delete(`/api/delivery/${delivery.id}`)
        ElMessage.success('申请已撤销')
        
        // 刷新列表
        fetchDeliveries()
        
        // 如果详情对话框打开，则关闭
        if (showDetailDialog.value) {
          showDetailDialog.value = false
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('撤销申请失败:', error)
          ElMessage.error('撤销申请失败')
        }
      }
    }
    
    // 获取状态类型
    const getStatusType = (status) => {
      const statusMap = {
        0: 'warning',   // 待审核
        1: 'success',   // 已通过
        2: 'danger'     // 已拒绝
      }
      return statusMap[status] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝'
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
      searchForm,
      pagination,
      deliveries,
      showDetailDialog,
      selectedDelivery,
      fetchDeliveries,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      viewDetail,
      cancelApplication,
      getStatusType,
      getStatusText,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.delivery-page {
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
