package com.pack.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pack.material.entity.PackMaterial;
import com.pack.material.entity.ProductPackBom;
import com.pack.material.mapper.PackMaterialMapper;
import com.pack.material.mapper.ProductPackBomMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ProductPackBomService extends ServiceImpl<ProductPackBomMapper, ProductPackBom> {

    @Resource
    private ProductPackBomMapper productPackBomMapper;

    @Resource
    private PackMaterialMapper packMaterialMapper;

    public IPage<ProductPackBom> page(Integer pageNum, Integer pageSize, Long productId) {
        Page<ProductPackBom> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductPackBom> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(ProductPackBom::getProductId, productId);
        }
        wrapper.orderByDesc(ProductPackBom::getCreateTime);
        return page(page, wrapper);
    }

    public List<ProductPackBom> getByProductId(Long productId) {
        return productPackBomMapper.selectByProductId(productId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveBom(ProductPackBom bom) {
        PackMaterial material = packMaterialMapper.selectById(bom.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物料不存在");
        }
        bom.setMaterialCode(material.getMaterialCode());
        bom.setMaterialName(material.getMaterialName());
        bom.setUnit(material.getUnit());
        return save(bom);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateBom(ProductPackBom bom) {
        PackMaterial material = packMaterialMapper.selectById(bom.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物料不存在");
        }
        bom.setMaterialCode(material.getMaterialCode());
        bom.setMaterialName(material.getMaterialName());
        bom.setUnit(material.getUnit());
        return updateById(bom);
    }
}
