<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">出入库记录</span>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="物料编码">
        <el-input v-model="searchForm.materialCode" placeholder="请输入物料编码" clearable />
      </el-form-item>
      <el-form-item label="记录类型">
        <el-select v-model="searchForm.recordType" placeholder="请选择类型" clearable style="width: 150px">
          <el-option label="采购入库" :value="1" />
          <el-option label="领用出库" :value="2" />
          <el-option label="报工核减" :value="3" />
          <el-option label="盘盈入库" :value="4" />
          <el-option label="盘亏出库" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData.records" border stripe>
      <el-table-column prop="recordNo" label="单据编号" width="160" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getRecordTypeTag(row.recordType)">
            {{ getRecordTypeName(row.recordType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="materialCode" label="物料编码" width="120" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column label="数量" width="100">
        <template #default="{ row }">
          <span :class="row.quantity > 0 ? 'normal-stock' : 'low-stock'">
            {{ row.quantity > 0 ? '+' : '' }}{{ row.quantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="operateTime" label="操作时间" width="180" />
      <el-table-column prop="relatedOrderNo" label="关联单据" width="160" />
      <el-table-column prop="remark" label="备注" />
    </el-table>

    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="tableData.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 20px; justify-content: flex-end; display: flex"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { stockRecordApi } from '@/api'

const searchForm = reactive({
  materialCode: '',
  recordType: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const tableData = ref({
  records: [],
  total: 0
})

const getRecordTypeName = (type) => {
  const names = { 1: '采购入库', 2: '领用出库', 3: '报工核减', 4: '盘盈入库', 5: '盘亏出库' }
  return names[type] || '未知'
}

const getRecordTypeTag = (type) => {
  const tags = { 1: 'success', 2: 'danger', 3: 'warning', 4: 'success', 5: 'danger' }
  return tags[type] || 'info'
}

const loadData = async () => {
  try {
    const res = await stockRecordApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const resetSearch = () => {
  searchForm.materialCode = ''
  searchForm.recordType = null
  loadData()
}

onMounted(() => {
  loadData()
})
</script>
