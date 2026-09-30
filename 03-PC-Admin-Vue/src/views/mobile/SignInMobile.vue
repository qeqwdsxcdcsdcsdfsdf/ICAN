<template>
  <div class="mobile-container">
    <div class="header">
      <h1>人脸+GPS考勤系统</h1>
    </div>
    
    <div class="login-form" v-if="!loggedIn">
      <el-form ref="form" :model="loginForm" :rules="rules" label-width="80px">
        <el-form-item label="工号" prop="staffNo">
          <el-input v-model="loginForm.staffNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="sign-in-page" v-else>
      <div class="user-info">
        <p>欢迎, {{ staffInfo.name }}</p>
        <p>{{ staffInfo.department }} - {{ staffInfo.position }}</p>
      </div>

      <div class="location-section">
        <div class="location-info">
          <p>当前位置</p>
          <p v-if="location.lat && location.lng">
            {{ location.lat.toFixed(6) }}, {{ location.lng.toFixed(6) }}
          </p>
          <p v-else>正在获取定位...</p>
        </div>
        <div class="distance-info" v-if="distanceInfo">
          <p>距离中心点: {{ distanceInfo.distance }}米</p>
          <p>允许范围: {{ distanceInfo.radius }}米</p>
          <p :class="distanceInfo.locationValid ? 'valid' : 'invalid'">
            {{ distanceInfo.message }}
          </p>
        </div>
        <el-button type="primary" @click="viewLocation" style="width: 100%; margin-top: 10px" :disabled="!location.lat">
          📍 查看当前位置
        </el-button>
      </div>

      <div class="sign-buttons">
        <el-button type="primary" @click="handleSignIn(1)" style="width: 100%; margin-bottom: 10px">
          上班签到
        </el-button>
        <el-button type="warning" @click="handleSignIn(2)" style="width: 100%">
          下班签到
        </el-button>
      </div>

      <div class="sign-history">
        <h3>最近签到记录</h3>
        <div v-if="records.length > 0">
          <div v-for="record in records" :key="record.id" class="record-item">
            <p>{{ record.signTime }}</p>
            <p>{{ record.signType === 1 ? '上班' : '下班' }}</p>
            <p :class="record.signResult === 0 ? 'success' : 'fail'">
              {{ record.signResult === 0 ? '成功' : '失败' }}
            </p>
          </div>
        </div>
        <p v-else>暂无签到记录</p>
      </div>

      <el-button type="danger" @click="handleLogout" style="width: 100%">退出登录</el-button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'SignInMobile',
  data() {
    return {
      loggedIn: false,
      loginForm: {
        staffNo: '',
        password: ''
      },
      rules: {
        staffNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      staffInfo: {},
      location: { lat: null, lng: null },
      distanceInfo: null,
      records: [],
      refreshTimer: null
    }
  },
  mounted() {
    const token = sessionStorage.getItem('staff_token')
    if (token) {
      this.loadStaffInfo()
    }
  },
  beforeDestroy() {
    this.stopRefreshTimer()
  },
  methods: {
    async handleLogin() {
      try {
        const res = await axios.post('/api/staff/login', {
          staffNo: this.loginForm.staffNo,
          password: this.loginForm.password
        })
        if (res.data.success) {
          this.staffInfo = res.data.staff
          this.loggedIn = true
          sessionStorage.setItem('staff_token', 'staff_token')
          await this.getLocation()
          await this.loadRecords()
          this.startRefreshTimer()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (error) {
        this.$message.error('登录失败')
      }
    },
    async getLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          async (position) => {
            this.location.lat = position.coords.latitude
            this.location.lng = position.coords.longitude
            await this.checkLocation()
          },
          (error) => {
            this.$message.error('获取定位失败: ' + error.message)
          },
          { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
        )
      } else {
        this.$message.error('您的浏览器不支持定位功能')
      }
    },
    async checkLocation() {
      try {
        const res = await axios.post('/api/location/check', {
          lat: this.location.lat,
          lng: this.location.lng
        })
        this.distanceInfo = res.data
      } catch (error) {
        console.error('检查定位失败', error)
      }
    },
    async handleSignIn(type) {
      if (!this.location.lat || !this.location.lng) {
        this.$message.error('请先获取定位')
        return
      }
      
      try {
        const res = await axios.post('/api/sign/in', {
          staffNo: this.staffInfo.staffNo,
          signType: type,
          lat: this.location.lat,
          lng: this.location.lng,
          faceMatchResult: 0
        })
        if (res.data.success) {
          this.$message.success(res.data.message)
          await this.loadRecords()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (error) {
        this.$message.error('签到失败')
      }
    },
    async loadRecords() {
      try {
        const res = await axios.get('/api/sign/records', {
          params: { staffNo: this.staffInfo.staffNo }
        })
        this.records = res.data.records || []
      } catch (error) {
        console.error('获取记录失败', error)
      }
    },
    async loadStaffInfo() {
      try {
        const res = await axios.get('/api/staff/list')
        const staff = res.data.find(s => s.staffNo === sessionStorage.getItem('staffNo'))
        if (staff) {
          this.staffInfo = staff
          this.loggedIn = true
          await this.getLocation()
          await this.loadRecords()
          this.startRefreshTimer()
        }
      } catch (error) {
        sessionStorage.removeItem('staff_token')
      }
    },
    handleLogout() {
      this.stopRefreshTimer()
      sessionStorage.removeItem('staff_token')
      this.loggedIn = false
      this.staffInfo = {}
      this.location = { lat: null, lng: null }
      this.distanceInfo = null
      this.records = []
      this.loginForm = { staffNo: '', password: '' }
    },
    viewLocation() {
      if (!this.location.lat || !this.location.lng) {
        this.$message.error('请先获取定位')
        return
      }
      const url = `https://www.amap.com/map/place?query=${this.location.lat},${this.location.lng}&adcode=110000`
      window.open(url, '_blank')
    },
    startRefreshTimer() {
      this.stopRefreshTimer()
      this.refreshTimer = setInterval(() => {
        this.loadRecords()
      }, 10000)
    },
    stopRefreshTimer() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    }
  }
}
</script>

<style scoped>
.mobile-container {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background: #f5f5f5;
}
.header {
  text-align: center;
  padding: 40px 0;
}
.header h1 {
  font-size: 24px;
  color: #333;
}
.login-form {
  background: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.sign-in-page {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.user-info {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}
.user-info p {
  margin: 5px 0;
}
.location-section {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}
.location-info, .distance-info {
  margin-bottom: 15px;
}
.distance-info .valid {
  color: #4CAF50;
}
.distance-info .invalid {
  color: #f44336;
}
.sign-buttons {
  padding: 20px 0;
}
.sign-history {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}
.sign-history h3 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
}
.record-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.record-item p {
  margin: 0;
  font-size: 14px;
}
.record-item .success {
  color: #4CAF50;
}
.record-item .fail {
  color: #f44336;
}
</style>