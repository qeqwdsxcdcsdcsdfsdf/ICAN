<template>
  <div class="layout-container">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">👤</div>
        <div class="logo-text">
          <span class="logo-title">考勤系统</span>
          <span class="logo-subtitle">Face Attendance</span>
        </div>
      </div>
      <el-menu 
        :default-active="activeMenu" 
        class="sidebar-menu" 
        @select="handleMenuSelect"
        background-color="#1a237e"
        text-color="#fff"
        active-text-color="#ffd54f"
      >
        <el-menu-item index="/attendance/list">
          <i class="el-icon-document"></i>
          <span>考勤记录</span>
        </el-menu-item>
        <el-menu-item index="/staff/list">
          <i class="el-icon-user"></i>
          <span>员工管理</span>
        </el-menu-item>
        <el-menu-item index="/location/set">
          <i class="el-icon-location"></i>
          <span>定位配置</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <span class="footer-version">v1.0.0</span>
      </div>
    </aside>
    <main class="main-content">
      <header class="header">
        <div class="header-left">
          <span class="header-title">{{ pageTitle }}</span>
          <span class="header-date">{{ currentDate }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <span class="user-icon">👤</span>
              <span class="user-name">管理员</span>
              <i class="el-icon-arrow-down"></i>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </header>
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: 'Layout',
  data() {
    return {
      activeMenu: '/attendance/list',
      pageTitles: {
        '/attendance/list': '考勤记录',
        '/staff/list': '员工管理',
        '/location/set': '定位配置'
      },
      currentDate: ''
    }
  },
  computed: {
    pageTitle() {
      return this.pageTitles[this.$route.path] || '考勤管理'
    }
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index
      if (this.$route.path !== index) {
        this.$router.push(index)
      }
    },
    handleCommand(command) {
      if (command === 'logout') {
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('userInfo')
        this.$router.push('/login')
      }
    },
    updateDate() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const weekDay = weekDays[now.getDay()]
      this.currentDate = `${year}-${month}-${day} ${weekDay}`
    }
  },
  mounted() {
    this.activeMenu = this.$route.path
    this.updateDate()
    setInterval(this.updateDate, 60000)
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1a237e 0%, #3949ab 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.logo-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}
.logo-text {
  display: flex;
  flex-direction: column;
}
.logo-title {
  font-size: 18px;
  font-weight: bold;
}
.logo-subtitle {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.6);
}
.sidebar-menu {
  flex: 1;
  border-right: none;
  padding-top: 20px;
}
.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 0 12px;
  border-radius: 8px;
}
.sidebar-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
}
.sidebar-menu .el-menu-item.is-active {
  background: rgba(255, 213, 79, 0.2);
  color: #ffd54f;
}
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
}
.footer-version {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}
.header {
  height: 64px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #1a237e;
}
.header-date {
  font-size: 13px;
  color: #999;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.3s;
}
.user-info:hover {
  background: #f5f5f5;
}
.user-icon {
  font-size: 20px;
}
.user-name {
  font-size: 14px;
  color: #333;
}
</style>