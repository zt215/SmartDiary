/**
 * ESC 返回上一级（按路由层级，非浏览器 history.back）
 * @param {import('vue-router').RouteLocationNormalizedLoaded} route
 * @returns {string|null} 上一级 path，null 表示已在顶层
 */
export function resolveEscParentRoute(route) {
  const name = route.name
  const path = route.path

  switch (name) {
    case 'login':
    case 'register':
    case 'forgotpassword':
      return null
    case 'home':
      return '/'
    case 'newDiary':
    case 'settings':
    case 'friends':
    case 'diaryCircle':
      return '/home'
    case 'diaryDetail':
      return '/home'
    case 'diaryCircleDetail':
      return route.query.from === 'friends' ? '/friends' : '/diary-circle'
    case 'enforcerLogin':
      return '/'
    case 'enforcerForgotPassword':
      return '/enforcer/login'
    case 'enforcerHome':
      return '/enforcer/login'
    default:
      if (path.startsWith('/diary-circle/')) {
        return route.query.from === 'friends' ? '/friends' : '/diary-circle'
      }
      if (path.startsWith('/diary/')) return '/home'
      if (path.startsWith('/enforcer/')) return '/enforcer/login'
      return '/'
  }
}

export function shouldIgnoreEscForTarget(target) {
  if (!target || !(target instanceof HTMLElement)) return false
  const tag = target.tagName
  // 单行输入框不拦截，便于好友/字迹圈/执法堂等页面 ESC 返回
  if (tag === 'TEXTAREA' || tag === 'SELECT') return true
  if (target.isContentEditable) return true
  return !!target.closest('.ql-editor, .ql-container')
}
