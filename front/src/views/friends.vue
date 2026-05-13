<template>
  <div class="friends-page">
    <header class="friends-header">
      <el-button text @click="goBack">← 返回</el-button>
      <h1 class="title">好友</h1>
      <span class="spacer"></span>
    </header>

    <main class="friends-main">
      <section class="card search-card">
        <h2 class="section-title">添加好友</h2>
        <p class="hint">输入对方注册时使用的完整手机号查找用户</p>
        <div class="search-row">
          <el-input
            v-model="phoneKeyword"
            placeholder="手机号"
            clearable
            maxlength="20"
            style="max-width: 280px"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" :loading="searching" @click="handleSearch">查找</el-button>
        </div>
        <div v-if="foundUser" class="found-user">
          <div class="user-row">
            <img v-if="foundUser.avatar" :src="foundUser.avatar" class="mini-avatar" alt="" />
            <div v-else class="mini-avatar placeholder">{{ (foundUser.name || '?').charAt(0) }}</div>
            <div class="user-meta">
              <div class="name">{{ foundUser.name }}</div>
              <div class="sub">{{ foundUser.phone }}</div>
            </div>
            <el-button type="primary" :loading="sending" @click="sendRequest">加好友</el-button>
          </div>
        </div>
      </section>

      <el-tabs v-model="activeTab" class="tabs" @tab-change="onTabChange">
        <el-tab-pane label="我的好友" name="friends">
          <div v-if="friends.length === 0" class="empty">暂无好友，去添加一位吧</div>
          <ul v-else class="user-list">
            <li v-for="f in friends" :key="f.id" class="user-row list-row">
              <img v-if="f.avatar" :src="f.avatar" class="mini-avatar" alt="" />
              <div v-else class="mini-avatar placeholder">{{ (f.name || '?').charAt(0) }}</div>
              <div class="user-meta">
                <div class="name">{{ f.name }}</div>
                <div class="sub">{{ f.phone }}</div>
              </div>
              <el-button type="danger" plain size="small" @click="confirmRemove(f)">删除好友</el-button>
            </li>
          </ul>
        </el-tab-pane>
        <el-tab-pane label="收到的申请" name="incoming">
          <div v-if="incoming.length === 0" class="empty">暂无待处理申请</div>
          <ul v-else class="user-list">
            <li v-for="r in incoming" :key="r.requestId" class="user-row list-row">
              <img v-if="r.avatar" :src="r.avatar" class="mini-avatar" alt="" />
              <div v-else class="mini-avatar placeholder">{{ (r.name || '?').charAt(0) }}</div>
              <div class="user-meta">
                <div class="name">{{ r.name }}</div>
                <div class="sub">{{ r.phone }}</div>
              </div>
              <el-button type="success" size="small" @click="acceptRequest(r)">同意</el-button>
              <el-button size="small" @click="rejectRequest(r)">拒绝</el-button>
            </li>
          </ul>
        </el-tab-pane>
        <el-tab-pane label="发出的申请" name="outgoing">
          <div v-if="outgoing.length === 0" class="empty">暂无待对方处理的申请</div>
          <ul v-else class="user-list">
            <li v-for="r in outgoing" :key="r.requestId" class="user-row list-row">
              <img v-if="r.avatar" :src="r.avatar" class="mini-avatar" alt="" />
              <div v-else class="mini-avatar placeholder">{{ (r.name || '?').charAt(0) }}</div>
              <div class="user-meta">
                <div class="name">{{ r.name }}</div>
                <div class="sub">{{ r.phone }}</div>
              </div>
              <el-tag type="warning" size="small">等待对方</el-tag>
            </li>
          </ul>
        </el-tab-pane>
      </el-tabs>
    </main>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  searchFriendUser,
  sendFriendRequest,
  acceptFriendRequest,
  rejectFriendRequest,
  listFriends,
  listIncomingFriendRequests,
  listOutgoingFriendRequests,
  removeFriend
} from '../api/friend'

export default {
  name: 'FriendsView',
  data () {
    return {
      userId: null,
      phoneKeyword: '',
      foundUser: null,
      searching: false,
      sending: false,
      activeTab: 'friends',
      friends: [],
      incoming: [],
      outgoing: []
    }
  },
  mounted () {
    const raw = localStorage.getItem('userInfo')
    if (!raw) {
      ElMessage.warning('请先登录')
      this.$router.push('/')
      return
    }
    try {
      const u = JSON.parse(raw)
      this.userId = u.id
    } catch (e) {
      ElMessage.error('用户信息无效')
      this.$router.push('/')
      return
    }
    const theme = localStorage.getItem('diary-theme') || 'default'
    document.body.className = document.body.className.replace(/theme-\w+/g, '')
    document.body.classList.add(`theme-${theme}`)
    this.refreshFriends()
  },
  methods: {
    goBack () {
      this.$router.push('/home')
    },
    onTabChange () {
      if (this.activeTab === 'friends') this.refreshFriends()
      if (this.activeTab === 'incoming') this.refreshIncoming()
      if (this.activeTab === 'outgoing') this.refreshOutgoing()
    },
    async refreshFriends () {
      if (!this.userId) return
      try {
        const res = await listFriends(this.userId)
        if (res && res.success) this.friends = res.data || []
        else ElMessage.error(res?.message || '加载好友失败')
      } catch (e) {
        ElMessage.error('加载好友失败')
      }
    },
    async refreshIncoming () {
      if (!this.userId) return
      try {
        const res = await listIncomingFriendRequests(this.userId)
        if (res && res.success) this.incoming = res.data || []
        else ElMessage.error(res?.message || '加载失败')
      } catch (e) {
        ElMessage.error('加载失败')
      }
    },
    async refreshOutgoing () {
      if (!this.userId) return
      try {
        const res = await listOutgoingFriendRequests(this.userId)
        if (res && res.success) this.outgoing = res.data || []
        else ElMessage.error(res?.message || '加载失败')
      } catch (e) {
        ElMessage.error('加载失败')
      }
    },
    async handleSearch () {
      if (!this.phoneKeyword || !this.phoneKeyword.trim()) {
        ElMessage.warning('请输入手机号')
        return
      }
      this.searching = true
      this.foundUser = null
      try {
        const res = await searchFriendUser(this.userId, this.phoneKeyword.trim())
        if (res && res.success) {
          this.foundUser = res.data
        } else {
          ElMessage.warning(res?.message || '未找到用户')
        }
      } catch (e) {
        ElMessage.error('查找失败')
      } finally {
        this.searching = false
      }
    },
    async sendRequest () {
      if (!this.foundUser) return
      this.sending = true
      try {
        const res = await sendFriendRequest(this.userId, this.foundUser.id)
        if (res && res.success) {
          ElMessage.success(res.message || '已发送')
          this.foundUser = null
          this.phoneKeyword = ''
          await this.refreshOutgoing()
          await this.refreshFriends()
        } else {
          ElMessage.warning(res?.message || '发送失败')
        }
      } catch (e) {
        ElMessage.error('发送失败')
      } finally {
        this.sending = false
      }
    },
    async acceptRequest (row) {
      try {
        const res = await acceptFriendRequest(this.userId, row.requestId)
        if (res && res.success) {
          ElMessage.success(res.message || '已同意')
          await this.refreshIncoming()
          await this.refreshFriends()
        } else {
          ElMessage.error(res?.message || '操作失败')
        }
      } catch (e) {
        ElMessage.error('操作失败')
      }
    },
    async rejectRequest (row) {
      try {
        const res = await rejectFriendRequest(this.userId, row.requestId)
        if (res && res.success) {
          ElMessage.success(res.message || '已拒绝')
          await this.refreshIncoming()
        } else {
          ElMessage.error(res?.message || '操作失败')
        }
      } catch (e) {
        ElMessage.error('操作失败')
      }
    },
    confirmRemove (f) {
      ElMessageBox.confirm(`确定与「${f.name}」解除好友关系？`, '删除好友', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          const res = await removeFriend(this.userId, f.id)
          if (res && res.success) {
            ElMessage.success(res.message || '已删除')
            await this.refreshFriends()
          } else {
            ElMessage.error(res?.message || '删除失败')
          }
        } catch (e) {
          ElMessage.error('删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.friends-page {
  min-height: 100vh;
  background: var(--bg-color, #f5f5f5);
  color: var(--text-color, #333);
}

.friends-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background: var(--primary-bg, linear-gradient(90deg, #3d8be4, #fabcac));
  color: #fff;
}

.title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  flex: 1;
  text-align: center;
}

.spacer {
  width: 64px;
}

.friends-main {
  max-width: 720px;
  margin: 0 auto;
  padding: 1.25rem;
}

.card {
  background: var(--bg-color, #fff);
  border-radius: 8px;
  padding: 1.25rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--border-color, #e8e8e8);
}

.section-title {
  margin: 0 0 0.5rem;
  font-size: 1.1rem;
}

.hint {
  margin: 0 0 1rem;
  font-size: 13px;
  color: #888;
}

.search-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;
}

.found-user {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color, #eee);
}

.user-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.list-row {
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--border-color, #f0f0f0);
}

.list-row:last-child {
  border-bottom: none;
}

.mini-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.mini-avatar.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-weight: 700;
  font-size: 18px;
}

.user-meta {
  flex: 1;
  min-width: 0;
}

.user-meta .name {
  font-weight: 600;
}

.user-meta .sub {
  font-size: 12px;
  color: #888;
}

.tabs {
  background: var(--bg-color, #fff);
  border-radius: 8px;
  padding: 0.5rem 1rem 1rem;
  border: 1px solid var(--border-color, #e8e8e8);
}

.empty {
  padding: 2rem 1rem;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.user-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
</style>
