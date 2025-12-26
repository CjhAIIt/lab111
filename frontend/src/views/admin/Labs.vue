<template>
  <div class="admin-labs-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>实验室管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加实验室
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
      <el-table v-loading="loading" :data="labs" stripe>
        <el-table-column prop="labName" label="实验室名称" min-width="150" />
        <el-table-column prop="requireSkill" label="技能要求" width="150" show-overflow-tooltip />
        <el-table-column prop="recruitNum" label="招募人数" width="100" />
        <el-table-column prop="currentNum" label="当前人数" width="100" />
        <el-table-column prop="labDesc" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewLab(scope.row)">查看</el-button>
            <el-button
              v-if="canManageLabs"
              size="small"
              type="primary"
              @click="editLab(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canManageLabs"
              size="small"
              type="danger"
              @click="deleteLab(scope.row)"
            >
              删除
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
    
    <!-- 添加/编辑实验室对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="dialogTitle"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="labFormRef"
        :model="labForm"
        :rules="labRules"
        label-width="80px"
      >
        <el-form-item label="实验室名称" prop="name">
          <el-input v-model="labForm.name" placeholder="请输入实验室名称" />
        </el-form-item>
        <el-form-item label="技能要求" prop="requireSkill">
          <el-input v-model="labForm.requireSkill" placeholder="请输入技能要求" />
        </el-form-item>
        <el-form-item label="招募人数" prop="recruitNum">
          <el-input-number v-model="labForm.recruitNum" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="labForm.status" placeholder="请选择状态">
            <el-option label="招募中" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="labForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入实验室描述"
          />
        </el-form-item>
        <el-form-item label="实验室图片">
          <FileUpload
            v-model="imageFiles"
            action="/api/file/upload"
            accept="image/*"
            :limit="5"
            :tip="'支持JPG、PNG格式，单个文件大小不超过5MB，最多上传5张图片'"
            list-type="picture-card"
            @success="handleImageUpload"
            @remove="handleImageRemove"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitLab">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 查看实验室详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="实验室详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedLab" class="lab-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="实验室名称">
            {{ selectedLab.labName || selectedLab.name }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedLab.status)">
              {{ getStatusText(selectedLab.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="技能要求">
            {{ selectedLab.requireSkill }}
          </el-descriptions-item>
          <el-descriptions-item label="招募人数/当前人数">
            {{ selectedLab.recruitNum }} / {{ selectedLab.currentNum }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(selectedLab.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ formatDateTime(selectedLab.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="实验室描述" :span="2">
            <div class="description-content">{{ selectedLab.labDesc || selectedLab.description }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="实验室图片" :span="2">
            <div v-if="selectedLab.images && selectedLab.images.length > 0" class="lab-images">
              <el-image
                v-for="(image, index) in selectedLab.images"
                :key="index"
                :src="image"
                :preview-src-list="selectedLab.images"
                fit="cover"
                class="lab-image"
                :initial-index="index"
              />
            </div>
            <div v-else class="no-images">暂无图片</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDetailDialog = false">关闭</el-button>
          <el-button
            v-if="canManageLabs"
            type="primary"
            @click="editLab(selectedLab)"
          >
            编辑
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import FileUpload from '@/components/FileUpload.vue'
import { useUserStore } from '@/stores/user'

export default {
  name: 'AdminLabs',
  components: {
    Plus,
    FileUpload
  },
  setup() {
    const userStore = useUserStore()
    const canManageLabs = computed(() => userStore.userRole === 'super_admin')
    // 加载状态
    const loading = ref(false)
    const submitting = ref(false)
    
    // 表单引用
    const labFormRef = ref(null)
    
    // 搜索表单
    const searchForm = reactive({
      name: '',
      status: ''
    })
    
    // 分页数据
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    
    // 实验室列表
    const labs = ref([])
    
    // 显示添加/编辑对话框
    const showDialog = ref(false)
    
    // 显示详情对话框
    const showDetailDialog = ref(false)
    const selectedLab = ref(null)
    
    // 是否为编辑模式
    const isEdit = ref(false)
    
    // 对话框标题
    const dialogTitle = computed(() => {
      return isEdit.value ? '编辑实验室' : '添加实验室'
    })
    
    // 实验室表单
    const labForm = reactive({
      id: '',
      name: '',
      description: '',
      requireSkill: '',
      recruitNum: 10,
      status: 1, // 1-招募中, 2-已关闭（对应后端 TINYINT）
      images: []
    })
    
    // 图片上传相关
    const imageFiles = ref([])
    const tempImageUrls = ref([])
    
    // 表单验证规则
    const labRules = {
      name: [
        { required: true, message: '请输入实验室名称', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入实验室描述', trigger: 'blur' }
      ],
      requireSkill: [
        { required: true, message: '请输入技能要求', trigger: 'blur' }
      ],
      recruitNum: [
        { required: true, message: '请输入招募人数', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
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
    
    // 显示添加对话框
    const showAddDialog = () => {
      isEdit.value = false
      resetLabForm()
      showDialog.value = true
    }
    
    // 查看实验室详情
    const viewLab = (lab) => {
      selectedLab.value = lab
      showDetailDialog.value = true
    }
    
    // 编辑实验室
    const editLab = (lab) => {
      isEdit.value = true
      
      // 填充表单数据
      labForm.id = lab.id
      labForm.name = lab.labName || lab.name
      labForm.description = lab.labDesc || lab.description
      labForm.requireSkill = lab.requireSkill || ''
      labForm.recruitNum = lab.recruitNum || 10
      labForm.status = lab.status
      labForm.images = lab.images || []
      
      // 设置图片文件列表
      if (lab.images && lab.images.length > 0) {
        imageFiles.value = lab.images.map((url, index) => ({
          uid: index,
          name: `image-${index}`,
          status: 'success',
          url: url
        }))
      } else {
        imageFiles.value = []
      }
      
      // 关闭详情对话框（如果打开）
      if (showDetailDialog.value) {
        showDetailDialog.value = false
      }
      
      showDialog.value = true
    }
    
    // 删除实验室
    const deleteLab = async (lab) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除实验室"${lab.name}"吗？删除后将无法恢复。`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        await request.delete(`/api/labs/${lab.id}`)
        ElMessage.success('实验室删除成功')
        
        // 刷新列表
        fetchLabs()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除实验室失败:', error)
          ElMessage.error('删除实验室失败')
        }
      }
    }
    
    // 提交实验室表单
    const submitLab = async () => {
      if (!labFormRef.value) return
      
      try {
        await labFormRef.value.validate()
        
        submitting.value = true
        
        const formData = {
          labName: labForm.name,
          labDesc: labForm.description,
          requireSkill: labForm.requireSkill || '',
          recruitNum: labForm.recruitNum || 0,
          currentNum: 0,
          status: labForm.status,
          images: labForm.images || []
        }
        
        if (isEdit.value) {
          // 更新实验室
          await request.put(`/api/labs/${labForm.id}`, formData)
          ElMessage.success('实验室更新成功')
        } else {
          // 添加实验室
          await request.post('/api/labs', formData)
          ElMessage.success('实验室添加成功')
        }
        
        showDialog.value = false
        
        // 刷新列表
        fetchLabs()
      } catch (error) {
        console.error('提交实验室失败:', error)
        ElMessage.error(error.message || '提交实验室失败')
      } finally {
        submitting.value = false
      }
    }
    
    // 重置实验室表单
    const resetLabForm = () => {
      labForm.id = ''
      labForm.name = ''
      labForm.teacher = ''
      labForm.location = ''
      labForm.status = 1
      labForm.description = ''
      labForm.images = []
      
      // 重置图片上传
      imageFiles.value = []
      tempImageUrls.value = []
      
      // 清除验证状态
      if (labFormRef.value) {
        labFormRef.value.clearValidate()
      }
    }
    
    // 处理图片上传成功
    const handleImageUpload = (response, file) => {
      labForm.images.push(response.data.url || response.data.path)
    }
    
    // 处理图片删除
    const handleImageRemove = (file, fileList) => {
      // 更新图片URL列表
      labForm.images = fileList
        .filter(item => item.url)
        .map(item => item.url)
    }
    
    // 获取状态类型
    const getStatusType = (status) => {
      const s = Number(status)
      const statusMap = {
        0: 'info',
        1: 'success',
        2: 'danger'
      }
      return statusMap[s] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const s = Number(status)
      const statusMap = {
        0: '未开始',
        1: '招募中',
        2: '已关闭'
      }
      return statusMap[s] || '未知'
    }
    
    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchLabs()
    })
    
    return {
      loading,
      submitting,
      labFormRef,
      searchForm,
      pagination,
      labs,
      showDialog,
      showDetailDialog,
      selectedLab,
      isEdit,
      dialogTitle,
      labForm,
      labRules,
      imageFiles,
      tempImageUrls,
      fetchLabs,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      showAddDialog,
      viewLab,
      editLab,
      deleteLab,
      submitLab,
      resetLabForm,
      handleImageUpload,
      handleImageRemove,
      getStatusType,
      getStatusText,
      formatDateTime,
      canManageLabs
    }
  }
}
</script>

<style scoped>
.admin-labs-page {
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

.lab-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.lab-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  object-fit: cover;
}

.no-images {
  color: #909399;
  font-style: italic;
}
</style>