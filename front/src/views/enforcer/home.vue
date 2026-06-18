<template>
  <div class="enforcer-home">
    <header class="top-bar">
      <div class="brand">
        <span class="brand-title">字迹 · 执法堂</span>
        <span class="brand-sub">{{ enforcerName }}</span>
      </div>
      <el-button type="danger" plain @click="logout">退出执法堂</el-button>
    </header>
    <el-tabs v-model="activeTab" class="main-tabs" @tab-change="onTabChange">
      <el-tab-pane label="用户账户列表" name="users">
        <div class="search-bar">
          <el-select v-model="userSearchField" placeholder="搜索字段" style="width: 130px">
            <el-option label="UID" value="uid" />
            <el-option label="昵称" value="name" />
            <el-option label="手机号" value="phone" />
            <el-option label="邮箱" value="email" />
          </el-select>
          <el-input v-model="userSearchKey" placeholder="请输入搜索内容" clearable style="width: 240px" @input="filterUsers" />
        </div>
        <el-table v-loading="usersLoading" :data="filteredUsers" stripe border style="width: 100%">
          <el-table-column prop="uid" label="UID" width="120" />
          <el-table-column prop="name" label="昵称" min-width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="生日" width="120">
            <template #default="{ row }">{{ formatDateOnly(row.birthday) || '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.accountStatus === 1" type="info">已注销</el-tag>
              <el-tag v-else-if="row.banned" type="danger" style="cursor:pointer" @click="showBanDetail(row)">封禁中</el-tag>
              <el-tag v-else type="success">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.accountStatus === 1" @click="openEdit(row)">修改</el-button>
              <el-button v-if="row.banned" link type="danger" :disabled="row.accountStatus === 1" @click="confirmUnban(row)">
                {{ row._countdown || '解封' }}
              </el-button>
              <el-button v-else link type="warning" :disabled="row.accountStatus === 1" @click="openBan(row)">封禁</el-button>
              <el-button link type="danger" :disabled="row.accountStatus === 1" @click="confirmCancel(row)">注销</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="动态列表" name="circles">
        <div class="search-bar">
          <el-input v-model="circleSearchKey" placeholder="搜索发布者、内容或评论" clearable style="width: 300px" @keyup.enter="searchCircles" />
          <el-button type="primary" @click="searchCircles">搜索</el-button>
        </div>
        <el-table v-loading="circlesLoading" :data="circles" stripe border style="width: 100%" @row-click="openCircleDetail">
          <el-table-column prop="userName" label="发布者" width="120" />
          <el-table-column label="内容摘要" min-width="240" show-overflow-tooltip>
            <template #default="{ row }">{{ stripHtml(row.content) }}</template>
          </el-table-column>
          <el-table-column label="点赞" width="70" prop="likeCount" />
          <el-table-column label="评论" width="70" prop="commentCount" />
          <el-table-column label="发布时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.hidden" type="info">已隐藏</el-tag>
              <el-tag v-else type="success">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.hidden" link type="success" @click.stop="handleShowCircle(row)">显示</el-button>
              <el-button v-else link type="warning" @click.stop="handleHideCircle(row)">隐藏</el-button>
              <el-button link type="warning" @click.stop="openBanFromCircle(row)">封禁</el-button>
              <el-button link type="danger" @click.stop="confirmDeleteCircle(row)">删除</el-button>
            </template>
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
      <el-tab-pane label="管理员账户列表" name="enforcers">
        <div class="search-bar">
          <el-button type="primary" @click="openAddEnforcer">新增管理员</el-button>
        </div>
        <el-table v-loading="enforcersLoading" :data="enforcers" stripe border style="width: 100%">
          <el-table-column prop="uid" label="UID" width="100" />
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" :disabled="row.id === enforcerInfo?.id" @click="confirmDeleteEnforcer(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
   </el-tabs>

    <!-- 封禁 -->
    <el-dialog v-model="banVisible" title="封禁用户" width="480px" destroy-on-close append-to-body>
      <p class="dialog-user">用户：{{ banTarget?.name }}（UID {{ banTarget?.uid }}）</p>
      <el-form label-width="88px" @submit.prevent>
        <el-form-item label="封禁时长" required>
          <el-select v-model="banForm.durationDays" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in banDurations" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="封禁理由" required>
          <el-select v-model="banForm.reason" placeholder="请选择" style="width: 100%">
            <el-option v-for="r in banReasons" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="banForm.reason === '其他'" label="自定义理由" required>
          <el-input v-model="banForm.customReason" type="textarea" :rows="3" placeholder="请填写封禁理由" />
        </el-form-item>
        <el-form-item v-else label="补充说明">
          <el-input v-model="banForm.customReason" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banVisible = false">取消</el-button>
        <el-button type="warning" :loading="banSubmitting" @click="submitBan">确认封禁</el-button>
      </template>
    </el-dialog>
    <!-- 修改 -->
    <el-dialog v-model="editVisible" title="修改用户信息" width="520px" destroy-on-close append-to-body>
      <el-form label-width="100px" @submit.prevent>
        <el-form-item label="UID">
          <el-input :model-value="editForm.uid" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input :model-value="editForm.phone" disabled />
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
            v-model="editForm.birthday"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="editForm.address" />
        </el-form-item>
      </el-form>
      <p class="edit-tip">不可修改 UID、手机号与密码</p>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
    <!-- 封禁详情 -->
    <el-dialog v-model="banDetailVisible" title="封禁详情" width="420px" destroy-on-close append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="封禁理由">{{ banDetailRow?.banReason || '未知' }}</el-descriptions-item>
        <el-descriptions-item v-if="banDetailRow?.banCustomReason" label="补充说明">{{ banDetailRow.banCustomReason }}</el-descriptions-item>
        <el-descriptions-item label="解封时间">{{ banDetailRow?.banEndTime ? formatDateTime(banDetailRow.banEndTime) : '未知' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    <!-- 动态详情 -->
    <el-dialog v-model="circleDetailVisible" title="动态详情" width="640px" destroy-on-close append-to-body>
      <div v-loading="circleDetailLoading">
        <div class="circle-detail-header">
          <span class="circle-detail-user">{{ circleDetail?.userName }}</span>
          <span class="circle-detail-time">{{ formatDateTime(circleDetail?.createTime) }}</span>
        </div>
        <div class="circle-detail-content" v-html="circleDetail?.content"></div>
        <el-divider />
        <div class="circle-detail-comments">
          <h4>评论 ({{ circleComments.length }})</h4>
          <div v-if="!circleComments.length" class="no-comments">暂无评论</div>
          <div v-for="c in circleComments" :key="c.id" class="comment-item">
            <span class="comment-user">{{ c.userName }}</span>
            <span class="comment-text">{{ c.content }}</span>
            <span class="comment-time">{{ formatDateTime(c.createTime) }}</span>
            <div v-if="c.replies?.length" class="reply-list">
              <div v-for="r in c.replies" :key="r.id" class="reply-item">
                <span class="comment-user">{{ r.userName }}</span>
                <span v-if="r.replyToUserName" class="reply-to">回复 <b>{{ r.replyToUserName }}</b></span>
                <span class="comment-text">{{ r.content }}</span>
                <span class="comment-time">{{ formatDateTime(r.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 新增管理员 -->
    <el-dialog v-model="addEnforcerVisible" title="新增管理员" width="480px" destroy-on-close append-to-body>
      <el-form label-width="88px" @submit.prevent>
        <el-form-item label="搜索用户" required>
          <el-input v-model="enforcerSearchKeyword" placeholder="输入手机号 / UID / 邮箱（支持模糊搜索）" @keyup.enter="handleSearchUser">
            <template #append>
              <el-button @click="handleSearchUser" :loading="enforcerSearchLoading">搜索</el-button>
            </template>
          </el-input>
        </el-form-item>
        <div v-if="searchedUser" class="enforcer-search-result">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="用户名称">{{ searchedUser.name }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ searchedUser.phone }}</el-descriptions-item>
            <el-descriptions-item label="UID">{{ searchedUser.uid }}</el-descriptions-item>
            <el-descriptions-item v-if="searchedUser.email" label="邮箱">{{ searchedUser.email }}</el-descriptions-item>
          </el-descriptions>
          <p v-if="searchedUser.alreadyEnforcer" class="enforcer-search-warn">该用户已是管理员</p>
        </div>
        <el-form-item v-if="searchedUser && !searchedUser.alreadyEnforcer" label="设置密码" required>
          <el-input v-model="newEnforcerPassword" type="password" placeholder="为新管理员设置密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addEnforcerVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="addEnforcerSubmitting"
          :disabled="!searchedUser || searchedUser.alreadyEnforcer || !newEnforcerPassword"
          @click="submitAddEnforcer"
        >确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listAdminUsers,
  listAdminDiaryCircles,
  cancelAdminUser,
  banAdminUser,
  unbanAdminUser,
  updateAdminUser,
  deleteDiaryCircle,
  hideDiaryCircle,
 showDiaryCircle
} from '@/api/enforcer';
import { listAdminEnforcers, addAdminEnforcer, deleteAdminEnforcer, searchUsersByKeyword } from '@/api/enforcer'
import { formatDateTime, formatDateOnly } from '@/utils/dateFormat'
import service from '@/api/auth'
const router = useRouter()
const activeTab = ref('users')
const users = ref([])
const onTabChange = (tab) => {
  if (tab === "users") loadUsers()
  else if (tab === "circles") loadCircles()
  else if (tab === "enforcers") loadEnforcers()
}
const circles = ref([])
const usersLoading = ref(false)
const circlesLoading = ref(false)
const circlePage = ref(1)
const circlePageSize = 20
const circleTotal = ref(0)
const circleSearchKey = ref('')
const enforcerInfo = ref(null)
const userSearchField = ref('name')
const userSearchKey = ref('')
const filteredUsers = ref([])
const banVisible = ref(false)
const banSubmitting = ref(false)
const banTarget = ref(null)
const banCircleId = ref(null)
const banForm = reactive({ durationDays: 7, reason: '', customReason: '' })
const editVisible = ref(false)
const editSubmitting = ref(false)
const editForm = reactive({
  id: null,
  uid: '',
  phone: '',
  name: '',
  email: '',
  birthday: '',
  address: ''
})
const banDetailVisible = ref(false)
const banDetailRow = ref(null)
const circleDetailVisible = ref(false)
const circleDetailLoading = ref(false)
const circleDetail = ref(null)
const circleComments = ref([])
const enforcers = ref([])
const enforcersLoading = ref(false)
const addEnforcerVisible = ref(false)
const addEnforcerSubmitting = ref(false)
const enforcerSearchKeyword = ref('')
const enforcerSearchLoading = ref(false)
const searchedUser = ref(null)  // single user result from list
const newEnforcerPassword = ref('')

const banDurations = [
  { label: '1天', value: 1 },
  { label: '3天', value: 3 },
  { label: '7天', value: 7 },
  { label: '30天', value: 30 },
  { label: '90天', value: 90 },
  { label: '180天', value: 180 },
  { label: '1年', value: 365 },
  { label: '3年', value: 1095 },
  { label: '5年', value: 1825 },
  { label: '10年', value: 3650 }
]
const banReasons = ['发布违规内容', '恶意骚扰', '广告刷屏', '传播不实信息', '其他']
const formatCountdown = (endTime) => {
  const now = Date.now()
  const end = new Date(endTime).getTime()
  if (end <= now) return null
  const diff = end - now
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  if (days > 0) return `解封 ${days}天${hours}时`
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `解封 ${hours}时${minutes}分`
}
const applyCountdowns = () => {
  users.value.forEach(row => {
    if (row.banned && row.banEndTime) {
      row._countdown = formatCountdown(row.banEndTime) || '解封'
    }
  })
}
const confirmUnban = (row) => {
  ElMessageBox.confirm(
    `确定解封用户「${row.name}」（UID ${row.uid}）？\n封禁原因：${row.banReason || '未知'}${row.banCustomReason ? ' — ' + row.banCustomReason : ''}`,
    '解封确认',
    { type: 'warning', confirmButtonText: '确认解封', cancelButtonText: '取消' }
  ).then(async () => {
    const res = await unbanAdminUser(row.id, getEnforcerId())
    if (res?.success) {
      ElMessage.success('已解封')
      loadUsers()
    } else {
      ElMessage.error(res?.message || '解封失败')
    }
  }).catch(() => {})
}
const handleHideCircle = (row) => {
  ElMessageBox.confirm(
    `确定隐藏该动态？隐藏后普通用户将无法看到此动态。`,
    '隐藏确认',
    { type: 'warning', confirmButtonText: '确认隐藏', cancelButtonText: '取消' }
  ).then(async () => {
    const res = await hideDiaryCircle(row.id, getEnforcerId())
    if (res?.success) {
      ElMessage.success('已隐藏')
      loadCircles()
    } else {
      ElMessage.error(res?.message || '隐藏失败')
    }
  }).catch(() => {})
}
const handleShowCircle = async (row) => {
  const res = await showDiaryCircle(row.id, getEnforcerId())
  if (res?.success) {
    ElMessage.success('已恢复显示')
    loadCircles()
  } else {
    ElMessage.error(res?.message || '恢复失败')
  }
}
const confirmDeleteCircle = (row) => {
  ElMessageBox.confirm(
    `确定删除该动态？此操作不可恢复。`,
    '删除确认',
    { type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  ).then(async () => {
    const res = await deleteDiaryCircle(row.id, getEnforcerId())
    if (res?.success) {
      ElMessage.success('已删除')
      loadCircles()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  }).catch(() => {})
}
const openBanFromCircle = (row) => {
  banTarget.value = { id: row.userId, name: row.userName, uid: '' }
  banCircleId.value = row.id
  banForm.durationDays = 7
  banForm.reason = ''
  banForm.customReason = ''
  banVisible.value = true
}
const showBanDetail = (row) => {
  banDetailRow.value = row
  banDetailVisible.value = true
}
const openCircleDetail = async (row) => {
  circleDetailVisible.value = true
  circleDetailLoading.value = true
  circleDetail.value = null
  circleComments.value = []
  try {
    const [circleRes, commentRes] = await Promise.all([
      service.get(`/api/diary-circle/${row.id}`),
      service.get(`/api/comment/list/${row.id}`)
    ])
    if (circleRes?.success) circleDetail.value = circleRes.data
    if (commentRes?.success) circleComments.value = commentRes.data || []
  } finally {
    circleDetailLoading.value = false
  }
}
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
      filterUsers()
    } else {
      ElMessage.error(res?.message || '加载用户失败')
    }
  } catch (e) {
    ElMessage.error('请求失败，请检查网络或后端服务')
  } finally {
    usersLoading.value = false
    applyCountdowns()
  }
}
const filterUsers = () => {
  const key = userSearchKey.value.trim().toLowerCase()
  if (!key) {
    filteredUsers.value = users.value
    return
  }
  const field = userSearchField.value
  filteredUsers.value = users.value.filter(row => {
    let val = row[field]
    if (field === 'uid') val = String(row.uid)
    if (!val) return false
    return String(val).toLowerCase().includes(key)
  })
}
const loadCircles = async () => {
  const id = getEnforcerId()
  if (!id) return
  circlesLoading.value = true
  try {
    const res = await listAdminDiaryCircles(id, circlePage.value, circlePageSize, circleSearchKey.value.trim())
    if (res?.success && res.data) {
      circles.value = res.data.list || []
      circleTotal.value = res.data.total || 0
    } else {
      ElMessage.error(res?.message || '加载动态失败')
    }
  } catch (e) {
    ElMessage.error('请求失败，请检查网络或后端服务')
  } finally {
    circlesLoading.value = false
  }
}
const searchCircles = () => {
  circlePage.value = 1
  loadCircles()
}
const confirmCancel = (row) => {
  ElMessageBox.confirm(
    `确定注销用户「${row.name}」？将清除其私人日记、草稿与好友关系，字迹圈动态保留并显示为「已注销用户」。`,
    '注销确认',
    { type: 'warning', confirmButtonText: '确认注销', cancelButtonText: '取消' }
  ).then(async () => {
    const res = await cancelAdminUser(getEnforcerId(), row.id)
    if (res?.success) {
      ElMessage.success('用户已注销')
      loadUsers()
    } else {
      ElMessage.error(res?.message || '注销失败')
    }
  }).catch(() => {})
}
const openBan = (row) => {
  banTarget.value = row
  banCircleId.value = null
  banForm.durationDays = 7
  banForm.reason = ''
  banForm.customReason = ''
  banVisible.value = true
}
const submitBan = async () => {
  if (!banForm.reason) {
    ElMessage.warning('请选择封禁理由')
    return
  }
  if (banForm.reason === '其他' && !banForm.customReason.trim()) {
    ElMessage.warning('请填写自定义封禁理由')
    return
  }
  banSubmitting.value = true
  try {
    const res = await banAdminUser(banTarget.value.id, {
      enforcerId: getEnforcerId(),
      durationDays: banForm.durationDays,
      reason: banForm.reason,
      customReason: banForm.customReason.trim() || null
    })
    if (res?.success) {
      ElMessage.success('封禁成功')
      banVisible.value = false
      if (banCircleId.value) {
        await hideDiaryCircle(banCircleId.value, getEnforcerId())
      }
      loadUsers()
      loadCircles()
    } else {
      ElMessage.error(res?.message || '封禁失败')
    }
  } finally {
    banSubmitting.value = false
  }
}
const openEdit = (row) => {
  editForm.id = row.id
  editForm.uid = row.uid
  editForm.phone = row.phone
  editForm.name = row.name
  editForm.email = row.email || ''
  editForm.birthday = row.birthday ? formatDateOnly(row.birthday).replace(/\//g, '-') : ''
  editForm.address = row.address || ''
  editVisible.value = true
}
const submitEdit = async () => {
  if (!editForm.name.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  editSubmitting.value = true
  try {
    const res = await updateAdminUser(editForm.id, {
      enforcerId: getEnforcerId(),
      name: editForm.name.trim(),
      email: editForm.email || null,
      birthday: editForm.birthday || null,
      address: editForm.address || null
    })
    if (res?.success) {
      ElMessage.success('修改成功')
      editVisible.value = false
      loadUsers()
    } else {
      ElMessage.error(res?.message || '修改失败')
    }
  } finally {
    editSubmitting.value = false
  }
}

const loadEnforcers = async () => {
  const id = getEnforcerId()
  if (!id) return
  enforcersLoading.value = true
  try {
    const res = await listAdminEnforcers(id)
    if (res?.success) {
      enforcers.value = res.data || []
    } else {
      ElMessage.error(res?.message || '加载管理员列表失败')
    }
  } catch {
    ElMessage.error('请求失败，请检查网络或后端服务')
  } finally {
    enforcersLoading.value = false
  }
}

const openAddEnforcer = () => {
  enforcerSearchKeyword.value = ''
  searchedUser.value = null
  newEnforcerPassword.value = ''
  addEnforcerVisible.value = true
}

const handleSearchUser = async () => {
  const keyword = enforcerSearchKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning("请输入搜索关键词")
    return
  }
  const id = getEnforcerId()
  if (!id) return
  enforcerSearchLoading.value = true
  searchedUser.value = null
  try {
    const res = await searchUsersByKeyword(id, enforcerSearchKeyword.value.trim())
    if (res?.success && res.data?.length) {
      searchedUser.value = res.data[0]
    } else {
      ElMessage.warning(res?.message || '未找到匹配的用户')
    }
  } catch {
    ElMessage.error('搜索失败')
  } finally {
    enforcerSearchLoading.value = false
  }
}

const submitAddEnforcer = async () => {
  if (!searchedUser.value || searchedUser.value.alreadyEnforcer) return
  const pwd = newEnforcerPassword.value
  if (!pwd) {
    ElMessage.warning('请设置密码')
    return
  }
  const id = getEnforcerId()
  if (!id) return
  addEnforcerSubmitting.value = true
  try {
    const res = await addAdminEnforcer(id, {
      phone: searchedUser.value.phone,
      password: pwd
    })
    if (res?.success) {
      ElMessage.success('管理员添加成功')
      addEnforcerVisible.value = false
      loadEnforcers()
    } else {
      ElMessage.error(res?.message || '添加失败')
    }
  } catch {
    ElMessage.error('请求失败')
  } finally {
    addEnforcerSubmitting.value = false
  }
}

const confirmDeleteEnforcer = (row) => {
  ElMessageBox.confirm(
    `确定删除管理员「${row.name}」（手机号 ${row.phone}）？`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  ).then(async () => {
    const res = await deleteAdminEnforcer(getEnforcerId(), row.id)
    if (res?.success) {
      ElMessage.success('管理员已删除')
      loadEnforcers()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  }).catch(() => {})
}

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('enforcerInfo')
    ElMessage.success('已退出登录')
    router.replace('/enforcer/login')
  }).catch(() => {})
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

const handleKeydown = (e) => {
  if (e.key === 'Escape') {
    // 如果有弹窗打开，先关闭弹窗
    if (banVisible.value) {
      banVisible.value = false
      e.stopImmediatePropagation()
      return
    }
    if (editVisible.value) {
      editVisible.value = false
      e.stopImmediatePropagation()
      return
    }
    if (banDetailVisible.value) {
      banDetailVisible.value = false
      e.stopImmediatePropagation()
      return
    }
    if (circleDetailVisible.value) {
      circleDetailVisible.value = false
      e.stopImmediatePropagation()
      return
    }
    if (addEnforcerVisible.value) {
      addEnforcerVisible.value = false
      e.stopImmediatePropagation()
      return
    }
    e.stopImmediatePropagation()
    logout()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown, true)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown, true)
})

</script>
<style scoped>
.enforcer-home {
  height: 100vh;
  overflow-y: auto;
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
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.dialog-user {
  margin: 0 0 16px;
  color: #606266;
}
.edit-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
.circle-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.circle-detail-user {
  font-weight: 600;
  font-size: 15px;
}
.circle-detail-time {
  color: #909399;
  font-size: 13px;
}
.circle-detail-content {
  line-height: 1.7;
  font-size: 14px;
  word-break: break-all;
}
.circle-detail-content img {
  max-width: 100%;
  border-radius: 6px;
  margin-top: 8px;
}
.circle-detail-comments h4 {
  margin: 0 0 10px;
  font-size: 14px;
}
.no-comments {
  color: #909399;
  font-size: 13px;
}
.comment-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}
.comment-item .comment-user {
  display: block;
  margin-bottom: 2px;
}
.comment-item .comment-text {
  display: block;
}
.comment-item .comment-time {
  display: block;
  margin-top: 2px;
}
.comment-user {
  font-weight: 600;
  margin-right: 8px;
}
.comment-text {
  margin-right: 8px;
}
.comment-time {
  color: #909399;
  font-size: 12px;
}
.reply-list {
  margin-top: 6px;
  padding-left: 20px;
}
.reply-item {
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 13px;
}
.reply-item .comment-user,
.reply-item .reply-to {
  display: inline;
}
.reply-item .comment-text {
  display: block;
}
.reply-item .comment-time {
  display: block;
  margin-top: 2px;
}
.reply-to {
  color: #909399;
  margin: 0 4px;
}

.enforcer-search-result {
  margin-bottom: 16px;
}
.enforcer-search-warn {
  color: #f56c6c;
  font-size: 13px;
  margin-top: 8px;
}
</style>








