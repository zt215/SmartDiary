<template>
  <div class="auth-login-page">
    <div class="video-background">
      <video autoplay muted loop playsinline class="background-video">
        <source src="/背景.mp4" type="video/mp4" />
      </video>
      <div class="video-overlay" />
    </div>

    <div class="left">
      <div class="left-copy" :class="{ show: showLeft }">
        <h1 class="title">字迹维护</h1>
        <h1 class="subtitle">执法监察...</h1>
        <h2 class="welcome">——欢迎你，执法者</h2>
      </div>
    </div>

    <div class="right" :class="{ show: showRight }">
      <h1 class="logo logo-enforcer">字迹—执法堂</h1>
      <form ref="loginFormRef" class="form" @submit.prevent="submitLogin">
        <div class="field">
          <el-input
            v-model="account"
            placeholder="手机号 / UID / 邮箱"
          />
        </div>
        <div class="field">
          <el-input
            v-model="password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
          >
            <template #suffix>
              <el-button link @click="showPassword = !showPassword">
                {{ showPassword ? '隐藏' : '显示' }}
              </el-button>
            </template>
          </el-input>
        </div>
        <el-button
          native-type="submit"
          class="submit-btn"
          :loading="loading"
        >
          进入执法堂
        </el-button>
        <div class="bottom">
          <el-button link @click="$router.push('/enforcer/forgotpassword')">忘记密码</el-button>
          <el-button link @click="$router.push('/')">返回字迹登录</el-button>
        </div>
        <button type="submit" class="login-submit-hidden" tabindex="-1" aria-hidden="true">登录</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { enforcerLogin } from '@/api/enforcer'
import { useLoginEnter } from '@/utils/loginEnter'
import '@/styles/auth-login-shell.css'

const router = useRouter()
const loginFormRef = ref(null)
const account = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const showLeft = ref(false)
const showRight = ref(false)

onMounted(() => {
  setTimeout(() => { showLeft.value = true }, 200)
  setTimeout(() => { showRight.value = true }, 900)
})

const submitLogin = async () => {
  if (!account.value.trim() || !password.value.trim()) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await enforcerLogin({
      account: account.value.trim(),
      password: password.value
    })
    if (res?.success && res.data) {
      localStorage.setItem('enforcerInfo', JSON.stringify(res.data))
      ElMessage.success('登录成功')
      router.replace('/enforcer/home')
    } else {
      ElMessage.error(res?.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error(e?.message || '网络错误')
  } finally {
    loading.value = false
  }
}

useLoginEnter(loginFormRef, () => submitLogin())
</script>

<style scoped>
.form {
  border: none;
  margin: 0;
  padding: 0;
  width: 100%;
}
.login-submit-hidden {
  position: absolute;
  width: 0;
  height: 0;
  padding: 0;
  margin: 0;
  border: 0;
  opacity: 0;
  pointer-events: none;
}
</style>
