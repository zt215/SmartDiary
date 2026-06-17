import { ElMessage, ElMessageBox } from 'element-plus'
import { resolveEscParentRoute, shouldIgnoreEscForTarget } from './navigationBack'

export function clearUserSession() {
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  localStorage.removeItem('diary-theme')
}

export function clearEnforcerSession() {
  localStorage.removeItem('enforcerInfo')
}

function hasBlockingOverlay() {
  const overlays = document.querySelectorAll('.el-overlay')
  for (const el of overlays) {
    if (el.style.display !== 'none' && window.getComputedStyle(el).display !== 'none') {
      return true
    }
  }
  return !!document.querySelector('.el-message-box__wrapper')
}

export async function confirmLogoutUser(router) {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    clearUserSession()
    ElMessage.success('已退出登录')
    await router.replace('/')
  } catch {
    // 用户取消
  }
}

export async function handleEscNavigation(router, e) {
  if (e.key !== 'Escape') return
  if (hasBlockingOverlay()) return
  if (shouldIgnoreEscForTarget(e.target)) return

  const route = router.currentRoute.value
  const name = route.name

  if (name === 'home') {
    e.preventDefault()
    await confirmLogoutUser(router)
    return
  }

  if (name === 'enforcerHome') {
    e.preventDefault()
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      clearEnforcerSession()
      ElMessage.success('已退出登录')
      await router.replace('/enforcer/login')
    } catch {
      // 用户取消
    }
    return
  }

  const parent = resolveEscParentRoute(route)
  if (!parent || parent === route.path) return

  e.preventDefault()
  await router.push(parent)
}
