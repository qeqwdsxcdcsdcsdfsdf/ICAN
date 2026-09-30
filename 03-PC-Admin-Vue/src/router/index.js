import Vue from 'vue'
import Router from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import LocationSet from '../views/system/LocationSet.vue'
import AttendanceList from '../views/attendance/AttendanceList.vue'
import StaffList from '../views/staff/StaffList.vue'
import SignInMobile from '../views/mobile/SignInMobile.vue'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      path: '/mobile',
      name: 'SignInMobile',
      component: SignInMobile
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      name: 'Layout',
      component: Layout,
      redirect: '/attendance/list',
      children: [
        {
          path: '/location/set',
          name: 'LocationSet',
          component: LocationSet
        },
        {
          path: '/attendance/list',
          name: 'AttendanceList',
          component: AttendanceList
        },
        {
          path: '/staff/list',
          name: 'StaffList',
          component: StaffList
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  if (to.path === '/login' || to.path === '/mobile') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router