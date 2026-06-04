import axios from 'axios'

const BASE_URL = 'http://localhost:3000/api/diary-circle'

/**
 * 获取动态列表
 */
export function getDiaryCircleList(page = 1, pageSize = 10, userId = null, filter = 'all') {
  const params = { page, pageSize, filter }
  if (userId !== null && userId !== undefined) {
    params.userId = userId
  }
  return axios.get(`${BASE_URL}/list`, { params })
    .then(res => res.data)
}

/**
 * 根据 ID 获取动态详情
 */
export function getDiaryCircleById(id, userId = null) {
  const params = {}
  if (userId !== null && userId !== undefined) {
    params.userId = userId
  }
  return axios.get(`${BASE_URL}/${id}`, { params })
    .then(res => res.data)
}

/**
 * 发布新动态
 */
export function publishDiaryCircle(data) {
  return axios.post(`${BASE_URL}/publish`, data)
    .then(res => res.data)
}

/**
 * 点赞/取消点赞
 */
export function toggleLike(id, isLike, userId) {
  return axios.post(`${BASE_URL}/like/${id}`, null, {
    params: { isLike, userId }
  }).then(res => res.data)
}

/**
 * 删除动态
 */
export function deleteDiaryCircle(id, userId) {
  return axios.delete(`${BASE_URL}/delete/${id}`, {
    params: { userId }
  })
    .then(res => res.data)
}

/**
 * 获取用户的动态
 */
export function getUserDiaries(userId, viewerId = null) {
  const params = {}
  if (viewerId !== null && viewerId !== undefined) {
    params.viewerId = viewerId
  }
  return axios.get(`${BASE_URL}/user/${userId}`, { params })
    .then(res => res.data)
}
