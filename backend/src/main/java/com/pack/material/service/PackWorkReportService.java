package com.pack.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pack.material.common.Constants;
import com.pack.material.dto.WorkReportDTO;
import com.pack.material.entity.*;
import com.pack.material.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PackWorkReportService extends ServiceImpl<PackWorkReportMapper, PackWorkReport> {

    @Resource
    private PackWorkReportMapper packWorkReportMapper;

    @Resource
    private ProductPackBomMapper productPackBomMapper;

    @Resource
    private FinishedProductMapper finishedProductMapper;

    @Resource
    private PackMaterialMapper packMaterialMapper;

    @Resource
    private PackInventoryMapper packInventoryMapper;

    @Resource
    private PackStockRecordMapper packStockRecordMapper;

    @Resource
    private WorkReportMaterialMapper workReportMaterialMapper;

    @Resource
    private PurchaseRequestMapper purchaseRequestMapper;

    @Resource
    private RedisStockService redisStockService;

    public IPage<PackWorkReport> page(Integer pageNum, Integer pageSize, Integer status) {
        Page<PackWorkReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PackWorkReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PackWorkReport::getStatus, status);
        }
        wrapper.orderByDesc(PackWorkReport::getCreateTime);
        return page(page, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public PackWorkReport createReport(WorkReportDTO dto) {
        FinishedProduct product = finishedProductMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new RuntimeException("成品不存在");
        }
        List<ProductPackBom> bomList = productPackBomMapper.selectByProductId(dto.getProductId());
        if (bomList == null || bomList.isEmpty()) {
            throw new RuntimeException("该成品未配置包装BOM");
        }
        Map<Long, Integer> materialNeed = new HashMap<>();
        for (ProductPackBom bom : bomList) {
            int quantity = bom.getDosage().multiply(new BigDecimal(dto.getProductionQuantity())).setScale(0, BigDecimal.ROUND_UP).intValue();
            materialNeed.put(bom.getMaterialId(), quantity);
        }
        for (Map.Entry<Long, Integer> entry : materialNeed.entrySet()) {
            Integer availableStock = redisStockService.getStock(entry.getKey());
            if (availableStock == null) {
                PackInventory inventory = packInventoryMapper.selectOne(
                        new LambdaQueryWrapper<PackInventory>().eq(PackInventory::getMaterialId, entry.getKey()));
                availableStock = inventory != null ? inventory.getAvailableStock() : 0;
            }
            if (availableStock < entry.getValue()) {
                PackMaterial material = packMaterialMapper.selectById(entry.getKey());
                throw new RuntimeException("物料[" + material.getMaterialName() + "]库存不足，需要" + entry.getValue() + ", 可用库存" + availableStock);
            }
        }
        PackWorkReport report = new PackWorkReport();
        report.setReportNo("WR" + System.currentTimeMillis());
        report.setProductId(dto.getProductId());
        report.setProductCode(product.getProductCode());
        report.setProductName(product.getProductName());
        report.setProductionQuantity(dto.getProductionQuantity());
        report.setReportTime(LocalDateTime.now());
        report.setOperator(dto.getOperator());
        report.setWorkLine(dto.getWorkLine());
        report.setStatus(Constants.REPORT_STATUS_PENDING);
        report.setRemark(dto.getRemark());
        save(report);
        log.info("创建报工单成功: reportNo={}, productId={}, quantity={}",
                report.getReportNo(), dto.getProductId(), dto.getProductionQuantity());
        return report;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long reportId) {
        PackWorkReport report = getById(reportId);
        if (report == null) {
            throw new RuntimeException("报工单不存在");
        }
        if (!Constants.REPORT_STATUS_PENDING.equals(report.getStatus())) {
            throw new RuntimeException("报工单状态不正确");
        }
        List<ProductPackBom> bomList = productPackBomMapper.selectByProductId(report.getProductId());
        List<Long> lockedMaterials = new ArrayList<>();
        try {
            for (ProductPackBom bom : bomList) {
                int quantity = bom.getDosage().multiply(new BigDecimal(report.getProductionQuantity())).setScale(0, BigDecimal.ROUND_UP).intValue();
                boolean locked = redisStockService.decreaseStockWithLock(bom.getMaterialId(), quantity);
                if (!locked) {
                    throw new RuntimeException("物料[" + bom.getMaterialName() + "]库存不足");
                }
                lockedMaterials.add(bom.getMaterialId());
                int dbResult = packInventoryMapper.decreaseStock(bom.getMaterialId(), quantity);
                if (dbResult <= 0) {
                    throw new RuntimeException("数据库扣减库存失败: " + bom.getMaterialName());
                }
                PackMaterial material = packMaterialMapper.selectById(bom.getMaterialId());
                PackStockRecord record = new PackStockRecord();
                record.setRecordNo("DR" + System.currentTimeMillis() + bom.getMaterialId());
                record.setRecordType(Constants.RECORD_TYPE_REPORT_DEDUCT);
                record.setMaterialId(bom.getMaterialId());
                record.setMaterialCode(bom.getMaterialCode());
                record.setMaterialName(bom.getMaterialName());
                record.setQuantity(-quantity);
                record.setUnit(bom.getUnit());
                record.setOperator(report.getOperator());
                record.setOperateTime(LocalDateTime.now());
                record.setRelatedOrderNo(report.getReportNo());
                record.setRemark("报工自动核减");
                packStockRecordMapper.insert(record);
                BigDecimal standardQty = bom.getDosage().multiply(new BigDecimal(report.getProductionQuantity()));
                WorkReportMaterial reportMaterial = new WorkReportMaterial();
                reportMaterial.setReportId(report.getId());
                reportMaterial.setReportNo(report.getReportNo());
                reportMaterial.setMaterialId(bom.getMaterialId());
                reportMaterial.setMaterialCode(bom.getMaterialCode());
                reportMaterial.setMaterialName(bom.getMaterialName());
                reportMaterial.setStandardQuantity(standardQty);
                reportMaterial.setActualQuantity(new BigDecimal(quantity));
                reportMaterial.setDifferenceQuantity(new BigDecimal(quantity).subtract(standardQty));
                reportMaterial.setUnit(bom.getUnit());
                workReportMaterialMapper.insert(reportMaterial);
                checkAndGeneratePurchaseRequest(bom.getMaterialId());
            }
            report.setStatus(Constants.REPORT_STATUS_DEDUCTED);
            updateById(report);
            log.info("报工核减成功: reportId={}, reportNo={}", reportId, report.getReportNo());
            return true;
        } catch (Exception e) {
            log.error("报工核减失败，开始回滚Redis库存", e);
            for (ProductPackBom bom : bomList) {
                int quantity = bom.getDosage().multiply(new BigDecimal(report.getProductionQuantity())).setScale(0, BigDecimal.ROUND_UP).intValue();
                redisStockService.increaseStock(bom.getMaterialId(), quantity);
            }
            throw e;
        }
    }

    private void checkAndGeneratePurchaseRequest(Long materialId) {
        if (redisStockService.isStockLow(materialId)) {
            PackMaterial material = packMaterialMapper.selectById(materialId);
            PackInventory inventory = packInventoryMapper.selectOne(
                    new LambdaQueryWrapper<PackInventory>().eq(PackInventory::getMaterialId, materialId));
            if (material != null && inventory != null) {
                LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PurchaseRequest::getMaterialId, materialId)
                        .eq(PurchaseRequest::getStatus, Constants.PURCHASE_STATUS_PENDING);
                PurchaseRequest exist = purchaseRequestMapper.selectOne(wrapper);
                if (exist == null) {
                    PurchaseRequest request = new PurchaseRequest();
                    request.setRequestNo("PR" + System.currentTimeMillis());
                    request.setMaterialId(materialId);
                    request.setMaterialCode(material.getMaterialCode());
                    request.setMaterialName(material.getMaterialName());
                    request.setSpecification(material.getSpecification());
                    request.setCurrentStock(inventory.getCurrentStock());
                    request.setSafetyStock(material.getSafetyStock());
                    request.setSuggestedQuantity(material.getSafetyStock() * 2);
                    request.setUnit(material.getUnit());
                    request.setStatus(Constants.PURCHASE_STATUS_PENDING);
                    request.setRequestTime(LocalDateTime.now());
                    request.setOperator("system");
                    request.setRemark("库存低于安全线，请及时采购");
                    purchaseRequestMapper.insert(request);
                    log.info("生成请购单: materialId={}, currentStock={}, safetyStock={}",
                            materialId, inventory.getCurrentStock(), material.getSafetyStock());
                }
            }
        }
    }

    public List<WorkReportMaterial> getReportMaterials(Long reportId) {
        return workReportMaterialMapper.selectList(
                new LambdaQueryWrapper<WorkReportMaterial>().eq(WorkReportMaterial::getReportId, reportId));
    }
}
