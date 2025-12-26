<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="headers"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-exceed="handleExceed"
      :file-list="fileList"
      :limit="limit"
      :accept="accept"
      :multiple="multiple"
      :disabled="disabled"
      :data="data"
      :drag="drag"
      :list-type="listType"
      :auto-upload="autoUpload"
      class="upload-component"
    >
      <el-button v-if="!drag && listType !== 'picture-card'" type="primary">
        <el-icon><upload /></el-icon>
        <span>点击上传</span>
      </el-button>
      
      <div v-if="drag" class="drag-area">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          <div class="drag-title">将文件拖到此处，或<em>点击上传</em></div>
          <div class="drag-subtitle">支持{{ accept || '所有文件' }}，单个文件不超过{{ sizeLimit }}MB</div>
        </div>
      </div>
      
      <el-icon v-if="listType === 'picture-card'"><plus /></el-icon>
      
      <template v-if="!drag && listType !== 'picture-card'" #tip>
        <div class="el-upload__tip">
          {{ tip }}
        </div>
      </template>
      
      <template v-if="drag" #tip>
        <div class="el-upload__tip">
          {{ tip }}
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload, UploadFilled } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'

const props = defineProps({
  // 上传地址
  action: {
    type: String,
    default: '/api/upload'
  },
  // 文件列表
  modelValue: {
    type: Array,
    default: () => []
  },
  // 最大上传数量
  limit: {
    type: Number,
    default: 5
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: ''
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 上传时附带的额外参数
  data: {
    type: Object,
    default: () => ({})
  },
  // 是否启用拖拽上传
  drag: {
    type: Boolean,
    default: false
  },
  // 文件列表的类型
  listType: {
    type: String,
    default: 'text' // text/picture/picture-card
  },
  // 是否自动上传
  autoUpload: {
    type: Boolean,
    default: true
  },
  // 文件大小限制(MB)
  sizeLimit: {
    type: Number,
    default: 10
  },
  // 提示文字
  tip: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'error', 'change', 'remove'])

const uploadRef = ref()
const fileList = ref([])

// 上传地址
const uploadUrl = computed(() => props.action)

// 请求头
const headers = computed(() => {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 监听外部文件列表变化
watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal.length > 0) {
    fileList.value = newVal.map(item => {
      if (typeof item === 'string') {
        return {
          name: item.split('/').pop() || 'unknown',
          url: item
        }
      }
      return item
    })
  } else {
    fileList.value = []
  }
}, { immediate: true, deep: true })

// 上传前校验
const beforeUpload = (file) => {
  // 文件大小校验
  const isLtSize = file.size / 1024 / 1024 < props.sizeLimit
  if (!isLtSize) {
    ElMessage.error(`上传文件大小不能超过 ${props.sizeLimit}MB!`)
    return false
  }
  
  return true
}

// 上传成功
const handleSuccess = (response, uploadFile, uploadFiles) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    
    // 更新文件列表
    const updatedFileList = [...fileList.value]
    const fileIndex = updatedFileList.findIndex(item => item.uid === uploadFile.uid)
    
    if (fileIndex !== -1) {
      updatedFileList[fileIndex] = {
        ...uploadFile,
        url: response.data.url || response.data.path
      }
    } else {
      updatedFileList.push({
        ...uploadFile,
        url: response.data.url || response.data.path
      })
    }
    
    fileList.value = updatedFileList
    emit('update:modelValue', fileList.value)
    emit('success', response, uploadFile, uploadFiles)
    emit('change', fileList.value)
  } else {
    ElMessage.error(response.message || '上传失败')
    emit('error', response, uploadFile, uploadFiles)
  }
}

// 上传失败
const handleError = (error, uploadFile, uploadFiles) => {
  ElMessage.error('上传失败')
  emit('error', error, uploadFile, uploadFiles)
}

// 文件超出限制
const handleExceed = (files, uploadFiles) => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
}

// 手动上传
const submit = () => {
  uploadRef.value?.submit()
}

// 清空文件列表
const clearFiles = () => {
  uploadRef.value?.clearFiles()
  fileList.value = []
  emit('update:modelValue', [])
  emit('change', [])
}

// 暴露方法
defineExpose({
  submit,
  clearFiles
})
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.upload-component {
  width: 100%;
}

.el-upload__tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.drag-area {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  width: 100%;
  height: 180px;
  text-align: center;
  background: #fafafa;
  position: relative;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.drag-area:hover {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.05);
}

.el-icon--upload {
  font-size: 67px;
  color: #c0c4cc;
  margin: 0 0 16px;
  line-height: 50px;
}

.drag-title {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
}

.drag-subtitle {
  color: #909399;
  font-size: 12px;
}
</style>