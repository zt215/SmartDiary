<template>
    <!-- 背景 -->
    <div class="container">
        <!-- 左侧栏 -->
        <div class="left-bar">
            <ul>
                <!-- 注册成功后的欢迎界面 -->
                <div v-if="registerSuccess" class="success-content">
                    <div class="success-title">正在进行转生数据验证...</div>
                    <div class="user-name">{{ form.nickname }}</div>
                    <div class="success-message">即将跳转到登录页面...</div>
                </div>
                
                <!-- 默认欢迎界面 -->
                <div v-else>
                    <div class="title">欢迎来到字迹,执笔人</div>
                    <div class="subtitle">留下你的足迹，<br>开始你的书写之旅!</div>
                    <div class="welcome">让我们一起创造美好的文字世界！</div>
                </div>
            </ul>
        </div>
        
        <!-- 右侧栏 -->
        <div class="right-bar">
            <div class="logo">字迹-注册</div>
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
                                class="register-btn"
                            >
                                下一步
                            </el-button>
                        </el-form>
                    </div>
                    
                    <!-- 第二步：详细信息填写 -->
                    <div v-else class="step-form">
                        <el-form @submit.prevent="handleRegister">
                            <el-form-item label="昵称">
                                <el-input 
                                    type="text" 
                                    v-model="form.nickname" 
                                    placeholder="请输入昵称" 
                                    required
                                />
                            </el-form-item>
                            
                            <el-form-item label="生日">
                                <el-date-picker
                                    v-model="form.birthday"
                                    type="date"
                                    placeholder="请选择生日"
                                    format="YYYY-MM-DD"
                                    value-format="YYYY-MM-DD"
                                    style="width: 100%"
                                />
                            </el-form-item>
                            
                            <el-form-item label="所在地区">
                                <el-cascader
                                    v-model="addressCodes"
                                    :options="regionData"
                                    :props="{ expandTrigger: 'hover' }"
                                    placeholder="请选择省 / 市 / 区"
                                    clearable
                                    filterable
                                    style="width: 100%"
                                />
                            </el-form-item>
                            
                            <el-form-item label="密码">
                                <el-input 
                                    type="password" 
                                    v-model="form.password" 
                                    placeholder="请输入密码" 
                                    required
                                />
                            </el-form-item>
                            
                            <el-form-item label="确认密码">
                                <el-input 
                                    type="password" 
                                    v-model="form.confirmPassword" 
                                    placeholder="请再次输入密码" 
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
                                    注册
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
import { register } from '@/api/auth'
import service from '@/api/auth'
import { regionData } from 'element-china-area-data'
import { cascaderValueToStorage } from '@/utils/regionAddress'
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElButton,
  ElDatePicker,
  ElCascader,
  ElMessage
} from 'element-plus'

const router = useRouter()
const step = ref(1) // 1: 手机号验证, 2: 详细信息填写
const isSendingCode = ref(false) // 是否正在发送验证码
const countdown = ref(0) // 倒计时
const registerSuccess = ref(false) // 注册成功状态

const addressCodes = ref([])

const form = reactive({
    phone: '',
    verificationCode: '',
    nickname: '',
    birthday: '',
    password: '',
    confirmPassword: ''
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
        // 调用后端API检查手机号是否已存在
        const checkResponse = await service.post('/auth/check-phone', { phone: form.phone })
        if (!checkResponse.success) {
            ElMessage.error(checkResponse.message || '该手机号已被注册')
            return
        }
        
        // 发送验证码
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
        // 显示后端返回的具体错误信息
        if (error.response && error.response.data && error.response.data.message) {
            ElMessage.error(error.response.data.message)
        } else if (error.response && error.response.data) {
            ElMessage.error(error.response.data.message || '发送验证码失败')
        } else {
            ElMessage.error('发送验证码失败，请检查网络连接')
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
        // 调用后端API验证验证码
        const response = await service.post('/auth/verify-code', {
            phone: form.phone,
            verificationCode: form.verificationCode
        })
        
        if (response.success) {
            // 验证通过，进入下一步
            console.log('手机号和验证码验证通过')
            step.value = 2
        } else {
            ElMessage.error(response.message || '验证码错误')
        }
    } catch (error) {
        console.error('验证失败:', error)
        ElMessage.error('验证过程中发生错误，请稍后再试')
    }
}

// 生成默认头像（显示"字记"）
const generateDefaultAvatar = () => {
    const canvas = document.createElement('canvas')
    const size = 400 // 头像尺寸
    canvas.width = size
    canvas.height = size
    
    const ctx = canvas.getContext('2d')
    
    // 绘制渐变背景
    const gradient = ctx.createLinearGradient(0, 0, size, size)
    gradient.addColorStop(0, '#667eea')
    gradient.addColorStop(1, '#764ba2')
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, size, size)
    
    // 绘制文字
    ctx.fillStyle = '#ffffff'
    ctx.font = 'bold 180px "Zhi Mang Xing", "KaiTi", cursive, serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    
    // 绘制"字记"文字
    ctx.fillText('字记', size / 2, size / 2)
    
    // 转换为Base64
    return canvas.toDataURL('image/png')
}

// 注册处理
const handleRegister = async () => {
    // 确认密码验证
    if (form.password !== form.confirmPassword) {
        ElMessage.error('两次输入的密码不一致')
        return
    }

    const addressStored = cascaderValueToStorage(addressCodes.value)
    if (!addressStored) {
        ElMessage.error('请选择所在地区（省 / 市 / 区）')
        return
    }

    try {
        // 生成默认头像
        const defaultAvatar = generateDefaultAvatar()
        
        // 构造符合后端要求的数据结构
        const userData = {
            name: form.nickname,
            password: form.password,
            phone: form.phone,
            birthday: form.birthday,
            address: addressStored,
            avatar: defaultAvatar
        }

        // 调用后端注册接口
        const response = await register(userData)
        
        if (response.success) {
            // 注册成功
            registerSuccess.value = true
            
            // 3秒后跳转到登录页面
            setTimeout(() => {
                router.push('/') 
            }, 3000)
        } else {
            // 注册失败
            ElMessage.error(response.message || '注册失败')
        }
    } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error('注册过程中发生错误，请稍后再试')
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

.register-btn {
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

.register-btn:hover {
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