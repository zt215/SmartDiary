<template>
  <div class="diary-circle-container">
    <!-- 顶部导航栏 -->
    <header class="circle-header">
      <div class="header-left" @click="goBack">
        <span class="back-btn">&larr;</span>
        <span class="back-text">回到我的字迹</span>
      </div>
      <h1 class="header-title">字迹圈</h1>
      <div class="header-right">
        <button class="publish-btn-top" @click="showPublishOptions">
          <i class="icon-publish">+</i> 发布
        </button>
      </div>
    </header>

    <div class="feed-filter-bar">
      <button
        type="button"
        class="filter-btn"
        :class="{ active: feedFilter === 'all' }"
        @click="switchFeedFilter('all')"
      >
        全部
      </button>
      <button
        type="button"
        class="filter-btn"
        :class="{ active: feedFilter === 'friends' }"
        @click="switchFeedFilter('friends')"
      >
        好友
      </button>
    </div>

    <div class="circle-body-scroll">
      <!-- 日记列表 -->
      <div class="diary-list">
        <div 
          v-for="diary in diaryList" 
          :key="diary.id" 
          class="diary-item"
          role="button"
          tabindex="0"
          @click="viewComments(diary)"
          @keydown.enter.prevent="viewComments(diary)"
        >
          <div class="diary-header">
            <img :src="diary.userAvatar" :alt="diary.userName" class="diary-user-avatar" />
            <div class="diary-user-info">
              <span class="diary-user-name">{{ diary.userName }}</span>
              <span class="diary-time">{{ formatDateTime(diary.createTime) }}</span>
            </div>
          </div>
          <div class="diary-content">
            <div
              v-if="hasRichTextContent(diary.content)"
              class="diary-text diary-rich-content"
              v-html="diary.content"
            ></div>
            <p v-else class="diary-text">无内容</p>
          </div>
          <div class="diary-footer">
            <div class="diary-stats">
              <span class="stat-item">
                <i class="icon-like"></i> {{ diary.likeCount || 0 }}
              </span>
              <span class="stat-item">
                <i class="icon-comment"></i> {{ diary.commentCount || 0 }}
              </span>
            </div>
            <div class="diary-actions">
              <button 
                class="action-btn" 
                :class="{ active: diary.isLiked }"
                @click.stop="toggleLike(diary)"
              >
                <i class="icon-like"></i> {{ diary.isLiked ? '已赞' : '赞' }}
              </button>
              <button type="button" class="action-btn" @click.stop="viewComments(diary)">
                <i class="icon-comment"></i> 评论
              </button>
            </div>
          </div>
        </div>

        <!-- 空状态提示 -->
        <div v-if="diaryList.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <p class="empty-text">{{ emptyText }}</p>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <button @click="loadMore" class="load-more-btn">加载更多</button>
      </div>
    </div>

    <el-dialog
      v-model="showPublishBox"
      title="发布动态"
      width="680px"
      top="6vh"
      append-to-body
      destroy-on-close
      class="publish-dialog"
      :close-on-click-modal="false"
      @opened="onPublishDialogOpened"
      @closed="onPublishDialogClosed"
    >
      <p v-if="publishSourceTitle" class="publish-source-tip">
        来自日记「{{ publishSourceTitle }}」，此处修改不会影响原日记
      </p>
      <div ref="publishEditor" class="publish-quill-wrap"></div>
      <template #footer>
        <div class="publish-dialog-footer">
          <el-button @click="hidePublishBox">取消</el-button>
          <el-button type="primary" @click="publishDiary">发布</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showDiaryPicker"
      title="选择要发布的日记"
      width="560px"
      destroy-on-close
      @closed="resetPickerFilters"
    >
      <div v-if="pickerLoading" class="picker-status">加载中...</div>
      <template v-else>
        <div class="picker-filters">
          <el-date-picker
            v-model="pickerFilterDate"
            type="date"
            placeholder="按日期筛选"
            value-format="YYYY-MM-DD"
            clearable
            class="picker-date-input"
          />
          <el-input
            v-model="pickerKeyword"
            placeholder="搜索标题或正文"
            clearable
            class="picker-keyword-input"
          />
        </div>
        <div v-if="allMyDiaries.length === 0" class="picker-status">暂无日记，请先在首页写日记</div>
        <div v-else-if="filteredPickerDiaries.length === 0" class="picker-status">没有符合条件的日记</div>
        <ul v-else class="diary-picker-list">
          <li
            v-for="d in filteredPickerDiaries"
            :key="d.id"
            class="diary-picker-item"
            @click="selectDiaryForPublish(d)"
          >
            <strong>{{ d.title || '无标题' }}</strong>
            <span class="picker-date">{{ formatPickerDate(d.createTime) }}</span>
            <p>{{ getCircleListPreview(d.content) }}</p>
          </li>
        </ul>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import Quill from 'quill';
import 'quill/dist/quill.snow.css';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getDiaryCircleList, publishDiaryCircle, toggleLike } from '../api/diaryCircle';
import { getDiariesByUserId } from '../api/diary';
import {
  hasRichTextContent,
  getCircleListPreview,
  buildCircleContentFromDiary,
  stripHtml,
  createQuillModules
} from '../utils/richContent';
import { formatDateTime, formatDateOnly, toDateKey } from '../utils/dateFormat';

export default {
  name: 'DiaryCircle',
  data() {
    return {
      user: {
        id: null,
        name: '用户',
        avatar: '/path/to/avatar.jpg'
      },
      publishHtml: '',
      publishQuill: null,
      showDiaryPicker: false,
      allMyDiaries: [],
      pickerFilterDate: '',
      pickerKeyword: '',
      pickerLoading: false,
      publishSourceTitle: '',
      diaryList: [],
      page: 1,
      pageSize: 10,
      hasMore: true,
      loading: false,
      showPublishBox: false,
      pendingPublishHtml: '',
      feedFilter: 'all'
    };
  },
  computed: {
    emptyText () {
      if (this.feedFilter === 'friends') {
        return '好友还没有发布动态，去添加好友或看看全部动态吧'
      }
      return '暂无动态，快来发布第一篇日记吧！'
    },
    filteredPickerDiaries () {
      let list = this.allMyDiaries
      if (this.pickerFilterDate) {
        list = list.filter((d) => this.getDiaryDateKey(d.createTime) === this.pickerFilterDate)
      }
      const kw = (this.pickerKeyword || '').trim().toLowerCase()
      if (kw) {
        list = list.filter((d) => {
          const title = (d.title || '').toLowerCase()
          const body = stripHtml(d.content || '').toLowerCase()
          return title.includes(kw) || body.includes(kw)
        })
      }
      return list
    }
  },
  beforeUnmount () {
    this.destroyPublishEditor()
  },
  mounted() {
    // 获取用户信息
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
      try {
        this.user = JSON.parse(userInfo);
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    
    // 恢复用户主题
    const userTheme = this.user.theme;
    if (userTheme) {
      this.changeTheme(userTheme);
      console.log('使用主题:', userTheme);
    } else {
      this.changeTheme('default');
    }
    
    // 加载日记列表
    this.loadDiaries();
  },
  methods: {
    goBack() {
      this.$router.push('/home');
    },
    switchFeedFilter (filter) {
      if (this.feedFilter === filter) return
      this.feedFilter = filter
      this.page = 1
      this.diaryList = []
      this.hasMore = true
      this.loadDiaries()
    },
    showPublishOptions() {
      ElMessageBox.confirm(
        '您想如何发布内容？',
        '选择发布方式',
        {
          confirmButtonText: '新建发布',
          cancelButtonText: '使用已有日记',
          distinguishCancelAndClose: true,
          type: 'info'
        }
      ).then(() => {
        this.openPublishWithDraft('', '');
      }).catch((action) => {
        if (action === 'cancel') {
          // 点击"使用已有日记"
          this.selectFromDiaries();
        }
      });
    },
    hidePublishBox() {
      this.showPublishBox = false;
    },
    onPublishDialogOpened () {
      this.$nextTick(() => {
        this.destroyPublishEditor();
        this.$nextTick(() => {
          this.initPublishEditor(this.pendingPublishHtml || '');
        });
      });
    },
    onPublishDialogClosed () {
      this.publishSourceTitle = '';
      this.pendingPublishHtml = '';
      this.destroyPublishEditor();
    },
    openPublishWithDraft (html = '', sourceTitle = '') {
      this.pendingPublishHtml = html;
      this.publishSourceTitle = sourceTitle;
      this.showPublishBox = true;
    },
    initPublishEditor (initialHtml = '') {
      const el = this.$refs.publishEditor
      if (!el || this.publishQuill) return
      this.publishQuill = new Quill(el, {
        theme: 'snow',
        placeholder: '分享你的心情，可插入图片、链接...',
        modules: createQuillModules((msg) => ElMessage.warning(msg))
      })
      const syncHtml = () => {
        this.publishHtml = this.publishQuill.root.innerHTML
      }
      this.publishQuill.on('text-change', syncHtml)
      if (initialHtml) {
        this.publishQuill.root.innerHTML = initialHtml
      }
      syncHtml()
    },
    resetPickerFilters () {
      this.pickerFilterDate = ''
      this.pickerKeyword = ''
    },
    getDiaryDateKey (time) {
      return toDateKey(time)
    },
    getPublishContent () {
      if (this.publishQuill) {
        return this.publishQuill.root.innerHTML
      }
      return this.publishHtml
    },
    destroyPublishEditor () {
      if (this.$refs.publishEditor) {
        this.$refs.publishEditor.innerHTML = ''
      }
      this.publishQuill = null
      this.publishHtml = ''
    },
    hasRichTextContent,
    getCircleListPreview,
    formatPickerDate (time) {
      return formatDateOnly(time)
    },
    formatDateTime,
    async selectFromDiaries () {
      if (!this.user?.id) {
        ElMessage.error('请先登录')
        return
      }
      this.resetPickerFilters()
      this.showDiaryPicker = true
      this.pickerLoading = true
      this.allMyDiaries = []
      try {
        const res = await getDiariesByUserId(this.user.id)
        if (res?.success) {
          this.allMyDiaries = (res.data || []).sort(
            (a, b) => new Date(b.createTime) - new Date(a.createTime)
          )
        } else {
          ElMessage.error(res?.message || '加载日记失败')
          this.showDiaryPicker = false
        }
      } catch (e) {
        console.error(e)
        ElMessage.error('加载日记失败')
        this.showDiaryPicker = false
      } finally {
        this.pickerLoading = false
      }
    },
    selectDiaryForPublish (diary) {
      const draft = buildCircleContentFromDiary(diary)
      if (!hasRichTextContent(draft)) {
        ElMessage.warning('该日记没有可发布的内容')
        return
      }
      this.showDiaryPicker = false
      this.openPublishWithDraft(draft, diary.title || '无标题')
    },
    async loadDiaries() {
      if (this.loading) return;
      
      this.loading = true;
      try {
        // 获取当前登录用户 ID
        const userStr = localStorage.getItem('userInfo')
        const userId = userStr ? JSON.parse(userStr).id : null
        
        // 调用后端 API 加载日记
        const res = await getDiaryCircleList(this.page, this.pageSize, userId, this.feedFilter);
        
        if (res && res.success) {
          const newList = res.data || [];
          
          // 如果是第一页，清空列表
          if (this.page === 1) {
            this.diaryList = newList.map(item => ({
              ...item
              // isLiked 由后端返回
            }));
          } else {
            // 加载更多数据
            this.diaryList.push(...newList.map(item => ({
              ...item
              // isLiked 由后端返回
            })));
          }
          
          // 判断是否还有更多数据
          this.hasMore = newList.length === this.pageSize;
        } else {
          ElMessage.error(res?.message || '加载失败');
        }
      } catch (e) {
        console.error('加载日记失败:', e);
        ElMessage.error('加载失败：' + (e.message || '网络错误'));
      } finally {
        this.loading = false;
      }
    },
    async publishDiary() {
      const content = this.getPublishContent()
      if (!hasRichTextContent(content)) {
        ElMessage.warning('请输入文字或插入图片后再发布');
        return;
      }
      
      if (!this.user || !this.user.id) {
        ElMessage.error('请先登录');
        return;
      }
      
      try {
        const res = await publishDiaryCircle({
          userId: this.user.id,
          content
        });
        
        if (res && res.success) {
          // 添加到列表
          const newDiary = {
            ...res.data,
            createTime: '刚刚',
            likeCount: 0,
            commentCount: 0,
            isLiked: false
          };
          
          if (this.feedFilter === 'all') {
            this.diaryList.unshift(newDiary);
          }
          this.hidePublishBox();
          
          ElMessage.success(this.feedFilter === 'friends' ? '发布成功，可在「全部」中查看' : '发布成功');
        } else {
          ElMessage.error(res?.message || '发布失败');
        }
      } catch (e) {
        console.error('发布失败:', e);
        ElMessage.error('发布失败：' + (e.message || '网络错误'));
      }
    },
    toggleLike(diary) {
      if (!this.user || !this.user.id) {
        ElMessage.warning('请先登录');
        return;
      }
      
      // 调用后端 API 点赞
      toggleLike(diary.id, !diary.isLiked, this.user.id).then(res => {
        if (res && res.success) {
          diary.isLiked = !diary.isLiked;
          if (diary.isLiked) {
            diary.likeCount = (diary.likeCount || 0) + 1;
          } else {
            diary.likeCount = Math.max(0, (diary.likeCount || 0) - 1);
          }
        } else {
          ElMessage.error(res?.message || '操作失败');
        }
      }).catch(err => {
        console.error('点赞失败:', err);
        ElMessage.error('操作失败');
      });
    },
    viewComments(diary) {
      // 跳转到动态详情页
      this.$router.push(`/diary-circle/${diary.id}`);
    },
    loadMore() {
      this.page++;
      this.loadDiaries();
    },
    changeTheme(themeName) {
      // 更新当前主题
      document.body.className = document.body.className.replace(/theme-\w+/g, '');
      document.body.classList.add(`theme-${themeName}`);
    }
  }
};
</script>

<style>
/* 全局主题变量定义 */
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

/* 发布弹窗（append-to-body，需全局样式） */
.el-dialog.publish-dialog {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 12vh);
  margin-bottom: 6vh;
  overflow: hidden;
}

.el-dialog.publish-dialog .el-dialog__header {
  flex-shrink: 0;
}

.el-dialog.publish-dialog .el-dialog__body {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding-top: 8px;
  padding-bottom: 12px;
}

.el-dialog.publish-dialog .el-dialog__footer {
  flex-shrink: 0;
  padding: 12px 20px 18px;
  border-top: 1px solid #eee;
}

.publish-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.publish-dialog .publish-source-tip {
  margin: 0 0 12px;
  font-size: 13px;
  color: #666;
}

.publish-dialog .publish-quill-wrap {
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background: #fff;
}

.publish-dialog .publish-quill-wrap .ql-toolbar {
  border: none;
  border-bottom: 1px solid #dee2e6;
}

.publish-dialog .publish-quill-wrap .ql-container {
  border: none;
  font-size: 14px;
  max-height: 42vh;
}

.publish-dialog .publish-quill-wrap .ql-editor {
  min-height: 140px;
  max-height: 42vh;
  overflow-y: auto;
}
</style>

<style scoped>
.diary-circle-container {
  height: 100vh;
  background-color: var(--bg-color);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.circle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: var(--primary-bg);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
}

.feed-filter-bar {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem;
  background: var(--bg-color, #fff);
  border-bottom: 1px solid var(--border-color, #e8e8e8);
  flex-shrink: 0;
}

.filter-btn {
  padding: 0.4rem 1.25rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 20px;
  background: transparent;
  color: var(--text-color, #333);
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.15s, color 0.15s, border-color 0.15s;
}

.filter-btn.active {
  background: var(--calendar-today-bg, #3d8be4);
  border-color: var(--calendar-today-bg, #3d8be4);
  color: #fff;
}

.filter-btn:hover:not(.active) {
  background: var(--hover-bg, #f0f0f0);
}

.circle-body-scroll {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.2s;
}

.header-left:hover {
  opacity: 0.8;
}

.back-btn {
  font-size: 24px;
  margin-right: 8px;
}

.back-text {
  font-size: 16px;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.publish-btn-top {
  padding: 0.5rem 1.25rem;
  background-color: var(--calendar-today-bg);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.publish-btn-top:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.icon-publish {
  font-size: 16px;
  font-weight: bold;
}

.picker-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.picker-date-input {
  width: 160px;
}

.picker-keyword-input {
  flex: 1;
  min-width: 180px;
}

.diary-rich-content {
  line-height: 1.6;
  font-size: 15px;
  word-break: break-word;
  max-height: 280px;
  overflow: hidden;
}

.diary-rich-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 0.25rem 0;
}

.diary-rich-content :deep(p) {
  margin: 0 0 0.5rem;
}

.diary-picker-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 360px;
  overflow-y: auto;
}

.diary-picker-item {
  padding: 0.75rem;
  border-bottom: 1px solid var(--border-color, #eee);
  cursor: pointer;
  transition: background-color 0.15s;
}

.diary-picker-item:hover {
  background: var(--hover-bg, #f5f5f5);
}

.diary-picker-item strong {
  display: block;
  margin-bottom: 0.25rem;
}

.picker-date {
  font-size: 12px;
  color: #999;
  margin-left: 0.5rem;
}

.diary-picker-item p {
  margin: 0.35rem 0 0;
  font-size: 13px;
  color: #666;
  line-height: 1.4;
}

.picker-status {
  text-align: center;
  padding: 1.5rem;
  color: #999;
}

/* 日记列表 */
.diary-list {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1rem;
}

.diary-item {
  background-color: var(--bg-color);
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.2s;
  cursor: pointer;
}

.diary-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.diary-header {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.diary-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 0.75rem;
}

.diary-user-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.diary-user-name {
  font-weight: 600;
  color: var(--text-color);
  font-size: 15px;
}

.diary-time {
  font-size: 12px;
  color: #999;
}

.diary-content {
  margin-bottom: 1rem;
}

.diary-text {
  line-height: 1.6;
  color: var(--text-color);
  font-size: 15px;
  white-space: pre-wrap;
  word-break: break-word;
}

.diary-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.diary-stats {
  display: flex;
  gap: 1rem;
}

.stat-item {
  font-size: 13px;
  color: var(--text-color);
  opacity: 0.7;
}

.icon-like,
.icon-comment {
  margin-right: 4px;
}

.diary-actions {
  display: flex;
  gap: 0.75rem;
}

.action-btn {
  padding: 0.4rem 1rem;
  background-color: var(--hover-bg);
  border: none;
  border-radius: 16px;
  font-size: 13px;
  color: var(--text-color);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn:hover {
  opacity: 0.8;
}

.action-btn.active {
  background-color: var(--calendar-event-bg);
  color: #fff;
}

.action-btn.active:hover {
  opacity: 0.9;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 1rem;
}

.empty-text {
  color: var(--text-color);
  opacity: 0.6;
  font-size: 16px;
}

/* 加载更多 */
.load-more {
  text-align: center;
  margin-top: 1.5rem;
}

.load-more-btn {
  padding: 0.75rem 2rem;
  background-color: var(--bg-color);
  color: var(--calendar-today-bg);
  border: 1px solid var(--calendar-today-bg);
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.load-more-btn:hover {
  background-color: #3d8be4;
  color: white;
}
</style>
