<template>
  <div class="new-diary-container">
    <!-- 顶部工具栏 -->
    <header class="editor-header">
      <div class="header-left">
        <el-button text @click="goBack">
          ← 返回
        </el-button>
      </div>
      <div class="header-center">
        <h2 class="editor-title">新建日记</h2>
      </div>
      <div class="header-right">
        <span v-if="draftHint" class="draft-hint">{{ draftHint }}</span>
        <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="saveDiary" :loading="saving">保存</el-button>
      </div>
    </header>

    <!-- 编辑区域 -->
    <main class="editor-main">
      <!-- 标题输入 -->
      <div class="title-input-container">
        <el-input
          v-model="diaryForm.title"
          placeholder="请输入日记标题..."
          class="title-input"
          size="large"
        />
      </div>

    <!-- 日期选择（可不是写今日） -->
    <div class="date-picker-container">
      <el-date-picker
        v-model="diaryForm.date"
        type="date"
        placeholder="选择日记日期"
        :editable="false"
        value-format="YYYY-MM-DD"
      />
    </div>

    <!-- 如果不在今日，则提供回到今日按钮 -->
    <div class="back-to-today-row" v-if="!isTodayDiary">
      <el-button size="small" @click="backToToday">回到今日</el-button>
    </div>

      <!-- 富文本编辑器 -->
      <div class="editor-container">
        <div ref="editorContainer" class="quill-editor"></div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import { createDiary } from '../api/diary'
import { createQuillModules } from '../utils/richContent'
import { loadDiaryDraft, saveDiaryDraft, clearDiaryDraft } from '../utils/diaryDraft'

export default {
  name: 'NewDiaryView',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const editorContainer = ref(null)
    const quillEditor = ref(null)
    const diaryForm = ref({
      title: '',
      content: '',
      // 用于创建时设置 createTime（后端会优先使用传入的 createTime）
      date: new Date().toISOString().slice(0, 10) // YYYY-MM-DD
    })

    // 如果从“按日期查看页面”进入新建日记，则沿用该日期
    const qDate = route.query?.date
    const hasQDate = typeof qDate === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(qDate)
    if (hasQDate) {
      diaryForm.value.date = qDate
    }
    const formatLocalYYYYMMDD = (d) => {
      const yyyy = d.getFullYear()
      const mm = String(d.getMonth() + 1).padStart(2, '0')
      const dd = String(d.getDate()).padStart(2, '0')
      return `${yyyy}-${mm}-${dd}`
    }

    const todayStr = formatLocalYYYYMMDD(new Date())
    const isTodayDiary = computed(() => diaryForm.value.date === todayStr)
    const saving = ref(false)
    const draftHint = ref('')
    /**
     * 草稿策略：
     * - 返回 / 离开页面 / 点「保存草稿」/ 编辑时自动保存 → 写入 diary_draft 表
     * - 点「保存」正式发布 → 写入 diary 表并删除 diary_draft
     */
    const skipDraftPersist = ref(false)

    const getUserId = () => {
      const userStr = localStorage.getItem('userInfo')
      if (!userStr) return null
      try {
        return JSON.parse(userStr).id ?? null
      } catch {
        return null
      }
    }

    const syncFromEditor = () => {
      if (quillEditor.value) {
        diaryForm.value.content = quillEditor.value.root.innerHTML
      }
    }

    let autoSaveTimer = null
    const persistDraft = async (silent = true) => {
      if (skipDraftPersist.value) return false
      const userId = getUserId()
      if (!userId) {
        if (!silent) ElMessage.warning('请先登录后再保存草稿')
        return false
      }
      syncFromEditor()
      try {
        const ok = await saveDiaryDraft(userId, {
          title: diaryForm.value.title,
          content: diaryForm.value.content,
          date: diaryForm.value.date
        })
        if (ok && silent) {
          draftHint.value = '草稿已自动保存'
        } else if (!ok) {
          draftHint.value = ''
        }
        return ok
      } catch {
        if (!silent) ElMessage.error('草稿保存失败，请检查网络')
        return false
      }
    }

    const scheduleAutoSave = () => {
      if (skipDraftPersist.value) return
      clearTimeout(autoSaveTimer)
      autoSaveTimer = setTimeout(() => {
        void persistDraft(true)
      }, 1200)
    }

    // 初始化编辑器
    onMounted(async () => {
      if (editorContainer.value) {
        quillEditor.value = new Quill(editorContainer.value, {
          theme: 'snow',
          placeholder: '开始记录你的心情吧...',
          modules: createQuillModules((msg) => ElMessage.warning(msg))
        })

        quillEditor.value.on('text-change', () => {
          diaryForm.value.content = quillEditor.value.root.innerHTML
          scheduleAutoSave()
        })

        const userId = getUserId()
        if (userId) {
          const draftData = await loadDiaryDraft(userId)
          if (draftData) {
            diaryForm.value.title = draftData.title || ''
            if (!hasQDate && draftData.date) {
              diaryForm.value.date = draftData.date
            }
            if (draftData.content) {
              quillEditor.value.root.innerHTML = draftData.content
              diaryForm.value.content = draftData.content
            }
            draftHint.value = '已恢复上次草稿'
          }
        }
      }
    })

    watch(
      () => [diaryForm.value.title, diaryForm.value.date],
      () => scheduleAutoSave()
    )

    onBeforeUnmount(() => {
      clearTimeout(autoSaveTimer)
      if (!skipDraftPersist.value) {
        void persistDraft(true)
      }
      if (quillEditor.value) {
        quillEditor.value = null
      }
    })

    const goBack = async () => {
      await persistDraft(true)
      router.back()
    }

    const saveDraft = async () => {
      if (await persistDraft(false)) {
        skipDraftPersist.value = true
        clearTimeout(autoSaveTimer)
        ElMessage.success('草稿已保存，下次可继续编辑')
        router.back()
      } else {
        ElMessage.warning('请先输入标题或内容')
      }
    }

    const backToToday = () => {
      diaryForm.value.date = todayStr
    }

    // 保存日记
    const saveDiary = async () => {
      // 验证标题
      if (!diaryForm.value.title || diaryForm.value.title.trim() === '') {
        ElMessage.warning('请输入日记标题')
        return
      }

      // 获取内容
      const content = quillEditor.value ? quillEditor.value.root.innerHTML : diaryForm.value.content
      
      // 验证内容（纯HTML标签也算空内容）
      const textContent = quillEditor.value ? quillEditor.value.getText().trim() : ''
      if (!textContent) {
        ElMessage.warning('请输入日记内容')
        return
      }

      // 获取用户信息
      const userInfo = localStorage.getItem('userInfo')
      if (!userInfo) {
        ElMessage.error('用户信息不存在，请重新登录')
        router.push('/')
        return
      }

      let user
      try {
        user = JSON.parse(userInfo)
      } catch (e) {
        ElMessage.error('用户信息解析失败，请重新登录')
        router.push('/')
        return
      }

      saving.value = true
      try {
        // 为了避免时区导致“日期偏一天”，这里取当天中午作为时间基准
        const localDate = diaryForm.value.date
          ? new Date(diaryForm.value.date + 'T12:00:00')
          : new Date()
        const createTimeIso = localDate.toISOString()

        const res = await createDiary({
          userId: user.id,
          title: diaryForm.value.title.trim(),
          content: content,
          createTime: createTimeIso
        })

        if (res && res.success) {
          ElMessage.success('日记保存成功')
          skipDraftPersist.value = true
          clearTimeout(autoSaveTimer)
          await clearDiaryDraft(user.id)
          // 返回首页
          router.push('/home')
        } else {
          ElMessage.error(res?.message || '保存失败')
        }
      } catch (e) {
        console.error('保存日记错误:', e)
        ElMessage.error('保存失败: ' + (e.message || '网络错误'))
      } finally {
        saving.value = false
      }
    }

    return {
      editorContainer,
      diaryForm,
      saving,
      draftHint,
      goBack,
      saveDraft,
      saveDiary,
      isTodayDiary,
      backToToday
    }
  }
}
</script>

<style scoped>
.new-diary-container {
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

.draft-hint {
  font-size: 12px;
  color: #888;
  max-width: 140px;
  text-align: right;
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

.title-input-container {
  margin-bottom: 1.5rem;
}

.date-picker-container {
  margin-bottom: 1.5rem;
}

.date-picker-container :deep(.el-date-editor) {
  width: 100%;
}

.back-to-today-row {
  margin-top: -1rem;
  margin-bottom: 1.25rem;
  display: flex;
  justify-content: flex-end;
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

  .quill-editor :deep(.ql-editor) {
    padding: 1rem;
    min-height: 300px;
  }
}
</style>
