package com.pack.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pack.material.entity.PackStockRecord;
import com.pack.material.mapper.PackStockRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PackStockRecordService extends ServiceImpl<PackStockRecordMapper, PackStockRecord> {

    public IPage<PackStockRecord> page(Integer pageNum, Integer pageSize, String materialCode, Integer recordType) {
        Page<PackStockRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PackStockRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(materialCode)) {
            wrapper.like(PackStockRecord::getMaterialCode, materialCode);
        }
        if (recordType != null) {
            wrapper.eq(PackStockRecord::getRecordType, recordType);
        }
        wrapper.orderByDesc(PackStockRecord::getOperateTime);
        return page(page, wrapper);
    }
}
