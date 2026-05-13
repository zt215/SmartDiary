import { codeToText } from 'element-china-area-data'

/**
 * 是否为「行政区划码」存盘格式（多段数字码，逗号分隔）
 */
export function isStoredRegionCodes (stored) {
  if (stored == null || typeof stored !== 'string') return false
  const parts = stored.split(',').map((s) => s.trim()).filter(Boolean)
  if (parts.length < 2) return false
  return parts.every((p) => /^\d+$/.test(p))
}

/** 展示用：码串 → 空格分隔的中文地名 */
export function formatRegionAddressLabel (stored) {
  if (stored == null || stored === '') return ''
  if (!isStoredRegionCodes(stored)) return String(stored)
  return stored
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
    .map((code) => codeToText[code] || code)
    .join(' ')
}

/** 存盘字符串 → 级联选择器 v-model 数组 */
export function parseRegionCodesToCascaderValue (stored) {
  if (!isStoredRegionCodes(stored)) return []
  return stored.split(',').map((s) => s.trim()).filter(Boolean)
}

/** 级联选择器 v-model 数组 → 存盘字符串 */
export function cascaderValueToStorage (codes) {
  if (!codes || !Array.isArray(codes) || codes.length === 0) return ''
  return codes.join(',')
}
