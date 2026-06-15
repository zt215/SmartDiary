import { ElMessageBox } from 'element-plus'
import { h } from 'vue'
import { formatDateTime } from './dateFormat'

export function showUserBanAlert(res) {
  const ban = res?.banInfo
  if (!ban) {
    return Promise.resolve()
  }
  const end = ban.endTime ? formatDateTime(ban.endTime) : '未知'
  const reason = ban.reasonText || ban.reason || '违反社区规范'
  return ElMessageBox.alert(
    h('div', [
      h('p', `解封时间：${end}`),
      h('p', `封禁理由：${reason}`),
      h('p', '封禁期间无法发布动态或评论。')
    ]),
    '账号封禁提醒',
    { confirmButtonText: '我知道了', type: 'warning' }
  )
}

export function isUserBannedResponse(res) {
  return res && !res.success && (res.code === 'USER_BANNED' || res.banInfo)
}
