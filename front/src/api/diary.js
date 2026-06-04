import service from './auth.js'

// 注意：如果 auth.js 没有导出 service，请使用以下方式：
// import axios from 'axios'
// const baseURL = import.meta.env.VITE_BASE_API || 'http://localhost:3000'
// const service = axios.create({
//   baseURL,
//   timeout: 5000
// })
// service.interceptors.request.use(config => {
//   const token = localStorage.getItem('token')
//   if (token) {
//     config.headers = config.headers || {}
//     config.headers.Authorization = `Bearer ${token}`
//   }
//   return config
// }, error => Promise.reject(error))
// service.interceptors.response.use(response => response.data, error => Promise.reject(error))

// 创建日记
export function createDiary(data) {
  return service({
    url: '/diary/create',
    method: 'post',
    data
  })
}

// 根据ID获取日记
export function getDiaryById(id) {
  return service({
    url: `/diary/${id}`,
    method: 'get'
  })
}

// 根据用户ID获取所有日记
export function getDiariesByUserId(userId) {
  return service({
    url: `/diary/user/${userId}`,
    method: 'get'
  })
}

// 更新日记
export function updateDiary(data) {
  return service({
    url: '/diary/update',
    method: 'put',
    data
  })
}

// 删除日记
export function deleteDiary(id) {
  return service({
    url: `/diary/${id}`,
    method: 'delete'
  })
}

// 搜索日记
export function searchDiaries(userId, keyword) {
  return service({
    url: '/diary/search',
    method: 'get',
    params: {
      userId,
      keyword
    }
  })
}

// 获取日记草稿
export function getDiaryDraft(userId) {
  return service({
    url: `/diary/draft/${userId}`,
    method: 'get'
  })
}

// 保存日记草稿（每用户一条，自动 upsert）
export function saveDiaryDraft(data) {
  return service({
    url: '/diary/draft',
    method: 'put',
    data
  })
}

// 删除日记草稿
export function deleteDiaryDraft(userId) {
  return service({
    url: `/diary/draft/${userId}`,
    method: 'delete'
  })
}
