<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand">
        <div class="brand-icon">👤</div>
        <div class="brand-text">
          <h1>人脸+GPS考勤系统</h1>
          <p>Face Attendance System</p>
        </div>
      </div>
      <div class="features">
        <div class="feature-item">
          <span class="feature-icon">🔐</span>
          <span class="feature-text">人脸识别</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">📍</span>
          <span class="feature-text">GPS定位</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">📋</span>
          <span class="feature-text">考勤管理</span>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-box">
        <h2 class="login-title">管理员登录</h2>
        <p class="login-subtitle">请输入管理员账号和密码</p>
        <el-form ref="form" :model="form" :rules="rules" label-width="0" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              prefix-icon="el-icon-user"
              class="input-field"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              type="password" 
              v-model="form.password" 
              placeholder="请输入密码" 
              prefix-icon="el-icon-lock"
              class="input-field"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" class="login-btn">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <span>测试账号: admin / 123456</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminLogin } from '../api'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: 'admin',
        password: '123456'
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    async handleLogin() {
      try {
        const res = await adminLogin(this.form)
        if (res.data.success) {
          sessionStorage.setItem('token', 'admin_token')
          sessionStorage.setItem('userInfo', JSON.stringify(res.data.data?.admin || res.data.admin))
          this.$message.success(res.data.message)
          this.$router.push('/')
        } else {
          this.$message.error(res.data.message)
        }
      } catch (error) {
        this.$message.error('登录失败')
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
}
.login-left {
  width: 55%;
  background: linear-gradient(135deg, #1a237e 0%, #3949ab 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 80px;
}
.brand-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 40px;
}
.brand-text h1 {
  font-size: 36px;
  color: #fff;
  margin: 0;
  font-weight: bold;
}
.brand-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 8px 0 0 0;
}
.features {
  display: flex;
  gap: 60px;
}
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.feature-icon {
  font-size: 32px;
}
.feature-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}
.login-right {
  width: 45%;
  background: #f8f9fa;
  display: flex;
  justify-content: center;
  align-items: center;
}
.login-box {
  width: 400px;
  padding: 50px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}
.login-title {
  font-size: 24px;
  color: #1a237e;
  text-align: center;
  margin: 0 0 8px 0;
  font-weight: bold;
}
.login-subtitle {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin: 0 0 30px 0;
}
.login-form {
  margin-bottom: 20px;
}
.input-field {
  height: 48px;
  border-radius: 8px;
}
.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  background: linear-gradient(135deg, #1a237e 0%, #3949ab 100%);
  border: none;
}
.login-btn:hover {
  background: linear-gradient(135deg, #151c6a 0%, #303f9f 100%);
}
.login-footer {
  text-align: center;
}
.login-footer span {
  font-size: 12px;
  color: #999;
}
</style>