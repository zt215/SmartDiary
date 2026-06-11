<template>
  <div class="enforcer-home">
    <header class="top-bar">
      <div class="brand">
        <span class="brand-title">字迹 · 执法台</span>
        <span class="brand-sub">{{ enforcerName }}</span>
      </div>
      <el-button type="danger" plain @click="logout">退出执法台</el-button>
    </header>

    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="用户账户列表" name="users">
        <el-table v-loading="usersLoading" :data="users" stripe border style="width: 100%">
          <el-table-column prop="uid" label="UID" width="120" />
          <el-table-column prop="name" label="昵称" min-width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="生日" width="120">
            <template #default="{ row }">{{ formatDateOnly(row.birthday) || '—' }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="动态列表" name="circles">
        <el-table v-loading="circlesLoading" :data="circles" stripe border style="width: 100%">
          <el-table-column prop="id" label="动态ID" width="80" />
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="userName" label="发布者" width="120" />
          <el-table-column label="内容摘要" min-width="240" show-overflow-tooltip>
            <template #default="{ row }">
              {{ stripHtml(row.content) }}
            </template>
          </el-table-column>
          <el-table-column label="点赞" width="70" prop="likeCount" />
          <el-table-column label="评论" width="70" prop="commentCount" />
          <el-table-column label="发布时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <div class="pager">
          <el-pagination
            v-model:current-page="circlePage"
            :page-size="circlePageSize"
            :total="circleTotal"
            layout="total, prev, pager, next"
            @current-change="loadCircles"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listAdminUsers, listAdminDiaryCircles } from '@/api/enforcer'
import { formatDateTime, formatDateOnly } from '@/utils/dateFormat'

const router = useRouter()
const activeTab = ref('users')
const users = ref([])
const circles = ref([])
const usersLoading = ref(false)
const circlesLoading = ref(false)
const circlePage = ref(1)
const circlePageSize = 20
const circleTotal = ref(0)

const enforcerInfo = ref(null)

const enforcerName = computed(() => {
  const n = enforcerInfo.value?.name
  return n ? `执法者：${n}` : ''
})

const getEnforcerId = () => {
  const raw = localStorage.getItem('enforcerInfo')
  if (!raw) return null
  try {
    return JSON.parse(raw).id ?? null
  } catch {
    return null
  }
}

const stripHtml = (html) => {
  if (!html) return ''
  const d = document.createElement('div')
  d.innerHTML = html
  return (d.textContent || '').trim() || '（无文字）'
}

const loadUsers = async () => {
  const id = getEnforcerId()
  if (!id) return
  usersLoading.value = true
  try {
    const res = await listAdminUsers(id)
    if (res?.success) {
      users.value = res.data || []
    } else {
      ElMessage.error(res?.message || '加载用户失败')
    }
  } finally {
    usersLoading.value = false
  }
}

const loadCircles = async () => {
  const id = getEnforcerId()
  if (!id) return
  circlesLoading.value = true
  try {
    const res = await listAdminDiaryCircles(id, circlePage.value, circlePageSize)
    if (res?.success && res.data) {
      circles.value = res.data.list || []
      circleTotal.value = res.data.total || 0
    } else {
      ElMessage.error(res?.message || '加载动态失败')
    }
  } finally {
    circlesLoading.value = false
  }
}

const logout = () => {
  localStorage.removeItem('enforcerInfo')
  router.push('/enforcer/login')
}

onMounted(() => {
  const raw = localStorage.getItem('enforcerInfo')
  if (!raw) {
    router.replace('/enforcer/login')
    return
  }
  try {
    enforcerInfo.value = JSON.parse(raw)
  } catch {
    router.replace('/enforcer/login')
    return
  }
  loadUsers()
  loadCircles()
})
</script>

<style scoped>
.enforcer-home {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 0 24px 32px;
}
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
}
.brand-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}
.brand-sub {
  margin-left: 16px;
  color: #909399;
  font-size: 14px;
}
.main-tabs {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
