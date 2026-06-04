import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const REPLY_VISIBLE_LIMIT = 3

/** 展示被回复者昵称（兼容旧数据无 replyToUserId 时回退为根评论作者） */
export function getReplyToUserName(reply, rootComment) {
  if (reply?.replyToUserName) return reply.replyToUserName
  if (reply?.replyToUserId && rootComment) {
    if (rootComment.userId === reply.replyToUserId) return rootComment.userName
    const matched = (rootComment.replies || []).find((r) => r.userId === reply.replyToUserId)
    if (matched?.userName) return matched.userName
  }
  if (reply?.parentId && rootComment?.userName) return rootComment.userName
  return null
}

/**
 * 评论回复：点击他人评论进入回复态；回复超过 3 条折叠
 */
export function useCommentReplies(getCurrentUserId) {
  const replyTarget = ref(null)
  const expandedReplyParentIds = ref({})

  const canReplyTo = (comment) => {
    const uid = getCurrentUserId()
    if (!uid) return false
    return comment.userId !== uid
  }

  const startReply = (rootComment, displayUser) => {
    const user = displayUser || rootComment
    replyTarget.value = {
      parentId: rootComment.id,
      userName: user.userName,
      replyToUserId: user.userId,
      rootComment
    }
  }

  const startReplyToReply = (rootComment, reply) => {
    replyTarget.value = {
      parentId: rootComment.id,
      userName: reply.userName,
      replyToUserId: reply.userId,
      rootComment
    }
  }

  const cancelReply = () => {
    replyTarget.value = null
  }

  const commentInputPlaceholder = computed(() => {
    if (replyTarget.value) {
      return `回复 @${replyTarget.value.userName}...`
    }
    return '写下你的评论...'
  })

  const isReplyMode = computed(() => !!replyTarget.value)

  const submitButtonText = computed(() => (replyTarget.value ? '发表回复' : '发表评论'))

  const getVisibleReplies = (comment) => {
    const replies = comment.replies || []
    if (replies.length <= REPLY_VISIBLE_LIMIT) return replies
    if (expandedReplyParentIds.value[comment.id]) return replies
    return replies.slice(0, REPLY_VISIBLE_LIMIT)
  }

  const hiddenReplyCount = (comment) => {
    const replies = comment.replies || []
    if (replies.length <= REPLY_VISIBLE_LIMIT) return 0
    if (expandedReplyParentIds.value[comment.id]) return 0
    return replies.length - REPLY_VISIBLE_LIMIT
  }

  const isRepliesExpanded = (comment) => !!expandedReplyParentIds.value[comment.id]

  const expandReplies = (comment) => {
    expandedReplyParentIds.value = {
      ...expandedReplyParentIds.value,
      [comment.id]: true
    }
  }

  const collapseReplies = (comment) => {
    const next = { ...expandedReplyParentIds.value }
    delete next[comment.id]
    expandedReplyParentIds.value = next
  }

  const handleReplyClick = (rootComment, displayUser) => {
    if (!getCurrentUserId()) {
      ElMessage.warning('请先登录')
      return
    }
    const target = displayUser || rootComment
    if (!canReplyTo(target)) return
    if (displayUser) {
      startReplyToReply(rootComment, displayUser)
    } else {
      startReply(rootComment)
    }
  }

  const appendSubmittedComment = (commentList, resData, user, parentId, rootComment) => {
    const item = {
      ...resData,
      userName: user.name,
      userAvatar: user.avatar,
      createTime: '刚刚',
      likeCount: resData.likeCount ?? 0,
      isLiked: false
    }

    if (parentId && rootComment) {
      if (!rootComment.replies) rootComment.replies = []
      const replyToUserId = replyTarget.value?.replyToUserId ?? null
      const replyToUserName = replyTarget.value?.userName ?? null
      rootComment.replies.push({
        ...item,
        parentId,
        replyToUserId,
        replyToUserName
      })
      cancelReply()
      return 'reply'
    }

    commentList.push({ ...item, replies: [] })
    cancelReply()
    return 'top'
  }

  const removeCommentFromList = (commentList, comment) => {
    if (comment.parentId) {
      const root = commentList.find((c) => c.id === comment.parentId)
      if (root?.replies) {
        root.replies = root.replies.filter((r) => r.id !== comment.id)
      }
      return
    }
    const idx = commentList.findIndex((c) => c.id === comment.id)
    if (idx !== -1) commentList.splice(idx, 1)
  }

  return {
    getReplyToUserName,
    replyTarget,
    canReplyTo,
    startReply,
    startReplyToReply,
    cancelReply,
    commentInputPlaceholder,
    isReplyMode,
    submitButtonText,
    getVisibleReplies,
    hiddenReplyCount,
    isRepliesExpanded,
    expandReplies,
    collapseReplies,
    handleReplyClick,
    appendSubmittedComment,
    removeCommentFromList
  }
}
