<template>
  <div class="page-container">
    <div class="page-header">
      <span class="title">成品管理</span>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增成品
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="成品名称">
        <el-input v-model="searchForm.productName" placeholder="请输入成品名称" clearable />
      </el-form-item>
      <el-form-item label="产品分类">
        <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
          <el-option label="智能设备" value="智能设备" />
          <el-option label="音频设备" value="音频设备" />
          <el-option label="可穿戴" value="可穿戴" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData.records" border stripe>
      <el-table-column prop="productCode" label="成品编码" width="120" />
      <el-table-column prop="productName" label="成品名称" />
      <el-table-column prop="specification" label="规格型号" />
      <el-table-column prop="category" label="产品分类" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.category }}</el-tag>
        </template>
      </el-table-column>
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
        <el-form-item label="成品编码" prop="productCode">
          <el-input v-model="form.productCode" placeholder="请输入成品编码" />
        </el-form-item>
        <el-form-item label="成品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入成品名称" />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="form.specification" placeholder="请输入规格型号" />
        </el-form-item>
        <el-form-item label="产品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择产品分类" style="width: 100%">
            <el-option label="智能设备" value="智能设备" />
            <el-option label="音频设备" value="音频设备" />
            <el-option label="可穿戴" value="可穿戴" />
          </el-select>
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入计量单位" />
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
import { productApi } from '@/api'

const searchForm = reactive({
  productName: '',
  category: ''
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
  productCode: '',
  productName: '',
  specification: '',
  category: '',
  unit: '',
  remark: ''
})

const rules = {
  productCode: [{ required: true, message: '请输入成品编码', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入成品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }]
}

const loadData = async () => {
  try {
    const res = await productApi.page({
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
  searchForm.productName = ''
  searchForm.category = ''
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增成品'
  Object.keys(form).forEach(key => {
    form[key] = ''
    form.id = null
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑成品'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该成品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await productApi.delete(row.id)
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
      await productApi.update(form)
      ElMessage.success('更新成功')
    } else {
      await productApi.save(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>
