<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">包装BOM配置</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增BOM
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="成品">
        <el-select v-model="searchForm.productId" placeholder="请选择成品" clearable style="width: 250px" filterable @change="loadData">
          <el-option
            v-for="product in productList"
            :key="product.id"
            :label="product.productName + '(' + product.productCode + ')'"
            :value="product.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData.records" border stripe>
      <el-table-column prop="productCode" label="成品编码" width="120" />
      <el-table-column prop="productName" label="成品名称" width="150" />
      <el-table-column prop="materialCode" label="物料编码" width="120" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column prop="dosage" label="单位用量" width="100" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
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
        <el-form-item label="物料" prop="materialId">
          <el-select v-model="form.materialId" placeholder="请选择物料" style="width: 100%" filterable @change="onMaterialChange">
            <el-option
              v-for="material in materialList"
              :key="material.id"
              :label="material.materialName + '(' + material.materialCode + ')'"
              :value="material.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="单位用量" prop="dosage">
          <el-input-number v-model="form.dosage" :min="0" :precision="2" :step="0.1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="form.unit" disabled placeholder="选择物料后自动填充" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bomApi, productApi, materialApi } from '@/api'

const searchForm = reactive({
  productId: null
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
const materialList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  productId: null,
  productCode: '',
  materialId: null,
  materialCode: '',
  materialName: '',
  dosage: 0,
  unit: '',
  remark: ''
})

const rules = {
  productId: [{ required: true, message: '请选择成品', trigger: 'change' }],
  materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
  dosage: [{ required: true, message: '请输入单位用量', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await bomApi.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      productId: searchForm.productId || undefined
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

const loadMaterials = async () => {
  try {
    materialList.value = await materialApi.list()
  } catch (error) {
    console.error('加载物料列表失败:', error)
  }
}

const resetSearch = () => {
  searchForm.productId = null
  loadData()
}

const onProductChange = (productId) => {
  const product = productList.value.find(p => p.id === productId)
  if (product) {
    form.productCode = product.productCode
  }
}

const onMaterialChange = (materialId) => {
  const material = materialList.value.find(m => m.id === materialId)
  if (material) {
    form.materialCode = material.materialCode
    form.materialName = material.materialName
    form.unit = material.unit
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增BOM'
  Object.keys(form).forEach(key => {
    form[key] = key === 'dosage' ? 0 : ''
    form.id = null
    form.productId = null
    form.materialId = null
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑BOM'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该BOM配置吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await bomApi.delete(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await bomApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await bomApi.save(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  loadProducts()
  loadMaterials()
  loadData()
})
</script>
