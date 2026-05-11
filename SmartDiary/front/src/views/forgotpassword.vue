<template>
    <!-- 背景 -->
    <div class="container">
        <!-- 左侧栏 -->
        <div class="left-bar">
            <ul>
                <!-- 注册成功后的欢迎界面 -->
                <div v-if="resetSuccess" class="success-content">
                    <div class="success-title">正在进行数据修改...</div>
                    <div class="user-name">{{ form.nickname }}</div>
                    <div class="success-message">即将跳转到登录页面...</div>
                </div>
                
                <!-- 默认欢迎界面 -->
                <div v-else>
                    <div class="title">执笔人,不要忘记字迹!</div>
                    <div class="subtitle">这里有独属于你的足迹，<br>从遗忘中归来吧!</div>
                    <div class="welcome">让我们一起创造美好的文字世界！</div>
                </div>
            </ul>
        </div>
        
        <!-- 右侧栏 -->
        <div class="right-bar">
            <div class="logo">字迹-忘记密码</div>
            <div class="form">
                <div class="account-select">
                    <!-- 第一步：手机号验证 -->
                    <div v-if="step === 1" class="step-form">
                        <el-form @submit.prevent="validatePhoneAndCode">
                            <el-form-item label="手机号">
                                <el-input 
                                    type="tel" 
                                    v-model="form.phone" 
                                    placeholder="请输入手机号" 
                                    required
                                />
                            </el-form-item>
                            
                            <el-form-item label="验证码">
                                <div class="code-input-container">
                                    <el-input 
                                        type="text" 
                                        v-model="form.verificationCode" 
                                        placeholder="请输入验证码" 
                                        required 
                                    />
                                    <el-button 
                                        type="primary" 
                                        :disabled="isSendingCode || countdown > 0" 
                                        @click="sendVerificationCode"
                                    >
                                        {{ getCodeButtonText }}
                                    </el-button>
                                </div>
                            </el-form-item>
                            
                            <el-button 
                                type="primary" 
                                native-type="submit" 
                                class="reset-btn"
                            >
                                下一步
                            </el-button>
                        </el-form>
                    </div>
                    <!-- 第二步：重置密码 -->
                    <div v-else class="step-form">
                        <el-form @submit.prevent="resetPassword">
                            <el-form-item label="新密码">
                                <el-input 
                                    type="password" 
                                    v-model="form.newPassword" 
                                    placeholder="请输入新密码" 
                                    required
                                />
                            </el-form-item>
                            
                            <el-form-item label="确认新密码">
                                <el-input 
                                    type="password" 
                                    v-model="form.confirmNewPassword" 
                                    placeholder="请再次输入新密码" 
                                    required
                                />
                            </el-form-item>
                            
                            <div class="form-actions">
                                <el-button 
                                    type="info" 
                                    @click="step = 1"
                                >
                                    上一步
                                </el-button>
                                <el-button 
                                    type="primary" 
                                    native-type="submit"
                                >
                                    完成
                                </el-button>
                            </div>
                        </el-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElButton,
  ElMessage
} from 'element-plus'
import service, { forgotPassword } from '@/api/auth'

const router = useRouter()
const step = ref(1) // 1: 手机号验证
const isSendingCode = ref(false) // 是否正在发送验证码
const countdown = ref(0) // 倒计时
const resetSuccess = ref(false) // 重置成功状态

const form = reactive({
    phone: '',
    verificationCode: '',
    newPassword: '',
    confirmNewPassword: ''
})

// 获取验证码按钮文本
const getCodeButtonText = computed(() => {
    if (countdown.value > 0) {
        return `${countdown.value}秒后重发`
    }
    return '获取验证码'
})

// 发送验证码
const sendVerificationCode = async () => {
    // 简单的手机号格式验证
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(form.phone)) {
        ElMessage.error('请输入正确的手机号')
        return
    }
    
    isSendingCode.value = true
    try {
        // 忘记密码场景：先校验手机号是否已注册
        const checkResponse = await service.post('/auth/check-phone', { phone: form.phone })
        if (checkResponse.success) {
            // success=true 表示手机号未被注册（注册流程使用），此处视为未注册
            ElMessage.error('该手机号未注册')
            return
        }
        
        // 直接发送验证码（后台会校验频率/调试模式）
        const response = await service.post('/auth/send-verification-code', { phone: form.phone })
        
        if (response.success) {
            ElMessage.success('验证码已发送')
            if (response.debug) {
                console.log('【调试模式】验证码:', response.debug)
            }
            startCountdown()
        } else {
            ElMessage.error(response.message || '发送验证码失败')
        }
    } catch (error) {
        console.error('发送验证码失败:', error)
        if (error.response && error.response.data && error.response.data.message) {
            ElMessage.error(error.response.data.message)
        } else {
            ElMessage.error('发送验证码失败，请稍后重试')
        }
    } finally {
        isSendingCode.value = false
    }
}

// 开始倒计时
const startCountdown = () => {
    countdown.value = 60
    const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
            clearInterval(timer)
        }
    }, 1000)
}

// 验证手机号和验证码
const validatePhoneAndCode = async () => {
    // 手机号格式验证
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(form.phone)) {
        ElMessage.error('请输入正确的手机号')
        return
    }
    
    // 验证码验证
    if (!form.verificationCode || form.verificationCode.length !== 6) {
        ElMessage.error('请输入6位验证码')
        return
    }
    
    try {
        const response = await service.post('/auth/verify-code', {
            phone: form.phone,
            verificationCode: form.verificationCode
        })
        
        if (response.success) {
            step.value = 2
        } else {
            ElMessage.error(response.message || '验证码错误或已过期')
        }
    } catch (error) {
        console.error('验证验证码失败:', error)
        if (error.response && error.response.data && error.response.data.message) {
            ElMessage.error(error.response.data.message)
        } else {
            ElMessage.error('验证失败，请稍后重试')
        }
    }
}

// 重置密码处理
const resetPassword = async () => {
    // 验证密码是否一致
    if (form.newPassword !== form.confirmNewPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
    }
    
    // 密码强度验证（至少8位，包含字母和数字）
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/
    if (!passwordRegex.test(form.newPassword)) {
        ElMessage.error('密码至少8位，且必须包含字母和数字')
        return
    }
    
    try {
        // 调用后端API重置密码
        const response = await forgotPassword({
            phone: form.phone,
            newPassword: form.newPassword,
            verificationCode: form.verificationCode
        })
        
        if (response.success) {
            // 重置成功
            resetSuccess.value = true
            
            // 3秒后跳转到登录页面
            setTimeout(() => {
                router.push('/') 
            }, 3000)
        } else {
            ElMessage.error(response.message || '密码重置失败')
        }
    } catch (error) {
        console.error('重置密码失败:', error)
        if (error.response && error.response.data && error.response.data.message) {
            ElMessage.error(error.response.data.message)
        } else {
            ElMessage.error('密码重置失败，请稍后重试')
        }
    }
}
</script>

<style scoped>
.container {
    
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    gap: 10px;
    padding: 20px;
    box-sizing: border-box;
    background: linear-gradient(135deg, #2985fe, #5ed1ff);
}
.left-bar {
    flex: 1;
    max-width: 55%;
}

.left-bar::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 6px;
    height: 100%;
    background: rgba(0, 0, 0, 0.1);
    z-index: 1;
}

.title {
    font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
    font-size: 50px;
    background: linear-gradient(90deg, #ff7e5f, #3a0885);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin: auto;
    margin-top: 90px;
    margin-left: -10%;
}

.subtitle {
    font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
    font-size: 60px;
    background: linear-gradient(90deg,#3a0885,#fabcac);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin: auto;
    margin-top: 70px;
    margin-left: -10%;
}

.welcome {
    font-size: 40px;
    font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
    background: linear-gradient(90deg,#ffffff,#e144e4);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin: auto;
    margin-top: 70px;
    margin-left: -10%;
}

.right-bar {
    flex: 1;
    max-width: 400px;
    display: flex;
    flex-direction: column;
    width: 400px;
    padding: 20px;
    height: 60vh;
}

.right-bar .logo {
    font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
    font-size: 40px;
    background: linear-gradient(90deg, #ff7e5f, #3a0885);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin: 20px auto;
    text-align: center;
}

.form {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.reset-btn {
    width: 100%;
    padding: 12px;
    background: linear-gradient(135deg, #2985fe, #5ed1ff);
    color: white;
    border: none;
    border-radius: 5px;
    font-size: 18px;
    cursor: pointer;
    margin-top: 10px;
}

.reset-btn:hover {
    opacity: 0.9;
    background: #badfff;
}
.step-form h2 {
    text-align: center;
    margin-bottom: 20px;
    color: #333;
}
.form-actions {
    display: flex;
    gap: 10px;
    justify-content: space-between;
}

.form-actions :deep(.el-button) {
    flex: 1;
}

.verification-group {
    margin-bottom: 15px;
}

.code-input-container {
    display: flex;
    gap: 10px;
}

.code-input-container :deep(.el-input) {
    flex: 1;
}

.success-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 100%;
    text-align: center;
    margin-left: -40%;
}

.success-title {
    font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
    font-size: 40px;
    background: linear-gradient(90deg, #ff7e5f, #3a0885);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 30px;
}

.user-name {
    font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
    font-size: 40px;
    background: linear-gradient(90deg, #3a0885, #fabcac);
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