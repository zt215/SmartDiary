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
            <span class="diary-date">{{ formatDateTime(diary.createTime) }}</span>
            <span v-if="diary.updateTime && diary.updateTime !== diary.createTime" class="diary-update-time">
              更新于 {{ formatDateTime(diary.updateTime) }}
            </span>
          </div>
        </div>

        <!-- 内容显示 -->
        <div class="diary-content" v-html="diary.content"></div>
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
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import { getDiaryById, updateDiary, deleteDiary as deleteDiaryApi } from '../api/diary'
import { formatDateTime } from '../utils/dateFormat'
import { createQuillModules } from '../utils/richContent'

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
    const isOwner = ref(false)
    const currentUserId = ref(null)

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

    // 初始化编辑器
    const initEditor = () => {
      if (editorContainer.value && !quillEditor.value) {
        quillEditor.value = new Quill(editorContainer.value, {
          theme: 'snow',
          placeholder: '开始记录你的心情吧...',
          modules: createQuillModules((msg) => ElMessage.warning(msg))
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
      enterEditMode,
      cancelEdit,
      saveDiary,
      deleteDiary,
      goBack,
      formatDateTime
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
</style>
