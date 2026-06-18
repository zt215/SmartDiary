import service from './auth.js'

const base = '/enforcer'

export function enforcerLogin(data) {
  return service({
    url: `${base}/auth/login`,
    method: 'post',
    data
  })
}

export function enforcerForgotPassword(data) {
  return service({
    url: `${base}/auth/forgotpassword`,
    method: 'post',
    data
  })
}

export function enforcerSendCode(data) {
  return service({
    url: `${base}/auth/send-verification-code`,
    method: 'post',
    data
  })
}

export function enforcerCheckPhone(data) {
  return service({
    url: `${base}/auth/check-phone`,
    method: 'post',
    data
  })
}

export function enforcerVerifyCode(data) {
  return service({
    url: `${base}/auth/verify-code`,
    method: 'post',
    data
  })
}

export function listAdminUsers(enforcerId) {
  return service({
    url: `${base}/admin/users`,
    method: 'get',
    params: { enforcerId }
  })
}

export function listAdminDiaryCircles(enforcerId, page = 1, pageSize = 20, keyword = '') {
  return service({
    url: `${base}/admin/diary-circles`,
    method: 'get',
    params: { enforcerId, page, pageSize, keyword: keyword || undefined }
  })
}

export function cancelAdminUser(enforcerId, userId) {
  return service({
    url: `${base}/admin/users/${userId}/cancel`,
    method: 'post',
    params: { enforcerId }
  })
}

export function banAdminUser(userId, data) {
  return service({
    url: `${base}/admin/users/${userId}/ban`,
    method: 'post',
    data
  })
}

export function unbanAdminUser(userId, enforcerId) {
  return service({
    url: `${base}/admin/users/${userId}/unban`,
    method: 'post',
    params: { enforcerId }
  })
}

export function updateAdminUser(userId, data) {
  return service({
    url: `${base}/admin/users/${userId}`,
    method: 'put',
    data
  })
}

export function deleteDiaryCircle(circleId, enforcerId) {
  return service({
    url: `${base}/admin/diary-circles/${circleId}/delete`,
    method: 'post',
    params: { enforcerId }
  })
}

export function hideDiaryCircle(circleId, enforcerId) {
  return service({
    url: `${base}/admin/diary-circles/${circleId}/hide`,
    method: 'post',
    params: { enforcerId }
  })
}

export function showDiaryCircle(circleId, enforcerId) {
  return service({
    url: `${base}/admin/diary-circles/${circleId}/show`,
    method: 'post',
    params: { enforcerId }
  })
}

export function listAdminEnforcers(enforcerId) {
  return service({
    url: `${base}/admin/enforcers`,
    method: 'get',
    params: { enforcerId }
  })
}

export function addAdminEnforcer(enforcerId, data) {
  return service({
    url: `${base}/admin/enforcers`,
    method: 'post',
    params: { enforcerId },
    data
  })
}

export function deleteAdminEnforcer(enforcerId, targetEnforcerId) {
  return service({
    url: `${base}/admin/enforcers/${targetEnforcerId}`,
    method: 'delete',
    params: { enforcerId }
  })
}

export function searchUsersByKeyword(enforcerId, keyword) {
  return service({
    url: `${base}/admin/users/search`,
    method: 'get',
    params: { enforcerId, keyword }
  })
}
