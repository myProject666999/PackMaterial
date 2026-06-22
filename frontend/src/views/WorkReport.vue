<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">包装报工</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增报工
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
          <el-option label="待核减" :value="1" />
          <el-option label="已核减" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData.records" border stripe>
      <el-table-column prop="reportNo" label="报工单号" width="160" />
      <el-table-column prop="productCode" label="成品编码" width="120" />
      <el-table-column prop="productName" label="成品名称" />
      <el-table-column prop="productionQuantity" label="生产数量" width="100" />
      <el-table-column prop="workLine" label="包装线" width="100" />
      <el-table-column prop="operator" label="操作人" width="100" />
      <el-table-column prop="reportTime" label="报工时间" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTag(row.status)">
            {{ getStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 1"
            size="small"
            type="success"
            link
            @click="handleDeduct(row)"
          >核减库存</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增报工" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="成品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择成品" style="width: 100%" filterable @change="onProductChange">
            <el-option
              v-for="product in productList"
              :key="product.id"
              :label="product.productName + '(' + product.productCode + ')'"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生产数量" prop="productionQuantity">
          <el-input-number v-model="form.productionQuantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="包装线">
          <el-input v-model="form.workLine" placeholder="请输入包装线" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="form.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <div v-if="bomList.length > 0" style="margin-top: 20px">
        <h4 style="margin-bottom: 10px">预计用料明细：</h4>
        <el-table :data="bomPreviewList" border size="small">
          <el-table-column prop="materialName" label="物料名称" />
          <el-table-column prop="dosage" label="单位用量" width="100" />
          <el-table-column prop="needQuantity" label="需求数量" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column label="库存状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.availableStock >= row.needQuantity ? 'success' : 'danger'">
                {{ row.availableStock >= row.needQuantity ? '充足' : '不足' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="报工详情" width="700px">
      <el-descriptions :column="2" border v-if="reportDetail.report">
        <el-descriptions-item label="报工单号">{{ reportDetail.report.reportNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(reportDetail.report.status)">
            {{ getStatusName(reportDetail.report.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="成品编码">{{ reportDetail.report.productCode }}</el-descriptions-item>
        <el-descriptions-item label="成品名称">{{ reportDetail.report.productName }}</el-descriptions-item>
        <el-descriptions-item label="生产数量">{{ reportDetail.report.productionQuantity }}</el-descriptions-item>
        <el-descriptions-item label="包装线">{{ reportDetail.report.workLine || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ reportDetail.report.operator }}</el-descriptions-item>
        <el-descriptions-item label="报工时间">{{ reportDetail.report.reportTime }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin: 20px 0 10px">用料明细：</h4>
      <el-table :data="reportDetail.materials" border size="small">
        <el-table-column prop="materialName" label="物料名称" />
        <el-table-column prop="standardQuantity" label="标准用量" width="100" />
        <el-table-column prop="actualQuantity" label="实际用量" width="100" />
        <el-table-column label="用量差异" width="100">
          <template #default="{ row }">
            <span :class="row.differenceQuantity > 0 ? 'low-stock' : 'normal-stock'">
              {{ row.differenceQuantity > 0 ? '+' : '' }}{{ row.differenceQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
      </el-table>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { workReportApi, productApi, bomApi, inventoryApi } from '@/api'

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

const productList = ref([])
const bomList = ref([])
const bomPreviewList = ref([])

const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
  productId: null,
  productionQuantity: 1,
  operator: '',
  workLine: '',
  remark: ''
})

const reportDetail = ref({
  report: null,
  materials: []
})

const rules = {
  productId: [{ required: true, message: '请选择成品', trigger: 'change' }],
  productionQuantity: [{ required: true, message: '请输入生产数量', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const getStatusName = (status) => {
  const names = { 1: '待核减', 2: '已核减', 3: '已取消' }
  return names[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { 1: 'warning', 2: 'success', 3: 'info' }
  return tags[status] || 'info'
}

const loadData = async () => {
  try {
    const res = await workReportApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const loadProducts = async () => {
  try {
    productList.value = await productApi.list()
  } catch (error) {
    console.error('加载成品列表失败:', error)
  }
}

const resetSearch = () => {
  searchForm.status = null
  loadData()
}

const onProductChange = async (productId) => {
  try {
    bomList.value = await bomApi.getByProductId(productId)
    bomPreviewList.value = []
    for (const bom of bomList.value) {
      const stock = await inventoryApi.getAvailableStock(bom.materialId)
      bomPreviewList.value.push({
        ...bom,
        needQuantity: Math.ceil(bom.dosage * form.productionQuantity),
        availableStock: stock.availableStock || 0
      })
    }
  } catch (error) {
    console.error('加载BOM失败:', error)
  }
}

const handleAdd = () => {
  Object.keys(form).forEach(key => {
    form[key] = key === 'productionQuantity' ? 1 : ''
    form.productId = null
  })
  bomList.value = []
  bomPreviewList.value = []
  dialogVisible.value = true
}

const handleDetail = async (row) => {
  try {
    reportDetail.value = await workReportApi.getDetail(row.id)
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
  }
}

const handleDeduct = (row) => {
  ElMessageBox.confirm('确定要核减库存吗？核减后将自动扣减对应包装物料库存。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await workReportApi.deduct(row.id)
      ElMessage.success('核减成功')
      loadData()
    } catch (error) {
      console.error('核减失败:', error)
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    await workReportApi.create(form)
    ElMessage.success('报工成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  loadProducts()
  loadData()
})
</script>
