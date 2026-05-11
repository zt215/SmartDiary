import axios from 'axios'

const BASE_URL = 'http://localhost:3000/api/comment'

/**
 * 添加评论
 */
export function addComment(data) {
  return axios.post(`${BASE_URL}/add`, data)
    .then(res => res.data)
}

/**
 * 获取评论列表
 */
export function getComments(circleId, userId) {
  const params = {}
  if (userId !== null && userId !== undefined) {
    params.userId = userId
  }
  return axios.get(`${BASE_URL}/list/${circleId}`, { params })
    .then(res => res.data)
}

/**
 * 删除评论
 */
export function deleteComment(id, userId, diaryId) {
  const params = {}
  if (userId !== null && userId !== undefined) {
    params.userId = userId
  }
  if (diaryId !== null && diaryId !== undefined) {
    params.diaryId = diaryId
  }
  return axios.delete(`${BASE_URL}/delete/${id}`, { params })
    .then(res => res.data)
}

/**
 * 点赞评论
 */
export function toggleCommentLike(id, isLike, userId) {
  return axios.post(`${BASE_URL}/like/${id}`, null, {
    params: { isLike, userId }
  }).then(res => res.data)
}
