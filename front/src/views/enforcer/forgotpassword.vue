<template>
  <div class="container">
    <div class="video-background">
      <video autoplay muted loop playsinline class="background-video">
        <source src="/背景.mp4" type="video/mp4" />
      </video>
      <div class="video-overlay" />
    </div>
    <div class="left-bar">
      <div v-if="resetSuccess" class="success-content">
        <div class="success-title">密码已更新</div>
        <div class="success-message">即将返回执法者登录…</div>
      </div>
      <div v-else>
        <div class="title">执法者，请牢记凭证</div>
        <div class="subtitle">重置后将使用新密码<br />进入执法堂</div>
      </div>
    </div>
    <div class="right-bar">
      <div class="logo">字迹—执法者 · 忘记密码</div>
      <div class="form auth-transparent-form">
        <div v-if="step === 1" class="step-form">
          <el-form @submit.prevent="goNext">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="执法者绑定手机号" />
            </el-form-item>
            <el-form-item label="验证码">
              <div class="code-row">
                <el-input v-model="form.verificationCode" placeholder="验证码" />
                <el-button
                  type="primary"
                  :disabled="isSendingCode || countdown > 0"
                  @click="sendCode"
                >
                  {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <div class="form-actions">
              <el-button @click="router.push('/enforcer/login')">返回</el-button>
              <el-button type="primary" native-type="submit">下一步</el-button>
            </div>
          </el-form>
        </div>
        <div v-else class="step-form">
          <el-form @submit.prevent="resetPwd">
            <el-form-item label="新密码">
              <el-input v-model="form.newPassword" type="password" placeholder="新密码" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="form.confirmNewPassword" type="password" placeholder="再次输入" />
            </el-form-item>
            <div class="form-actions">
              <el-button @click="step = 1">返回</el-button>
              <el-button type="primary" native-type="submit">完成</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  enforcerForgotPassword,
  enforcerSendCode,
  enforcerCheckPhone
} from '@/api/enforcer'
import '@/styles/auth-transparent-form.css'

const router = useRouter()
const step = ref(1)
const isSendingCode = ref(false)
const countdown = ref(0)
const resetSuccess = ref(false)
const form = reactive({
  phone: '',
  verificationCode: '',
  newPassword: '',
  confirmNewPassword: ''
})

const sendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.error('请输入正确手机号')
    return
  }
  isSendingCode.value = true
  try {
    const check = await enforcerCheckPhone({ phone: form.phone })
    if (!check?.success) {
      ElMessage.error(check?.message || '该手机号未绑定执法者')
      return
    }
    const res = await enforcerSendCode({ phone: form.phone })
    if (res?.success) {
      ElMessage.success('验证码已发送')
      if (res.debug) console.log('【调试】验证码:', res.debug)
      countdown.value = 60
      const t = setInterval(() => {
        countdown.value -= 1
        if (countdown.value <= 0) clearInterval(t)
      }, 1000)
    } else {
      ElMessage.error(res?.message || '发送失败')
    }
  } finally {
    isSendingCode.value = false
  }
}

const goNext = () => {
  if (!form.phone || !form.verificationCode) {
    ElMessage.warning('请填写手机号和验证码')
    return
  }
  step.value = 2
}

const resetPwd = async () => {
  if (!form.newPassword || form.newPassword !== form.confirmNewPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  try {
    const res = await enforcerForgotPassword({
      phone: form.phone,
      newPassword: form.newPassword,
      verificationCode: form.verificationCode
    })
    if (res?.success) {
      resetSuccess.value = true
      setTimeout(() => router.push('/enforcer/login'), 2000)
    } else {
      ElMessage.error(res?.message || '重置失败')
    }
  } catch {
    ElMessage.error('网络错误')
  }
}
</script>

<style scoped>
.container {
  position: relative;
  min-height: 100vh;
  display: flex;
}
.video-background {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
}
.background-video {
  position: absolute;
  top: 50%;
  left: 50%;
  min-width: 100%;
  min-height: 100%;
  transform: translate(-50%, -50%);
  object-fit: cover;
}
.video-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.25);
}
.left-bar {
  position: relative;
  z-index: 1;
  flex: 1;
  max-width: 55%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #f8f0e8;
}
.left-bar .title {
  font-size: 50px;
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  background: linear-gradient(90deg, #ff7e5f, #3a0885);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  text-align: center;
}
.left-bar .subtitle {
  font-size: 60px;
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  background: linear-gradient(90deg, #3a0885, #fabcac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 20px 0 0;
  text-align: center;
}
.right-bar {
  position: relative;
  z-index: 1;
  flex: 1;
  max-width: 450px;
  width: 450px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.logo {
  font-size: 34px;
  white-space: nowrap;
  text-align: center;
  margin: 0 auto 24px;
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  background: linear-gradient(90deg, #ff7e5f, #3a0885);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.form {
  width: 100%;
}
.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.code-row .el-button {
  min-width: 110px;
}
.form-actions {
  display: flex;
  gap: 10px;
  justify-content: space-between;
  margin-top: 16px;
}
.form-actions :deep(.el-button) {
  flex: 1;
}
.success-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  justify-content: center;
  align-items: center;
  height: 100%;
}
.success-title {
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  font-size: 34px;
  white-space: nowrap;
  background: linear-gradient(90deg, #ff7e5f, #3a0885);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 30px;
}
.success-message {
  font-size: 24px;
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  background: linear-gradient(90deg, #ffffff, #e144e4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
</style>


