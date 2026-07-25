import { createRouter, createWebHistory } from 'vue-router'

// 布局组件（懒加载）
const DefaultLayout = () => import('@/layouts/DefaultLayout.vue')
const AdminLayout = () => import('@/layouts/AdminLayout.vue')

// 页面组件（懒加载）
const LoginView = () => import('@/views/auth/LoginView.vue')
const RegisterView = () => import('@/views/auth/RegisterView.vue')
const HomeView = () => import('@/views/homepage/HomeView.vue')
const DigestArchiveView = () => import('@/views/homepage/DigestArchiveView.vue')
const TrendListView = () => import('@/views/trend/TrendListView.vue')
const TrendDetailView = () => import('@/views/trend/TrendDetailView.vue')
const TagBrowseView = () => import('@/views/trend/TagBrowseView.vue')
const ResearchView = () => import('@/views/chat/ResearchView.vue')
const ProfileView = () => import('@/views/user/ProfileView.vue')
const InterestView = () => import('@/views/user/InterestView.vue')
const ArticleManageView = () => import('@/views/admin/ArticleManageView.vue')
const UserInterestView = () => import('@/views/admin/UserInterestView.vue')
const PipelineTaskView = () => import('@/views/admin/PipelineTaskView.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
    meta: { title: '注册', guest: true }
  },
  {
    path: '/',
    component: DefaultLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: HomeView,
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'digest',
        name: 'DigestArchive',
        component: DigestArchiveView,
        meta: { title: '每日摘要', requiresAuth: true }
      },
      {
        path: 'trends',
        name: 'TrendList',
        component: TrendListView,
        meta: { title: '技术趋势', requiresAuth: true }
      },
      {
        path: 'trends/:id',
        name: 'TrendDetail',
        component: TrendDetailView,
        meta: { title: '趋势详情', requiresAuth: true }
      },
      {
        path: 'tags/:tag',
        name: 'TagBrowse',
        component: TagBrowseView,
        meta: { title: '标签浏览', requiresAuth: true }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: ResearchView,
        meta: { title: '研究助手', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: ProfileView,
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'interests',
        name: 'Interests',
        component: InterestView,
        meta: { title: '兴趣管理', requiresAuth: true }
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'articles',
        name: 'AdminArticles',
        component: ArticleManageView,
        meta: { title: '内容管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'tags',
        name: 'AdminTags',
        component: ArticleManageView,  // 暂时复用，后续替换
        meta: { title: '标签管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'pipeline',
        name: 'AdminPipeline',
        component: PipelineTaskView,
        meta: { title: '内容流水线', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'users/interests',
        name: 'AdminUserInterests',
        component: UserInterestView,
        meta: { title: '用户兴趣', requiresAuth: true, requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫 — 鉴权
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 需要登录但未登录 → 跳转登录页
  if (to.meta.requiresAuth && !token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  // 已登录但访问游客页（登录/注册）→ 跳转首页
  if (to.meta.guest && token) {
    return next({ name: 'Home' })
  }

  next()
})

export default router
