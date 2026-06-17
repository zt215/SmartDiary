import { onMounted, onUnmounted } from 'vue'

/**
 * 登录页 Enter 提交（Element Plus 的 el-input 不会把 enter 事件传到外层）
 * @param {import('vue').Ref<HTMLElement|null>} formRef
 * @param {() => void | Promise<void>} getSubmit
 */
export function useLoginEnter(formRef, getSubmit) {
  const handler = (e) => {
    if (e.key !== 'Enter' || e.isComposing || e.repeat) return
    const active = document.activeElement
    if (!active || active.tagName !== 'INPUT') return
    const form = formRef.value
    if (!form || !form.contains(active)) return
    e.preventDefault()
    e.stopPropagation()
    getSubmit()
  }

  onMounted(() => {
    window.addEventListener('keydown', handler, true)
  })

  onUnmounted(() => {
    window.removeEventListener('keydown', handler, true)
  })
}
