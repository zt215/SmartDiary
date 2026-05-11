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

    <div class="circle-body-scroll">
      <!-- 发布框 -->
      <div v-if="showPublishBox" class="publish-box">
        <div class="publish-header">
          <h3>发布动态</h3>
          <button class="close-publish-btn" @click="hidePublishBox">
            <span>&times;</span>
          </button>
        </div>
        <div class="publisher-info">
          <img :src="user.avatar" :alt="user.name" class="publisher-avatar" />
          <div class="publisher-content">
            <textarea 
              v-model="newDiaryContent" 
              placeholder="分享你的日记片段..." 
              class="publish-input"
              rows="3"
            ></textarea>
            <div class="publish-actions">
              <button class="publish-btn" @click="publishDiary" :disabled="!newDiaryContent.trim()">
                发布
              </button>
            </div>
          </div>
        </div>
      </div>

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
              <span class="diary-time">{{ diary.createTime }}</span>
            </div>
          </div>
          <div class="diary-content">
            <p class="diary-text">{{ diary.content }}</p>
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
          <p class="empty-text">暂无动态，快来发布第一篇日记吧！</p>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <button @click="loadMore" class="load-more-btn">加载更多</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus';
import { getDiaryCircleList, publishDiaryCircle, toggleLike } from '../api/diaryCircle';

export default {
  name: 'DiaryCircle',
  data() {
    return {
      user: {
        id: null,
        name: '用户',
        avatar: '/path/to/avatar.jpg'
      },
      newDiaryContent: '',
      diaryList: [],
      page: 1,
      pageSize: 10,
      hasMore: true,
      loading: false,
      showPublishBox: false
    };
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
        // 点击"新建发布"
        this.showPublishBox = true;
        this.$nextTick(() => {
          document.querySelector('.publish-input')?.focus();
        });
      }).catch((action) => {
        if (action === 'cancel') {
          // 点击"使用已有日记"
          this.selectFromDiaries();
        }
      });
    },
    hidePublishBox() {
      this.showPublishBox = false;
      this.newDiaryContent = '';
    },
    selectFromDiaries() {
      // TODO: 从用户的日记中选择一篇来发布
      ElMessage.info('功能开发中：从日记列表选择');
      // 后续可以实现：弹出一个对话框，显示用户的日记列表，选择一篇发布到字迹圈
    },
    async loadDiaries() {
      if (this.loading) return;
      
      this.loading = true;
      try {
        // 获取当前登录用户 ID
        const userStr = localStorage.getItem('userInfo')
        const userId = userStr ? JSON.parse(userStr).id : null
        
        // 调用后端 API 加载日记
        const res = await getDiaryCircleList(this.page, this.pageSize, userId);
        
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
      if (!this.newDiaryContent.trim()) {
        ElMessage.warning('请输入内容');
        return;
      }
      
      if (!this.user || !this.user.id) {
        ElMessage.error('请先登录');
        return;
      }
      
      try {
        // 调用后端 API 发布日记
        const res = await publishDiaryCircle({
          userId: this.user.id,
          content: this.newDiaryContent
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
          
          this.diaryList.unshift(newDiary);
          this.newDiaryContent = '';
          this.showPublishBox = false;
          
          ElMessage.success('发布成功');
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

/* 发布框 */
.publish-box {
  background-color: var(--bg-color);
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.publish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--border-color);
}

.publish-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color);
}

.close-publish-btn {
  width: 28px;
  height: 28px;
  border: none;
  background-color: transparent;
  color: var(--text-color);
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-publish-btn:hover {
  background-color: var(--hover-bg);
  opacity: 0.7;
}

.publisher-info {
  display: flex;
  gap: 1rem;
}

.publisher-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.publisher-content {
  flex: 1;
}

.publish-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  transition: border-color 0.2s;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.publish-input:focus {
  outline: none;
  border-color: var(--calendar-today-bg);
}

.publish-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

.publish-btn {
  padding: 0.5rem 1.5rem;
  background-color: var(--calendar-today-bg);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s, transform 0.2s;
}

.publish-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
}

.publish-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
