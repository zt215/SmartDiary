<template>
  <button
    type="button"
    class="comment-like-stack"
    :class="{ active: isLiked }"
    :aria-label="isLiked ? '取消点赞' : '点赞'"
    @click.stop="$emit('toggle')"
  >
    <span class="like-sprite-icon" aria-hidden="true"></span>
    <span class="like-sprite-count">{{ count ?? 0 }}</span>
  </button>
</template>

<script>
export default {
  name: 'CommentLikeButton',
  props: {
    isLiked: { type: Boolean, default: false },
    count: { type: Number, default: 0 }
  },
  emits: ['toggle']
}
</script>

<style scoped>
.comment-like-stack {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 2px;
  min-width: 40px;
  padding: 4px 6px;
  margin: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.15s;
}

.comment-like-stack:hover {
  background-color: var(--hover-bg, rgba(0, 0, 0, 0.04));
}

.like-sprite-icon {
  display: block;
  width: 28px;
  height: 28px;
  background-image: url('../assets/comment-like-sprite.svg');
  background-repeat: no-repeat;
  background-size: 28px 56px;
  background-position: 0 0;
}

.comment-like-stack.active .like-sprite-icon {
  background-position: 0 -28px;
}

.like-sprite-count {
  font-size: 12px;
  line-height: 1.2;
  color: var(--text-color, #666);
  opacity: 0.85;
  font-variant-numeric: tabular-nums;
}

.comment-like-stack.active .like-sprite-count {
  color: var(--calendar-today-bg, #3d8be4);
  font-weight: 600;
  opacity: 1;
}
</style>
