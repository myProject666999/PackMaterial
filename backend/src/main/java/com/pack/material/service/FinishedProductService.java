package com.pack.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pack.material.entity.FinishedProduct;
import com.pack.material.mapper.FinishedProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class FinishedProductService extends ServiceImpl<FinishedProductMapper, FinishedProduct> {

    public IPage<FinishedProduct> page(Integer pageNum, Integer pageSize, String productName, String category) {
        Page<FinishedProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FinishedProduct> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(productName)) {
            wrapper.like(FinishedProduct::getProductName, productName);
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(FinishedProduct::getCategory, category);
        }
        wrapper.orderByDesc(FinishedProduct::getCreateTime);
        return page(page, wrapper);
    }

    public java.util.List<FinishedProduct> listAll() {
        return list(new LambdaQueryWrapper<FinishedProduct>().orderByDesc(FinishedProduct::getCreateTime));
    }
}
