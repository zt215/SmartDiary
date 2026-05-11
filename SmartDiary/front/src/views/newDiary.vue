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
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import { createDiary } from '../api/diary'

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

    // 初始化编辑器
    onMounted(() => {
      if (editorContainer.value) {
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
          diaryForm.value.content = quillEditor.value.root.innerHTML
        })

        // 从localStorage恢复草稿（可选）
        const draft = localStorage.getItem('diaryDraft')
        if (draft) {
          try {
            const draftData = JSON.parse(draft)
            diaryForm.value.title = draftData.title || ''
            // 如果有路由携带日期，则优先使用该日期；否则使用草稿日期
            if (!hasQDate) {
              diaryForm.value.date = draftData.date || new Date().toISOString().slice(0, 10)
            }
            if (draftData.content) {
              quillEditor.value.root.innerHTML = draftData.content
            }
          } catch (e) {
            console.error('恢复草稿失败:', e)
          }
        }
      }
    })

    // 清理
    onBeforeUnmount(() => {
      if (quillEditor.value) {
        quillEditor.value = null
      }
    })

    // 返回
    const goBack = () => {
      router.back()
    }

    // 保存草稿
    const saveDraft = () => {
      const draftData = {
        title: diaryForm.value.title,
        content: diaryForm.value.content,
        date: diaryForm.value.date
      }
      localStorage.setItem('diaryDraft', JSON.stringify(draftData))
      ElMessage.success('草稿已保存')
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
          // 清除草稿
          localStorage.removeItem('diaryDraft')
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
