<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">请购清单</span>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
          <el-option label="待处理" :value="1" />
          <el-option label="已采购" :value="2" />
          <el-option label="已关闭" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData.records" border stripe>
      <el-table-column prop="requestNo" label="请购单号" width="160" />
      <el-table-column prop="materialCode" label="物料编码" width="120" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column prop="specification" label="规格型号" />
      <el-table-column prop="currentStock" label="当前库存" width="100">
        <template #default="{ row }">
          <span class="low-stock">{{ row.currentStock }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="safetyStock" label="安全库存" width="100" />
      <el-table-column prop="suggestedQuantity" label="建议采购量" width="100">
        <template #default="{ row }">
          <span class="normal-stock">{{ row.suggestedQuantity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)">
            {{ getStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestTime" label="请购时间" width="180" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            size="small"
            type="success"
            link
            @click="handleProcess(row)"
          >标记已采购</el-button>
          <el-button
            v-if="row.status === 1"
            size="small"
            type="info"
            link
            @click="handleClose(row)"
          >关闭</el-button>
        </template>
      </el-table-column>
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

    <el-dialog v-model="closeDialogVisible" title="关闭请购单" width="400px">
      <el-form :model="closeForm" label-width="100px">
        <el-form-item label="操作人">
          <el-input v-model="closeForm.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="关闭原因">
          <el-input v-model="closeForm.remark" type="textarea" :rows="3" placeholder="请输入关闭原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmClose">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { purchaseRequestApi } from '@/api'

const searchForm = reactive({
  status: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const tableData = ref({
  records: [],
  total: 0
})

const closeDialogVisible = ref(false)
const currentId = ref(null)
const closeForm = reactive({
  operator: '',
  remark: ''
})

const getStatusName = (status) => {
  const names = { 1: '待处理', 2: '已采购', 3: '已关闭' }
  return names[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { 1: 'warning', 2: 'success', 3: 'info' }
  return tags[status] || 'info'
}

const loadData = async () => {
  try {
    const res = await purchaseRequestApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: searchForm.status || undefined
    })
    tableData.value = res
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const resetSearch = () => {
  searchForm.status = null
  loadData()
}

const handleProcess = (row) => {
  ElMessageBox.confirm('确定要标记为已采购吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await purchaseRequestApi.process(row.id, { operator: 'admin' })
      ElMessage.success('操作成功')
      loadData()
    } catch (error) {
      console.error('操作失败:', error)
    }
  })
}

const handleClose = (row) => {
  currentId.value = row.id
  closeForm.operator = ''
  closeForm.remark = ''
  closeDialogVisible.value = true
}

const confirmClose = async () => {
  if (!closeForm.operator) {
    ElMessage.warning('请输入操作人')
    return
  }
  try {
    await purchaseRequestApi.close(currentId.value, closeForm)
    ElMessage.success('关闭成功')
    closeDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('关闭失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>
