<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">库存管理</span>
      <div>
        <el-button type="success" @click="handleStockIn">
          <el-icon><CirclePlus /></el-icon>
          采购入库
        </el-button>
        <el-button type="warning" @click="handleStockOut">
          <el-icon><Minus /></el-icon>
          领用出库
        </el-button>
      </div>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="物料编码">
        <el-input v-model="searchForm.materialCode" placeholder="请输入物料编码" clearable />
      </el-form-item>
      <el-form-item label="库存预警">
        <el-switch v-model="searchForm.lowStock" active-text="是" inactive-text="否" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData.records" border stripe>
      <el-table-column prop="materialCode" label="物料编码" width="120" />
      <el-table-column label="物料名称" width="150">
        <template #default="{ row }">
          {{ getMaterialName(row.materialId) }}
        </template>
      </el-table-column>
      <el-table-column label="物料类型" width="100">
        <template #default="{ row }">
          <el-tag>{{ getMaterialType(row.materialId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentStock" label="当前库存" width="100" :cell-class-name="cellClassName" />
      <el-table-column prop="lockedStock" label="锁定库存" width="100" />
      <el-table-column prop="availableStock" label="可用库存" width="100" :cell-class-name="cellClassName" />
      <el-table-column label="安全库存" width="100">
        <template #default="{ row }">
          {{ getSafetyStock(row.materialId) }}
        </template>
      </el-table-column>
      <el-table-column label="库位" width="100">
        <template #default="{ row }">
          {{ getWarehouseLocation(row.materialId) }}
        </template>
      </el-table-column>
      <el-table-column prop="lastInTime" label="最近入库" width="180" />
      <el-table-column prop="lastOutTime" label="最近出库" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="isLowStock(row) ? 'danger' : 'success'">
            {{ isLowStock(row) ? '库存预警' : '正常' }}
          </el-tag>
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

    <el-dialog v-model="operateDialogVisible" :title="operateDialogTitle" width="500px">
      <el-form :model="operateForm" :rules="operateRules" ref="operateFormRef" label-width="100px">
        <el-form-item label="物料" prop="materialId">
          <el-select v-model="operateForm.materialId" placeholder="请选择物料" style="width: 100%" filterable>
            <el-option
              v-for="material in materialList"
              :key="material.id"
              :label="material.materialName + '(' + material.materialCode + ')'"
              :value="material.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="operateForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="operateForm.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="关联单据">
          <el-input v-model="operateForm.relatedOrderNo" placeholder="请输入关联单据号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="operateForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="operateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOperateSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { inventoryApi, materialApi } from '@/api'

const searchForm = reactive({
  materialCode: '',
  lowStock: false
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const tableData = ref({
  records: [],
  total: 0
})

const materialList = ref([])

const operateDialogVisible = ref(false)
const operateDialogTitle = ref('')
const operateType = ref('')
const operateFormRef = ref(null)

const operateForm = reactive({
  materialId: null,
  quantity: 1,
  operator: '',
  relatedOrderNo: '',
  remark: ''
})

const operateRules = {
  materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const getMaterialName = (materialId) => {
  const material = materialList.value.find(m => m.id === materialId)
  return material ? material.materialName : '-'
}

const getMaterialType = (materialId) => {
  const material = materialList.value.find(m => m.id === materialId)
  return material ? material.materialType : '-'
}

const getSafetyStock = (materialId) => {
  const material = materialList.value.find(m => m.id === materialId)
  return material ? material.safetyStock : '-'
}

const getWarehouseLocation = (materialId) => {
  const material = materialList.value.find(m => m.id === materialId)
  return material ? material.warehouseLocation : '-'
}

const isLowStock = (row) => {
  const material = materialList.value.find(m => m.id === row.materialId)
  return material && row.availableStock <= material.safetyStock
}

const cellClassName = ({ row }) => {
  if (isLowStock(row)) {
    return 'low-stock-cell'
  }
  return ''
}

const loadData = async () => {
  try {
    const res = await inventoryApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const loadMaterials = async () => {
  try {
    materialList.value = await materialApi.list()
  } catch (error) {
    console.error('加载物料列表失败:', error)
  }
}

const resetSearch = () => {
  searchForm.materialCode = ''
  searchForm.lowStock = false
  loadData()
}

const handleStockIn = () => {
  operateType.value = 'in'
  operateDialogTitle.value = '采购入库'
  Object.keys(operateForm).forEach(key => {
    operateForm[key] = key === 'quantity' ? 1 : ''
    operateForm.materialId = null
  })
  operateDialogVisible.value = true
}

const handleStockOut = () => {
  operateType.value = 'out'
  operateDialogTitle.value = '领用出库'
  Object.keys(operateForm).forEach(key => {
    operateForm[key] = key === 'quantity' ? 1 : ''
    operateForm.materialId = null
  })
  operateDialogVisible.value = true
}

const handleOperateSubmit = async () => {
  if (!operateFormRef.value) return
  try {
    await operateFormRef.value.validate()
    if (operateType.value === 'in') {
      await inventoryApi.stockIn(operateForm)
      ElMessage.success('入库成功')
    } else {
      await inventoryApi.stockOut(operateForm)
      ElMessage.success('出库成功')
    }
    operateDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => {
  loadMaterials()
  loadData()
})
</script>

<style scoped>
:deep(.low-stock-cell) {
  color: #F56C6C !important;
  font-weight: 600;
}
</style>
