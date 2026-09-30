<template>
  <div class="attendance-list-container">
    <el-card>
      <div class="filter-bar">
        <el-input v-model="searchKeyword" placeholder="搜索工号/姓名" style="width: 200px" @keyup.enter="loadRecords" />
        <el-select v-model="locationStatus" placeholder="定位状态" style="width: 120px">
          <el-option :label="'全部'" :value="'all'" />
          <el-option :label="'定位合规'" :value="0" />
          <el-option :label="'超出范围'" :value="1" />
          <el-option :label="'获取失败'" :value="2" />
        </el-select>
        <el-select v-model="signType" placeholder="签到类型" style="width: 100px">
          <el-option :label="'全部'" :value="'all'" />
          <el-option :label="'上班'" :value="1" />
          <el-option :label="'下班'" :value="2" />
        </el-select>
        <el-date-picker v-model="searchDate" type="date" placeholder="选择日期" style="width: 150px" />
        <el-button type="primary" @click="loadRecords">查询</el-button>
        <el-button @click="exportExcel">导出Excel</el-button>
        <el-button type="success" @click="refreshRecords">🔄 刷新</el-button>
        <el-button type="danger" @click="handleDeleteSelected" :disabled="selectedIds.length === 0">
          🗑️ 删除选中 ({{ selectedIds.length }})
        </el-button>
      </div>
      <el-table :data="records" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="staffNo" label="工号" />
        <el-table-column prop="staffName" label="姓名" />
        <el-table-column prop="signType" label="签到类型" :formatter="formatSignType" />
        <el-table-column prop="signTime" label="签到时间" :formatter="formatSignTime" />
        <el-table-column prop="status" label="状态" :formatter="formatStatus" />
        <el-table-column prop="signLat" label="签到纬度" />
        <el-table-column prop="signLng" label="签到经度" />
        <el-table-column prop="distance" label="距离(米)" />
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="viewLocation(scope.row)" v-if="scope.row.signLat && scope.row.signLng">
              📍 查看位置
            </el-button>
            <span v-else style="color:#999">无位置</span>
          </template>
        </el-table-column>
        <el-table-column prop="locationStatus" label="定位状态" :formatter="formatLocationStatus" />
        <el-table-column prop="signResult" label="签到结果" :formatter="formatSignResult" />
        <el-table-column prop="failReason" label="失败原因" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getAttendanceRecords, deleteAttendanceRecords } from '../../api'

export default {
  name: 'AttendanceList',
  data() {
    return {
      records: [],
      locationStatus: 'all',
      signType: 'all',
      searchKeyword: '',
      searchDate: '',
      refreshTimer: null,
      selectedIds: []
    }
  },
  async mounted() {
    await this.loadRecords()
    this.startRefreshTimer()
  },
  beforeDestroy() {
    this.stopRefreshTimer()
  },
  methods: {
    async loadRecords() {
      try {
        const params = {}
        if (this.locationStatus !== 'all') {
          params.locationStatus = this.locationStatus
        }
        if (this.signType !== 'all') {
          params.signType = this.signType
        }
        if (this.searchKeyword) {
          params.keyword = this.searchKeyword
        }
        if (this.searchDate) {
          params.date = this.searchDate
        }
        const res = await getAttendanceRecords(params)
        if (res.data.success) {
          this.records = res.data.data || []
        }
      } catch (error) {
        this.$message.error('加载记录失败')
      }
    },
    refreshRecords() {
      this.loadRecords()
      this.$message.success('记录已刷新')
    },
    startRefreshTimer() {
      this.refreshTimer = setInterval(() => {
        this.loadRecords()
      }, 10000)
    },
    stopRefreshTimer() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },
    formatSignType(row) {
      return row.signType === 1 ? '上班' : '下班'
    },
    formatSignTime(row) {
      if (!row.signTime) return '-'
      const date = new Date(row.signTime)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },
    formatStatus(row) {
      const statusMap = { 0: '正常', 1: '迟到', 2: '早退', 3: '缺勤' }
      return statusMap[row.status] || '-'
    },
    formatLocationStatus(row) {
      const statusMap = { 0: '合规', 1: '越界', 2: '失败' }
      return statusMap[row.locationStatus] || '-'
    },
    formatSignResult(row) {
      return row.signResult === 0 ? '成功' : '失败'
    },
    exportExcel() {
      this.$message.info('导出功能开发中')
    },
    viewLocation(row) {
      if (!row.signLat || !row.signLng) {
        this.$message.error('无位置信息')
        return
      }
      const url = `https://www.amap.com/map/place?query=${row.signLat},${row.signLng}&adcode=110000`
      window.open(url, '_blank')
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    async handleDeleteSelected() {
      if (this.selectedIds.length === 0) {
        this.$message.warning('请先选择要删除的记录')
        return
      }
      try {
        await this.$confirm(`确定删除选中的 ${this.selectedIds.length} 条记录？`, '提示', { type: 'warning' })
        const res = await deleteAttendanceRecords({ ids: this.selectedIds })
        if (res.data.success) {
          this.$message.success(res.data.message)
          this.selectedIds = []
          await this.loadRecords()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.attendance-list-container {
  padding: 20px;
}
.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>