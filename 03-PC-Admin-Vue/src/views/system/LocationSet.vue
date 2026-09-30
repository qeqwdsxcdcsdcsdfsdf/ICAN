<template>
  <div class="location-set-container">
    <el-card>
      <h3>定位配置</h3>
      <el-form ref="form" :model="form" label-width="120px">
        <el-form-item label="配置名称">
          <el-input v-model="form.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="中心点纬度">
          <el-input v-model="form.latitude" placeholder="请输入中心点纬度" />
        </el-form-item>
        <el-form-item label="中心点经度">
          <el-input v-model="form.longitude" placeholder="请输入中心点经度" />
        </el-form-item>
        <el-form-item label="允许打卡半径(米)">
          <el-input-number v-model="form.radius" :min="50" :max="5000" :step="50" />
        </el-form-item>
        <el-form-item label="当前地址">
          <el-input v-model="currentAddress" placeholder="点击获取定位后显示地址" disabled />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存配置</el-button>
          <el-button type="success" @click="getCurrentLocation">📍 获取当前位置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card style="margin-top: 20px;">
      <h3>当前配置信息</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="配置名称">{{ config.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="中心点纬度">{{ config.latitude || '-' }}</el-descriptions-item>
        <el-descriptions-item label="中心点经度">{{ config.longitude || '-' }}</el-descriptions-item>
        <el-descriptions-item label="允许打卡半径">{{ config.radius ? config.radius + '米' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后修改时间" :span="2">{{ config.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script>
import { getLocationConfig, updateLocationConfig, reverseGeocode } from '../../api'

export default {
  name: 'LocationSet',
  data() {
    return {
      form: {
        id: null,
        name: '',
        latitude: '',
        longitude: '',
        radius: 500
      },
      config: {},
      currentAddress: ''
    }
  },
  async mounted() {
    await this.loadConfig()
  },
  methods: {
    async loadConfig() {
      try {
        const res = await getLocationConfig()
        if (res.data.success && res.data.data) {
          this.config = res.data.data
          this.form = {
            id: res.data.data.id,
            name: res.data.data.name,
            latitude: res.data.data.latitude,
            longitude: res.data.data.longitude,
            radius: res.data.data.radius || 500
          }
        }
      } catch (error) {
        this.$message.error('加载配置失败')
      }
    },
    async handleSave() {
      try {
        const res = await updateLocationConfig(this.form)
        if (res.data.success) {
          this.$message.success(res.data.message)
          await this.loadConfig()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    getCurrentLocation() {
      if (navigator.geolocation) {
        this.$message.info('正在获取当前位置...')
        navigator.geolocation.getCurrentPosition(
          (position) => {
            this.form.latitude = position.coords.latitude.toFixed(6)
            this.form.longitude = position.coords.longitude.toFixed(6)
            
            this.reverseGeocode(position.coords.latitude, position.coords.longitude)
            
            this.$message.success('位置获取成功')
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
    reverseGeocode(lat, lng) {
      reverseGeocode({ lat, lon: lng })
        .then(res => {
          if (res.data.success) {
            try {
              let data = res.data.data
              if (typeof data === 'string') {
                data = JSON.parse(data)
              }
              if (data && (data.display_name || data.address)) {
                this.currentAddress = data.display_name || data.address
              } else {
                this.currentAddress = `${lat.toFixed(6)}, ${lng.toFixed(6)}`
              }
            } catch (e) {
              console.log('解析地址失败:', e)
              this.currentAddress = `${lat.toFixed(6)}, ${lng.toFixed(6)}`
            }
          } else {
            this.currentAddress = `${lat.toFixed(6)}, ${lng.toFixed(6)}`
          }
        })
        .catch(error => {
          console.log('反向地理编码失败:', error)
          this.currentAddress = `${lat.toFixed(6)}, ${lng.toFixed(6)}`
        })
    }
  }
}
</script>

<style scoped>
.location-set-container {
  padding: 20px;
}
</style>