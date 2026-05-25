<template>
  <div class="settings-container">
    <!-- 顶部工具栏 -->
    <header class="settings-header">
      <div class="header-left">
        <el-button text @click="goBack">
          ← 返回
        </el-button>
      </div>
      <div class="header-center">
        <h2 class="settings-title">{{ isEditing ? '编辑信息' : '个人设置' }}</h2>
      </div>
      <div class="header-right">
        <el-button v-if="!isEditing" @click="enterEditMode">编辑</el-button>
        <template v-else>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="saveUserInfo" :loading="saving">保存</el-button>
        </template>
      </div>
    </header>

    <!-- 内容区域 -->
    <main class="settings-main">
      <div class="settings-content">
        <!-- 用户信息卡片 -->
        <div class="info-card">
          <h3 class="card-title">基本信息</h3>
          
          <!-- 查看模式 -->
          <template v-if="!isEditing">
            <!-- 头像显示 -->
            <div class="avatar-section">
              <div class="avatar-container">
                <img 
                  v-if="user.avatar" 
                  :src="user.avatar" 
                  alt="头像" 
                  class="avatar-image"
                />
                <div v-else class="avatar-placeholder">
                  {{ user.name ? user.name.charAt(0).toUpperCase() : 'U' }}
                </div>
              </div>
            </div>
            <div class="info-item">
              <label class="info-label">账号 UID</label>
              <div class="info-value uid-value">{{ user.uid ?? '未分配' }}</div>
            </div>
            <div class="info-item">
              <label class="info-label">用户名</label>
              <div class="info-value">{{ user.name || '未设置' }}</div>
            </div>
            <div class="info-item">
              <label class="info-label">手机号</label>
              <div class="info-value">{{ user.phone || '未设置' }}</div>
            </div>
            <div class="info-item">
              <label class="info-label">邮箱</label>
              <div class="info-value">{{ user.email || '未设置' }}</div>
            </div>
            <div class="info-item">
              <label class="info-label">生日</label>
              <div class="info-value">{{ formatDate(user.birthday) || '未设置' }}</div>
            </div>
            <div class="info-item">
              <label class="info-label">地址</label>
              <div class="info-value">{{ formatRegionAddressLabel(user.address) || '未设置' }}</div>
            </div>
          </template>

          <!-- 编辑模式 -->
          <template v-else>
            <el-form :model="editForm" label-width="100px" label-position="left">
              <!-- 头像上传 -->
              <el-form-item label="头像">
                <div class="avatar-upload-section">
                  <div class="avatar-preview-container">
                    <img 
                      v-if="editForm.avatar" 
                      :src="editForm.avatar" 
                      alt="头像预览" 
                      class="avatar-preview"
                    />
                    <div v-else class="avatar-preview-placeholder">
                      {{ editForm.name ? editForm.name.charAt(0).toUpperCase() : 'U' }}
                    </div>
                  </div>
                  <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                    accept="image/*"
                  >
                    <el-button type="primary" size="small">选择图片</el-button>
                  </el-upload>
                  <div class="upload-tip">支持 JPG、PNG 格式，大小不超过 2MB</div>
                </div>
              </el-form-item>
              <el-form-item label="账号 UID">
                <el-input :model-value="user.uid != null ? String(user.uid) : ''" disabled />
                <div class="form-tip">UID 由系统自动分配，不可修改</div>
              </el-form-item>
              <el-form-item label="用户名">
                <el-input v-model="editForm.name" placeholder="请输入用户名" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="editForm.email" type="email" placeholder="选填" clearable />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="editForm.phone" placeholder="请输入手机号" disabled />
                <div class="form-tip">手机号不可修改</div>
              </el-form-item>
              <el-form-item label="生日">
                <el-date-picker
                  v-model="editForm.birthday"
                  type="date"
                  placeholder="选择生日"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="所在地区">
                <el-cascader
                  v-model="addressCascaderValue"
                  :options="regionData"
                  :props="{ expandTrigger: 'hover' }"
                  placeholder="请选择省 / 市 / 区"
                  clearable
                  filterable
                  style="width: 100%"
                />
              </el-form-item>
            </el-form>
          </template>
        </div>

        <!-- 隐私设置 -->
        <div class="info-card privacy-card">
          <h3 class="card-title">隐私设置</h3>
          <p class="privacy-desc">UID 始终可被他人搜索；手机号、邮箱可按需隐藏或限制搜索。</p>
          <div class="privacy-item">
            <div class="privacy-label-wrap">
              <span class="privacy-label">允许通过手机号搜索到我</span>
              <span class="privacy-hint">关闭后，他人无法用手机号查找到你</span>
            </div>
            <el-switch
              v-model="privacyForm.allowPhoneSearch"
              :loading="privacySaving"
              @change="savePrivacySettings"
            />
          </div>
          <div class="privacy-item">
            <div class="privacy-label-wrap">
              <span class="privacy-label">对好友隐藏手机号</span>
              <span class="privacy-hint">开启后，好友资料中不显示你的手机号</span>
            </div>
            <el-switch
              v-model="privacyForm.hidePhone"
              :loading="privacySaving"
              @change="savePrivacySettings"
            />
          </div>
          <div class="privacy-item">
            <div class="privacy-label-wrap">
              <span class="privacy-label">对好友隐藏邮箱</span>
              <span class="privacy-hint">开启后，好友资料中不显示你的邮箱</span>
            </div>
            <el-switch
              v-model="privacyForm.hideEmail"
              :loading="privacySaving"
              @change="savePrivacySettings"
            />
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-card">
          <h3 class="card-title">账户操作</h3>
          <div class="action-buttons">
            <el-button type="default" @click="handleLogout" class="action-btn">
              退出登录
            </el-button>
            <el-button type="danger" @click="handleDeleteAccount" class="action-btn">
              注销账号
            </el-button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { regionData } from 'element-china-area-data'
import { updateUser, deleteUser } from '../api/auth'
import {
  formatRegionAddressLabel,
  parseRegionCodesToCascaderValue,
  cascaderValueToStorage,
  isStoredRegionCodes
} from '../utils/regionAddress'

export default {
  name: 'SettingsView',
  setup() {
    const router = useRouter()
    const user = ref({
      id: null,
      uid: null,
      name: '',
      phone: '',
      email: '',
      birthday: null,
      address: '',
      avatar: ''
    })
    const isEditing = ref(false)
    const saving = ref(false)
    const editForm = ref({
      name: '',
      phone: '',
      email: '',
      birthday: '',
      address: '',
      avatar: ''
    })

    const addressCascaderValue = ref([])
    const privacyForm = ref({
      allowPhoneSearch: true,
      hidePhone: false,
      hideEmail: false
    })
    const privacySaving = ref(false)

    const syncPrivacyForm = () => {
      privacyForm.value = {
        allowPhoneSearch: user.value.allowPhoneSearch !== false && user.value.allowPhoneSearch !== 0,
        hidePhone: user.value.hidePhone === true || user.value.hidePhone === 1,
        hideEmail: user.value.hideEmail === true || user.value.hideEmail === 1
      }
    }

    // 加载用户信息
    const loadUserInfo = () => {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try {
          user.value = JSON.parse(userInfo)
          syncPrivacyForm()
        } catch (e) {
          console.error('解析用户信息失败:', e)
          ElMessage.error('用户信息解析失败')
        }
      } else {
        ElMessage.warning('用户信息不存在，请重新登录')
        router.push('/')
      }
    }

    // 格式化日期
    const formatDate = (date) => {
      if (!date) return ''
      if (typeof date === 'string') {
        date = new Date(date)
      }
      return date.toLocaleDateString('zh-CN')
    }

    // 进入编辑模式
    const enterEditMode = () => {
      isEditing.value = true
      editForm.value = {
        name: user.value.name || '',
        phone: user.value.phone || '',
        email: user.value.email || '',
        birthday: user.value.birthday ? (typeof user.value.birthday === 'string' ? user.value.birthday : new Date(user.value.birthday).toISOString().split('T')[0]) : '',
        address: user.value.address || '',
        avatar: user.value.avatar || ''
      }
      addressCascaderValue.value = parseRegionCodesToCascaderValue(user.value.address || '')
    }

    // 取消编辑
    const cancelEdit = () => {
      isEditing.value = false
      addressCascaderValue.value = []
      editForm.value = {
        name: '',
        phone: '',
        email: '',
        birthday: '',
        address: ''
      }
    }

    // 压缩图片
    const compressImage = (file, maxWidth = 400, maxHeight = 400, quality = 0.8) => {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = (e) => {
          const img = new Image()
          img.onload = () => {
            const canvas = document.createElement('canvas')
            let width = img.width
            let height = img.height

            // 计算压缩后的尺寸
            if (width > height) {
              if (width > maxWidth) {
                height = (height * maxWidth) / width
                width = maxWidth
              }
            } else {
              if (height > maxHeight) {
                width = (width * maxHeight) / height
                height = maxHeight
              }
            }

            canvas.width = width
            canvas.height = height

            const ctx = canvas.getContext('2d')
            ctx.drawImage(img, 0, 0, width, height)

            // 转换为base64
            const base64 = canvas.toDataURL('image/jpeg', quality)
            resolve(base64)
          }
          img.onerror = reject
          img.src = e.target.result
        }
        reader.onerror = reject
        reader.readAsDataURL(file)
      })
    }

    // 头像上传前验证
    const beforeAvatarUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('图片大小不能超过 2MB!')
        return false
      }
      return true
    }

    // 处理头像上传
    const handleAvatarUpload = async (options) => {
      const file = options.file
      try {
        // 压缩图片后再转换为base64
        const compressedBase64 = await compressImage(file)
        editForm.value.avatar = compressedBase64
        ElMessage.success('头像已选择，请点击保存')
      } catch (e) {
        console.error('图片处理错误:', e)
        ElMessage.error('图片处理失败，请重试')
      }
    }

    // 保存用户信息
    const saveUserInfo = async () => {
      if (!editForm.value.name || editForm.value.name.trim() === '') {
        ElMessage.warning('用户名不能为空')
        return
      }

      saving.value = true
      try {
        const rawAddr = user.value.address || ''
        let addressStored = cascaderValueToStorage(addressCascaderValue.value)
        if (!addressStored && rawAddr && !isStoredRegionCodes(rawAddr)) {
          addressStored = rawAddr
        }
        if (!addressStored) {
          addressStored = null
        }
        const res = await updateUser({
          id: user.value.id,
          name: editForm.value.name.trim(),
          phone: editForm.value.phone,
          email: editForm.value.email?.trim() || null,
          birthday: editForm.value.birthday || null,
          address: addressStored,
          avatar: editForm.value.avatar || null
        })

        if (res && res.success) {
          ElMessage.success('信息更新成功')
          const updated = res.data ? { ...res.data } : {}
          user.value = {
            ...user.value,
            ...updated,
            name: editForm.value.name.trim(),
            email: editForm.value.email?.trim() || null,
            birthday: editForm.value.birthday || null,
            address: addressStored,
            avatar: editForm.value.avatar || null
          }
          // 更新localStorage
          localStorage.setItem('userInfo', JSON.stringify(user.value))
          // 退出编辑模式
          isEditing.value = false
        } else {
          ElMessage.error(res?.message || '更新失败')
        }
      } catch (e) {
        console.error('更新用户信息错误:', e)
        ElMessage.error('更新失败: ' + (e.message || '网络错误'))
      } finally {
        saving.value = false
      }
    }

    // 退出登录
    const handleLogout = () => {
      ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        // 清除用户信息和token
        localStorage.removeItem('userInfo')
        localStorage.removeItem('token')
        localStorage.removeItem('diary-theme')
        
        ElMessage.success('已退出登录')
        // 跳转到登录页
        router.push('/')
      }).catch(() => {
        // 用户取消
      })
    }

    // 注销账号
    const handleDeleteAccount = () => {
      ElMessageBox.confirm(
        '注销账号后，您的所有数据将被永久删除，此操作不可恢复！确定要注销账号吗？',
        '警告',
        {
          confirmButtonText: '确定注销',
          cancelButtonText: '取消',
          type: 'error',
          dangerouslyUseHTMLString: false
        }
      ).then(() => {
        // 再次确认
        return ElMessageBox.confirm(
          '请再次确认：注销账号将删除您的所有日记和数据，此操作不可恢复！',
          '最后确认',
          {
            confirmButtonText: '我确定注销',
            cancelButtonText: '取消',
            type: 'error'
          }
        )
      }).then(async () => {
        try {
          const res = await deleteUser(user.value.id)
          if (res && res.success) {
            ElMessage.success('账号已注销')
            // 清除所有本地存储
            localStorage.removeItem('userInfo')
            localStorage.removeItem('token')
            localStorage.removeItem('diary-theme')
            // 跳转到登录页
            router.push('/')
          } else {
            ElMessage.error(res?.message || '注销失败')
          }
        } catch (e) {
          console.error('注销账号错误:', e)
          ElMessage.error('注销失败: ' + (e.message || '网络错误'))
        }
      }).catch(() => {
        // 用户取消
      })
    }

    // 保存隐私设置
    const savePrivacySettings = async () => {
      if (!user.value.id) return
      privacySaving.value = true
      try {
        const res = await updateUser({
          id: user.value.id,
          name: user.value.name,
          allowPhoneSearch: privacyForm.value.allowPhoneSearch,
          hidePhone: privacyForm.value.hidePhone,
          hideEmail: privacyForm.value.hideEmail
        })
        if (res && res.success) {
          ElMessage.success('隐私设置已保存')
          if (res.data) {
            user.value = { ...user.value, ...res.data }
          } else {
            user.value = {
              ...user.value,
              allowPhoneSearch: privacyForm.value.allowPhoneSearch,
              hidePhone: privacyForm.value.hidePhone,
              hideEmail: privacyForm.value.hideEmail
            }
          }
          localStorage.setItem('userInfo', JSON.stringify(user.value))
        } else {
          ElMessage.error(res?.message || '保存失败')
          syncPrivacyForm()
        }
      } catch (e) {
        console.error('保存隐私设置错误:', e)
        ElMessage.error('保存失败')
        syncPrivacyForm()
      } finally {
        privacySaving.value = false
      }
    }

    // 返回
    const goBack = () => {
      router.back()
    }

    onMounted(() => {
      loadUserInfo()
    })

    return {
      user,
      isEditing,
      saving,
      editForm,
      regionData,
      addressCascaderValue,
      privacyForm,
      privacySaving,
      formatRegionAddressLabel,
      formatDate,
      enterEditMode,
      cancelEdit,
      saveUserInfo,
      savePrivacySettings,
      beforeAvatarUpload,
      handleAvatarUpload,
      handleLogout,
      handleDeleteAccount,
      goBack
    }
  }
}
</script>

<style scoped>
.settings-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.settings-header {
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
  width: 120px;
}

.header-right {
  justify-content: flex-end;
}

.header-center {
  flex: 1;
  text-align: center;
}

.settings-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 500;
  color: #333;
}

.settings-main {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
}

.settings-content {
  max-width: 800px;
  margin: 0 auto;
}

.info-card,
.action-card,
.privacy-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.privacy-desc {
  margin: -0.5rem 0 1.25rem;
  font-size: 0.9rem;
  color: #888;
  line-height: 1.5;
}

.privacy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.privacy-item:last-child {
  border-bottom: none;
}

.privacy-label-wrap {
  flex: 1;
  min-width: 0;
}

.privacy-label {
  display: block;
  font-weight: 500;
  color: #333;
}

.privacy-hint {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.85rem;
  color: #999;
  line-height: 1.4;
}

.card-title {
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #333;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 0.75rem;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  width: 100px;
  font-weight: 500;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #333;
  font-size: 1rem;
}

.edit-btn {
  margin-left: auto;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 1rem;
}

.avatar-container {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 1rem;
  border: 3px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  color: #999;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.edit-avatar-btn {
  margin-top: 0.5rem;
}

.avatar-upload-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1rem;
}

.avatar-preview-container {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  color: #999;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.upload-tip {
  font-size: 0.875rem;
  color: #999;
  margin-top: -0.5rem;
}

.form-tip {
  font-size: 0.875rem;
  color: #999;
  margin-top: 0.25rem;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.action-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-main {
    padding: 1rem;
  }

  .settings-header {
    padding: 0.75rem 1rem;
  }

  .settings-title {
    font-size: 1.2rem;
  }

  .info-card,
  .action-card {
    padding: 1.5rem;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-label {
    margin-bottom: 0.5rem;
  }
}
</style>
