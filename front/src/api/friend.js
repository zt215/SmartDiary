import service from './auth'

export function searchFriendUser (userId, phone) {
  return service({
    url: '/friends/search-user',
    method: 'get',
    params: { userId, phone }
  })
}

export function sendFriendRequest (fromUserId, toUserId) {
  return service({
    url: '/friends/request',
    method: 'post',
    data: { fromUserId, toUserId }
  })
}

export function acceptFriendRequest (userId, requestId) {
  return service({
    url: '/friends/accept',
    method: 'post',
    data: { userId, requestId }
  })
}

export function rejectFriendRequest (userId, requestId) {
  return service({
    url: '/friends/reject',
    method: 'post',
    data: { userId, requestId }
  })
}

export function listFriends (userId) {
  return service({
    url: '/friends/list',
    method: 'get',
    params: { userId }
  })
}

export function listIncomingFriendRequests (userId) {
  return service({
    url: '/friends/incoming',
    method: 'get',
    params: { userId }
  })
}

export function listOutgoingFriendRequests (userId) {
  return service({
    url: '/friends/outgoing',
    method: 'get',
    params: { userId }
  })
}

export function removeFriend (userId, friendUserId) {
  return service({
    url: '/friends/remove',
    method: 'delete',
    params: { userId, friendUserId }
  })
}

export function getFriendProfile (userId, friendUserId) {
  return service({
    url: '/friends/profile',
    method: 'get',
    params: { userId, friendUserId }
  })
}
