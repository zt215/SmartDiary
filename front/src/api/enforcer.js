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

export function listAdminDiaryCircles(enforcerId, page = 1, pageSize = 20) {
  return service({
    url: `${base}/admin/diary-circles`,
    method: 'get',
    params: { enforcerId, page, pageSize }
  })
}
