<template>
  <div class="staff-list-container">
    <el-card>
      <el-button type="primary" @click="handleAdd">新增员工</el-button>
      
      <div class="search-bar" style="margin-top: 15px;">
        <el-input v-model="searchForm.staffNo" placeholder="工号" style="width: 150px; margin-right: 10px;" @keyup.enter.native="loadStaff" />
        <el-input v-model="searchForm.name" placeholder="姓名" style="width: 150px; margin-right: 10px;" @keyup.enter.native="loadStaff" />
        <el-input v-model="searchForm.department" placeholder="部门" style="width: 150px; margin-right: 10px;" @keyup.enter.native="loadStaff" />
        <el-select v-model="searchForm.status" placeholder="状态" style="width: 120px; margin-right: 10px;">
          <el-option label="全部" value="" />
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="success" @click="loadStaff">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="staffList" border style="margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="staffNo" label="工号" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="department" label="部门" />
        <el-table-column prop="position" label="职位" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="人脸照片" width="100">
          <template slot-scope="scope">
            <el-image 
              v-if="scope.row.faceFeature" 
              :src="'data:image/jpeg;base64,' + scope.row.faceFeature" 
              style="width: 60px; height: 60px; border-radius: 50%;"
              fit="cover"
              @error="handleImageError($event)"
            />
            <span v-else style="color: #999;">未注册</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" :formatter="formatStatus" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form ref="form" :model="form" label-width="80px">
        <el-form-item label="工号" prop="staffNo">
          <el-input v-model="form.staffNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（不修改请留空）" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="form.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getStaffList, addStaff, updateStaff, deleteStaff } from '../../api'

export default {
  name: 'StaffList',
  data() {
    return {
      staffList: [],
      dialogVisible: false,
      dialogTitle: '新增员工',
      searchForm: {
        staffNo: '',
        name: '',
        department: '',
        status: ''
      },
      form: {
        id: null,
        staffNo: '',
        name: '',
        password: '',
        department: '',
        position: '',
        phone: '',
        email: ''
      }
    }
  },
  async mounted() {
    await this.loadStaff()
  },
  methods: {
    async loadStaff() {
      try {
        let res = await getStaffList()
        if (res.data.success) {
          let list = res.data.data || []
          
          if (this.searchForm.staffNo) {
            list = list.filter(item => item.staffNo.includes(this.searchForm.staffNo))
          }
          if (this.searchForm.name) {
            list = list.filter(item => item.name.includes(this.searchForm.name))
          }
          if (this.searchForm.department) {
            list = list.filter(item => item.department && item.department.includes(this.searchForm.department))
          }
          if (this.searchForm.status !== '') {
            list = list.filter(item => item.status === this.searchForm.status)
          }
          
          this.staffList = list
        }
      } catch (error) {
        this.$message.error('加载员工列表失败')
      }
    },
    resetSearch() {
      this.searchForm = {
        staffNo: '',
        name: '',
        department: '',
        status: ''
      }
      this.loadStaff()
    },
    handleAdd() {
      this.dialogTitle = '新增员工'
      this.form = {
        id: null,
        staffNo: '',
        name: '',
        password: '',
        department: '',
        position: '',
        phone: '',
        email: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑员工'
      this.form = { 
        ...row,
        password: ''
      }
      this.dialogVisible = true
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定删除该员工？', '提示', { type: 'warning' })
        const res = await deleteStaff({ id: row.id })
        if (res.data.success) {
          this.$message.success(res.data.message)
          await this.loadStaff()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    async handleSave() {
      try {
        let res
        if (this.form.id) {
          res = await updateStaff(this.form)
        } else {
          res = await addStaff(this.form)
        }
        if (res.data.success) {
          this.$message.success(res.data.message)
          this.dialogVisible = false
          await this.loadStaff()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    formatStatus(row) {
      return row.status === 1 ? '启用' : '禁用'
    },
    handleImageError(e) {
      e.target.style.display = 'none'
    }
  }
}
</script>

<style scoped>
.staff-list-container {
  padding: 20px;
}
</style>