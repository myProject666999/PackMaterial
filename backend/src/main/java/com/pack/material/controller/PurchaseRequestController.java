package com.pack.material.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pack.material.common.Result;
import com.pack.material.entity.PurchaseRequest;
import com.pack.material.service.PurchaseRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/purchase-request")
@CrossOrigin
public class PurchaseRequestController {

    @Resource
    private PurchaseRequestService purchaseRequestService;

    @GetMapping("/page")
    public Result<IPage<PurchaseRequest>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) Integer status) {
        log.debug("查询请购单列表: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        return Result.success(purchaseRequestService.page(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<PurchaseRequest> getById(@PathVariable Long id) {
        log.debug("查询请购单详情: id={}", id);
        return Result.success(purchaseRequestService.getById(id));
    }

    @PostMapping("/process/{id}")
    public Result<Boolean> process(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String operator = params.get("operator");
        log.debug("处理请购单: id={}, operator={}", id, operator);
        return Result.success(purchaseRequestService.processPurchase(id, operator));
    }

    @PostMapping("/close/{id}")
    public Result<Boolean> close(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String operator = params.get("operator");
        String remark = params.get("remark");
        log.debug("关闭请购单: id={}, operator={}, remark={}", id, operator, remark);
        return Result.success(purchaseRequestService.closePurchase(id, operator, remark));
    }
}
