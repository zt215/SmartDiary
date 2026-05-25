<template>
  <div class="home-container">
    <!-- 顶部用户信息栏 -->
    <header class="top-bar">
      <div class="user-info" @click="goToSettings">
        <img :src="user.avatar" :alt="user.name" class="avatar" />
        <span class="username">{{ user.name }}</span>
      </div>
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索日记..." 
          class="search-input"
        />
        <button class="search-btn" @click="loadDiaries">搜索</button>
      </div>
      <div class="top-functions">
        <button class="function-btn friends-btn" @click="goToFriends">
          <span
            v-if="newFriendDiaryCount > 0"
            class="friend-badge"
            :class="{ 'friend-badge--offset': incomingFriendCount > 0 }"
          >{{ newFriendDiaryBadgeText }}</span>
          <span v-if="incomingFriendCount > 0" class="friend-badge">{{ incomingFriendBadgeText }}</span>
          <span class="friends-btn-text">好友</span>
        </button>
        <button class="function-btn" @click="goToDiaryCircle">字迹圈</button>
        <button class="function-btn" @click="goToSettings">个人设置</button>
        <button class="function-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <!-- 主体内容区域 -->
    <main class="main-content">
      <!-- 左侧日记本区域 -->
      <section class="diary-section">
        <div class="section-header">
          <h2 class="section-title">我的字迹</h2>
          <button class="add-diary-btn" @click="createNewDiary">+ 新建日记</button>
        </div>
        <div class="diary-list">
          <div 
            v-for="diary in filteredDiaries" 
            :key="diary.id" 
            class="diary-item"
          >
            <div class="diary-content-wrapper" @click="viewDiary(diary)">
              <h3 v-html="highlightKeyword(diary.title || '无标题')"></h3>
              <p class="diary-date">{{ formatDate(diary.date || diary.createTime) }}</p>
              <p class="diary-preview" v-html="getPreviewContent(diary.content)"></p>
            </div>
            <!-- 调试信息：显示 userId -->
            <div style="font-size: 12px; color: #999; margin-top: 8px;">
              diary.userId: {{ diary.userId }}, currentUserId: {{ currentUserId }}
            </div>
            <button 
              v-if="isOwner(diary)" 
              class="delete-diary-btn" 
              @click.stop="deleteDiary(diary)"
              title="删除日记"
            >
              <i class="icon-delete"></i>
            </button>
          </div>
        </div>
      </section>

      <!-- 右侧日历和功能栏 -->
      <aside class="sidebar">
        <!-- 日历组件 -->
        <div class="calendar-widget">
          <h2>日历</h2>
          <div class="calendar">
            <div class="calendar-header">
              <div class="calendar-nav-left">
                <button @click="prevYear" class="calendar-nav-btn">&lt;&lt;</button>
                <button @click="prevMonth" class="calendar-nav-btn">&lt;</button>
              </div>
              <span class="month-year-display" @click="toggleYearMonthDropdown">{{ currentMonthYear }}</span>
              <div class="calendar-nav-right">
                <button @click="nextMonth" class="calendar-nav-btn">&gt;</button>
                <button @click="nextYear" class="calendar-nav-btn">&gt;&gt;</button>
              </div>
            </div>
            <div v-if="showYearMonthDropdown" class="year-month-dropdown" @click.stop>
              <div class="ym-panel">
                <div class="ym-year-topbar">
                  <button class="ym-year-nav-btn" @click="changeTempYear(-1)">&lt;</button>
                  <div class="ym-year-top-title">{{ tempYear }}年</div>
                  <button class="ym-year-nav-btn" @click="changeTempYear(1)">&gt;</button>
                </div>

                <div class="ym-months-grid">
                  <div
                    v-for="opt in monthOptions"
                    :key="opt.value"
                    class="ym-month-item"
                    :class="{ active: opt.value === tempMonth }"
                    @click="tempMonth = opt.value"
                  >
                    {{ opt.label }}
                  </div>
                </div>

                <div class="picker-actions ym-actions">
                  <button class="picker-cancel-btn" @click="cancelYearMonthDropdown">取消</button>
                  <button class="picker-confirm-btn" @click="confirmYearMonthDropdown">确定</button>
                </div>
              </div>
            </div>
            <div class="calendar-grid">
              <div class="weekdays">
                <span v-for="day in weekdays" :key="day">{{ day }}</span>
              </div>
              <div class="days">
                <span 
                  v-for="day in daysInMonth" 
                  :key="day.date" 
                  :class="{ 'today': day.isToday && isViewingToday && !day.isSelected, 'has-event': day.hasEvent && !day.isSelected, 'selected': day.isSelected }"
                  @click="selectDate(day.date)"
                >
                  {{ day.day }}
                </span>
              </div>
            </div>
            <!-- 回到今日按钮 -->
            <div v-if="!isViewingToday" class="back-to-today-container">
              <button @click="backToToday" class="back-to-today-btn">回到今日</button>
            </div>
          </div>
        </div>

        <!-- 功能栏 -->
        <div class="functions-panel">
          <h2>功能</h2>
          <ul class="function-list">
            <li @click="openStatistics"><i class="icon-stats"></i>统计分析</li>
            <li @click="openBackup"><i class="icon-backup"></i>数据备份</li>
            <li @click="openImport"><i class="icon-import"></i>导入数据</li>
            <li @click="openThemes"><i class="icon-theme"></i>主题设置</li>
          </ul>
        </div>
      </aside>
    </main>
    
    <!-- 主题设置模态框 -->
    <div v-if="showThemeModal" class="modal-overlay" @click="closeThemeModal">
      <div class="modal-content" @click.stop>
        <h3>选择主题</h3>
        <div class="theme-options">
          <div 
            v-for="theme in themes" 
            :key="theme.name"
            class="theme-option"
            :class="{ active: currentTheme === theme.name }"
            @click="changeTheme(theme.name)"
          >
            <div class="theme-preview" :class="`theme-${theme.name}`">
              <div class="preview-top-bar"></div>
              <div class="preview-content">
                <div class="preview-sidebar"></div>
              </div>
            </div>
            <span>{{ theme.label }}</span>
          </div>
        </div>
        <button class="close-btn" @click="closeThemeModal">关闭</button>
      </div>
    </div>

    <!-- 统计分析模态框 -->
    <div v-if="showStatisticsModal" class="modal-overlay" @click="closeStatisticsModal">
      <div class="modal-content statistics-modal" @click.stop>
        <h3>统计分析</h3>
        <div class="statistics-content">
          <div class="stat-item">
            <span class="stat-label">日记总数：</span>
            <span class="stat-value">{{ statistics.totalCount }} 篇</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">本月日记：</span>
            <span class="stat-value">{{ statistics.thisMonthCount }} 篇</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总字数：</span>
            <span class="stat-value">{{ statistics.totalWords.toLocaleString() }} 字</span>
          </div>
          <div class="stat-item" v-if="statistics.earliestDate">
            <span class="stat-label">最早日记：</span>
            <span class="stat-value">{{ formatDate(statistics.earliestDate) }}</span>
          </div>
          <div class="stat-item" v-if="statistics.latestDate">
            <span class="stat-label">最近日记：</span>
            <span class="stat-value">{{ formatDate(statistics.latestDate) }}</span>
          </div>
          <div class="monthly-stats" v-if="statistics.monthlyCounts.length > 0">
            <h4>各月份统计</h4>
            <div class="monthly-list">
              <div 
                v-for="item in statistics.monthlyCounts" 
                :key="item.month"
                class="monthly-item"
                @click="jumpToMonth(item)"
              >
                <span class="month-label">{{ item.month }}</span>
                <span class="month-count">{{ item.count }} 篇</span>
              </div>
            </div>
          </div>
        </div>
        <button class="close-btn" @click="closeStatisticsModal">关闭</button>
      </div>
    </div>

  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus';
import { getDiariesByUserId, searchDiaries, createDiary, deleteDiary } from '../api/diary';
import { updateTheme } from '../api/auth';
import { listIncomingFriendRequests, listFriends } from '../api/friend';
import { countFriendsWithNewDiary } from '@/utils/friendDiarySeen';

export default {
  name: 'HomeView',
  data() {
    return {
      searchQuery: '',
      isSearching: false, // 标记是否正在进行搜索
      user: {
        id: null,
        name: '用户名',
        avatar: '/path/to/avatar.jpg'
      },
      currentUserId: null, // 当前登录用户 ID
      diaries: [],
      allDiaries: [], // 保存所有日记，用于筛选
      loading: false,
      selectedDate: null, // 选中的日期
      currentMonth: new Date().getMonth(),
      currentYear: new Date().getFullYear(),
      weekdays: ['一', '二', '三', '四', '五', '六', '日'],
      showThemeModal: false,
      showStatisticsModal: false,
      showYearMonthDropdown: false,
      tempYear: new Date().getFullYear(),
      tempMonth: new Date().getMonth(),
      currentTheme: 'default',
      themes: [
        { name: 'default', label: '默认主题' },
        { name: 'dark', label: '暗色主题' },
        { name: 'light', label: '亮色主题' }
      ],
      statistics: {
        totalCount: 0,
        thisMonthCount: 0,
        totalWords: 0,
        earliestDate: null,
        latestDate: null,
        monthlyCounts: []
      },
      incomingFriendCount: 0,
      newFriendDiaryCount: 0
    };
  },
  computed: {
    incomingFriendBadgeText () {
      if (this.incomingFriendCount > 99) return '99+'
      return String(this.incomingFriendCount)
    },
    newFriendDiaryBadgeText () {
      if (this.newFriendDiaryCount > 99) return '99+'
      return String(this.newFriendDiaryCount)
    },
    filteredDiaries() {
      // 只有当用户点击搜索按钮后（isSearching为true），才返回搜索结果
      if (this.isSearching && this.searchQuery && this.searchQuery.trim() !== '') {
        return this.diaries;
      }
      // 如果选中了日期，筛选该日期的日记
      if (this.selectedDate) {
        return this.allDiaries.filter(diary => {
          if (!diary.createTime) return false;
          const diaryDate = new Date(diary.createTime);
          return diaryDate.getDate() === this.selectedDate.getDate() &&
                 diaryDate.getMonth() === this.selectedDate.getMonth() &&
                 diaryDate.getFullYear() === this.selectedDate.getFullYear();
        });
      }
      // 如果没有选中日期，默认显示今天的日记
      const today = new Date();
      return this.allDiaries.filter(diary => {
        if (!diary.createTime) return false;
        const diaryDate = new Date(diary.createTime);
        return diaryDate.getDate() === today.getDate() &&
               diaryDate.getMonth() === today.getMonth() &&
               diaryDate.getFullYear() === today.getFullYear();
      });
    },
    currentMonthYear() {
      const months = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'];
      return `${this.currentYear}年${months[this.currentMonth]}`;
    },
    monthOptions() {
      return Array.from({ length: 12 }, (_, i) => ({
        value: i,
        label: `${i + 1}月`
      }));
    },
    yearOptions() {
      const years = new Set();
      (this.allDiaries || []).forEach(diary => {
        const d = diary?.createTime ? new Date(diary.createTime) : (diary?.date ? new Date(diary.date) : null);
        if (d && !Number.isNaN(d.getTime())) {
          years.add(d.getFullYear());
        }
      });

      // 如果暂时没有日记数据，给一个可选区间，保证不会空列表
      if (years.size === 0) {
        for (let i = -10; i <= 10; i++) {
          years.add(this.currentYear + i);
        }
      }

      return Array.from(years).sort((a, b) => a - b);
    },
    isCurrentMonth() {
      const today = new Date();
      return this.currentYear === today.getFullYear() && 
             this.currentMonth === today.getMonth();
    },
    isViewingToday() {
      // 如果没有选中日期，说明正在查看今天的日记（默认）
      if (!this.selectedDate) {
        return true;
      }
      const today = new Date();
      // 判断选中的日期是否是今天
      return this.selectedDate.getDate() === today.getDate() &&
             this.selectedDate.getMonth() === today.getMonth() &&
             this.selectedDate.getFullYear() === today.getFullYear();
    },
    daysInMonth() {
      // 简化的日历实现
      const days = [];
      const date = new Date(this.currentYear, this.currentMonth + 1, 0);
      const totalDays = date.getDate();
      
      // 获取有日记的日期集合（使用allDiaries，显示所有有日记的日期）
      const diaryDates = new Set();
      this.allDiaries.forEach(diary => {
        if (diary.createTime) {
          const diaryDate = new Date(diary.createTime);
          if (diaryDate.getFullYear() === this.currentYear && diaryDate.getMonth() === this.currentMonth) {
            diaryDates.add(diaryDate.getDate());
          }
        }
      });
      
      for (let i = 1; i <= totalDays; i++) {
        const date = new Date(this.currentYear, this.currentMonth, i);
        const isSelected = this.selectedDate && 
          date.getDate() === this.selectedDate.getDate() &&
          date.getMonth() === this.selectedDate.getMonth() &&
          date.getFullYear() === this.selectedDate.getFullYear();
        days.push({
          day: i,
          date: date,
          isToday: this.isToday(date),
          hasEvent: diaryDates.has(i),
          isSelected: isSelected
        });
      }
      return days;
    }
  },
  mounted() {
    // 检查是否刚登录成功
    if (this.$route.query.login === 'success') {
      ElMessage.success('登录成功！');
      // 移除URL中的查询参数
      this.$router.replace({ query: {} });
    }
    
    // 从 localStorage 获取用户信息
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
      try {
        this.user = JSON.parse(userInfo);
        this.currentUserId = this.user.id; // 保存当前用户 ID
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    
    // 如果用户已登录，加载日记数据（加载完成后再根据 URL 恢复从详情返回的日历日期）
    if (this.user && this.user.id) {
      this.loadDiaries().then(() => this.applyReturnDateFromRoute());
      this.refreshIncomingFriendBadge();
      this.refreshNewFriendDiaryBadge();
    } else {
      ElMessage.warning('请先登录');
      this.$router.push('/');
    }
    
    // 页面加载时恢复用户之前选择的主题
    // 严格使用数据库中用户的主题配置，确保每个账号的主题独立
    const userTheme = this.user.theme;
    if (userTheme) {
      // 如果数据库中有主题配置，直接使用
      this.changeTheme(userTheme);
      console.log('使用数据库中的主题:', userTheme);
    } else {
      // 如果数据库中没有主题配置（老用户），使用默认主题
      this.changeTheme('default');
      console.log('用户使用默认主题');
    }
  },
  methods: {
    async loadDiaries() {
      if (!this.user || !this.user.id) {
        return;
      }
      
      this.loading = true;
      try {
        // 如果搜索，先加载所有日记用于筛选，然后加载搜索结果
        if (this.searchQuery && this.searchQuery.trim() !== '') {
          // 标记为搜索状态
          this.isSearching = true;
          // 加载所有日记（如果还没有加载）
          if (this.allDiaries.length === 0) {
            const allRes = await getDiariesByUserId(this.user.id);
            if (allRes && allRes.success) {
              this.allDiaries = (allRes.data || []).map(diary => ({
                ...diary,
                date: diary.createTime ? new Date(diary.createTime) : new Date()
              }));
            }
          }
          // 加载搜索结果
          const res = await searchDiaries(this.user.id, this.searchQuery);
          if (res && res.success) {
            const diaryList = res.data || [];
            this.diaries = diaryList.map(diary => ({
              ...diary,
              date: diary.createTime ? new Date(diary.createTime) : new Date()
            }));
            // 搜索模式下不设置selectedDate，让filteredDiaries直接返回所有搜索结果
            this.selectedDate = null;
          } else {
            ElMessage.error(res?.message || '搜索失败');
            this.diaries = [];
            this.selectedDate = null;
          }
        } else {
          // 不是搜索模式，标记为非搜索状态
          this.isSearching = false;
          // 加载所有日记
          const res = await getDiariesByUserId(this.user.id);
          if (res && res.success) {
            const diaryList = res.data || [];
            this.diaries = diaryList.map(diary => ({
              ...diary,
              date: diary.createTime ? new Date(diary.createTime) : new Date()
            }));
            // 保存所有日记（用于筛选）
            this.allDiaries = this.diaries;
            // 不在此处清空 selectedDate：从日记详情返回时需要保留/由 applyReturnDateFromRoute 恢复
          } else {
            ElMessage.error(res?.message || '加载日记失败');
          }
        }
      } catch (e) {
        console.error('加载日记错误:', e);
        ElMessage.error('加载日记失败: ' + (e.message || '网络错误'));
      } finally {
        this.loading = false;
      }
    },
    formatDate(date) {
      if (!date) return '';
      if (typeof date === 'string') {
        date = new Date(date);
      }
      return date.toLocaleDateString('zh-CN');
    },
    stripHtml(html) {
      if (!html) return '';
      // 创建临时DOM元素来提取纯文本
      const tmp = document.createElement('div');
      tmp.innerHTML = html;
      const text = tmp.textContent || tmp.innerText || '';
      // 限制显示长度
      return text.length > 50 ? text.substring(0, 50) + '...' : text;
    },
    escapeHtml(text) {
      if (!text) return '';
      const div = document.createElement('div');
      div.textContent = text;
      return div.innerHTML;
    },
    highlightKeyword(text) {
      if (!text || !this.searchQuery || this.searchQuery.trim() === '') {
        return this.escapeHtml(text || '');
      }
      // 转义HTML，防止XSS攻击
      const escapedText = this.escapeHtml(text);
      const keyword = this.escapeHtml(this.searchQuery.trim());
      // 使用正则表达式进行大小写不敏感的替换
      const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
      return escapedText.replace(regex, '<mark>$1</mark>');
    },
    getPreviewContent(content) {
      if (!content) return '无内容';
      // 提取纯文本
      const text = this.stripHtml(content);
      // 如果有搜索查询，高亮关键字
      if (this.searchQuery && this.searchQuery.trim() !== '') {
        return this.highlightKeyword(text);
      }
      return this.escapeHtml(text);
    },
    // 判断是否是日记所有者
    isOwner(diary) {
      console.log('检查日记权限:', diary);
      console.log('currentUserId:', this.currentUserId, 'diary.userId:', diary.userId);
      const result = this.currentUserId && diary.userId === this.currentUserId;
      console.log('isOwner 结果:', result);
      return result;
    },
    // 删除日记
    deleteDiary(diary) {
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
          const res = await deleteDiary(diary.id)
          if (res && res.success) {
            ElMessage.success('日记已删除')
            // 从列表中移除
            this.allDiaries = this.allDiaries.filter(d => d.id !== diary.id)
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
    },
    viewDiary(diary) {
      // 带上返回后要恢复的日历日期，避免从详情返回后回到「今天」
      const d = this.selectedDate
        ? new Date(this.selectedDate)
        : (diary.createTime ? new Date(diary.createTime) : null);
      const query = {};
      if (d && !Number.isNaN(d.getTime())) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        query.returnDate = `${y}-${m}-${day}`;
      }
      this.$router.push({ path: `/diary/${diary.id}`, query });
    },
    applyReturnDateFromRoute() {
      const raw = this.$route.query.returnDate;
      const q = Array.isArray(raw) ? raw[0] : raw;
      if (typeof q !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(q)) {
        return;
      }
      const [y, month, day] = q.split('-').map(Number);
      this.currentYear = y;
      this.currentMonth = month - 1;
      this.selectedDate = new Date(y, month - 1, day);
      this.searchQuery = '';
      this.isSearching = false;
      this.$router.replace({ path: '/home', query: {} });
    },
    createNewDiary() {
      // 根据当前选中日期初始化新建日记日期
      const d = this.selectedDate ? this.selectedDate : new Date();
      const yyyy = d.getFullYear();
      const mm = (d.getMonth() + 1).toString().padStart(2, '0');
      const dd = d.getDate().toString().padStart(2, '0');
      const dateStr = `${yyyy}-${mm}-${dd}`;

      this.$router.push({ path: '/new-diary', query: { date: dateStr } });
    },
    prevMonth() {
      if (this.currentMonth === 0) {
        this.currentMonth = 11;
        this.currentYear--;
      } else {
        this.currentMonth--;
      }
      // 翻页后清空选中日期，避免列表与日历不同步
      this.selectedDate = null;
      this.showYearMonthDropdown = false;
    },
    nextMonth() {
      if (this.currentMonth === 11) {
        this.currentMonth = 0;
        this.currentYear++;
      } else {
        this.currentMonth++;
      }
      // 翻页后清空选中日期，避免列表与日历不同步
      this.selectedDate = null;
      this.showYearMonthDropdown = false;
    },
    prevYear() {
      this.currentYear--;
      this.selectedDate = null;
      this.showYearMonthDropdown = false;
    },
    nextYear() {
      this.currentYear++;
      this.selectedDate = null;
      this.showYearMonthDropdown = false;
    },
    selectDate(date) {
      // 设置选中日期
      this.selectedDate = date;
      // 清空搜索框和搜索状态
      this.searchQuery = '';
      this.isSearching = false;
      this.showYearMonthDropdown = false;
      // 筛选该日期的日记
      // filteredDiaries 计算属性会自动处理
    },
    toggleYearMonthDropdown() {
      if (!this.showYearMonthDropdown) {
        this.tempYear = this.currentYear;
        this.tempMonth = this.currentMonth;
      }
      this.showYearMonthDropdown = !this.showYearMonthDropdown;
    },
    cancelYearMonthDropdown() {
      this.showYearMonthDropdown = false;
    },
    confirmYearMonthDropdown() {
      this.currentYear = this.tempYear;
      this.currentMonth = this.tempMonth;
      this.selectedDate = null;
      this.showYearMonthDropdown = false;
    },
    changeTempYear(delta) {
      const years = this.yearOptions || [];
      if (years.length === 0) return;

      const idx = years.indexOf(this.tempYear);
      const currentIndex = idx >= 0 ? idx : 0;
      let nextIndex = currentIndex + delta;
      if (nextIndex < 0) nextIndex = 0;
      if (nextIndex > years.length - 1) nextIndex = years.length - 1;
      this.tempYear = years[nextIndex];
    },
    backToToday() {
      const today = new Date();
      this.currentYear = today.getFullYear();
      this.currentMonth = today.getMonth();
      // 选中今天的日期（显示今天的日记）
      this.selectedDate = new Date(today.getFullYear(), today.getMonth(), today.getDate());
      // 清空搜索框和搜索状态
      this.searchQuery = '';
      this.isSearching = false;
      this.showYearMonthDropdown = false;
    },
    isToday(date) {
      const today = new Date();
      return date.getDate() === today.getDate() &&
             date.getMonth() === today.getMonth() &&
             date.getFullYear() === today.getFullYear();
    },
    goToSettings() {
      // 跳转到个人设置页面
      this.$router.push('/settings');
    },
    goToDiaryCircle() {
      // 跳转到字迹圈页面
      this.$router.push('/diary-circle');
    },
    goToFriends() {
      this.$router.push('/friends');
    },
    async refreshIncomingFriendBadge () {
      if (!this.currentUserId) return
      try {
        const res = await listIncomingFriendRequests(this.currentUserId)
        if (res && res.success) {
          this.incomingFriendCount = (res.data || []).length
        }
      } catch (e) {
        console.error('加载好友申请数量失败:', e)
      }
    },
    async refreshNewFriendDiaryBadge () {
      if (!this.currentUserId) return
      try {
        const res = await listFriends(this.currentUserId)
        if (res && res.success) {
          this.newFriendDiaryCount = countFriendsWithNewDiary(this.currentUserId, res.data || [])
        }
      } catch (e) {
        console.error('加载好友新字迹提醒失败:', e)
      }
    },
    handleLogout() {
      // 退出登录
      ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        // 清除用户信息和 token
        localStorage.removeItem('userInfo');
        localStorage.removeItem('token');
        // 清除主题缓存，避免影响下一个登录的用户
        localStorage.removeItem('diary-theme');
        
        ElMessage.success('已退出登录');
        // 跳转到登录页
        this.$router.push('/');
      }).catch(() => {
        // 用户取消
      });
    },
    openStatistics() {
      this.showYearMonthDropdown = false;
      // 计算统计数据
      this.calculateStatistics();
      // 打开统计分析模态框
      this.showStatisticsModal = true;
    },
    closeStatisticsModal() {
      this.showStatisticsModal = false;
    },
    jumpToMonth(item) {
      console.log('点击月份:', item);
      console.log('月份字符串:', item.month);
      console.log('月份类型:', typeof item.month);
      
      // 解析月份字符串，格式为 "2026年3月"（无空格）
      const match = item.month.match(/(\d{4})年(\d{1,2})月/);
      console.log('正则匹配结果:', match);
      
      if (match) {
        const year = parseInt(match[1]);
        const month = parseInt(match[2]) - 1; // JavaScript 月份从 0 开始
        
        console.log('跳转年份:', year, '月份:', month);
        
        // 切换日历到对应月份
        this.currentYear = year;
        this.currentMonth = month;
        
        // 找到该月的所有日记
        const monthDiaries = this.allDiaries.filter(diary => {
          const diaryDate = diary.createTime ? new Date(diary.createTime) : new Date(diary.date);
          return diaryDate.getFullYear() === year && diaryDate.getMonth() === month;
        });
        
        // 如果有日记，选中该月最早的日记
        if (monthDiaries.length > 0) {
          // 按时间排序，找到最早的
          const sortedDiaries = monthDiaries.sort((a, b) => {
            const dateA = a.createTime ? new Date(a.createTime) : new Date(a.date);
            const dateB = b.createTime ? new Date(b.createTime) : new Date(b.date);
            return dateA - dateB;
          });
          
          // 选中最早的日记
          const earliestDiary = sortedDiaries[0];
          this.selectedDate = earliestDiary.createTime ? new Date(earliestDiary.createTime) : new Date(earliestDiary.date);
        } else {
          // 如果该月没有日记，清除选中
          this.selectedDate = null;
        }
        
        // 关闭统计弹窗
        this.closeStatisticsModal();
        
        ElMessage.success(`已切换到 ${item.month}`);
      } else {
        console.error('月份格式解析失败，实际格式为:', JSON.stringify(item.month));
        ElMessage.error('月份格式解析失败');
      }
    },
    calculateStatistics() {
      const diaries = this.allDiaries || [];
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth();
      
      // 日记总数
      this.statistics.totalCount = diaries.length;
      
      // 本月日记数
      this.statistics.thisMonthCount = diaries.filter(diary => {
        const diaryDate = diary.createTime ? new Date(diary.createTime) : new Date(diary.date);
        return diaryDate.getFullYear() === currentYear && diaryDate.getMonth() === currentMonth;
      }).length;
      
      // 计算总字数
      this.statistics.totalWords = this.getTotalWordCount(diaries);
      
      // 最早和最近日记日期
      if (diaries.length > 0) {
        const dates = diaries
          .map(diary => diary.createTime ? new Date(diary.createTime) : new Date(diary.date))
          .sort((a, b) => a - b);
        this.statistics.earliestDate = dates[0];
        this.statistics.latestDate = dates[dates.length - 1];
      } else {
        this.statistics.earliestDate = null;
        this.statistics.latestDate = null;
      }
      
      // 按月份统计
      this.statistics.monthlyCounts = this.getMonthlyCounts(diaries);
    },
    getTotalWordCount(diaries) {
      let totalWords = 0;
      diaries.forEach(diary => {
        if (diary.content) {
          // 提取纯文本并计算字数
          const tmp = document.createElement('div');
          tmp.innerHTML = diary.content;
          const text = tmp.textContent || tmp.innerText || '';
          totalWords += text.length;
        }
        if (diary.title) {
          totalWords += diary.title.length;
        }
      });
      return totalWords;
    },
    getMonthlyCounts(diaries) {
      const monthMap = new Map();
      
      diaries.forEach(diary => {
        const diaryDate = diary.createTime ? new Date(diary.createTime) : new Date(diary.date);
        const year = diaryDate.getFullYear();
        const month = diaryDate.getMonth() + 1;
        const key = `${year}-${month.toString().padStart(2, '0')}`;
        const label = `${year}年${month}月`;
        
        if (!monthMap.has(key)) {
          monthMap.set(key, { month: label, count: 0 });
        }
        monthMap.get(key).count++;
      });
      
      // 转换为数组并按时间倒序排序
      return Array.from(monthMap.values())
        .sort((a, b) => {
          const dateA = new Date(a.month.replace('年', '-').replace('月', ''));
          const dateB = new Date(b.month.replace('年', '-').replace('月', ''));
          return dateB - dateA;
        })
        .slice(0, 12); // 只显示最近12个月
    },
    openBackup() {
      this.showYearMonthDropdown = false;
      // 数据备份
      ElMessageBox.confirm(
        '确定要导出所有日记数据吗？',
        '数据备份',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(() => {
        this.exportDiaries();
      }).catch(() => {
        // 用户取消
      });
    },
    exportDiaries() {
      try {
        const diaries = this.allDiaries || [];
        if (diaries.length === 0) {
          ElMessage.warning('没有日记数据可导出');
          return;
        }
        
        // 准备导出数据
        const exportData = {
          exportDate: new Date().toISOString(),
          user: {
            id: this.user.id,
            name: this.user.name
          },
          totalCount: diaries.length,
          diaries: diaries.map(diary => ({
            id: diary.id,
            title: diary.title,
            content: diary.content,
            createTime: diary.createTime,
            updateTime: diary.updateTime
          }))
        };
        
        // 转换为JSON字符串
        const jsonString = JSON.stringify(exportData, null, 2);
        
        // 创建Blob对象
        const blob = new Blob([jsonString], { type: 'application/json;charset=utf-8' });
        
        // 创建下载链接
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        
        // 生成文件名（包含日期）
        const dateStr = new Date().toISOString().split('T')[0];
        link.download = `diary-backup-${dateStr}.json`;
        
        // 触发下载
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        // 释放URL对象
        URL.revokeObjectURL(url);
        
        ElMessage.success('数据导出成功！');
      } catch (e) {
        console.error('导出数据错误:', e);
        ElMessage.error('导出失败: ' + (e.message || '未知错误'));
      }
    },
    openImport() {
      this.showYearMonthDropdown = false;
      // 触发文件选择
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.json,application/json';
      input.onchange = (e) => {
        const file = e.target.files[0];
        if (file) {
          this.handleImportFile(file);
        }
      };
      input.click();
    },
    handleImportFile(file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const content = e.target.result;
          const importData = JSON.parse(content);
          
          // 验证数据格式
          if (!importData.diaries || !Array.isArray(importData.diaries)) {
            ElMessage.error('文件格式不正确，请选择正确的备份文件');
            return;
          }
          
          // 获取用户信息
          const userName = importData.user?.name || '未知用户';
          const diaryCount = importData.diaries.length;
          
          // 显示确认对话框
          ElMessageBox.confirm(
            `是否导入用户为 ${userName} 的日记？共 ${diaryCount} 篇`,
            '导入数据',
            {
              confirmButtonText: '确定导入',
              cancelButtonText: '取消',
              type: 'info'
            }
          ).then(() => {
            this.importDiaries(importData.diaries);
          }).catch(() => {
            // 用户取消
          });
        } catch (e) {
          console.error('解析文件错误:', e);
          ElMessage.error('文件解析失败，请确认文件格式正确');
        }
      };
      reader.onerror = () => {
        ElMessage.error('文件读取失败');
      };
      reader.readAsText(file);
    },
    async importDiaries(diaries) {
      if (!diaries || diaries.length === 0) {
        ElMessage.warning('没有日记数据可导入');
        return;
      }
      
      try {
        let successCount = 0;
        let failCount = 0;
        
        // 显示加载提示
        const loadingMessage = ElMessage({
          message: '正在导入日记...',
          type: 'info',
          duration: 0
        });
        
        // 遍历导入每篇日记
        for (const diary of diaries) {
          try {
            const res = await createDiary({
              userId: this.user.id,
              title: diary.title || '无标题',
              content: diary.content || ''
            });
            
            if (res && res.success) {
              successCount++;
            } else {
              failCount++;
            }
          } catch (e) {
            console.error('导入日记错误:', e);
            failCount++;
          }
        }
        
        // 关闭加载提示
        loadingMessage.close();
        
        // 显示导入结果
        if (successCount > 0) {
          ElMessage.success(`导入完成！成功：${successCount} 篇${failCount > 0 ? `，失败：${failCount} 篇` : ''}`);
          // 重新加载日记列表
          await this.loadDiaries();
        } else {
          ElMessage.error(`导入失败，共 ${failCount} 篇`);
        }
      } catch (e) {
        console.error('导入数据错误:', e);
        ElMessage.error('导入失败: ' + (e.message || '未知错误'));
      }
    },
    openThemes() {
      this.showYearMonthDropdown = false;
      this.showThemeModal = true;
    },
    closeThemeModal() {
      this.showThemeModal = false;
    },
    changeTheme(themeName) {
      // 更新当前主题
      this.currentTheme = themeName;
      
      // 移除旧的主题类并添加新主题类到 body 元素
      document.body.className = document.body.className.replace(/theme-\w+/g, '');
      document.body.classList.add(`theme-${themeName}`);
      
      // 保存到 localStorage 以便下次访问时记住用户的选择
      localStorage.setItem('diary-theme', themeName);
      
      // 保存到数据库
      if (this.user && this.user.id) {
        updateTheme(this.user.id, themeName).then(res => {
          if (res && res.success) {
            console.log('主题已保存到数据库');
            // 更新本地用户信息
            this.user.theme = themeName;
            localStorage.setItem('userInfo', JSON.stringify(this.user));
          } else {
            console.error('主题保存失败:', res?.message);
          }
        }).catch(err => {
          console.error('主题保存异常:', err);
        });
      }
      
      // 关闭模态框
      this.closeThemeModal();
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
.home-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: var(--primary-bg);
  border-bottom: 1px solid var(--border-color);
}

.function-btn{
  font-size: 20px;
  margin-left: auto;
  margin-right: 20px;
  background: transparent;
  border: none;
  color: #fff;
  cursor: pointer;
}

.friends-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.friends-btn-text {
  position: relative;
}

.friend-badge {
  position: absolute;
  top: -6px;
  right: -10px;
  left: auto;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  color: #fff;
  background: #f56c6c;
  border-radius: 9px;
  box-sizing: border-box;
  pointer-events: none;
  z-index: 1;
}

.friend-badge--offset {
  right: 22px;
}

.username{
  font-size: 20px;
  margin-left: auto;
  margin-right: 20px;
  background: transparent;
  border: none;
  color: #fff;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.2s;
}

.user-info:hover {
  opacity: 0.8;
}

.section-title{
  background: var(--primary-bg);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.search-box {
  display: flex;
}

.search-input {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px 0 0 4px;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.search-btn {
  padding: 0.5rem 1rem;
  background-color: var(--calendar-today-bg);
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.diary-section {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.add-diary-btn {
  padding: 0.5rem 1rem;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.diary-item {
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  margin-bottom: 1rem;
  cursor: pointer;
  transition: box-shadow 0.2s;
  background-color: var(--bg-color);
  color: var(--text-color);
  position: relative;
}

.diary-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.diary-content-wrapper {
  width: 100%;
}

/* 删除日记按钮 */
.delete-diary-btn {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background-color: transparent;
  color: #999;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.diary-item:hover .delete-diary-btn {
  opacity: 1;
}

.delete-diary-btn:hover {
  background-color: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.icon-delete::before {
  content: "🗑️";
  font-size: 14px;
}

.diary-item h3 mark,
.diary-preview mark {
  background-color: #ffeb3b;
  color: #333;
  padding: 2px 4px;
  border-radius: 2px;
  font-weight: 600;
}

.sidebar {
  width: 300px;
  border-left: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  background-color: var(--sidebar-bg);
}

.calendar-widget, .functions-panel {
  padding: 1rem;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.calendar {
  position: relative;
}

.calendar-nav-left,
.calendar-nav-right {
  display: flex;
  gap: 0.35rem;
}

.calendar-nav-btn {
  width: 26px;
  height: 26px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background-color: transparent;
  color: var(--text-color);
  opacity: 0.55;
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.15s, background-color 0.15s;
}

.calendar-nav-btn:hover {
  opacity: 0.9;
  background-color: var(--hover-bg);
}

.month-year-display {
  cursor: pointer;
  padding: 0.25rem 0.35rem;
  border-radius: 4px;
  font-weight: 600;
  user-select: none;
  opacity: 0.95;
  transition: background-color 0.15s;
}

.month-year-display:hover {
  background-color: var(--hover-bg);
}

.year-month-dropdown {
  position: absolute;
  top: 40px;
  left: 0;
  right: 0;
  z-index: 50;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: var(--bg-color);
  padding: 0.6rem 0.6rem 0.5rem;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.ym-panel {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.ym-year-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}

.ym-year-top-title {
  font-weight: 600;
  color: var(--text-color);
  text-align: center;
  flex: 1;
}

.ym-year-nav-btn {
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 50%;
  background-color: transparent;
  color: var(--text-color);
  opacity: 0.55;
  cursor: pointer;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.ym-year-nav-btn:hover {
  opacity: 0.9;
  background-color: var(--hover-bg);
}

.ym-months-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.4rem;
}

.ym-month-item {
  padding: 0.4rem 0.25rem;
  border-radius: 6px;
  cursor: pointer;
  text-align: center;
  color: var(--text-color);
  opacity: 0.85;
}

.ym-month-item.active {
  background-color: var(--calendar-today-bg);
  color: white;
  opacity: 1;
}

.ym-actions {
  margin-top: 0.75rem;
}

.calendar-grid {
  text-align: center;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.days span {
  padding: 0.5rem;
  cursor: pointer;
  border-radius: 4px;
}

.days span.today {
  background-color: var(--calendar-today-bg);
  color: white;
}

.days span.has-event {
  background-color: var(--calendar-event-bg);
  color: #212529;
}

.days span.selected {
  background-color: var(--calendar-today-bg);
  color: white;
}

.back-to-today-container {
  margin-top: 1rem;
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.back-to-today-btn {
  padding: 0.5rem 1.5rem;
  background-color: var(--calendar-today-bg);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.back-to-today-btn:hover {
  background-color: #0056b3;
  opacity: 0.9;
}

.function-list {
  list-style: none;
  padding: 0;
}

.function-list li {
  padding: 0.75rem;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  display: flex;
  align-items: center;
  color: var(--text-color);
}

.function-list li:last-child {
  border-bottom: none;
}

.function-list li:hover {
  background-color: var(--hover-bg);
}

/* 主题选择模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-color);
  padding: 2rem;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  color: var(--text-color);
}

.theme-options {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin: 1rem 0;
}

.theme-option {
  flex: 1;
  min-width: 120px;
  text-align: center;
  cursor: pointer;
  border-radius: 4px;
  padding: 0.5rem;
}

.theme-option.active {
  border: 2px solid var(--calendar-today-bg);
}

.theme-preview {
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
  border: 1px solid var(--border-color);
}

.preview-top-bar {
  height: 20px;
}

.preview-content {
  height: 60px;
  display: flex;
}

.preview-sidebar {
  width: 30%;
  height: 100%;
}

.close-btn {
  padding: 0.5rem 1rem;
  background-color: var(--calendar-today-bg);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  float: right;
}

/* 各主题预览样式 */
.theme-default .preview-top-bar {
  background: linear-gradient(90deg, #3d8be4, #fabcac);
}

.theme-default .preview-content {
  background-color: #fff;
}

.theme-default .preview-sidebar {
  background-color: #f8f9fa;
}

.theme-dark .preview-top-bar {
  background: linear-gradient(90deg, #1a3a5f, #4a2c4a);
}

.theme-dark .preview-content {
  background-color: #121212;
}

.theme-dark .preview-sidebar {
  background-color: #1e1e1e;
}

.theme-light .preview-top-bar {
  background: linear-gradient(90deg, #a8d0ff, #ffd1dc);
}

.theme-light .preview-content {
  background-color: #ffffff;
}

.theme-light .preview-sidebar {
  background-color: #fafafa;
}

/* 统计分析模态框样式 */
.statistics-modal {
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.statistics-content {
  margin: 1.5rem 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--border-color);
}

.stat-item:last-of-type {
  border-bottom: none;
}

.stat-label {
  font-weight: 500;
  color: var(--text-color);
  font-size: 1rem;
}

.stat-value {
  font-weight: 600;
  color: var(--calendar-today-bg);
  font-size: 1.1rem;
}

.monthly-stats {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 2px solid var(--border-color);
}

.monthly-stats h4 {
  margin: 0 0 1rem 0;
  color: var(--text-color);
  font-size: 1.1rem;
}

.monthly-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 300px;
  overflow-y: auto;
}

.monthly-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background-color: var(--hover-bg);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.monthly-item:hover {
  background-color: var(--calendar-today-bg);
  transform: translateX(5px);
}

.monthly-item:hover .month-label,
.monthly-item:hover .month-count {
  color: white;
}

.month-label {
  color: var(--text-color);
  font-size: 0.95rem;
}

.month-count {
  color: var(--calendar-today-bg);
  font-weight: 500;
  font-size: 0.95rem;
}

.year-month-picker-row {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.picker-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.picker-label {
  font-weight: 500;
}

.picker-select {
  padding: 0.4rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.picker-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.25rem;
}

.picker-cancel-btn {
  padding: 0.5rem 1rem;
  background-color: transparent;
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  cursor: pointer;
}

.picker-confirm-btn {
  padding: 0.5rem 1rem;
  background-color: var(--calendar-today-bg);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.picker-confirm-btn:hover {
  opacity: 0.95;
}
</style>