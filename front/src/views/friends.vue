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

        <p class="hint">输入 UID、邮箱或完整手机号查找用户</p>

        <div class="search-row">

          <el-input

            v-model="searchKeyword"

            placeholder="UID / 邮箱 / 手机号"

            clearable

            maxlength="64"

            style="max-width: 320px"

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

              <div class="sub">UID {{ foundUser.uid ?? '—' }}</div>
              <div class="sub">{{ formatPrivateField(foundUser.phone, '手机号') }}</div>
              <div class="sub">{{ formatPrivateField(foundUser.email, '邮箱') }}</div>

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

              <div class="friend-info-clickable" @click="showFriendDiaries(f)">

                <div class="mini-avatar-wrap">
                  <img v-if="f.avatar" :src="f.avatar" class="mini-avatar" alt="" />
                  <div v-else class="mini-avatar placeholder">{{ (f.name || '?').charAt(0) }}</div>
                  <span v-if="hasNewFriendDiary(f)" class="friend-badge">1</span>
                </div>

                <div class="user-meta">

                  <div class="name">{{ f.name }}</div>

                  <div class="sub">{{ formatFriendContact(f) }}</div>

                  <div class="view-hint">点击查看字迹</div>

                </div>

              </div>

              <div class="friend-actions">
                <el-button type="primary" plain size="small" @click.stop="showFriendProfile(f)">查看信息</el-button>
                <el-button type="danger" plain size="small" @click.stop="confirmRemove(f)">删除好友</el-button>
              </div>

            </li>

          </ul>

        </el-tab-pane>

        <el-tab-pane name="incoming">

          <template #label>

            <span class="tab-label-badge-wrap">

              收到的申请

              <span v-if="incoming.length > 0" class="friend-badge">{{ incomingBadgeText }}</span>

            </span>

          </template>

          <div v-if="incoming.length === 0" class="empty">暂无待处理申请</div>

          <ul v-else class="user-list">

            <li v-for="r in incoming" :key="r.requestId" class="user-row list-row">

              <img v-if="r.avatar" :src="r.avatar" class="mini-avatar" alt="" />

              <div v-else class="mini-avatar placeholder">{{ (r.name || '?').charAt(0) }}</div>

              <div class="user-meta">

                <div class="name">{{ r.name }}</div>

                <div class="sub">{{ formatFriendContact(r) }}</div>

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

                <div class="sub">{{ formatFriendContact(r) }}</div>

              </div>

              <el-tag type="warning" size="small">等待对方</el-tag>

            </li>

          </ul>

        </el-tab-pane>

      </el-tabs>

    </main>



    <el-dialog

      v-model="diariesVisible"

      :title="diariesDialogTitle"

      width="520px"

      destroy-on-close

      @closed="onDiariesClosed"

    >

      <div v-if="diariesLoading" class="diaries-loading">加载中...</div>

      <div v-else-if="friendDiaries.length === 0" class="diaries-empty">好友还没有发布字迹</div>

      <ul v-else class="friend-diary-list">

        <li

          v-for="diary in friendDiaries"

          :key="diary.id"

          class="friend-diary-item"

          :class="{ 'friend-diary-item--new': isNewFriendDiaryItem(diary) }"

          @click="viewFriendDiary(diary)"

        >

          <div class="friend-diary-item-head">

            <span v-if="isNewFriendDiaryItem(diary)" class="friend-diary-new-tag">新</span>

            <p class="friend-diary-content">{{ diary.content }}</p>

          </div>

          <div class="friend-diary-meta">

            <span>{{ formatDiaryTime(diary.createTime) }}</span>

            <span>赞 {{ diary.likeCount || 0 }} · 评 {{ diary.commentCount || 0 }}</span>

          </div>

        </li>

      </ul>

    </el-dialog>

    <el-dialog
      v-model="profileVisible"
      title="好友资料"
      width="420px"
      destroy-on-close
      @closed="onProfileClosed"
    >
      <div v-if="profileLoading" class="profile-loading">加载中...</div>
      <div v-else-if="friendProfile" class="profile-body">
        <div class="profile-avatar-wrap">
          <img v-if="friendProfile.avatar" :src="friendProfile.avatar" class="profile-avatar" alt="" />
          <div v-else class="profile-avatar placeholder">{{ (friendProfile.name || '?').charAt(0) }}</div>
        </div>
        <div class="profile-item">
          <span class="profile-label">用户名</span>
          <span class="profile-value">{{ friendProfile.name || '未设置' }}</span>
        </div>
        <div class="profile-item">
          <span class="profile-label">账号 UID</span>
          <span class="profile-value">{{ friendProfile.uid ?? '未设置' }}</span>
        </div>
        <div class="profile-item">
          <span class="profile-label">手机号</span>
          <span class="profile-value">{{ formatPrivateField(friendProfile.phone, '手机号') }}</span>
        </div>
        <div class="profile-item">
          <span class="profile-label">邮箱</span>
          <span class="profile-value">{{ formatPrivateField(friendProfile.email, '邮箱') }}</span>
        </div>
        <div class="profile-item">
          <span class="profile-label">生日</span>
          <span class="profile-value">{{ formatDate(friendProfile.birthday) || '未设置' }}</span>
        </div>
        <div class="profile-item">
          <span class="profile-label">地址</span>
          <span class="profile-value">{{ formatRegionAddressLabel(friendProfile.address) || '未设置' }}</span>
        </div>
      </div>
    </el-dialog>

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

  removeFriend,

  getFriendProfile

} from '../api/friend'

import { getUserDiaries } from '../api/diaryCircle'
import {
  hasNewFriendDiary as checkNewFriendDiary,
  getFriendDiarySeenAt,
  isNewFriendDiaryItem as checkNewFriendDiaryItem,
  markFriendDiarySeen
} from '@/utils/friendDiarySeen'
import { formatRegionAddressLabel } from '@/utils/regionAddress'



export default {

  name: 'FriendsView',

  data () {

    return {

      userId: null,

      searchKeyword: '',

      foundUser: null,

      searching: false,

      sending: false,

      activeTab: 'friends',

      friends: [],

      incoming: [],

      outgoing: [],

      diariesVisible: false,

      diariesLoading: false,

      selectedFriend: null,

      friendDiaries: [],

      diarySeenBeforeOpen: 0,

      profileVisible: false,

      profileLoading: false,

      friendProfile: null

    }

  },

  computed: {

    incomingBadgeText () {

      const n = this.incoming.length

      if (n > 99) return '99+'

      return String(n)

    },

    diariesDialogTitle () {

      const name = this.selectedFriend?.name || '好友'

      return `${name}的字迹`

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

    this.refreshIncoming()

  },

  methods: {

    hasNewFriendDiary (f) {
      return checkNewFriendDiary(this.userId, f.id, f.latestDiaryTime)
    },

    formatRegionAddressLabel,

    formatDate (date) {
      if (!date) return ''
      const d = typeof date === 'string' ? new Date(date) : date
      if (Number.isNaN(d.getTime())) return ''
      return d.toLocaleDateString('zh-CN')
    },

    formatPrivateField (value, label) {
      if (value) return value
      return `${label}已隐藏`
    },

    formatFriendContact (user) {
      if (user && user.phone) return user.phone
      return '手机号已隐藏'
    },

    onProfileClosed () {
      this.friendProfile = null
      this.profileLoading = false
    },

    async showFriendProfile (f) {
      if (!f?.id) return
      this.profileVisible = true
      this.profileLoading = true
      this.friendProfile = null
      try {
        const res = await getFriendProfile(this.userId, f.id)
        if (res && res.success) {
          this.friendProfile = res.data
        } else {
          ElMessage.error(res?.message || '加载好友资料失败')
          this.profileVisible = false
        }
      } catch (e) {
        ElMessage.error('加载好友资料失败')
        this.profileVisible = false
      } finally {
        this.profileLoading = false
      }
    },

    formatDiaryTime (time) {

      if (!time) return ''

      const d = typeof time === 'string' ? new Date(time) : time

      if (Number.isNaN(d.getTime())) return String(time)

      return d.toLocaleString('zh-CN')

    },

    goBack () {

      this.$router.push('/home')

    },

    onTabChange () {

      if (this.activeTab === 'friends') this.refreshFriends()

      if (this.activeTab === 'incoming') this.refreshIncoming()

      if (this.activeTab === 'outgoing') this.refreshOutgoing()

    },

    onDiariesClosed () {

      if (this.selectedFriend) {
        this.markFriendDiariesSeen(this.selectedFriend)
        this.refreshFriends()
      }

      this.selectedFriend = null

      this.friendDiaries = []

      this.diariesLoading = false

      this.diarySeenBeforeOpen = 0

    },

    isNewFriendDiaryItem (diary) {
      return checkNewFriendDiaryItem(diary?.createTime, this.diarySeenBeforeOpen)
    },

    markFriendDiariesSeen (f) {
      if (!f?.id) return
      let seenAt = f.latestDiaryTime
      if (this.friendDiaries?.length) {
        for (const d of this.friendDiaries) {
          if (!d.createTime) continue
          const t = new Date(d.createTime).getTime()
          if (!seenAt || t > new Date(seenAt).getTime()) {
            seenAt = d.createTime
          }
        }
      }
      if (seenAt) {
        markFriendDiarySeen(this.userId, f.id, seenAt)
      }
    },

    viewFriendDiary (diary) {

      if (!diary?.id) return

      if (this.selectedFriend) {
        this.markFriendDiariesSeen(this.selectedFriend)
      }

      this.diariesVisible = false

      this.$router.push(`/diary-circle/${diary.id}`)

    },

    async showFriendDiaries (f) {

      if (!f?.id) return

      this.selectedFriend = f

      this.diarySeenBeforeOpen = getFriendDiarySeenAt(this.userId, f.id)

      this.diariesVisible = true

      this.diariesLoading = true

      this.friendDiaries = []

      try {

        const res = await getUserDiaries(f.id, this.userId)

        if (res && res.success) {

          this.friendDiaries = res.data || []

        } else {

          ElMessage.error(res?.message || '加载好友字迹失败')

          this.diariesVisible = false

        }

      } catch (e) {

        ElMessage.error('加载好友字迹失败')

        this.diariesVisible = false

      } finally {

        this.diariesLoading = false

      }

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

      if (!this.searchKeyword || !this.searchKeyword.trim()) {

        ElMessage.warning('请输入 UID、邮箱或手机号')

        return

      }

      this.searching = true

      this.foundUser = null

      try {

        const res = await searchFriendUser(this.userId, this.searchKeyword.trim())

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

          this.searchKeyword = ''

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



.friend-info-clickable {

  display: flex;

  align-items: center;

  gap: 0.75rem;

  flex: 1;

  min-width: 0;

  cursor: pointer;

  border-radius: 8px;

  padding: 0.25rem;

  margin: -0.25rem;

  transition: background-color 0.15s;

}



.friend-info-clickable:hover {

  background-color: var(--hover-bg, rgba(0, 0, 0, 0.04));

}



.view-hint {

  font-size: 11px;

  color: #aaa;

  margin-top: 2px;

}



.mini-avatar-wrap {

  position: relative;

  flex-shrink: 0;

}



.mini-avatar-wrap .friend-badge {

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



.tab-label-badge-wrap {

  position: relative;

  display: inline-block;

}



.tab-label-badge-wrap .friend-badge {

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



.diaries-loading,

.diaries-empty {

  text-align: center;

  padding: 2rem;

  color: #999;

  font-size: 14px;

}



.friend-diary-list {

  list-style: none;

  margin: 0;

  padding: 0;

  max-height: 60vh;

  overflow-y: auto;

}



.friend-diary-item {

  padding: 0.85rem 0;

  border-bottom: 1px solid var(--border-color, #f0f0f0);

  cursor: pointer;

  transition: background-color 0.15s;

}



.friend-diary-item:hover {

  background-color: var(--hover-bg, rgba(0, 0, 0, 0.03));

}



.friend-diary-item--new {

  background-color: #fff5f5;

  border-left: 3px solid #f56c6c;

  padding-left: 0.65rem;

  margin-left: -0.65rem;

}



.friend-diary-item--new:hover {

  background-color: #ffeded;

}



.friend-diary-item:last-child {

  border-bottom: none;

}



.friend-diary-item-head {

  display: flex;

  align-items: flex-start;

  gap: 0.5rem;

  margin-bottom: 0.5rem;

}



.friend-diary-new-tag {

  flex-shrink: 0;

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

}



.friend-diary-content {

  margin: 0;

  flex: 1;

  font-size: 14px;

  line-height: 1.5;

  word-break: break-word;

  white-space: pre-wrap;

}



.friend-diary-meta {

  display: flex;

  justify-content: space-between;

  gap: 0.5rem;

  font-size: 12px;

  color: #888;

}



.friend-actions {

  display: flex;

  flex-direction: column;

  gap: 0.5rem;

  flex-shrink: 0;

  width: 88px;

}

.friend-actions :deep(.el-button) {

  width: 100%;

  margin: 0;

  padding-left: 0;

  padding-right: 0;

}



.profile-loading {

  text-align: center;

  padding: 2rem;

  color: #999;

}



.profile-body {

  padding: 0.5rem 0;

}



.profile-avatar-wrap {

  display: flex;

  justify-content: center;

  margin-bottom: 1.25rem;

}



.profile-avatar {

  width: 80px;

  height: 80px;

  border-radius: 50%;

  object-fit: cover;

}



.profile-avatar.placeholder {

  display: flex;

  align-items: center;

  justify-content: center;

  background: linear-gradient(135deg, #667eea, #764ba2);

  color: #fff;

  font-weight: 700;

  font-size: 32px;

}



.profile-item {

  display: flex;

  padding: 0.65rem 0;

  border-bottom: 1px solid var(--border-color, #f0f0f0);

}



.profile-item:last-child {

  border-bottom: none;

}



.profile-label {

  width: 72px;

  flex-shrink: 0;

  color: #888;

  font-size: 14px;

}



.profile-value {

  flex: 1;

  font-size: 14px;

  word-break: break-word;

}

</style>


