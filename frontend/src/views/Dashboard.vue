<template>
  <div>
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">物料总数</div>
          <div class="value" style="color: #409EFF">{{ stats.materialCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">库存总数量</div>
          <div class="value" style="color: #67C23A">{{ stats.totalStock }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">库存预警</div>
          <div class="value" style="color: #F56C6C">{{ stats.lowStockCount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">待处理请购</div>
          <div class="value" style="color: #E6A23C">{{ stats.pendingPurchaseCount }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="page-container">
          <div class="page-header">
            <span class="title">库存预警物料</span>
          </div>
          <el-table :data="lowStockList" border stripe>
            <el-table-column prop="materialCode" label="物料编码" width="120" />
            <el-table-column prop="materialName" label="物料名称" />
            <el-table-column prop="materialType" label="物料类型" width="100" />
            <el-table-column prop="specification" label="规格型号" />
            <el-table-column label="当前库存" width="100">
              <template #default="{ row }">
                <span class="low-stock">{{ row.currentStock }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="safetyStock" label="安全库存" width="100" />
            <el-table-column prop="unit" label="单位" width="80" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-container">
          <div class="page-header">
            <span class="title">物料类型分布</span>
          </div>
          <div ref="chartRef" style="height: 300px"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt20">
      <el-col :span="24">
        <div class="page-container">
          <div class="page-header">
            <span class="title">最近出入库记录</span>
          </div>
          <el-table :data="recentRecords" border stripe>
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
            <el-table-column prop="remark" label="备注" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { materialApi, inventoryApi, stockRecordApi, purchaseRequestApi } from '@/api'

const chartRef = ref(null)
const stats = ref({
  materialCount: 0,
  totalStock: 0,
  lowStockCount: 0,
  pendingPurchaseCount: 0
})
const lowStockList = ref([])
const recentRecords = ref([])

const loadData = async () => {
  try {
    const [materials, inventory, purchase, records] = await Promise.all([
      materialApi.list(),
      inventoryApi.page({ pageNum: 1, pageSize: 1000 }),
      purchaseRequestApi.page({ pageNum: 1, pageSize: 1, status: 1 }),
      stockRecordApi.page({ pageNum: 1, pageSize: 10 })
    ])

    stats.value.materialCount = materials.length
    stats.value.totalStock = inventory.records.reduce((sum, item) => sum + item.currentStock, 0)
    stats.value.pendingPurchaseCount = purchase.total

    const lowStockItems = []
    const typeMap = {}
    for (const inv of inventory.records) {
      const material = materials.find(m => m.id === inv.materialId)
      if (material) {
        if (inv.currentStock <= material.safetyStock) {
          lowStockItems.push({
            ...inv,
            materialName: material.materialName,
            materialType: material.materialType,
            specification: material.specification,
            safetyStock: material.safetyStock
          })
        }
        typeMap[material.materialType] = (typeMap[material.materialType] || 0) + 1
      }
    }
    stats.value.lowStockCount = lowStockItems.length
    lowStockList.value = lowStockItems.slice(0, 8)
    recentRecords.value = records.records

    await nextTick()
    initChart(typeMap)
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const initChart = (typeMap) => {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '物料类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: Object.keys(typeMap).map(key => ({
          value: typeMap[key],
          name: key
        }))
      }
    ]
  }
  chart.setOption(option)
}

const getRecordTypeName = (type) => {
  const names = { 1: '采购入库', 2: '领用出库', 3: '报工核减', 4: '盘盈入库', 5: '盘亏出库' }
  return names[type] || '未知'
}

const getRecordTypeTag = (type) => {
  const tags = { 1: 'success', 2: 'danger', 3: 'warning', 4: 'success', 5: 'danger' }
  return tags[type] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.mb20 {
  margin-bottom: 20px;
}
.mt20 {
  margin-top: 20px;
}
</style>
