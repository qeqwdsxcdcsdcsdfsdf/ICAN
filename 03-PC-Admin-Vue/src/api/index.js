import axios from 'axios'

const baseURL = '/api'

const request = axios.create({
  baseURL: baseURL,
  timeout: 10000
})

export const adminLogin = (params) => request.post('/admin/login', params)

export const getLocationConfig = () => request.get('/location/config')
export const updateLocationConfig = (params) => request.post('/location/config', params)

export const getAttendanceRecords = (params) => request.get('/sign/records', { params })
export const deleteAttendanceRecords = (params) => request.post('/sign/delete', params)
export const reverseGeocode = (params) => request.get('/sign/reverse-geocode', { params })

export const getStaffList = () => request.get('/staff/list')
export const addStaff = (params) => request.post('/staff/add', params)
export const updateStaff = (params) => request.post('/staff/update', params)
export const deleteStaff = (params) => request.post('/staff/delete', params)