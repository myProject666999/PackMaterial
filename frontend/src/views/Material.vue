<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">物料档案</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增物料
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="物料名称">
        <el-input v-model="searchForm.materialName" placeholder="请输入物料名称" clearable />
      </el-form-item>
      <el-form-item label="物料类型">
        <el-select v-model="searchForm.materialType" placeholder="请选择物料类型" clearable>
          <el-option label="纸箱" value="纸箱" />
          <el-option label="泡沫" value="泡沫" />
          <el-option label="胶带" value="胶带" />
          <el-option label="标签" value="标签" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData.records" border stripe>
      <el-table-column prop="materialCode" label="物料编码" width="120" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column prop="specification" label="规格型号" />
      <el-table-column prop="materialType" label="物料类型" width="100">
        <template #default="{ row }">
          <el-tag>{{ row.materialType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="warehouseLocation" label="库位" width="100" />
      <el-table-column prop="safetyStock" label="安全库存" width="100" />
      <el-table-column prop="supplier" label="供应商" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="物料编码" prop="materialCode">
          <el-input v-model="form.materialCode" placeholder="请输入物料编码" />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="form.materialName" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.specification" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="物料类型" prop="materialType">
          <el-select v-model="form.materialType" placeholder="请选择物料类型" style="width: 100%">
            <el-option label="纸箱" value="纸箱" />
            <el-option label="泡沫" value="泡沫" />
            <el-option label="胶带" value="胶带" />
            <el-option label="标签" value="标签" />
          </el-select>
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入计量单位" />
        </el-form-item>
        <el-form-item label="库位">
          <el-input v-model="form.warehouseLocation" placeholder="请输入库位" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number v-model="form.safetyStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="form.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { materialApi } from '@/api'

const searchForm = reactive({
  materialName: '',
  materialType: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
})

const tableData = ref({
  records: [],
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  materialCode: '',
  materialName: '',
  specification: '',
  materialType: '',
  unit: '',
  warehouseLocation: '',
  safetyStock: 0,
  supplier: '',
  remark: ''
})

const rules = {
  materialCode: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入物料名称', trigger: 'blur' }],
  materialType: [{ required: true, message: '请选择物料类型', trigger: 'change' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }],
  safetyStock: [{ required: true, message: '请输入安全库存', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await materialApi.page({
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
  searchForm.materialName = ''
  searchForm.materialType = ''
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增物料'
  Object.keys(form).forEach(key => {
    form[key] = key === 'safetyStock' ? 0 : ''
    form.id = null
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑物料'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该物料吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await materialApi.delete(row.id)
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
      await materialApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await materialApi.save(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

loadData()
</script>
