<template>
  <el-container class="app-container">
    <el-header class="header">
      <div class="logo">
        <el-icon :size="28" color="#fff"><Package /></el-icon>
        <span class="title">包装物料管理系统</span>
      </div>
      <div class="user-info">
        <el-icon><User /></el-icon>
        <span>管理员</span>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="aside">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>库存概览</span>
          </el-menu-item>
          <el-menu-item index="/material">
            <el-icon><Goods /></el-icon>
            <span>物料档案</span>
          </el-menu-item>
          <el-menu-item index="/inventory">
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </el-menu-item>
          <el-menu-item index="/stock-record">
            <el-icon><Document /></el-icon>
            <span>出入库记录</span>
          </el-menu-item>
          <el-menu-item index="/product">
            <el-icon><Present /></el-icon>
            <span>成品管理</span>
          </el-menu-item>
          <el-menu-item index="/bom">
            <el-icon><List /></el-icon>
            <span>包装BOM</span>
          </el-menu-item>
          <el-menu-item index="/work-report">
            <el-icon><EditPen /></el-icon>
            <span>包装报工</span>
          </el-menu-item>
          <el-menu-item index="/purchase-request">
            <el-icon><ShoppingCart /></el-icon>
            <span>请购清单</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style lang="scss" scoped>
.app-container {
  height: 100vh;
}

.header {
  background: linear-gradient(90deg, #409EFF 0%, #67C23A 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  color: #fff;

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;

    .title {
      font-size: 20px;
      font-weight: 600;
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
  }
}

.aside {
  background: #304156;
  height: calc(100vh - 60px);
  overflow-y: auto;

  :deep(.el-menu) {
    border-right: none;
  }
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
