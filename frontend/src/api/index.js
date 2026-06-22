import request from '@/utils/request'

export const materialApi = {
  page: (params) => request.get('/material/page', { params }),
  list: () => request.get('/material/list'),
  get: (id) => request.get(`/material/${id}`),
  save: (data) => request.post('/material', data),
  update: (data) => request.put('/material', data),
  delete: (id) => request.delete(`/material/${id}`)
}

export const inventoryApi = {
  page: (params) => request.get('/inventory/page', { params }),
  getByMaterialId: (materialId) => request.get(`/inventory/${materialId}`),
  getAvailableStock: (materialId) => request.get(`/inventory/available/${materialId}`),
  stockIn: (data) => request.post('/inventory/in', data),
  stockOut: (data) => request.post('/inventory/out', data)
}

export const stockRecordApi = {
  page: (params) => request.get('/stock-record/page', { params }),
  get: (id) => request.get(`/stock-record/${id}`)
}

export const productApi = {
  page: (params) => request.get('/product/page', { params }),
  list: () => request.get('/product/list'),
  get: (id) => request.get(`/product/${id}`),
  save: (data) => request.post('/product', data),
  update: (data) => request.put('/product', data),
  delete: (id) => request.delete(`/product/${id}`)
}

export const bomApi = {
  page: (params) => request.get('/bom/page', { params }),
  getByProductId: (productId) => request.get(`/bom/product/${productId}`),
  get: (id) => request.get(`/bom/${id}`),
  save: (data) => request.post('/bom', data),
  update: (data) => request.put('/bom', data),
  delete: (id) => request.delete(`/bom/${id}`)
}

export const workReportApi = {
  page: (params) => request.get('/work-report/page', { params }),
  getDetail: (id) => request.get(`/work-report/${id}`),
  getMaterials: (reportId) => request.get(`/work-report/materials/${reportId}`),
  create: (data) => request.post('/work-report', data),
  deduct: (id) => request.post(`/work-report/deduct/${id}`)
}

export const purchaseRequestApi = {
  page: (params) => request.get('/purchase-request/page', { params }),
  get: (id) => request.get(`/purchase-request/${id}`),
  process: (id, data) => request.post(`/purchase-request/process/${id}`, data),
  close: (id, data) => request.post(`/purchase-request/close/${id}`, data)
}
