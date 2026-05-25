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

export function markFriendDiarySeen (userId, friendId, seenAt) {
  if (!userId || !friendId) return
  const map = getFriendDiarySeenMap(userId)
  const time = seenAt instanceof Date ? seenAt.getTime() : new Date(seenAt).getTime()
  if (!Number.isNaN(time)) {
    map[String(friendId)] = time
    localStorage.setItem(storageKey(userId), JSON.stringify(map))
  }
}

export function getFriendDiarySeenAt (userId, friendId) {
  if (!userId || !friendId) return 0
  return getFriendDiarySeenMap(userId)[String(friendId)] || 0
}

export function isNewFriendDiaryItem (createTime, seenBeforeMs) {
  if (!createTime) return false
  const t = new Date(createTime).getTime()
  if (Number.isNaN(t)) return false
  return t > (seenBeforeMs || 0)
}

export function hasNewFriendDiary (userId, friendId, latestDiaryTime) {
  if (!userId || !friendId || !latestDiaryTime) return false
  const latest = new Date(latestDiaryTime).getTime()
  if (Number.isNaN(latest)) return false
  const seen = getFriendDiarySeenAt(userId, friendId)
  return latest > seen
}

export function countFriendsWithNewDiary (userId, friends) {
  if (!userId || !friends?.length) return 0
  return friends.filter(f => hasNewFriendDiary(userId, f.id, f.latestDiaryTime)).length
}
