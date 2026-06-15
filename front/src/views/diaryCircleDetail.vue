<template>
  <div class="circle-detail-container">
    <!-- 顶部导航栏 -->
    <header class="detail-header">
      <div class="header-left" @click="goBack">
        <span class="back-btn">&larr;</span>
        <span class="back-text">{{ backText }}</span>
      </div>
      <h1 class="header-title">动态详情</h1>
      <div class="header-right">
        <button
          v-if="isDiaryOwner"
          class="header-delete-btn"
          @click="deleteDiary"
        >
          删除动态
        </button>
      </div>
    </header>

    <!-- 内容区域 -->
    <main class="detail-main">
      <div class="detail-content">
        <!-- 发布者信息 -->
        <div class="publisher-info">
          <img :src="diary.userAvatar" :alt="diary.userName" class="publisher-avatar" />
          <div class="publisher-details">
            <span class="publisher-name">{{ diary.userName }}</span>
            <span class="publish-time">{{ formatDateTime(diary.createTime) }}</span>
          </div>
        </div>

        <!-- 动态内容（富文本，与日记一致） -->
        <div class="diary-content diary-rich-content" v-html="diary.content"></div>

        <!-- 统计信息 -->
        <div class="diary-stats">
          <span
            class="stat-item stat-like"
            :class="{ active: diary.isLiked }"
            @click="toggleDiaryLike"
          >
            <i class="icon-like"></i> {{ diary.likeCount || 0 }} {{ diary.isLiked ? '已赞' : '点赞' }}
          </span>
          <span class="stat-item">
            <i class="icon-comment"></i> {{ totalCommentCount }} 评论
          </span>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <div class="comment-header">
            <h3>评论</h3>
            <span class="comment-count">({{ totalCommentCount }})</span>
          </div>
          
          <!-- 评论列表 -->
          <div class="comment-list">
            <div 
              v-for="comment in commentList" 
              :key="comment.id" 
              class="comment-item"
            >
              <div class="comment-body">
                <div class="comment-header-row">
                  <img :src="comment.userAvatar" :alt="comment.userName" class="comment-avatar" />
                  <div class="comment-user-info">
                    <span class="comment-user-name">{{ comment.userName }}</span>
                    <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
                  </div>
                </div>
                <div
                  class="comment-content"
                  :class="{ 'is-replyable': canReplyTo(comment) }"
                  @click="onCommentBodyClick(comment)"
                >
                  {{ comment.content }}
                </div>
                <div v-if="canDeleteComment(comment)" class="comment-meta-actions">
                  <button
                    class="comment-action-btn delete-btn"
                    @click.stop="deleteComment(comment)"
                  >
                    删除
                  </button>
                </div>

                <!-- 回复列表 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                  <div 
                    v-for="reply in getVisibleReplies(comment)" 
                    :key="reply.id" 
                    class="reply-item"
                  >
                    <div class="reply-body">
                      <div class="reply-header-row">
                        <img :src="reply.userAvatar" :alt="reply.userName" class="reply-avatar" />
                        <div class="reply-user-info">
                          <div class="reply-user-line">
                            <span class="reply-user-name">{{ reply.userName }}</span>
                            <template v-if="getReplyToUserName(reply, comment)">
                              <span class="reply-relation-text">回复</span>
                              <span class="reply-to-user-name">@{{ getReplyToUserName(reply, comment) }}</span>
                            </template>
                          </div>
                          <span class="reply-time">{{ formatCommentTime(reply.createTime) }}</span>
                        </div>
                      </div>
                      <div
                        class="reply-content"
                        :class="{ 'is-replyable': canReplyTo(reply) }"
                        @click="onReplyBodyClick(comment, reply)"
                      >
                        {{ reply.content }}
                      </div>
                      <div v-if="canDeleteComment(reply)" class="comment-meta-actions">
                        <button
                          class="comment-action-btn delete-btn"
                          @click.stop="deleteComment(reply)"
                        >
                          删除
                        </button>
                      </div>
                    </div>
                    <CommentLikeButton
                      :is-liked="!!reply.isLiked"
                      :count="reply.likeCount || 0"
                      @toggle="likeComment(reply)"
                    />
                  </div>
                <button
                  v-if="hiddenReplyCount(comment) > 0"
                  type="button"
                  class="reply-toggle-btn"
                  @click="expandReplies(comment)"
                >
                  展开 {{ hiddenReplyCount(comment) }} 条回复
                </button>
                <button
                  v-else-if="isRepliesExpanded(comment) && (comment.replies?.length || 0) > REPLY_VISIBLE_LIMIT"
                  type="button"
                  class="reply-toggle-btn"
                  @click="collapseReplies(comment)"
                >
                  收起回复
                </button>
                </div>
              </div>
              <CommentLikeButton
                :is-liked="!!comment.isLiked"
                :count="comment.likeCount || 0"
                @toggle="likeComment(comment)"
              />
            </div>
            
            <!-- 空状态 -->
            <div v-if="commentList.length === 0" class="empty-comments">
              <p>暂无评论，快来抢沙发吧！</p>
            </div>
          </div>
          
          <!-- 发布评论框 -->
          <div class="publish-comment-box">
            <div class="publish-comment-main">
              <div v-if="isReplyMode" class="reply-mode-bar">
                <span>正在回复 @{{ replyTarget.userName }}</span>
                <button type="button" class="cancel-reply-btn" @click="cancelReply">取消</button>
              </div>
              <textarea 
                ref="commentInputRef"
                v-model="newComment" 
                :placeholder="commentInputPlaceholder" 
                class="comment-input"
                rows="3"
              ></textarea>
            </div>
            <button class="submit-comment-btn" @click="submitComment" :disabled="!newComment.trim()">
              {{ submitButtonText }}
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDiaryCircleById, toggleLike, deleteDiaryCircle } from '../api/diaryCircle'
import { getComments, addComment, toggleCommentLike, deleteComment as deleteCommentApi } from '../api/comment'
import { useCommentReplies, REPLY_VISIBLE_LIMIT, getReplyToUserName } from '../composables/useCommentReplies'
import CommentLikeButton from '../components/CommentLikeButton.vue'
import { formatDateTime, formatCommentTime } from '../utils/dateFormat'
import { isUserBannedResponse, showUserBanAlert } from '../utils/banNotice'

export default {
  name: 'DiaryCircleDetailView',
  components: { CommentLikeButton },
  setup() {
    const router = useRouter()
    const route = useRoute()

    const backRoute = computed(() =>
      route.query.from === 'friends' ? '/friends' : '/diary-circle'
    )
    const backText = computed(() =>
      route.query.from === 'friends' ? '回到好友' : '回到字迹圈'
    )
    const goBackToList = () => {
      router.push(backRoute.value)
    }

    const diary = ref({
      id: null,
      userId: null,
      content: '',
      createTime: null,
      likeCount: 0,
      commentCount: 0,
      isLiked: false,
      userName: '',
      userAvatar: ''
    })
    const commentList = ref([])
    const newComment = ref('')
    const commentInputRef = ref(null)
    const currentUserId = ref(null)
    const isDiaryOwner = ref(false)

    const {
      replyTarget,
      canReplyTo,
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
    } = useCommentReplies(() => currentUserId.value)

    const totalCommentCount = computed(() =>
      commentList.value.reduce((sum, c) => sum + 1 + (c.replies?.length || 0), 0)
    )

    const focusCommentInput = () => {
      nextTick(() => commentInputRef.value?.focus())
    }

    const onCommentBodyClick = (comment) => {
      handleReplyClick(comment)
      focusCommentInput()
    }

    const onReplyBodyClick = (rootComment, reply) => {
      handleReplyClick(rootComment, reply)
      focusCommentInput()
    }

    // 获取用户信息
    const getUserInfo = () => {
      const userStr = localStorage.getItem('userInfo')
      if (userStr) {
        return JSON.parse(userStr)
      }
      return null
    }

    // 加载动态数据
    const loadDiaryCircle = async () => {
      const circleId = route.params.id
      if (!circleId) {
        ElMessage.error('动态 ID 不存在')
        router.push(backRoute.value)
        return
      }

      try {
        const user = getUserInfo()
        currentUserId.value = user?.id || null
        const res = await getDiaryCircleById(circleId, currentUserId.value)
        if (res && res.success) {
          diary.value = res.data
          isDiaryOwner.value = !!(currentUserId.value && diary.value.userId === currentUserId.value)
          // 加载评论
          loadComments(circleId, currentUserId.value)
        } else {
          ElMessage.error(res?.message || '加载动态失败')
          router.push(backRoute.value)
        }
      } catch (e) {
        console.error('加载动态错误:', e)
        ElMessage.error('加载动态失败：' + (e.message || '网络错误'))
        router.push(backRoute.value)
      }
    }

    // 加载评论
    const loadComments = async (circleId, userId) => {
      try {
        const res = await getComments(circleId, userId)
        if (res && res.success) {
          commentList.value = res.data || []
        }
      } catch (e) {
        console.error('加载评论失败:', e)
      }
    }

    // 发表评论
    const submitComment = async () => {
      if (!newComment.value.trim()) {
        ElMessage.warning('请输入评论内容')
        return
      }
      
      const user = getUserInfo()
      if (!user || !user.id) {
        ElMessage.error('请先登录')
        return
      }

      const parentId = replyTarget.value?.parentId ?? null
      const rootComment = replyTarget.value?.rootComment ?? null

      try {
        const res = await addComment({
          circleId: diary.value.id,
          userId: user.id,
          content: newComment.value,
          parentId,
          replyToUserId: replyTarget.value?.replyToUserId ?? null
        })
        
        if (res && res.success) {
          appendSubmittedComment(commentList.value, res.data, user, parentId, rootComment)
          newComment.value = ''
          diary.value.commentCount = (diary.value.commentCount || 0) + 1
          ElMessage.success(parentId ? '回复成功' : '评论成功')
        } else if (isUserBannedResponse(res)) {
          await showUserBanAlert(res)
        } else {
          ElMessage.error(res?.message || '评论失败')
        }
      } catch (e) {
        console.error('评论失败:', e)
        ElMessage.error('评论失败：' + (e.message || '网络错误'))
      }
    }

    // 点赞评论
    const likeComment = (comment) => {
      const user = getUserInfo()
      if (!user || !user.id) {
        ElMessage.warning('请先登录')
        return
      }
      
      const isLiked = !comment.isLiked
      toggleCommentLike(comment.id, isLiked, user.id).then(res => {
        if (res && res.success) {
          comment.isLiked = isLiked
          if (isLiked) {
            comment.likeCount = (comment.likeCount || 0) + 1
          } else {
            comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
          }
        } else {
          ElMessage.error(res?.message || '操作失败')
        }
      }).catch(err => {
        console.error('点赞评论失败:', err)
        ElMessage.error('操作失败')
      })
    }
    
    // 点赞动态
    const toggleDiaryLike = () => {
      const user = getUserInfo()
      if (!user || !user.id) {
        ElMessage.warning('请先登录')
        return
      }
      
      const isLiked = !diary.value.isLiked
      toggleLike(diary.value.id, isLiked, user.id).then(res => {
        if (res && res.success) {
          diary.value.isLiked = isLiked
          if (isLiked) {
            diary.value.likeCount = (diary.value.likeCount || 0) + 1
          } else {
            diary.value.likeCount = Math.max(0, (diary.value.likeCount || 0) - 1)
          }
        } else {
          ElMessage.error(res?.message || '操作失败')
        }
      }).catch(err => {
        console.error('点赞动态失败:', err)
        ElMessage.error('操作失败')
      })
    }

    const canDeleteComment = (comment) => {
      if (!currentUserId.value) return false
      return comment.userId === currentUserId.value || isDiaryOwner.value
    }

    const deleteComment = (comment) => {
      ElMessageBox.confirm('确定要删除这条评论吗？', '删除评论', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await deleteCommentApi(comment.id, currentUserId.value, diary.value.id)
        if (res && res.success) {
          removeCommentFromList(commentList.value, comment)
          diary.value.commentCount = Math.max(0, (diary.value.commentCount || 0) - 1)
          ElMessage.success('评论删除成功')
        } else {
          ElMessage.error(res?.message || '删除失败')
        }
      }).catch(() => {})
    }

    const deleteDiary = () => {
      ElMessageBox.confirm('确定删除这条动态吗？删除后无法恢复。', '删除动态', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await deleteDiaryCircle(diary.value.id, currentUserId.value)
        if (res && res.success) {
          ElMessage.success('动态删除成功')
          router.push(backRoute.value)
        } else {
          ElMessage.error(res?.message || '删除失败')
        }
      }).catch(() => {})
    }

    // 返回
    const goBack = () => {
      goBackToList()
    }

    const applyTheme = () => {
      const userStr = localStorage.getItem('userInfo')
      let themeName = 'default'
      if (userStr) {
        try {
          const u = JSON.parse(userStr)
          if (u.theme) themeName = u.theme
        } catch (e) {
          console.error('解析用户主题失败:', e)
        }
      }
      document.body.className = document.body.className.replace(/theme-\w+/g, '')
      document.body.classList.add(`theme-${themeName}`)
    }

    onMounted(() => {
      applyTheme()
      loadDiaryCircle()
    })

    return {
      diary,
      commentList,
      newComment,
      commentInputRef,
      replyTarget,
      totalCommentCount,
      canReplyTo,
      cancelReply,
      commentInputPlaceholder,
      isReplyMode,
      submitButtonText,
      getVisibleReplies,
      hiddenReplyCount,
      isRepliesExpanded,
      expandReplies,
      collapseReplies,
      onCommentBodyClick,
      onReplyBodyClick,
      getReplyToUserName,
      REPLY_VISIBLE_LIMIT,
      submitComment,
      likeComment,
      toggleDiaryLike,
      canDeleteComment,
      deleteComment,
      isDiaryOwner,
      deleteDiary,
      formatDateTime,
      formatCommentTime,
      goBack,
      backText
    }
  }
}
</script>

<style>
/* 与字迹圈 / 首页一致的主题变量（挂在 body.theme-* 上） */
.theme-default {
  --primary-bg: linear-gradient(90deg, #3d8be4, #fabcac);
  --text-color: #333;
  --bg-color: #fff;
  --border-color: #dee2e6;
  --sidebar-bg: #f8f9fa;
  --calendar-today-bg: #007bff;
  --calendar-event-bg: #ffc107;
  --hover-bg: #f8f9fa;
}

.theme-dark {
  --primary-bg: linear-gradient(90deg, #1a3a5f, #4a2c4a);
  --text-color: #eee;
  --bg-color: #121212;
  --border-color: #333;
  --sidebar-bg: #1e1e1e;
  --calendar-today-bg: #0d6efd;
  --calendar-event-bg: #cc9900;
  --hover-bg: #2d2d2d;
}

.theme-light {
  --primary-bg: linear-gradient(90deg, #a8d0ff, #ffd1dc);
  --text-color: #000;
  --bg-color: #ffffff;
  --border-color: #ccc;
  --sidebar-bg: #fafafa;
  --calendar-today-bg: #007bff;
  --calendar-event-bg: #ffc107;
  --hover-bg: #f0f0f0;
}
</style>

<style scoped>
.circle-detail-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bg-color);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: var(--primary-bg);
  color: #fff;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.back-btn {
  font-size: 1.2rem;
  color: #fff;
}

.back-text {
  color: rgba(255, 255, 255, 0.92);
  font-size: 0.95rem;
}

.header-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 500;
  color: #fff;
}

.header-delete-btn {
  border: none;
  border-radius: 6px;
  background: #ff4d4f;
  color: #fff;
  padding: 0.4rem 0.75rem;
  cursor: pointer;
}

.detail-main {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
}

.detail-content {
  max-width: 800px;
  margin: 0 auto;
  background-color: var(--bg-color);
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}

.publisher-info {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.publisher-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 1rem;
}

.publisher-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.publisher-name {
  font-weight: 600;
  color: var(--text-color);
  font-size: 1rem;
}

.publish-time {
  font-size: 0.85rem;
  color: var(--text-color);
  opacity: 0.65;
}

.diary-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-color);
  margin-bottom: 2rem;
  word-break: break-word;
}

.diary-rich-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 0.5rem 0;
}

.diary-rich-content :deep(p) {
  margin: 0 0 1rem;
}

.diary-rich-content :deep(a) {
  color: var(--calendar-today-bg);
}

.diary-stats {
  display: flex;
  gap: 2rem;
  padding: 1rem 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 2rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-color);
  opacity: 0.8;
  font-size: 0.9rem;
}

.stat-like {
  cursor: pointer;
  padding: 0.35rem 0.75rem;
  border-radius: 16px;
  background-color: var(--hover-bg);
  transition: background-color 0.15s, color 0.15s;
}

.stat-like:hover {
  opacity: 1;
}

.stat-like.active {
  background-color: var(--calendar-event-bg);
  color: #fff;
  opacity: 1;
}

.icon-like,
.icon-comment {
  font-style: normal;
}

/* 评论区样式 */
.comment-section {
  margin-top: 2rem;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.comment-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--text-color);
}

.comment-count {
  font-size: 0.9rem;
  color: var(--text-color);
  opacity: 0.75;
}

.comment-list {
  max-height: 500px;
  overflow-y: auto;
  margin-bottom: 1.5rem;
}

.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.comment-header-row,
.reply-header-row {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.comment-avatar,
.reply-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 0.75rem;
}

.comment-user-info,
.reply-user-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.comment-user-name,
.reply-user-name {
  font-weight: 600;
  color: var(--text-color);
  font-size: 14px;
}

.reply-user-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.35rem;
}

.reply-relation-text {
  font-size: 13px;
  color: var(--text-color);
  opacity: 0.55;
  font-weight: normal;
}

.reply-to-user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--calendar-today-bg);
}

.comment-time,
.reply-time {
  font-size: 12px;
  color: var(--text-color);
  opacity: 0.6;
}

.comment-content,
.reply-content {
  color: var(--text-color);
  line-height: 1.6;
  font-size: 14px;
  margin-bottom: 0.5rem;
}

.comment-content.is-replyable,
.reply-content.is-replyable {
  cursor: pointer;
  border-radius: 6px;
  padding: 0.25rem 0.35rem;
  margin-left: -0.35rem;
  transition: background-color 0.15s;
}

.comment-content.is-replyable:hover,
.reply-content.is-replyable:hover {
  background-color: var(--hover-bg);
}

.reply-item {
  display: flex;
  align-items: flex-start;
  gap: 0.35rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--border-color);
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-toggle-btn {
  display: block;
  width: 100%;
  margin-top: 0.5rem;
  padding: 0.35rem 0;
  border: none;
  background: none;
  color: var(--calendar-today-bg);
  font-size: 13px;
  cursor: pointer;
  text-align: left;
}

.reply-toggle-btn:hover {
  text-decoration: underline;
}

.publish-comment-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.reply-mode-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-color);
  opacity: 0.85;
}

.cancel-reply-btn {
  border: none;
  background: none;
  color: var(--calendar-today-bg);
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.cancel-reply-btn:hover {
  text-decoration: underline;
}

.comment-action-btn {
  padding: 0.25rem 0.75rem;
  background-color: var(--hover-bg);
  border: none;
  border-radius: 16px;
  font-size: 12px;
  color: var(--text-color);
  opacity: 0.9;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-action-btn:hover {
  opacity: 1;
  filter: brightness(0.95);
}

.delete-btn {
  background-color: #fff1f0;
  color: #cf1322;
}

.reply-list {
  margin-top: 1rem;
  padding-left: 3rem;
  background-color: var(--sidebar-bg);
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid var(--border-color);
}

.reply-item:last-child {
  border-bottom: none;
}

.empty-comments {
  text-align: center;
  padding: 2rem 1rem;
  color: var(--text-color);
  opacity: 0.55;
}

.publish-comment-box {
  display: flex;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
  margin-top: 1rem;
}

.comment-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.comment-input:focus {
  outline: none;
  border-color: var(--calendar-today-bg);
}

.submit-comment-btn {
  padding: 0.75rem 1.5rem;
  background-color: var(--calendar-today-bg);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-comment-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
}

.submit-comment-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-main {
    padding: 1rem;
  }

  .detail-content {
    padding: 1.5rem;
  }

  .diary-stats {
    gap: 1rem;
  }
}
</style>
