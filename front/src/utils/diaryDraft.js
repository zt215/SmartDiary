/** 新建日记草稿（服务端保存，按用户隔离） */

import { getDiaryDraft, saveDiaryDraft as saveDiaryDraftApi, deleteDiaryDraft } from '../api/diary'

const LEGACY_KEYS = ['diaryDraft']

export function hasDraftContent({ title, content } = {}) {
  if ((title || '').trim()) return true
  if (!content) return false
  const tmp = document.createElement('div')
  tmp.innerHTML = content
  return (tmp.textContent || tmp.innerText || '').trim().length > 0
}

function legacyKeysForUser(userId) {
  return [`diaryDraft_user_${userId}`, ...LEGACY_KEYS]
}

async function migrateLocalDraftToServer(userId) {
  for (const key of legacyKeysForUser(userId)) {
    const raw = localStorage.getItem(key)
    if (!raw) continue
    try {
      const data = JSON.parse(raw)
      if (hasDraftContent(data)) {
        await saveDiaryDraftApi({
          userId,
          title: data.title || '',
          content: data.content || '',
          diaryDate: data.date || data.diaryDate || null
        })
      }
    } catch {
      /* ignore corrupt local draft */
    } finally {
      localStorage.removeItem(key)
    }
  }
}

export async function loadDiaryDraft(userId) {
  if (!userId) return null
  await migrateLocalDraftToServer(userId)
  try {
    const res = await getDiaryDraft(userId)
    if (!res?.success || !res.data) return null
    const d = res.data
    return {
      title: d.title || '',
      content: d.content || '',
      date: d.diaryDate || ''
    }
  } catch {
    return null
  }
}

export async function saveDiaryDraft(userId, data) {
  if (!userId) return false
  if (!hasDraftContent(data)) {
    try {
      await deleteDiaryDraft(userId)
    } catch {
      /* ignore */
    }
    return false
  }
  try {
    const res = await saveDiaryDraftApi({
      userId,
      title: data.title || '',
      content: data.content || '',
      diaryDate: data.date || null
    })
    return !!res?.success
  } catch {
    return false
  }
}

export async function clearDiaryDraft(userId) {
  if (!userId) return
  try {
    await deleteDiaryDraft(userId)
  } catch {
    /* ignore */
  }
  for (const key of legacyKeysForUser(userId)) {
    localStorage.removeItem(key)
  }
}

export async function hasDiaryDraft(userId) {
  return !!(await loadDiaryDraft(userId))
}
