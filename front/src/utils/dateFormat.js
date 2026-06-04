/** 统一日期展示：2026/05/28 15:48 */

const RELATIVE_LABELS = new Set(['刚刚'])

function pad2(n) {
  return String(n).padStart(2, '0')
}

export function parseToDate(value) {
  if (!value) return null
  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value
  }
  if (typeof value === 'string') {
    const s = value.trim()
    if (!s || RELATIVE_LABELS.has(s)) return null
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
      const d = new Date(`${s}T00:00:00`)
      return Number.isNaN(d.getTime()) ? null : d
    }
    const d = new Date(s)
    return Number.isNaN(d.getTime()) ? null : d
  }
  return null
}

/** 2026/05/28 15:48 */
export function formatDateTime(value) {
  if (!value) return ''
  if (typeof value === 'string' && RELATIVE_LABELS.has(value.trim())) {
    return value.trim()
  }
  const d = parseToDate(value)
  if (!d) return ''
  return `${d.getFullYear()}/${pad2(d.getMonth() + 1)}/${pad2(d.getDate())} ${pad2(d.getHours())}:${pad2(d.getMinutes())}`
}

/** 2026/05/28（生日等纯日期） */
export function formatDateOnly(value) {
  if (!value) return ''
  const d = parseToDate(value)
  if (!d) {
    if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value.trim())) {
      return value.trim().replace(/-/g, '/')
    }
    return ''
  }
  return `${d.getFullYear()}/${pad2(d.getMonth() + 1)}/${pad2(d.getDate())}`
}

/** 评论时间：刚刚 / N分钟前 / 完整 datetime */
export function formatCommentTime(value) {
  if (!value) return ''
  if (typeof value === 'string' && RELATIVE_LABELS.has(value.trim())) {
    return value.trim()
  }
  const d = parseToDate(value)
  if (!d) return ''
  const now = Date.now()
  const diff = now - d.getTime()
  if (diff >= 0 && diff < 60000) return '刚刚'
  if (diff >= 0 && diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff >= 0 && diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return formatDateTime(d)
}

/** 筛选用 YYYY-MM-DD */
export function toDateKey(value) {
  const d = parseToDate(value)
  if (!d) return ''
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}
