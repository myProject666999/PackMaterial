package com.pack.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pack.material.common.Constants;
import com.pack.material.dto.StockOperateDTO;
import com.pack.material.entity.PackInventory;
import com.pack.material.entity.PackMaterial;
import com.pack.material.entity.PackStockRecord;
import com.pack.material.entity.PurchaseRequest;
import com.pack.material.mapper.PackInventoryMapper;
import com.pack.material.mapper.PackMaterialMapper;
import com.pack.material.mapper.PackStockRecordMapper;
import com.pack.material.mapper.PurchaseRequestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PackInventoryService extends ServiceImpl<PackInventoryMapper, PackInventory> {

    @Resource
    private PackInventoryMapper packInventoryMapper;

    @Resource
    private PackMaterialMapper packMaterialMapper;

    @Resource
    private PackStockRecordMapper packStockRecordMapper;

    @Resource
    private PurchaseRequestMapper purchaseRequestMapper;

    @Resource
    private RedisStockService redisStockService;

    @PostConstruct
    public void initStockCache() {
        log.info("开始初始化库存缓存到Redis...");
        List<PackInventory> inventories = list();
        for (PackInventory inventory : inventories) {
            redisStockService.setStock(inventory.getMaterialId(), inventory.getCurrentStock());
            PackMaterial material = packMaterialMapper.selectById(inventory.getMaterialId());
            if (material != null) {
                redisStockService.setSafetyStock(material.getId(), material.getSafetyStock());
            }
        }
        log.info("库存缓存初始化完成，共{}条记录", inventories.size());
    }

    public IPage<PackInventory> page(Integer pageNum, Integer pageSize, String materialCode, Boolean lowStock) {
        Page<PackInventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PackInventory> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(materialCode)) {
            wrapper.like(PackInventory::getMaterialCode, materialCode);
        }
        if (lowStock != null && lowStock) {
            List<PackMaterial> materials = packMaterialMapper.selectList(null);
            for (PackMaterial material : materials) {
                if (Boolean.TRUE.equals(redisStockService.isStockLow(material.getId()))) {
                    wrapper.or().eq(PackInventory::getMaterialId, material.getId());
                }
            }
        }
        wrapper.orderByDesc(PackInventory::getUpdateTime);
        return page(page, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean stockIn(StockOperateDTO dto) {
        PackMaterial material = packMaterialMapper.selectById(dto.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物料不存在");
        }
        int result = packInventoryMapper.increaseStock(dto.getMaterialId(), dto.getQuantity());
        if (result <= 0) {
            throw new RuntimeException("入库失败");
        }
        redisStockService.increaseStock(dto.getMaterialId(), dto.getQuantity());
        PackStockRecord record = new PackStockRecord();
        record.setRecordNo("IN" + System.currentTimeMillis());
        record.setRecordType(Constants.RECORD_TYPE_PURCHASE_IN);
        record.setMaterialId(dto.getMaterialId());
        record.setMaterialCode(material.getMaterialCode());
        record.setMaterialName(material.getMaterialName());
        record.setQuantity(dto.getQuantity());
        record.setUnit(material.getUnit());
        record.setOperator(dto.getOperator());
        record.setOperateTime(LocalDateTime.now());
        record.setRelatedOrderNo(dto.getRelatedOrderNo());
        record.setRemark(dto.getRemark());
        packStockRecordMapper.insert(record);
        log.info("采购入库成功: materialId={}, quantity={}, operator={}", dto.getMaterialId(), dto.getQuantity(), dto.getOperator());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean stockOut(StockOperateDTO dto) {
        PackMaterial material = packMaterialMapper.selectById(dto.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物料不存在");
        }
        boolean redisResult = redisStockService.decreaseStockWithLock(dto.getMaterialId(), dto.getQuantity());
        if (!redisResult) {
            throw new RuntimeException("库存不足");
        }
        int result = packInventoryMapper.decreaseStock(dto.getMaterialId(), dto.getQuantity());
        if (result <= 0) {
            redisStockService.increaseStock(dto.getMaterialId(), dto.getQuantity());
            throw new RuntimeException("出库失败");
        }
        PackStockRecord record = new PackStockRecord();
        record.setRecordNo("OUT" + System.currentTimeMillis());
        record.setRecordType(Constants.RECORD_TYPE_RECEIVE_OUT);
        record.setMaterialId(dto.getMaterialId());
        record.setMaterialCode(material.getMaterialCode());
        record.setMaterialName(material.getMaterialName());
        record.setQuantity(-dto.getQuantity());
        record.setUnit(material.getUnit());
        record.setOperator(dto.getOperator());
        record.setOperateTime(LocalDateTime.now());
        record.setRelatedOrderNo(dto.getRelatedOrderNo());
        record.setRemark(dto.getRemark());
        packStockRecordMapper.insert(record);
        checkAndGeneratePurchaseRequest(dto.getMaterialId());
        log.info("领用出库成功: materialId={}, quantity={}, operator={}", dto.getMaterialId(), dto.getQuantity(), dto.getOperator());
        return true;
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

    public PackInventory getByMaterialId(Long materialId) {
        return getOne(new LambdaQueryWrapper<PackInventory>().eq(PackInventory::getMaterialId, materialId));
    }

    public Integer getAvailableStock(Long materialId) {
        Integer stock = redisStockService.getStock(materialId);
        if (stock != null) {
            return stock;
        }
        PackInventory inventory = getByMaterialId(materialId);
        if (inventory != null) {
            redisStockService.setStock(materialId, inventory.getAvailableStock());
            return inventory.getAvailableStock();
        }
        return 0;
    }
}
