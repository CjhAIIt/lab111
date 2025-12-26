<template>
  <div class="admin-management">
    <div class="header">
      <h2>管理员账户管理</h2>
      <el-button type="primary" @click="showAddDialog">添加管理员</el-button>
    </div>

    <el-table :data="adminList" v-loading="loading" style="width: 100%">
      <el-table-column prop="username" label="用户名" width="180" />
      <el-table-column prop="realName" label="真实姓名" width="180" />
      <el-table-column prop="phone" label="电话号码" width="180" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="labName" label="负责实验室" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="editAdmin(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteAdminConfirm(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑管理员对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="adminForm"
        :model="adminForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" placeholder="请输入管理员用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="adminForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="adminForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="电话号码" prop="phone">
          <el-input v-model="adminForm.phone" placeholder="请输入电话号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="adminForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="负责实验室" prop="labId">
          <el-select v-model="adminForm.labId" placeholder="请选择负责实验室" style="width: 100%">
            <el-option
              v-for="lab in labList"
              :key="lab.id"
              :label="lab.name"
              :value="lab.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'AdminManagement',
  data() {
    return {
      dialogVisible: false,
      isEdit: false,
      adminForm: {
        id: null,
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        labId: null
      },
      labList: [], // 实验室列表
      rules: {
        username: [
          { required: true, message: '请输入管理员用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入电话号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        labId: [
          { required: true, message: '请选择负责实验室', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    ...mapState('admin', ['adminList', 'loading']),
    dialogTitle() {
      return this.isEdit ? '编辑管理员' : '添加管理员'
    }
  },
  created() {
    this.fetchAdminList()
    this.fetchLabList()
  },
  methods: {
    ...mapActions('admin', ['fetchAdminList', 'createAdmin', 'updateAdmin', 'deleteAdmin']),
    
    // 获取实验室列表
    async fetchLabList() {
      try {
        // 这里应该调用获取实验室列表的API
        // 暂时使用模拟数据
        this.labList = [
          { id: 1, name: '计算机实验室' },
          { id: 2, name: '物理实验室' },
          { id: 3, name: '化学实验室' }
        ]
      } catch (error) {
        console.error('获取实验室列表失败:', error)
      }
    },
    
    // 显示添加对话框
    showAddDialog() {
      this.isEdit = false
      this.dialogVisible = true
    },
    
    // 编辑管理员
    editAdmin(admin) {
      this.isEdit = true
      this.adminForm = { ...admin }
      this.dialogVisible = true
    },
    
    // 删除管理员确认
    deleteAdminConfirm(admin) {
      ElMessageBox.confirm(
        `确定要删除管理员 ${admin.realName} 吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        this.deleteAdmin(admin.id).then(() => {
          ElMessage.success('删除成功')
        }).catch(() => {
          ElMessage.error('删除失败')
        })
      })
    },
    
    // 提交表单
    submitForm() {
      this.$refs.adminForm.validate((valid) => {
        if (valid) {
          if (this.isEdit) {
            this.updateAdmin({ id: this.adminForm.id, data: this.adminForm }).then(() => {
              ElMessage.success('更新成功')
              this.dialogVisible = false
            }).catch(() => {
              ElMessage.error('更新失败')
            })
          } else {
            this.createAdmin(this.adminForm).then(() => {
              ElMessage.success('创建成功')
              this.dialogVisible = false
            }).catch(() => {
              ElMessage.error('创建失败')
            })
          }
        }
      })
    },
    
    // 重置表单
    resetForm() {
      this.adminForm = {
        id: null,
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        labId: null
      }
      this.$refs.adminForm?.resetFields()
    }
  }
}
</script>

<style scoped>
.admin-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
}
</style>