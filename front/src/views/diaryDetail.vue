<template>
  <div class="diary-detail-container">
    <!-- 顶部工具栏 -->
    <header class="editor-header">
      <div class="header-left">
        <el-button text @click="goBack">
          ← 返回
        </el-button>
      </div>
      <div class="header-center">
        <h2 class="editor-title">{{ isEditing ? '编辑日记' : '日记详情' }}</h2>
      </div>
      <div class="header-right">
        <el-button v-if="!isEditing && isOwner" type="danger" text @click="deleteDiary">
          删除
        </el-button>
        <el-button v-if="!isEditing" @click="enterEditMode">编辑</el-button>
        <template v-else>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="saveDiary" :loading="saving">保存</el-button>
        </template>
      </div>
    </header>

    <!-- 内容区域 -->
    <main class="editor-main">
      <!-- 查看模式 -->
      <div v-if="!isEditing" class="view-mode">
        <!-- 标题和日期 -->
        <div class="diary-header">
          <h1 class="diary-title">{{ diary.title || '无标题' }}</h1>
          <div class="diary-meta">
            <span class="diary-date">{{ formatDate(diary.createTime) }}</span>
            <span v-if="diary.updateTime && diary.updateTime !== diary.createTime" class="diary-update-time">
              更新于 {{ formatDate(diary.updateTime) }}
            </span>
          </div>
        </div>

        <!-- 内容显示 -->
        <div class="diary-content" v-html="diary.content"></div>
        
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
              <div class="comment-footer">
                <button class="comment-action-btn" @click.stop="likeComment(comment)">
                  <i class="icon-like"></i> {{ comment.likeCount || 0 }}
                </button>
                <button 
                  v-if="canDeleteComment(comment)" 
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
                  <div class="reply-header-row">
                    <img :src="reply.userAvatar" :alt="reply.userName" class="reply-avatar" />
                    <div class="reply-user-info">
                      <span class="reply-user-name">{{ reply.userName }}</span>
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
                  <div v-if="canDeleteComment(reply)" class="reply-footer">
                    <button
                      class="comment-action-btn delete-btn"
                      @click.stop="deleteComment(reply)"
                    >
                      删除
                    </button>
                  </div>
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

      <!-- 编辑模式 -->
      <div v-else class="edit-mode">
        <!-- 标题输入 -->
        <div class="title-input-container">
          <el-input
            v-model="editForm.title"
            placeholder="请输入日记标题..."
            class="title-input"
            size="large"
          />
        </div>

        <!-- 富文本编辑器 -->
        <div class="editor-container">
          <div ref="editorContainer" class="quill-editor"></div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import { getDiaryById, updateDiary, deleteDiary as deleteDiaryApi } from '../api/diary'
import { getComments, addComment, toggleCommentLike, deleteComment as deleteCommentApi } from '../api/comment'
import { useCommentReplies, REPLY_VISIBLE_LIMIT } from '../composables/useCommentReplies'

export default {
  name: 'DiaryDetailView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const editorContainer = ref(null)
    const quillEditor = ref(null)
    const diary = ref({
      id: null,
      title: '',
      content: '',
      createTime: null,
      updateTime: null
    })
    const isEditing = ref(false)
    const saving = ref(false)
    const editForm = ref({
      title: '',
      content: ''
    })
    // 评论相关
    const commentList = ref([])
    const newComment = ref('')
    const commentInputRef = ref(null)
    const isOwner = ref(false) // 是否是日记所有者
    const currentUserId = ref(null) // 当前登录用户 ID

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

    // 加载日记数据
    const loadDiary = async () => {
      const diaryId = route.params.id
      if (!diaryId) {
        ElMessage.error('日记 ID 不存在')
        router.push('/home')
        return
      }
    
      try {
        // 获取当前登录用户
        const userStr = localStorage.getItem('userInfo')
        const user = userStr ? JSON.parse(userStr) : null
        currentUserId.value = user ? user.id : null
        
        const res = await getDiaryById(diaryId)
        if (res && res.success) {
          diary.value = res.data
          // 判断是否是日记所有者
          isOwner.value = currentUserId.value && diary.value.userId === currentUserId.value
          
          // 加载评论
          loadComments(diaryId)
        } else {
          ElMessage.error(res?.message || '加载日记失败')
          router.push('/home')
        }
      } catch (e) {
        console.error('加载日记错误:', e)
        ElMessage.error('加载日记失败：' + (e.message || '网络错误'))
        router.push('/home')
      }
    }
        
    // 加载评论
    const loadComments = async (circleId) => {
      try {
        console.log('加载评论，userId:', currentUserId.value)
        const res = await getComments(circleId, currentUserId.value)
        if (res && res.success) {
          commentList.value = res.data || []
          console.log('评论数据:', commentList.value)
          if (commentList.value.length > 0) {
            console.log('第一条评论的 isLiked:', commentList.value[0].isLiked)
          }
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
          
      // 获取用户信息
      const userStr = localStorage.getItem('userInfo')
      if (!userStr) {
        ElMessage.error('请先登录')
        return
      }
          
      const user = JSON.parse(userStr)
          
      const parentId = replyTarget.value?.parentId ?? null
      const rootComment = replyTarget.value?.rootComment ?? null

      try {
        const res = await addComment({
          circleId: diary.value.id,
          userId: user.id,
          content: newComment.value,
          parentId
        })
            
        if (res && res.success) {
          appendSubmittedComment(commentList.value, res.data, user, parentId, rootComment)
          newComment.value = ''
          ElMessage.success(parentId ? '回复成功' : '评论成功')
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
      console.log('点击点赞，评论:', comment)
      console.log('评论 ID:', comment.id)
      console.log('当前 isLiked:', comment.isLiked)
      
      const userStr = localStorage.getItem('userInfo')
      console.log('localStorage userInfo:', userStr)
      
      if (!userStr) {
        ElMessage.warning('请先登录')
        return
      }
          
      const user = JSON.parse(userStr)
      console.log('解析后的用户:', user)
      console.log('用户 ID:', user.id)
      
      const isLiked = !comment.isLiked
      console.log('准备调用 API，isLiked:', isLiked)
      
      toggleCommentLike(comment.id, isLiked, user.id).then(res => {
        console.log('API 返回结果:', res)
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
    
    // 判断是否可以删除评论
    const canDeleteComment = (comment) => {
      if (!currentUserId.value) return false
      // 评论作者可以删除自己的评论，或者日记作者可以删除任何评论
      return comment.userId === currentUserId.value || isOwner.value
    }
    
    // 删除评论
    const deleteComment = (comment) => {
      ElMessageBox.confirm(
        '确定要删除这条评论吗？',
        '删除评论',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          const res = await deleteCommentApi(comment.id, currentUserId.value, diary.value.id)
          if (res && res.success) {
            ElMessage.success('评论已删除')
            // 从评论列表中移除
            removeCommentFromList(commentList.value, comment)
          } else {
            ElMessage.error(res?.message || '删除失败')
          }
        } catch (e) {
          console.error('删除评论失败:', e)
          ElMessage.error('删除失败：' + (e.message || '网络错误'))
        }
      }).catch(() => {
        // 用户取消删除
      })
    }
    
    // 删除日记
    const deleteDiary = () => {
      ElMessageBox.confirm(
        '确定要删除这篇日记吗？删除后无法恢复。',
        '删除日记',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          const res = await deleteDiaryApi(diary.value.id)
          if (res && res.success) {
            ElMessage.success('日记已删除')
            router.push('/home')
          } else {
            ElMessage.error(res?.message || '删除失败')
          }
        } catch (e) {
          console.error('删除日记失败:', e)
          ElMessage.error('删除失败：' + (e.message || '网络错误'))
        }
      }).catch(() => {
        // 用户取消删除
      })
    }
        
    // 格式化评论时间
    const formatCommentTime = (time) => {
      if (!time) return ''
      if (typeof time === 'string') return time
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
          
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return date.toLocaleDateString('zh-CN')
    }

    // 初始化编辑器
    const initEditor = () => {
      if (editorContainer.value && !quillEditor.value) {
        quillEditor.value = new Quill(editorContainer.value, {
          theme: 'snow',
          placeholder: '开始记录你的心情吧...',
          modules: {
            toolbar: [
              [{ 'header': [1, 2, 3, false] }],
              ['bold', 'italic', 'underline', 'strike'],
              [{ 'list': 'ordered'}, { 'list': 'bullet' }],
              [{ 'color': [] }, { 'background': [] }],
              [{ 'align': [] }],
              ['link', 'image'],
              ['clean']
            ]
          }
        })

        // 监听内容变化
        quillEditor.value.on('text-change', () => {
          editForm.value.content = quillEditor.value.root.innerHTML
        })
      }
    }

    // 进入编辑模式
    const enterEditMode = async () => {
      isEditing.value = true
      editForm.value.title = diary.value.title || ''
      editForm.value.content = diary.value.content || ''
      
      await nextTick()
      initEditor()
      
      if (quillEditor.value) {
        quillEditor.value.root.innerHTML = editForm.value.content
      }
    }

    // 取消编辑
    const cancelEdit = () => {
      isEditing.value = false
      editForm.value.title = ''
      editForm.value.content = ''
    }

    // 保存日记
    const saveDiary = async () => {
      // 验证标题
      if (!editForm.value.title || editForm.value.title.trim() === '') {
        ElMessage.warning('请输入日记标题')
        return
      }

      // 获取内容
      const content = quillEditor.value ? quillEditor.value.root.innerHTML : editForm.value.content
      
      // 验证内容（纯HTML标签也算空内容）
      const textContent = quillEditor.value ? quillEditor.value.getText().trim() : ''
      if (!textContent) {
        ElMessage.warning('请输入日记内容')
        return
      }

      saving.value = true
      try {
        const res = await updateDiary({
          id: diary.value.id,
          title: editForm.value.title.trim(),
          content: content
        })

        if (res && res.success) {
          ElMessage.success('日记更新成功')
          // 更新本地数据
          diary.value.title = editForm.value.title.trim()
          diary.value.content = content
          diary.value.updateTime = res.data.updateTime
          // 退出编辑模式
          isEditing.value = false
        } else {
          ElMessage.error(res?.message || '更新失败')
        }
      } catch (e) {
        console.error('更新日记错误:', e)
        ElMessage.error('更新失败: ' + (e.message || '网络错误'))
      } finally {
        saving.value = false
      }
    }

    // 返回
    const goBack = () => {
      const raw = route.query.returnDate
      const rd = Array.isArray(raw) ? raw[0] : raw
      if (typeof rd === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(rd)) {
        router.push({ path: '/home', query: { returnDate: rd } })
      } else {
        router.back()
      }
    }

    // 格式化日期
    const formatDate = (date) => {
      if (!date) return ''
      if (typeof date === 'string') {
        date = new Date(date)
      }
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    onMounted(() => {
      loadDiary()
    })

    onBeforeUnmount(() => {
      if (quillEditor.value) {
        quillEditor.value = null
      }
    })

    return {
      editorContainer,
      diary,
      isEditing,
      isOwner,
      saving,
      editForm,
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
      REPLY_VISIBLE_LIMIT,
      enterEditMode,
      cancelEdit,
      saveDiary,
      deleteDiary,
      goBack,
      formatDate,
      submitComment,
      likeComment,
      canDeleteComment,
      deleteComment,
      formatCommentTime
    }
  }
}
</script>

<style scoped>
.diary-detail-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-center {
  flex: 1;
  text-align: center;
}

.editor-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 500;
  color: #333;
}

.editor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 2rem;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  background-color: #fff;
}

/* 查看模式样式 */
.view-mode {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.diary-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e0e0e0;
}

.diary-title {
  margin: 0 0 1rem 0;
  font-size: 2rem;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.diary-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #666;
}

.diary-date,
.diary-update-time {
  display: flex;
  align-items: center;
}

.diary-content {
  flex: 1;
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  padding: 1rem 0;
  word-wrap: break-word;
}

.diary-content :deep(p) {
  margin: 0 0 1rem 0;
}

.diary-content :deep(h1),
.diary-content :deep(h2),
.diary-content :deep(h3) {
  margin: 1.5rem 0 1rem 0;
  font-weight: 600;
}

.diary-content :deep(h1) {
  font-size: 1.8rem;
}

.diary-content :deep(h2) {
  font-size: 1.5rem;
}

.diary-content :deep(h3) {
  font-size: 1.3rem;
}

.diary-content :deep(ul),
.diary-content :deep(ol) {
  margin: 0 0 1rem 0;
  padding-left: 2rem;
}

.diary-content :deep(li) {
  margin: 0.5rem 0;
}

.diary-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 1rem 0;
  border-radius: 4px;
}

.diary-content :deep(a) {
  color: #409eff;
  text-decoration: none;
}

.diary-content :deep(a:hover) {
  text-decoration: underline;
}

/* 编辑模式样式 */
.edit-mode {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.title-input-container {
  margin-bottom: 1.5rem;
}

.title-input {
  font-size: 1.5rem;
  font-weight: 500;
}

.title-input :deep(.el-input__wrapper) {
  box-shadow: none;
  border-bottom: 2px solid #e0e0e0;
  border-radius: 0;
  padding: 0.5rem 0;
}

.title-input :deep(.el-input__wrapper:hover) {
  box-shadow: none;
  border-bottom-color: #409eff;
}

.title-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: none;
  border-bottom-color: #409eff;
}

.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: #fff;
}

.quill-editor {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.quill-editor :deep(.ql-container) {
  flex: 1;
  overflow-y: auto;
  font-size: 16px;
  line-height: 1.8;
}

.quill-editor :deep(.ql-editor) {
  min-height: 400px;
  padding: 2rem;
}

.quill-editor :deep(.ql-toolbar) {
  border-top: none;
  border-left: none;
  border-right: none;
  border-bottom: 1px solid #e0e0e0;
  padding: 0.75rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .editor-main {
    padding: 1rem;
  }

  .editor-header {
    padding: 0.75rem 1rem;
    flex-wrap: wrap;
  }

  .editor-title {
    font-size: 1.2rem;
    order: 3;
    width: 100%;
    margin-top: 0.5rem;
  }

  .header-right {
    order: 2;
  }

  .diary-title {
    font-size: 1.5rem;
  }

  .quill-editor :deep(.ql-editor) {
    padding: 1rem;
    min-height: 300px;
  }
}

/* 评论区样式 */
.comment-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 2px solid #e0e0e0;
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
  color: #333;
}

.comment-count {
  font-size: 0.9rem;
  color: #666;
}

.comment-list {
  max-height: 500px;
  overflow-y: auto;
  margin-bottom: 1.5rem;
}

.comment-item {
  padding: 1rem;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
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
  color: #333;
  font-size: 14px;
}

.comment-time,
.reply-time {
  font-size: 12px;
  color: #999;
}

.comment-content,
.reply-content {
  color: #333;
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
  background-color: #f0f0f0;
}

.reply-footer {
  display: flex;
  gap: 0.5rem;
}

.reply-toggle-btn {
  display: block;
  width: 100%;
  margin-top: 0.5rem;
  padding: 0.35rem 0;
  border: none;
  background: none;
  color: #409eff;
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
  color: #666;
}

.cancel-reply-btn {
  border: none;
  background: none;
  color: #409eff;
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.cancel-reply-btn:hover {
  text-decoration: underline;
}

.comment-footer {
  display: flex;
  gap: 1rem;
}

.comment-action-btn {
  padding: 0.25rem 0.75rem;
  background-color: #f5f5f5;
  border: none;
  border-radius: 16px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-action-btn:hover {
  background-color: #e0e0e0;
}

.reply-list {
  margin-top: 1rem;
  padding-left: 3rem;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 1rem;
}

.reply-item {
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.reply-item:last-child {
  border-bottom: none;
}

.empty-comments {
  text-align: center;
  padding: 2rem 1rem;
  color: #999;
}

.publish-comment-box {
  display: flex;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e0e0e0;
  margin-top: 1rem;
}

.comment-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  background-color: #fff;
  color: #333;
}

.comment-input:focus {
  outline: none;
  border-color: #409eff;
}

.submit-comment-btn {
  padding: 0.75rem 1.5rem;
  background-color: #409eff;
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

/* 删除按钮样式 */
.comment-action-btn.delete-btn {
  color: var(--el-color-danger);
}

.comment-action-btn.delete-btn:hover {
  background-color: var(--el-color-danger-light-9);
}
</style>
