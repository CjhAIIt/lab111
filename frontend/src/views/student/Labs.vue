<template>
  <div class="labs-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实验室列表</span>
          <el-button type="primary" @click="fetchLabs">
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
              v-model="searchForm.name"
              placeholder="请输入实验室名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="招募中" :value="1" />
              <el-option label="已关闭" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 实验室列表 -->
      <div v-loading="loading" class="labs-list">
        <el-empty v-if="labs.length === 0 && !loading" description="暂无实验室信息" />
        
        <el-row :gutter="20" v-else>
          <el-col :span="8" v-for="lab in labs" :key="lab.id">
            <el-card class="lab-card" shadow="hover" @click="viewLabDetail(lab.id)">
              <div class="lab-header">
                <h3>{{ lab.labName }}</h3>
                <el-tag :type="getStatusType(lab.status)" size="small">
                  {{ getStatusText(lab.status) }}
                </el-tag>
              </div>
              
              <div class="lab-content">
                <div class="lab-description">
                  {{ lab.labDesc }}
                </div>
                
                <div class="lab-info">
                  <div class="info-item">
                    <el-icon><User /></el-icon>
                    <span>技能要求: {{ lab.requireSkill }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Clock /></el-icon>
                    <span>发布时间: {{ formatDate(lab.createTime) }}</span>
                  </div>
                </div>
              </div>
              
              <div class="lab-footer">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="lab.status !== 1"
                  @click.stop="applyLab(lab)"
                >
                  申请加入
                </el-button>
                <el-button size="small" @click.stop="viewLabDetail(lab.id)">
                  查看详情
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[6, 12, 18, 24]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 实验室详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="selectedLab?.name"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedLab" class="lab-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="实验室名称">
            {{ selectedLab.labName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedLab.status)">
              {{ getStatusText(selectedLab.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="技能要求" :span="2">
              {{ selectedLab.requireSkill }}
            </el-descriptions-item>
            <el-descriptions-item label="发布时间" :span="2">
              {{ formatDate(selectedLab.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="实验室描述" :span="2">
              <div class="description-content">{{ selectedLab.labDesc }}</div>
            </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button
            type="primary"
            :disabled="selectedLab?.status !== 1"
            @click="applyLab(selectedLab)"
          >
            申请加入
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 申请对话框 -->
    <el-dialog
      v-model="showApplyDialog"
      title="申请加入实验室"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyRules"
        label-width="80px"
      >
        <el-form-item label="实验室">
          <el-input v-model="applyForm.labName" disabled />
        </el-form-item>
        <el-form-item label="申请理由" prop="reason">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入申请理由"
          />
        </el-form-item>
        <el-form-item label="特长标签" prop="skillTags">
          <el-input
            v-model="applyForm.skillTags"
            placeholder="请输入您的特长标签，如：Java,Python,机器学习"
          />
        </el-form-item>
        <el-form-item label="学习进度" prop="studyProgress">
          <el-input
            v-model="applyForm.studyProgress"
            placeholder="请输入您的学习进度，如：大三在读、研一等"
          />
        </el-form-item>
        <el-form-item label="附件上传">
          <FileUpload
            v-model="attachmentFiles"
            action="/api/file/upload"
            accept=".pdf,.doc,.docx,.zip,.rar"
            :limit="5"
            :tip="'支持PDF、DOC、DOCX、ZIP、RAR格式，单个文件不超过10MB，最多上传5个文件'"
            @success="handleAttachmentUpload"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showApplyDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitApplication">
            提交申请
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
import { Refresh, User, Clock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import FileUpload from '@/components/FileUpload.vue'

export default {
  name: 'StudentLabs',
  components: {
    Refresh,
    User,
    Clock,
    FileUpload
  },
  setup() {
    const router = useRouter()
    
    // 加载状态
    const loading = ref(false)
    const submitting = ref(false)
    
    // 搜索表单
    const searchForm = reactive({
      name: '',
      status: ''
    })
    
    // 分页数据
    const pagination = reactive({
      current: 1,
      size: 6,
      total: 0
    })
    
    // 实验室列表
    const labs = ref([])
    
    // 显示详情对话框
    const showDetailDialog = ref(false)
    const selectedLab = ref(null)
    
    // 显示申请对话框
    const showApplyDialog = ref(false)
    const applyFormRef = ref(null)
    
    // 申请表单
    const applyForm = reactive({
      labId: '',
      labName: '',
      reason: '',
      skillTags: '',
      studyProgress: '',
      attachments: []
    })
    
    // 附件文件
    const attachmentFiles = ref([])
    
    // 申请表单验证规则
    const applyRules = {
      reason: [
        { required: true, message: '请输入申请理由', trigger: 'blur' },
        { min: 10, message: '申请理由不能少于10个字符', trigger: 'blur' }
      ],
      skillTags: [
        { required: true, message: '请输入特长标签', trigger: 'blur' }
      ],
      studyProgress: [
        { required: true, message: '请输入学习进度', trigger: 'blur' }
      ]
    }
    
    // 获取实验室列表
    const fetchLabs = async () => {
      loading.value = true
      try {
        const params = {
          pageNum: pagination.current,
          pageSize: pagination.size,
          labName: searchForm.name,
          status: searchForm.status
        }
        
        // 清除空值参数
        Object.keys(params).forEach(key => {
          if (params[key] === '') {
            delete params[key]
          }
        })
        
        const response = await request.get('/api/labs/list', { params })
        
        labs.value = response.data.records || []
        pagination.total = response.data.total || 0
      } catch (error) {
        console.error('获取实验室列表失败:', error)
        ElMessage.error('获取实验室列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索
    const handleSearch = () => {
      pagination.current = 1
      fetchLabs()
    }
    
    // 重置搜索
    const resetSearch = () => {
      searchForm.name = ''
      searchForm.status = ''
      pagination.current = 1
      fetchLabs()
    }
    
    // 分页大小改变
    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      fetchLabs()
    }
    
    // 当前页改变
    const handleCurrentChange = (current) => {
      pagination.current = current
      fetchLabs()
    }
    
    // 查看实验室详情
    const viewLabDetail = (labId) => {
      const lab = labs.value.find(item => item.id === labId)
      if (lab) {
        selectedLab.value = lab
        showDetailDialog.value = true
      }
    }
    
    // 申请实验室
    const applyLab = (lab) => {
      if (lab.status !== 1) {
        ElMessage.warning('该实验室已关闭招募')
        return
      }
      
      applyForm.labId = lab.id
      applyForm.labName = lab.labName
      applyForm.reason = ''
      applyForm.attachments = []
      attachmentFiles.value = []
      showDetailDialog.value = false
      showApplyDialog.value = true
    }
    
    // 重置申请表单
    const resetApplyForm = () => {
      applyForm.labId = ''
      applyForm.labName = ''
      applyForm.reason = ''
      applyForm.skillTags = ''
      applyForm.studyProgress = ''
      applyForm.attachments = []
      attachmentFiles.value = []
      
      // 清除验证状态
      if (applyFormRef.value) {
        applyFormRef.value.clearValidate()
      }
    }
    
    // 处理附件上传成功
    const handleAttachmentUpload = (response, file) => {
      applyForm.attachments.push(response.data.url || response.data.path)
    }
    
    // 提交申请
    const submitApplication = async () => {
      if (!applyFormRef.value) return
      
      try {
        await applyFormRef.value.validate()
        
        submitting.value = true
        
        const response = await request.post('/api/delivery/deliver', {
          labId: applyForm.labId,
          reason: applyForm.reason,
          skillTags: applyForm.skillTags,
          studyProgress: applyForm.studyProgress,
          attachments: applyForm.attachments
        })
        
        ElMessage.success('申请提交成功')
        showApplyDialog.value = false
        
        // 重置表单
        resetApplyForm()
        
        // 刷新列表
        fetchLabs()
      } catch (error) {
        if (error.message) {
          console.error('提交申请失败:', error)
        }
      } finally {
        submitting.value = false
      }
    }
    
    // 获取状态类型
    const getStatusType = (status) => {
      const statusMap = {
        1: 'success',  // 招募中
        2: 'danger'    // 已关闭
      }
      return statusMap[status] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        1: '招募中',
        2: '已关闭'
      }
      return statusMap[status] || '未知'
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchLabs()
    })
    
    return {
      loading,
      submitting,
      searchForm,
      pagination,
      labs,
      showDetailDialog,
      selectedLab,
      showApplyDialog,
      applyFormRef,
      applyForm,
      attachmentFiles,
      applyRules,
      fetchLabs,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      viewLabDetail,
      applyLab,
      resetApplyForm,
      handleAttachmentUpload,
      submitApplication,
      getStatusType,
      getStatusText,
      formatDate
    }
  }
}
</script>

<style scoped>
.labs-page {
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

.labs-list {
  min-height: 400px;
}

.lab-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.lab-card:hover {
  transform: translateY(-5px);
}

.lab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.lab-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.lab-content {
  margin-bottom: 15px;
}

.lab-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
  height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.lab-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.info-item .el-icon {
  margin-right: 5px;
}

.lab-footer {
  display: flex;
  justify-content: space-between;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.lab-detail {
  margin-bottom: 20px;
}

.description-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>