import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '库存概览' }
  },
  {
    path: '/material',
    name: 'Material',
    component: () => import('@/views/Material.vue'),
    meta: { title: '物料档案' }
  },
  {
    path: '/inventory',
    name: 'Inventory',
    component: () => import('@/views/Inventory.vue'),
    meta: { title: '库存管理' }
  },
  {
    path: '/stock-record',
    name: 'StockRecord',
    component: () => import('@/views/StockRecord.vue'),
    meta: { title: '出入库记录' }
  },
  {
    path: '/product',
    name: 'Product',
    component: () => import('@/views/Product.vue'),
    meta: { title: '成品管理' }
  },
  {
    path: '/bom',
    name: 'Bom',
    component: () => import('@/views/Bom.vue'),
    meta: { title: '包装BOM' }
  },
  {
    path: '/work-report',
    name: 'WorkReport',
    component: () => import('@/views/WorkReport.vue'),
    meta: { title: '包装报工' }
  },
  {
    path: '/purchase-request',
    name: 'PurchaseRequest',
    component: () => import('@/views/PurchaseRequest.vue'),
    meta: { title: '请购清单' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 包装物料管理系统` : '包装物料管理系统'
  next()
})

export default router
