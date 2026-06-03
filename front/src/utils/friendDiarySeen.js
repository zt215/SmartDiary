function storageKey (userId) {
  return `friendDiarySeen_${userId}`
}

export function getFriendDiarySeenMap (userId) {
  if (!userId) return {}
  try {
    const raw = localStorage.getItem(storageKey(userId))
    return raw ? JSON.parse(raw) : {}
  } catch {
    return {}
  }
}

function persistSeenMap (userId, map) {
  localStorage.setItem(storageKey(userId), JSON.stringify(map))
}

/** 单条字迹是否未读 */
export function isNewFriendDiaryItem (diary, userId, friendId) {
  if (!diary?.id || !userId || !friendId) return false
  const val = getFriendDiarySeenMap(userId)[String(friendId)]
  const idStr = String(diary.id)
  if (Array.isArray(val)) {
    return !val.map(String).includes(idStr)
  }
  // 兼容旧版：按时间戳判断
  if (typeof val === 'number') {
    if (!diary.createTime) return true
    const t = new Date(diary.createTime).getTime()
    return !Number.isNaN(t) && t > val
  }
  return true
}

/** 仅将单条字迹标为已读（按 ID，不影响同好友其它未读） */
export function markFriendDiaryItemSeen (userId, friendId, diaryId, allDiaries = null) {
  if (!userId || !friendId || diaryId == null) return
  const map = getFriendDiarySeenMap(userId)
  const key = String(friendId)
  const val = map[key]
  let ids = []

  if (Array.isArray(val)) {
    ids = val.map(String)
  } else if (typeof val === 'number' && allDiaries?.length) {
    const legacy = val
    ids = allDiaries
      .filter(d => {
        if (String(d.id) === String(diaryId)) return true
        if (!d.createTime) return false
        const t = new Date(d.createTime).getTime()
        return !Number.isNaN(t) && t <= legacy
      })
      .map(d => String(d.id))
  }

  const idStr = String(diaryId)
  if (!ids.includes(idStr)) ids.push(idStr)
  map[key] = ids
  persistSeenMap(userId, map)
}

export function countNewFriendDiaryItems (diaries, userId, friendId) {
  if (!diaries?.length || !userId || !friendId) return 0
  return diaries.filter(d => isNewFriendDiaryItem(d, userId, friendId)).length
}

/**
 * 拉取各好友未读字迹条数
 * @param {Function} getUserDiaries (friendId, viewerId) => Promise<{ success, data }>
 */
export async function buildFriendUnreadCounts (userId, friends, getUserDiaries) {
  const counts = {}
  if (!userId || !friends?.length) return counts

  await Promise.all(friends.map(async (f) => {
    if (!f?.id) return
    if (!f.latestDiaryTime) {
      counts[f.id] = 0
      return
    }
    try {
      const res = await getUserDiaries(f.id, userId)
      counts[f.id] = res?.success
        ? countNewFriendDiaryItems(res.data || [], userId, f.id)
        : 0
    } catch {
      counts[f.id] = 0
    }
  }))
  return counts
}

export function sumFriendUnreadCounts (counts) {
  if (!counts) return 0
  return Object.values(counts).reduce((sum, n) => sum + (Number(n) || 0), 0)
}

export function hasNewFriendDiary (userId, friendId, latestDiaryTime) {
  if (!userId || !friendId || !latestDiaryTime) return false
  const val = getFriendDiarySeenMap(userId)[String(friendId)]
  if (Array.isArray(val)) {
    return true
  }
  if (typeof val === 'number') {
    const latest = new Date(latestDiaryTime).getTime()
    return !Number.isNaN(latest) && latest > val
  }
  return true
}

export function countFriendsWithNewDiary (userId, friends) {
  if (!userId || !friends?.length) return 0
  return friends.filter(f => hasNewFriendDiary(userId, f.id, f.latestDiaryTime)).length
}
