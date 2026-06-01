import Quill from 'quill'

/**
 * 富文本（日记 / 字迹圈共用）
 */

export function stripHtml(html) {
  if (!html) return ''
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  return (tmp.textContent || tmp.innerText || '').replace(/\uFEFF/g, '').trim()
}

/** 是否含有效内容（文字、图片、视频等） */
export function hasRichTextContent(html) {
  if (!html || !String(html).trim()) return false
  if (stripHtml(html).length > 0) return true
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  return !!tmp.querySelector(
    'img[src], video[src], video source[src], iframe[src], audio[src], embed[src], object[data]'
  )
}

/** 列表摘要（纯文字，无文字时显示 [图片]/[视频]） */
export function getCircleListPreview(html, maxLen = 120) {
  const text = stripHtml(html)
  if (text) {
    return text.length > maxLen ? `${text.slice(0, maxLen)}...` : text
  }
  const tmp = document.createElement('div')
  tmp.innerHTML = html || ''
  if (tmp.querySelector('img[src]')) return '[图片]'
  if (tmp.querySelector('video[src], video source[src]')) return '[视频]'
  return '无内容'
}

/** 从私人日记生成字迹圈 HTML（标题 + 正文） */
export function buildCircleContentFromDiary(diary) {
  const title = (diary.title || '无标题')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  const body = diary.content || ''
  if (!hasRichTextContent(body)) {
    return `<p><strong>${title}</strong></p>`
  }
  return `<p><strong>${title}</strong></p>${body}`
}

/** Quill 工具栏（与新建日记一致） */
export const QUILL_TOOLBAR = [
  [{ header: [1, 2, 3, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ list: 'ordered' }, { list: 'bullet' }],
  [{ color: [] }, { background: [] }],
  [{ align: [] }],
  ['link', 'image'],
  ['clean']
]

function scaleSize(width, height, maxWidth, maxHeight) {
  let w = width
  let h = height
  if (!w || !h) return { width: 0, height: 0 }
  if (w > maxWidth) {
    h = (h * maxWidth) / w
    w = maxWidth
  }
  if (h > maxHeight) {
    w = (w * maxHeight) / h
    h = maxHeight
  }
  return { width: Math.max(1, Math.round(w)), height: Math.max(1, Math.round(h)) }
}

function canvasToJpegDataUrl(source, maxWidth, maxHeight, quality) {
  const { width, height } = scaleSize(source.width, source.height, maxWidth, maxHeight)
  if (!width || !height) {
    throw new Error('无法读取图片尺寸')
  }
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height
  const ctx = canvas.getContext('2d', { willReadFrequently: true })
  if (!ctx) throw new Error('无法创建画布')
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, width, height)
  ctx.drawImage(source, 0, 0, width, height)
  const dataUrl = canvas.toDataURL('image/jpeg', quality)
  if (!dataUrl || dataUrl.length < 24) {
    throw new Error('图片编码失败')
  }
  return dataUrl
}

/** 压缩图片后返回 JPEG Base64 */
export async function compressImageFile(file, maxWidth = 1280, maxHeight = 1280, quality = 0.82) {
  if (!file) throw new Error('请选择图片文件')
  const type = (file.type || '').toLowerCase()
  if (!type.startsWith('image/')) {
    throw new Error('请选择图片文件')
  }
  if (type.includes('heic') || type.includes('heif')) {
    throw new Error('不支持 HEIC 格式，请先转为 JPG 或 PNG')
  }

  if (typeof createImageBitmap === 'function') {
    try {
      const bitmap = await createImageBitmap(file)
      try {
        return canvasToJpegDataUrl(bitmap, maxWidth, maxHeight, quality)
      } finally {
        bitmap.close?.()
      }
    } catch (e) {
      console.warn('createImageBitmap 失败，尝试 FileReader', e)
    }
  }

  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const src = e.target?.result
      if (!src || typeof src !== 'string') {
        reject(new Error('读取图片失败'))
        return
      }
      const img = new Image()
      img.onload = () => {
        try {
          resolve(canvasToJpegDataUrl(img, maxWidth, maxHeight, quality))
        } catch (err) {
          reject(err)
        }
      }
      img.onerror = () => reject(new Error('图片格式无法解析，请使用 JPG 或 PNG'))
      img.src = src
    }
    reader.onerror = () => reject(new Error('读取图片失败'))
    reader.readAsDataURL(file)
  })
}

/** 将图片插入 Quill（兼容 Quill 2） */
export function insertImageToQuill(quill, dataUrl) {
  if (!quill || !dataUrl) return

  let index = 0
  try {
    const range = quill.getSelection()
    index = range && typeof range.index === 'number'
      ? range.index
      : Math.max(0, quill.getLength() - 1)
  } catch (_) {
    index = Math.max(0, quill.getLength() - 1)
  }

  const attempts = [
    () => quill.insertEmbed(index, 'image', dataUrl, 'user'),
    () => quill.insertEmbed(index, 'image', dataUrl),
    () => {
      if (typeof Quill.import !== 'function') throw new Error('no delta')
      const Delta = Quill.import('delta')
      quill.updateContents(
        new Delta().retain(index).insert({ image: dataUrl }),
        'user'
      )
    },
    () => {
      const safe = dataUrl.replace(/"/g, '&quot;')
      quill.root.innerHTML = quill.root.innerHTML + `<p><img src="${safe}"></p>`
      if (typeof quill.update === 'function') {
        quill.update('user')
      }
    }
  ]

  let lastErr
  for (const fn of attempts) {
    try {
      fn()
      try {
        quill.setSelection(index + 1, 0, 'silent')
      } catch (_) {
        /* ignore */
      }
      return
    } catch (err) {
      lastErr = err
    }
  }
  throw lastErr || new Error('插入图片失败')
}

/**
 * Quill 模块配置（Quill 2 须在 toolbar.handlers 中注册 image）
 */
export function createQuillModules(onError) {
  return {
    toolbar: {
      container: QUILL_TOOLBAR,
      handlers: {
        image: function imageHandler() {
          const quill = this.quill
          if (!quill) {
            onError?.('编辑器未就绪')
            return
          }
          const input = document.createElement('input')
          input.type = 'file'
          input.accept = 'image/jpeg,image/png,image/gif,image/webp,image/bmp'
          input.onchange = async () => {
            const file = input.files?.[0]
            if (!file) return
            if (file.size > 8 * 1024 * 1024) {
              onError?.('图片不能超过 8MB')
              return
            }
            try {
              const dataUrl = await compressImageFile(file)
              insertImageToQuill(quill, dataUrl)
            } catch (err) {
              console.error('插图失败:', err)
              onError?.(err?.message || '图片处理失败')
            }
          }
          input.click()
        }
      }
    }
  }
}
