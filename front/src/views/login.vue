<template>
  <div class="background login">
    <!-- 左侧栏 -->
    <div class="left">
      <h1 class="title" :class="{ show: showTitle }">时光流转</h1>
      <h1 class="subtitle" :class="{ show: showSubtitle }">故事待书...</h1>
      <h2 class="welcome" :class="{ show: showWelcome }">--欢迎你,执笔人</h2>
    </div>
    <!-- 右侧栏 -->
    <div class="right" :class="{ show: showRight }">
      <h1 class="logo">字迹</h1>
      <div class="form">
        <div class="account-select">
          <el-input
            v-model="account"
            placeholder="请输入手机号"
            style="width: 100%"
            @keyup.enter="login"
          >
            <template #prefix>
              <span style="padding-left: 10px; display: flex; align-items: center;">📱</span>
            </template>
          </el-input>
        </div>
        <div class="password-box">
          <el-input
            :type="showPassword ? 'text' : 'password'"
            v-model="password"
            placeholder="请输入密码"
            style="width: 100%"
            @keyup.enter="login"
          >
            <template #prefix>
              <span style="padding-left: 10px; display: flex; align-items: center;">🔑</span>
            </template>
            <template #suffix>
              <el-button link @click="showPassword = !showPassword">
                <span v-if="showPassword">
                  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#888" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                </span>
                <span v-else>
                  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#888" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-7 0-11-7-11-7a21.07 21.07 0 0 1 5.06-6.06"/><path d="M1 1l22 22"/><path d="M9.53 9.53A3 3 0 0 0 12 15a3 3 0 0 0 2.47-5.47"/></svg>
                </span>
              </el-button>
            </template>
          </el-input>
        </div>
        <el-button
          type="primary"
          style="width: 44%; height: 40px; margin-top: 30px; border-radius: 20px; font-size: 18px;"
          @click="login"
        >开始跃迁</el-button>
      </div>
      <div class="bottom">
        <el-button link @click="$router.push('/forgotpassword')">忘记密码</el-button>
        <el-button link @click="$router.push('/register')">注册</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { login as apiLogin } from '../api/auth'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

// 页面状态
const showTitle = ref(false)
const showSubtitle = ref(false)
const showWelcome = ref(false)
const showRight = ref(false)

// 登录表单数据
const account = ref('')
const password = ref('')
const showPassword = ref(false)

const router = useRouter()

// 页面加载时的动画效果
onMounted(() => {
  setTimeout(() => { showTitle.value = true }, 200)
  setTimeout(() => { showSubtitle.value = true }, 800)
  setTimeout(() => { showWelcome.value = true }, 1400)
  setTimeout(() => { showRight.value = true }, 2000)
})

// 登录方法
const login = async () => {
  // 登录请求发送
  try {
    const res = await apiLogin({
      account: account.value,
      password: password.value
    })
    
    if (res && res.success) {
      // 保存用户信息到localStorage
      if (res.data) {
        localStorage.setItem('userInfo', JSON.stringify(res.data))
      }
      // 登录成功直接跳转，并携带参数
      router.push('/home?login=success')
    } else {
      ElMessage.error(res.message || '账号或密码错误')
    }
  } catch (e) {
    console.error('Login error:', e)
    if (e.response) {
      // 服务器返回了错误响应
      ElMessage.error(e.response.data.message || '登录失败: ' + e.response.status)
    } else if (e.request) {
      // 请求已发出但没有收到响应
      ElMessage.error('网络错误，请检查服务器是否运行正常')
    } else {
      // 其他错误
      ElMessage.error('登录失败: ' + e.message)
    }
  }
}
</script>
<style scoped>
.background {
  background-color: white;
  height: 92vh;
  overflow: hidden;
}
.left {
  width: 50%;
  height: 95vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 0;
  left: 0;
}
.title {
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  font-size: 80px;
  background: linear-gradient(90deg, #ff7e5f, #3a0885);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: auto;
  margin-bottom: 0;
}
.subtitle {
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  font-size: 80px;
  background: linear-gradient(90deg,#3a0885,#fabcac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: auto;
  margin-top: 40px;
}
.welcome {
  font-size: 30px;
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  background: linear-gradient(90deg,#3d8be4,#fabcac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: auto;
  margin-top: 0;
  margin-left: 70%;
}
.right {
  width: 40%;
  height: 95vh;
  background: linear-gradient(135deg, #2985fe, #5ed1ff);
  border-top-left-radius: 100px;
  border-bottom-left-radius: 100px;
  padding: 20px;
  position: absolute;
  top: 0;
  right: 20px;
  opacity: 0;
  transform: translateX(100px);
  transition: opacity 1s, transform 1s;
}
.title, .subtitle, .welcome {
  opacity: 0;
  transition: opacity 1s;
}
.title.show, .subtitle.show, .welcome.show {
  opacity: 1;
}
.right.show {
  opacity: 1;
  transform: translateX(0);
}
.logo {
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  font-size: 90px;
  background: linear-gradient(90deg,#ffffff,#d32ce6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-align: center;
  margin-top: 30%;
}
.form {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.account-select {
  position: relative;
  width: 60%;
  margin-top: 5%;
  display: flex;
  align-items: center;
}
.password-box {
  position: relative;
  width: 60%;
  margin-top: 20px;
  display: flex;
  align-items: center;
}
.bottom{
  display: flex;
  align-items: center;
  margin-top: 10%;
  justify-content: space-between;
  width: 60%;
  margin-left: auto;
  margin-right: auto;
}
.bottom :deep(.el-button) {
  font-size: 20px;
  color: #fff;
}
</style>