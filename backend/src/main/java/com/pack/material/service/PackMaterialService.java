package com.pack.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pack.material.entity.PackInventory;
import com.pack.material.entity.PackMaterial;
import com.pack.material.mapper.PackInventoryMapper;
import com.pack.material.mapper.PackMaterialMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class PackMaterialService extends ServiceImpl<PackMaterialMapper, PackMaterial> {

    @Resource
    private PackInventoryMapper packInventoryMapper;

    @Resource
    private RedisStockService redisStockService;

    public IPage<PackMaterial> page(Integer pageNum, Integer pageSize, String materialName, String materialType) {
        Page<PackMaterial> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PackMaterial> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(materialName)) {
            wrapper.like(PackMaterial::getMaterialName, materialName);
        }
        if (StringUtils.hasText(materialType)) {
            wrapper.eq(PackMaterial::getMaterialType, materialType);
        }
        wrapper.orderByDesc(PackMaterial::getCreateTime);
        return page(page, wrapper);
    }

    public List<PackMaterial> listAll() {
        return list(new LambdaQueryWrapper<PackMaterial>().orderByDesc(PackMaterial::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveMaterial(PackMaterial material) {
        boolean saved = save(material);
        if (saved) {
            PackInventory inventory = new PackInventory();
            inventory.setMaterialId(material.getId());
            inventory.setMaterialCode(material.getMaterialCode());
            inventory.setCurrentStock(0);
            inventory.setLockedStock(0);
            inventory.setAvailableStock(0);
            packInventoryMapper.insert(inventory);
            redisStockService.setStock(material.getId(), 0);
            redisStockService.setSafetyStock(material.getId(), material.getSafetyStock());
            log.info("新增物料成功，初始化库存: materialId={}, materialCode={}", material.getId(), material.getMaterialCode());
        }
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateMaterial(PackMaterial material) {
        boolean updated = updateById(material);
        if (updated) {
            redisStockService.setSafetyStock(material.getId(), material.getSafetyStock());
            log.info("更新物料成功: materialId={}, materialCode={}", material.getId(), material.getMaterialCode());
        }
        return updated;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMaterial(Long id) {
        boolean deleted = removeById(id);
        if (deleted) {
            redisStockService.deleteStockCache(id);
            log.info("删除物料成功: materialId={}", id);
        }
        return deleted;
    }
}
