<template>
  <div class="background login" :class="{ 'login-transitioning': isTransitioning }">
    <!-- 视频背景 -->
    <div class="video-background">
      <video autoplay muted loop playsinline class="background-video">
        <source src="/背景.mp4" type="video/mp4">
        您的浏览器不支持视频标签。
      </video>
      <div class="video-overlay"></div>
    </div>
    
    <!-- 左侧栏 -->
    <div class="left">
      <h1 class="title" :class="{ show: showTitle }">时光流转</h1>
      <h1 class="subtitle" :class="{ show: showSubtitle }">故事待书...</h1>
      <h2 class="welcome" :class="{ show: showWelcome }">--欢迎你,执笔人</h2>
    </div>
    <!-- 右侧栏 -->
    <div class="right" :class="{ show: showRight }">
      <img src="/logo.png" alt="字迹" class="logo" />
      <form ref="loginFormRef" class="form" @submit.prevent="login">
        <div class="account-select">
          <el-input
            v-model="account"
            placeholder="手机号 / UID / 邮箱"
            style="width: 100%"
          />
        </div>
        <div class="password-box">
          <el-input
            :type="showPassword ? 'text' : 'password'"
            v-model="password"
            placeholder="请输入密码"
            style="width: 100%"
          >
            <template #suffix>
              <el-button link @click="showPassword = !showPassword">
                <span v-if="showPassword">
                  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#d8a8ff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z"/><circle cx="12" cy="12" r="3"/></svg>
                </span>
                <span v-else>
                  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#d8a8ff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-7 0-11-7-11-7a21.07 21.07 0 0 1 5.06-6.06"/><path d="M1 1l22 22"/><path d="M9.53 9.53A3 3 0 0 0 12 15a3 3 0 0 0 2.47-5.47"/></svg>
                </span>
              </el-button>
            </template>
          </el-input>
        </div>
        <div class="quill-btn-slot" :class="{ 'is-flying': flyQuill }">
        <Teleport to="body" :disabled="!flyQuill">
        <button
          ref="quillBtnRef"
          type="button"
          class="quill-btn"
          :class="{ 'is-flying': flyQuill }"
          :style="flyQuill ? flyQuillStyle : undefined"
          :disabled="isLoggingIn || isTransitioning"
          @mouseenter="regenerateWind"
          @focus="regenerateWind"
          @click="login"
        >
          <span class="quill-wrap">
            <span class="quill-fx" aria-hidden="true">
              <svg class="quill-wind-svg" viewBox="0 0 368 100" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg">
                <defs>
                  <linearGradient id="windFlowGrad" gradientUnits="userSpaceOnUse" x1="0" y1="50" x2="368" y2="50">
                    <stop offset="0%" stop-color="#ffffff" stop-opacity="0.88" />
                    <stop offset="62%" stop-color="#fff6e8" stop-opacity="0.55" />
                    <stop offset="88%" stop-color="#fffaf2" stop-opacity="0.28" />
                    <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
                  </linearGradient>
                  <filter id="windSoft" x="-8%" y="-20%" width="116%" height="140%">
                    <feGaussianBlur stdDeviation="0.45" />
                  </filter>
                  <clipPath id="windClip">
                    <rect x="0" y="0" width="368" height="100" />
                  </clipPath>
                  <mask id="windBoundsMask">
                    <rect x="0" y="0" width="368" height="100" fill="url(#windEdgeFade)" />
                  </mask>
                  <linearGradient id="windEdgeFade" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="0" y2="100">
                    <stop offset="0%" stop-color="black" />
                    <stop offset="5%" stop-color="white" />
                    <stop offset="95%" stop-color="white" />
                    <stop offset="100%" stop-color="black" />
                  </linearGradient>
                </defs>
                <g
                  v-if="windLines.length"
                  class="wind-lines"
                  filter="url(#windSoft)"
                  clip-path="url(#windClip)"
                  mask="url(#windBoundsMask)"
                >
                  <template v-for="(line, idx) in windLines" :key="'wl-' + windSeed + '-' + idx">
                    <path class="wind-track" :d="line.d" />
                    <path
                      class="wind-flow"
                      :class="'wind-flow-' + (idx + 1)"
                      :d="line.d"
                      :style="{ animationDelay: line.delay + 's' }"
                    />
                  </template>
                  <path
                    v-for="(wisp, idx) in windWisps"
                    :key="'ww-' + windSeed + '-' + idx"
                    class="wind-wisp"
                    :d="wisp.d"
                    :style="{ animationDelay: wisp.delay + 's' }"
                  />
                </g>
              </svg>
            </span>
            <svg class="quill-svg" viewBox="0 0 350 48" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <defs>
                <linearGradient id="loginFeatherBase" x1="0%" y1="50%" x2="100%" y2="50%">
                  <stop offset="0%" stop-color="#fcfbfa" />
                  <stop offset="100%" stop-color="#ebe6dc" />
                </linearGradient>
                <linearGradient id="loginShaftGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stop-color="#4a3220" />
                  <stop offset="100%" stop-color="#2a1a0c" />
                </linearGradient>
                <linearGradient id="loginNibGrad" x1="0%" y1="50%" x2="100%" y2="50%">
                  <stop offset="0%" stop-color="#f5e6b8" />
                  <stop offset="100%" stop-color="#8a6828" />
                </linearGradient>
              </defs>

              <!-- 细长笔尖（扁平，不粗块） -->
              <path
                d="M 252 23 L 334 24 L 332 26 L 252 25 Z"
                fill="url(#loginNibGrad)"
                stroke="#5a4418"
                stroke-width="0.35"
              />
              <path d="M 328 23.5 L 334 24 L 330 24.8 L 324 24.2 Z" fill="#1a1814" />
              <path d="M 256 24 L 318 24" stroke="rgba(255,255,255,0.45)" stroke-width="0.35" stroke-linecap="round" />

              <!-- 极短细笔杆 -->
              <path
                d="M 244 23.2 L 252 22.8 L 252 25.2 L 244 25.6 Z"
                fill="url(#loginShaftGrad)"
              />

              <!-- 羽轴（至羽尖，末端略细） -->
              <path
                d="M 242 24 L 0 24"
                stroke="#9e9488"
                stroke-width="0.85"
                stroke-linecap="round"
              />

              <!-- 羽片芯：杆部收窄，前段渐细至羽尖 -->
              <path
                d="M 242 23.75 C 205 23.45, 155 23.35, 105 23.4 C 72 23.42, 48 23.48, 28 23.52 L 0 24 L 28 24.48 C 48 24.52, 72 24.58, 105 24.6 C 155 24.65, 205 24.55, 242 24.25 Z"
                fill="url(#loginFeatherBase)"
                opacity="0.92"
              />
              <!-- 羽尖：加长、收尖 -->
              <path
                d="M 78 23.55 L 36 22.1 L 10 23.2 L 0 24 L 10 24.8 L 36 25.9 L 78 24.45 Z"
                fill="#f8f6f2"
                stroke="rgba(190, 184, 174, 0.75)"
                stroke-width="0.3"
              />

              <!-- 上侧羽枝（杆部不外撇，前段渐收至羽尖） -->
              <g fill="none" stroke-linecap="round">
                <path d="M 230 23 L 210 17 L 180 14 L 140 12 L 95 11 L 55 12 L 22 15 L 0 24" stroke="#e6e2d8" stroke-width="0.42" />
                <path d="M 218 23 L 198 18 L 168 15 L 125 13 L 82 13 L 42 14 L 14 17 L 0 24" stroke="#d8d4ca" stroke-width="0.4" />
                <path d="M 206 23 L 186 19 L 155 16 L 112 14 L 72 14 L 36 16 L 10 18 L 0 24" stroke="#f8f6f2" stroke-width="0.38" />
                <path d="M 194 23 L 172 19 L 140 17 L 98 15 L 60 15 L 28 17 L 8 19 L 0 24" stroke="#ccc8be" stroke-width="0.38" />
                <path d="M 182 24 L 160 20 L 128 18 L 88 16 L 52 16 L 22 18 L 6 20 L 0 24" stroke="#ebe8e2" stroke-width="0.36" />
                <path d="M 170 24 L 148 21 L 118 19 L 78 17 L 46 17 L 18 19 L 4 21 L 0 24" stroke="#d4d0c6" stroke-width="0.34" />
                <path d="M 158 24 L 132 22 L 100 20 L 68 18 L 38 18 L 14 20 L 2 22 L 0 24" stroke="#ffffff" stroke-width="0.32" opacity="0.8" />
                <path d="M 146 24 L 118 22 L 88 20 L 58 19 L 30 19 L 10 21 L 0 24" stroke="#e0dcd6" stroke-width="0.32" />
                <path d="M 134 24 L 102 22 L 72 21 L 48 20 L 24 21 L 6 22.5 L 0 24" stroke="#d0ccc4" stroke-width="0.3" />
                <path d="M 122 24 L 92 22.5 L 62 21.5 L 38 21 L 16 22 L 0 24" stroke="#f4f2ee" stroke-width="0.28" opacity="0.75" />
                <path d="M 108 24 L 78 22.8 L 52 22 L 28 22 L 8 23 L 0 24" stroke="#bab4aa" stroke-width="0.28" />
                <path d="M 90 24 L 62 23 L 38 22.5 L 18 22.8 L 0 24" stroke="#c8c4bc" stroke-width="0.26" />
                <path d="M 72 23.5 L 48 22.8 L 26 22.5 L 8 23.2 L 0 24" stroke="#b8b2a8" stroke-width="0.26" opacity="0.85" />
                <path d="M 52 23.4 L 32 22.6 L 14 22.8 L 0 24" stroke="#d0ccc4" stroke-width="0.24" />
                <path d="M 34 23.3 L 16 22.5 L 0 24" stroke="#e8e6e2" stroke-width="0.22" />
              </g>

              <!-- 下侧羽枝 -->
              <g fill="none" stroke-linecap="round">
                <path d="M 230 25 L 210 31 L 180 34 L 140 36 L 95 37 L 55 36 L 22 33 L 0 24" stroke="#e0dcd4" stroke-width="0.42" />
                <path d="M 218 25 L 198 30 L 168 33 L 125 35 L 82 35 L 42 34 L 14 31 L 0 24" stroke="#d8d4ca" stroke-width="0.4" />
                <path d="M 206 25 L 186 29 L 155 32 L 112 34 L 72 34 L 36 32 L 10 30 L 0 24" stroke="#f6f4f0" stroke-width="0.38" />
                <path d="M 194 25 L 172 29 L 140 31 L 98 33 L 60 33 L 28 31 L 8 29 L 0 24" stroke="#ccc8be" stroke-width="0.38" />
                <path d="M 182 25 L 160 28 L 128 30 L 88 32 L 52 32 L 22 30 L 6 28 L 0 24" stroke="#eae8e2" stroke-width="0.36" />
                <path d="M 170 25 L 148 27 L 118 29 L 78 31 L 46 31 L 18 29 L 4 27 L 0 24" stroke="#d4d0c6" stroke-width="0.34" />
                <path d="M 158 25 L 132 26 L 100 28 L 68 30 L 38 30 L 14 28 L 2 26 L 0 24" stroke="#ffffff" stroke-width="0.32" opacity="0.75" />
                <path d="M 146 25 L 118 26 L 88 28 L 58 29 L 30 29 L 10 27 L 0 24" stroke="#dcd8d0" stroke-width="0.32" />
                <path d="M 134 25 L 102 26 L 72 27 L 48 28 L 24 27 L 6 25.5 L 0 24" stroke="#d0ccc4" stroke-width="0.3" />
                <path d="M 122 25 L 92 25.5 L 62 26.5 L 38 27 L 16 26 L 0 24" stroke="#eeecea" stroke-width="0.28" opacity="0.7" />
                <path d="M 108 25 L 78 25.2 L 52 26 L 28 26 L 8 25 L 0 24" stroke="#b8b2a8" stroke-width="0.28" />
                <path d="M 90 25 L 62 25 L 38 25.5 L 18 25.2 L 0 24" stroke="#c4c0b8" stroke-width="0.26" />
                <path d="M 72 24.5 L 48 25.2 L 26 25.5 L 8 24.8 L 0 24" stroke="#b8b2a8" stroke-width="0.26" opacity="0.85" />
                <path d="M 52 24.6 L 32 25.4 L 14 25.2 L 0 24" stroke="#d0ccc4" stroke-width="0.24" />
                <path d="M 34 24.7 L 16 25.5 L 0 24" stroke="#e8e6e2" stroke-width="0.22" />
              </g>
            </svg>
            <span class="quill-label">开始跃迁</span>
          </span>
        </button>
        </Teleport>
        </div>
      <div class="bottom">
        <el-button link @click="$router.push('/forgotpassword')">忘记密码</el-button>
        <el-button link @click="$router.push('/register')">注册</el-button>
      </div>
      <div class="enforcer-entry">
        <el-button link @click="$router.push('/enforcer/login')">前往执法堂>>>>>></el-button>
      </div>
      <button type="submit" class="login-submit-hidden" tabindex="-1" aria-hidden="true">登录</button>
      </form>
    </div>

    <Teleport to="body">
      <div v-show="showHomeTransition" class="home-transition-panel" :style="homeTransitionStyle">
        <HomeView v-if="homeReady" />
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { login as apiLogin } from '../api/auth'
import { ref, onMounted, onUnmounted, nextTick, defineAsyncComponent } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useLoginEnter } from '../utils/loginEnter'

const HomeView = defineAsyncComponent(() => import('./home.vue'))

// 页面状态
const showTitle = ref(false)
const showSubtitle = ref(false)
const showWelcome = ref(false)
const showRight = ref(false)

// 登录表单数据
const account = ref('')
const password = ref('')
const showPassword = ref(false)
const isLoggingIn = ref(false)
const isTransitioning = ref(false)
const flyQuill = ref(false)
const quillBtnRef = ref(null)
const flyQuillStyle = ref({})
const showHomeTransition = ref(false)
const homeReady = ref(false)
const homeTransitionStyle = ref({})

/** 悬停风线：每次随机曲线与尾点，超出 viewBox 裁剪消失 */
const windLines = ref([])
const windWisps = ref([])
const windSeed = ref(0)
const WIND_W = 368
const WIND_H = 100
const WIND_CENTER_Y = 50

function windRand(min, max) {
  return min + Math.random() * (max - min)
}

function windRandInt(min, max) {
  return Math.floor(windRand(min, max + 1))
}

/** Catmull-Rom 转三次贝塞尔，保证是柔和曲线而非折线 */
function catmullRomPath(points) {
  if (points.length < 2) return ''
  const parts = [`M ${points[0].x.toFixed(1)} ${points[0].y.toFixed(1)}`]
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[Math.max(0, i - 1)]
    const p1 = points[i]
    const p2 = points[i + 1]
    const p3 = points[Math.min(points.length - 1, i + 2)]
    const t = 0.32 + Math.random() * 0.12
    const c1x = p1.x + ((p2.x - p0.x) / 6) * t * 2
    const c1y = p1.y + ((p2.y - p0.y) / 6) * t * 2
    const c2x = p2.x - ((p3.x - p1.x) / 6) * t * 2
    const c2y = p2.y - ((p3.y - p1.y) / 6) * t * 2
    parts.push(
      `C ${c1x.toFixed(1)} ${c1y.toFixed(1)}, ${c2x.toFixed(1)} ${c2y.toFixed(1)}, ${p2.x.toFixed(1)} ${p2.y.toFixed(1)}`,
    )
  }
  return parts.join(' ')
}

function buildWindCurve({ startX, startY, endX, endY, upper, spread }) {
  const seg = windRandInt(8, 11)
  const pts = [{ x: startX, y: startY }]
  for (let i = 1; i < seg; i++) {
    const t = i / seg
    const x = startX + (endX - startX) * t + windRand(-2.5, 2.5)
    const bulge = Math.sin(t * Math.PI) * spread * (upper ? -1 : 1)
    const wave =
      Math.sin(t * Math.PI * windRand(1.8, 3) + windRand(0, Math.PI * 2)) * windRand(1.5, 4) +
      Math.sin(t * Math.PI * windRand(3, 4.5) + windRand(0, 2)) * windRand(0.6, 1.8)
    const y = startY + (endY - startY) * t ** 0.95 + bulge + wave
    pts.push({ x, y })
  }
  pts.push({ x: endX, y: endY })
  return catmullRomPath(pts)
}

function buildWispCurve(ax, ay, endX, endY) {
  const c1x = ax + (endX - ax) * windRand(0.3, 0.5) + windRand(-6, 6)
  const c1y = ay + windRand(-5, 5)
  const c2x = ax + (endX - ax) * windRand(0.6, 0.82) + windRand(-5, 5)
  const c2y = ay + (endY - ay) * windRand(0.4, 0.65) + windRand(-5, 5)
  return `M ${ax.toFixed(1)} ${ay.toFixed(1)} C ${c1x.toFixed(1)} ${c1y.toFixed(1)}, ${c2x.toFixed(1)} ${c2y.toFixed(1)}, ${endX.toFixed(1)} ${endY.toFixed(1)}`
}

function regenerateWind() {
  if (flyQuill.value || isTransitioning.value) return
  const lines = []
  for (let i = 0; i < 6; i++) {
    const upper = i < 3
    const layer = i % 3
    const spread = 10 + layer * 4 + windRand(0, 5)
    const gapFromPen = 18 + layer * 5
    lines.push({
      d: buildWindCurve({
        startX: windRand(14, 20),
        startY: (upper ? WIND_CENTER_Y - gapFromPen : WIND_CENTER_Y + gapFromPen) + windRand(-0.8, 0.8),
        endX: windRand(305, WIND_W + 12),
        endY: upper ? windRand(-14, 18) : windRand(82, WIND_H + 10),
        upper,
        spread,
      }),
      delay: windRand(0, 0.38),
    })
  }
  const wisps = []
  const wispCount = windRandInt(2, 4)
  for (let i = 0; i < wispCount; i++) {
    wisps.push({
      d: buildWispCurve(
        windRand(295, 328),
        windRand(4, WIND_H - 4),
        windRand(335, WIND_W + 14),
        windRand(-10, WIND_H + 10),
      ),
      delay: windRand(0.4, 0.75),
    })
  }
  windLines.value = lines
  windWisps.value = wisps
  windSeed.value += 1
}

const router = useRouter()
const loginFormRef = ref(null)

/** 飞出屏幕左侧后额外多飞的像素 */
const QUILL_FLY_EXIT_PADDING = 64

/** 联动时首页在羽毛笔右侧，留出的间距（越大分得越开） */
const HOME_GAP_BEHIND_QUILL = 100

/** 联动动画时间轴（毫秒），整体约 1.5 倍速 */
const TRANSITION_TIMING = {
  homeStart: 235,
  couple: 870,
  quillExit: 1535,
  homeSettle: 1935,
  end: 2135,
}

let transitionRafId = null

const clamp01 = (t) => Math.max(0, Math.min(1, t))

const lerp = (a, b, t) => a + (b - a) * t

const cancelTransitionAnim = () => {
  if (transitionRafId != null) {
    cancelAnimationFrame(transitionRafId)
    transitionRafId = null
  }
}

const setQuillTransitionLock = (on) => {
  document.documentElement.classList.toggle('login-quill-transition', on)
}

const resetTransition = () => {
  cancelTransitionAnim()
  setQuillTransitionLock(false)
  isTransitioning.value = false
  flyQuill.value = false
  flyQuillStyle.value = {}
  showHomeTransition.value = false
  homeReady.value = false
  homeTransitionStyle.value = {}
  isLoggingIn.value = false
}

/** 三阶段：首页从右入 → 追上羽毛笔并联动 → 笔飞出、首页铺满 */
const startOrchestratedTransition = () => {
  return new Promise((resolve) => {
    const btn = quillBtnRef.value
    if (!btn) {
      resolve()
      return
    }

    const rect = btn.getBoundingClientRect()
    const vw = window.innerWidth
    const quillStartLeft = rect.left
    const quillWidth = rect.width
    const quillExitX = -(quillStartLeft + quillWidth + QUILL_FLY_EXIT_PADDING)

    const quillQxAtExit = quillExitX * 0.92
    const { homeStart, couple, quillExit, homeSettle, end } = TRANSITION_TIMING

    // 笔在 couple 时刻的位置，与整段左飞进度对齐，避免分段 ease 在衔接处刹停
    const meetQx = lerp(0, quillQxAtExit, couple / quillExit)
    const meetHx = quillStartLeft + meetQx + quillWidth + HOME_GAP_BEHIND_QUILL
    const hxAtCoupledEnd = meetHx + (quillQxAtExit - meetQx)

    isTransitioning.value = true
    setQuillTransitionLock(true)
    showHomeTransition.value = true
    flyQuill.value = true

    flyQuillStyle.value = {
      left: `${rect.left}px`,
      top: `${rect.top}px`,
      width: `${rect.width}px`,
      minHeight: `${rect.height}px`,
      height: 'auto',
      transform: 'translate3d(0, 0, 0)',
      transformOrigin: '24% 50%',
      transition: 'none',
      opacity: 1,
      zIndex: 10050,
      overflow: 'visible',
    }
    homeTransitionStyle.value = {
      transform: `translate3d(${vw}px, 0, 0)`,
      transition: 'none',
    }

    const t0 = performance.now()

    const tick = (now) => {
      const t = now - t0
      let qx
      let hx
      let quillOpacity = 1
      const behindHome = t >= couple && t < homeSettle

      if (t < quillExit) {
        qx = lerp(0, quillQxAtExit, t / quillExit)
      } else if (t < homeSettle) {
        const p = (t - quillExit) / (homeSettle - quillExit)
        qx = lerp(quillQxAtExit, quillExitX, p)
        quillOpacity = 1 - p
      } else {
        qx = quillExitX
        quillOpacity = 0
      }

      if (t < couple) {
        const hp = clamp01((t - homeStart) / (couple - homeStart))
        hx = t < homeStart ? vw : lerp(vw, meetHx, hp)
      } else if (t < quillExit) {
        hx = meetHx + (qx - meetQx)
      } else if (t < homeSettle) {
        const p = (t - quillExit) / (homeSettle - quillExit)
        hx = lerp(hxAtCoupledEnd, 0, p)
      } else {
        hx = 0
      }

      flyQuillStyle.value = {
        ...flyQuillStyle.value,
        transform: `translate3d(${qx}px, 0, 0)`,
        transformOrigin: '24% 50%',
        opacity: quillOpacity,
        zIndex: behindHome ? 10035 : 10050,
      }
      homeTransitionStyle.value = {
        transform: `translate3d(${hx}px, 0, 0)`,
        transition: 'none',
      }

      if (t < end) {
        transitionRafId = requestAnimationFrame(tick)
      } else {
        transitionRafId = null
        resolve()
      }
    }

    nextTick().then(() => {
      requestAnimationFrame(tick)
    })
  })
}

// 页面加载时的动画效果
onMounted(() => {
  setTimeout(() => { showTitle.value = true }, 200)
  setTimeout(() => { showSubtitle.value = true }, 800)
  setTimeout(() => { showWelcome.value = true }, 1400)
  setTimeout(() => { showRight.value = true }, 2000)
})

onUnmounted(() => {
  cancelTransitionAnim()
  setQuillTransitionLock(false)
})

// 登录方法
const login = async () => {
  if (isLoggingIn.value || isTransitioning.value) return
  if (!account.value.trim() || !password.value.trim()) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  isLoggingIn.value = true

  try {
    const res = await apiLogin({
      account: account.value,
      password: password.value
    })

    if (res && res.success) {
      if (res.data) {
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        homeReady.value = true
      }
      await startOrchestratedTransition()
      showHomeTransition.value = false
      flyQuill.value = false
      await router.push({ path: '/home', query: { login: 'success' } })
      resetTransition()
    } else {
      ElMessage.error(res?.message || '账号或密码错误')
    }
  } catch (e) {
    console.error('Login error:', e)
    if (e.response) {
      ElMessage.error(e.response.data?.message || '登录失败: ' + e.response.status)
    } else if (e.request) {
      ElMessage.error('网络错误，请检查服务器是否运行正常')
    } else {
      ElMessage.error('登录失败: ' + e.message)
    }
  } finally {
    isLoggingIn.value = false
  }
}

useLoginEnter(loginFormRef, () => login())
</script>
<style>
.home-transition-panel {
  position: fixed;
  inset: 0;
  z-index: 10040;
  overflow: auto;
  background: #f5f5f5;
  will-change: transform;
}
html.login-quill-transition,
html.login-quill-transition body {
  overflow-x: visible !important;
}
</style>
<style scoped>
.background {
  position: relative;
  height: 100vh;
  overflow: hidden;
}
.background.login-transitioning {
  overflow: visible;
}

/* 视频背景样式 */
.video-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

.background-video {
  position: absolute;
  top: 50%;
  left: 50%;
  min-width: 100%;
  min-height: 100%;
  width: auto;
  height: auto;
  transform: translate(-50%, -50%);
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.2); /* 减少覆盖层透明度，让视频更清晰 */
}

.left {
  width: 50%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}
.title {
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  font-size: 80px;
  background: linear-gradient(90deg, #ff7e5f, #3a0885);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: auto;
  margin-bottom: 0;
  text-shadow: 2px 2px 6px rgba(0, 0, 0, 0.4); /* 增强文字阴影，提高可读性 */
}
.subtitle {
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  font-size: 80px;
  background: linear-gradient(90deg,#3a0885,#fabcac);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: auto;
  margin-top: 40px;
  text-shadow: 2px 2px 6px rgba(0, 0, 0, 0.4);
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
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.4);
}
.right {
  width: 40%;
  height: 100vh;
  border-top-left-radius: 100px;
  border-bottom-left-radius: 100px;
  padding: 20px;
  position: absolute;
  top: 0;
  right: -80px;
  opacity: 0;
  transition: opacity 1s, right 1s;
  z-index: 1;
  background: transparent;
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
  right: 0;
}
.logo {
  display: block;
  width: auto;
  height: 100px;
  max-width: min(280px, 72%);
  margin: 30% auto 0;
  object-fit: contain;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.45));
}
.form {
  display: flex;
  flex-direction: column;
  align-items: center;
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
.account-select {
  position: relative;
  width: 60%;
  margin-top: 5%;
  display: flex;
  align-items: center;
}
.account-select :deep(.el-input),
.password-box :deep(.el-input) {
  background: transparent;
  border-radius: 10px;
  overflow: hidden;
}
.account-select :deep(.el-input__wrapper),
.password-box :deep(.el-input__wrapper) {
  background: transparent !important;
  border-radius: 10px;
  border: 1px solid rgba(250, 188, 172, 0.85);
  box-shadow: none;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.account-select :deep(.el-input__wrapper.is-focus),
.password-box :deep(.el-input__wrapper.is-focus) {
  border-color: #d8a8ff;
  box-shadow: 0 0 0 1px rgba(211, 44, 230, 0.35);
}
.account-select :deep(.el-input__inner),
.password-box :deep(.el-input__inner) {
  background: transparent;
  color: #f8f0e8;
}
.account-select :deep(.el-input__inner::placeholder),
.password-box :deep(.el-input__inner::placeholder) {
  color: rgba(220, 220, 220, 0.92);
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
.bottom :deep(.el-button.is-link) {
  font-size: 20px;
  color: #fabcac;
  text-shadow: 0 1px 4px rgba(58, 8, 133, 0.45);
  transition: color 0.25s;
}
.bottom :deep(.el-button.is-link:hover),
.bottom :deep(.el-button.is-link:focus-visible) {
  color: #ffe8dc;
}

.enforcer-entry {
  width: 60%;
  margin: 12px auto 0;
  text-align: center;
}
.enforcer-entry :deep(.el-button.is-link) {
  font-size: 16px;
  color: rgba(255, 230, 200, 0.95);
  letter-spacing: 0.12em;
}
.enforcer-entry :deep(.el-button.is-link:hover) {
  color: #fff;
}

/* 羽毛笔「开始跃迁」按钮 */
.quill-btn-slot {
  position: relative;
  width: 72%;
  max-width: 360px;
  min-height: 52px;
  overflow: visible;
  margin-top: 36px;
  margin-left: 20%;
  margin-right: auto;
  align-self: flex-start;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}
.quill-btn {
  margin-top: 0;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  display: block;
  width: 100%;
  max-width: none;
}
.quill-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}
.quill-wrap {
  position: relative;
  display: block;
  width: 100%;
  overflow: visible;
  transform-origin: 78% 50%;
  filter: drop-shadow(0 2px 8px rgba(25, 25, 35, 0.4));
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1), filter 0.4s ease;
}
.quill-btn:hover:not(:disabled):not(.is-flying) .quill-wrap,
.quill-btn:focus-visible:not(:disabled):not(.is-flying) .quill-wrap {
  transform: scale(1.12);
  filter: drop-shadow(0 3px 12px rgba(30, 28, 45, 0.35));
}
.quill-svg {
  position: relative;
  z-index: 1;
  width: 100%;
  height: auto;
  display: block;
  transform: scaleX(-1);
}
/* 悬停风线：绕笔两侧鼓开，笔尖→笔尾单向流动 */
.quill-fx {
  position: absolute;
  top: -48%;
  bottom: -48%;
  left: -8%;
  right: -8%;
  z-index: 0;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.35s ease;
}
.quill-btn:hover:not(:disabled) .quill-fx,
.quill-btn:focus-visible:not(:disabled) .quill-fx {
  opacity: 1;
}
.quill-wind-svg {
  width: 100%;
  height: 100%;
  overflow: hidden;
}
.wind-track,
.wind-flow {
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
}
.wind-track {
  stroke: rgba(255, 250, 240, 0.18);
  stroke-width: 0.75;
  opacity: 0;
  transition: opacity 0.4s ease;
}
.wind-flow {
  stroke: url(#windFlowGrad);
  stroke-width: 1;
  stroke-dasharray: 88 460;
  stroke-dashoffset: 548;
  opacity: 0;
}
.wind-wisp {
  fill: none;
  stroke: rgba(255, 248, 235, 0.35);
  stroke-width: 0.55;
  stroke-linecap: round;
  stroke-dasharray: 36 120;
  stroke-dashoffset: 156;
  opacity: 0;
}
.quill-btn:hover:not(:disabled) .wind-track,
.quill-btn:focus-visible:not(:disabled) .wind-track {
  opacity: 1;
}
.quill-btn:hover:not(:disabled) .wind-flow,
.quill-btn:focus-visible:not(:disabled) .wind-flow {
  opacity: 0.85;
  animation: wind-push 1.75s cubic-bezier(0.35, 0.08, 0.4, 1) infinite;
}
.quill-btn:hover:not(:disabled) .wind-wisp,
.quill-btn:focus-visible:not(:disabled) .wind-wisp {
  opacity: 0.7;
  animation: wind-push 2s cubic-bezier(0.35, 0.08, 0.4, 1) infinite;
}
.wind-flow-1 { animation-delay: 0s; stroke-width: 1.05; }
.wind-flow-2 { animation-delay: 0.16s; }
.wind-flow-3 { animation-delay: 0.32s; stroke-width: 0.9; }
.wind-flow-4 { animation-delay: 0.1s; }
.wind-flow-5 { animation-delay: 0.24s; stroke-width: 0.95; }
.wind-flow-6 { animation-delay: 0.4s; stroke-width: 0.85; }
@keyframes wind-push {
  from {
    stroke-dashoffset: 548;
  }
  to {
    stroke-dashoffset: 0;
  }
}
.quill-label {
  position: absolute;
  z-index: 2;
  right: 8%;
  left: auto;
  top: 50%;
  transform: translateY(-52%);
  font-family: 'Zhi Mang Xing', 'KaiTi', cursive, serif;
  font-size: 17px;
  color: #4a2870;
  letter-spacing: 0.2em;
  white-space: nowrap;
  pointer-events: none;
}
.quill-btn:hover:not(:disabled) .quill-label,
.quill-btn:focus-visible:not(:disabled) .quill-label {
  color: #5c3488;
}
.quill-btn.is-flying {
  position: fixed;
  margin: 0;
  box-sizing: border-box;
  overflow: visible;
  transform-origin: 24% 50%;
  will-change: transform;
}
.quill-btn.is-flying .quill-wrap {
  transform: none !important;
  filter: drop-shadow(0 2px 8px rgba(25, 25, 35, 0.4));
}
.quill-btn.is-flying .quill-fx {
  opacity: 0 !important;
}
/* 响应式设计 */
@media (max-width: 1200px) {
  .title, .subtitle {
    font-size: 60px;
  }
  .logo {
    height: 72px;
  }
}

@media (max-width: 768px) {
  .left, .right {
    width: 100%;
    position: relative;
    height: auto;
  }
  .right {
    border-radius: 50px;
    margin: 20px;
    right: 0;
  }
  .title, .subtitle {
    font-size: 40px;
  }
  .logo {
    height: 56px;
    margin-top: 20%;
  }
}
</style>