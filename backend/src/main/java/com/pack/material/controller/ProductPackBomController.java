package com.pack.material.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pack.material.common.Result;
import com.pack.material.entity.ProductPackBom;
import com.pack.material.service.ProductPackBomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bom")
@CrossOrigin
public class ProductPackBomController {

    @Resource
    private ProductPackBomService productPackBomService;

    @GetMapping("/page")
    public Result<IPage<ProductPackBom>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Long productId) {
        log.debug("查询BOM列表: pageNum={}, pageSize={}, productId={}", pageNum, pageSize, productId);
        return Result.success(productPackBomService.page(pageNum, pageSize, productId));
    }

    @GetMapping("/product/{productId}")
    public Result<List<ProductPackBom>> getByProductId(@PathVariable Long productId) {
        log.debug("查询成品BOM: productId={}", productId);
        return Result.success(productPackBomService.getByProductId(productId));
    }

    @GetMapping("/{id}")
    public Result<ProductPackBom> getById(@PathVariable Long id) {
        log.debug("查询BOM详情: id={}", id);
        return Result.success(productPackBomService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody ProductPackBom bom) {
        log.debug("新增BOM: {}", bom);
        return Result.success(productPackBomService.saveBom(bom));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody ProductPackBom bom) {
        log.debug("更新BOM: {}", bom);
        return Result.success(productPackBomService.updateBom(bom));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        log.debug("删除BOM: id={}", id);
        return Result.success(productPackBomService.removeById(id));
    }
}
