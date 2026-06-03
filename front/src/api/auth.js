import axios from 'axios'

// 使用 Vite 的环境变量（需要以 VITE_ 开头），没有时回退到 http://localhost:3000
const baseURL = import.meta.env.VITE_BASE_API || 'http://localhost:3000'

// 创建 axios 实例
const service = axios.create({
  baseURL,
  timeout: 5000
})

// 请求拦截器：示例添加 token
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：简单处理
service.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

// 导出 service 实例供其他模块使用
export default service

// 导出 API 方法
export function login (data) {
  return service({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register (data) {
  return service({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function forgotPassword (data) {
  return service({
    url: '/auth/forgotpassword',
    method: 'post',
    data
  })
}

export function updateTheme (userId, theme) {
  return service({
    url: '/auth/update-theme',
    method: 'put',
    data: { userId, theme }
  })
}

// 更新用户信息
export function updateUser (data) {
  return service({
    url: '/auth/update',
    method: 'put',
    data
  })
}

// 修改密码（需验证当前密码）
export function changePassword (data) {
  return service({
    url: '/auth/change-password',
    method: 'put',
    data
  })
}

// 删除用户（注销账号）
export function deleteUser (id) {
  return service({
    url: `/auth/${id}`,
    method: 'delete'
  })
}