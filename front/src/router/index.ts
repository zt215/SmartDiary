
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: () => import('@/views/login.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register.vue'),
    },
    {
      path:'/forgotpassword',
      name:'forgotpassword',
      component: () => import('@/views/forgotpassword.vue'),
    },
    {
      path:'/home',
      name:'home',
      component: () => import('@/views/home.vue'),
    },
    {
      path:'/new-diary',
      name:'newDiary',
      component: () => import('@/views/newDiary.vue'),
    },
    {
      path:'/diary/:id',
      name:'diaryDetail',
      component: () => import('@/views/diaryDetail.vue'),
    },
    {
      path:'/settings',
      name:'settings',
      component: () => import('@/views/settings.vue'),
    },
    {
      path:'/diary-circle',
      name:'diaryCircle',
      component: () => import('@/views/diaryCircle.vue'),
    },
    {
      path:'/diary-circle/:id',
      name:'diaryCircleDetail',
      component: () => import('@/views/diaryCircleDetail.vue'),
    },
    
  ],
})

export default router
