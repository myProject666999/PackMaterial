package com.pack.material.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pack.material.common.Constants;
import com.pack.material.entity.PurchaseRequest;
import com.pack.material.mapper.PurchaseRequestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PurchaseRequestService extends ServiceImpl<PurchaseRequestMapper, PurchaseRequest> {

    public IPage<PurchaseRequest> page(Integer pageNum, Integer pageSize, Integer status) {
        Page<PurchaseRequest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PurchaseRequest::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseRequest::getCreateTime);
        return page(page, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean processPurchase(Long id, String operator) {
        PurchaseRequest request = getById(id);
        if (request == null) {
            throw new RuntimeException("请购单不存在");
        }
        if (!Constants.PURCHASE_STATUS_PENDING.equals(request.getStatus())) {
            throw new RuntimeException("请购单状态不正确");
        }
        request.setStatus(Constants.PURCHASE_STATUS_PURCHASED);
        request.setOperator(operator);
        request.setUpdateTime(LocalDateTime.now());
        log.info("请购单已处理: id={}, requestNo={}", id, request.getRequestNo());
        return updateById(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean closePurchase(Long id, String operator, String remark) {
        PurchaseRequest request = getById(id);
        if (request == null) {
            throw new RuntimeException("请购单不存在");
        }
        request.setStatus(Constants.PURCHASE_STATUS_CLOSED);
        request.setOperator(operator);
        request.setRemark(remark);
        request.setUpdateTime(LocalDateTime.now());
        log.info("请购单已关闭: id={}, requestNo={}", id, request.getRequestNo());
        return updateById(request);
    }
}
