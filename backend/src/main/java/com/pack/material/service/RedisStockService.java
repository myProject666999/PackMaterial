package com.pack.material.service;

import com.pack.material.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisStockService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    public void setStock(Long materialId, Integer stock) {
        String key = Constants.REDIS_STOCK_PREFIX + materialId;
        redisTemplate.opsForValue().set(key, stock);
        log.debug("设置Redis库存: materialId={}, stock={}", materialId, stock);
    }

    public Integer getStock(Long materialId) {
        String key = Constants.REDIS_STOCK_PREFIX + materialId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    public void setSafetyStock(Long materialId, Integer safetyStock) {
        String key = Constants.REDIS_SAFETY_STOCK_PREFIX + materialId;
        redisTemplate.opsForValue().set(key, safetyStock);
        log.debug("设置Redis安全库存: materialId={}, safetyStock={}", materialId, safetyStock);
    }

    public Integer getSafetyStock(Long materialId) {
        String key = Constants.REDIS_SAFETY_STOCK_PREFIX + materialId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    public boolean isStockLow(Long materialId) {
        Integer stock = getStock(materialId);
        Integer safetyStock = getSafetyStock(materialId);
        if (stock == null || safetyStock == null) {
            return false;
        }
        return stock <= safetyStock;
    }

    public boolean decreaseStockWithLock(Long materialId, int quantity) {
        String lockKey = Constants.REDIS_STOCK_LOCK_PREFIX + materialId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(10, Constants.STOCK_LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取库存锁失败: materialId={}", materialId);
                return false;
            }
            try {
                Integer currentStock = getStock(materialId);
                if (currentStock == null) {
                    log.warn("库存不存在于Redis中: materialId={}", materialId);
                    return false;
                }
                if (currentStock < quantity) {
                    log.warn("库存不足: materialId={}, currentStock={}, need={}", materialId, currentStock, quantity);
                    return false;
                }
                int newStock = currentStock - quantity;
                setStock(materialId, newStock);
                log.debug("扣减Redis库存成功: materialId={}, oldStock={}, newStock={}, quantity={}",
                        materialId, currentStock, newStock, quantity);
                return true;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("扣减库存被中断: materialId={}", materialId, e);
            return false;
        }
    }

    public void increaseStock(Long materialId, int quantity) {
        String lockKey = Constants.REDIS_STOCK_LOCK_PREFIX + materialId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(10, Constants.STOCK_LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取库存锁失败: materialId={}", materialId);
                return;
            }
            try {
                Integer currentStock = getStock(materialId);
                if (currentStock == null) {
                    setStock(materialId, quantity);
                } else {
                    int newStock = currentStock + quantity;
                    setStock(materialId, newStock);
                    log.debug("增加Redis库存成功: materialId={}, oldStock={}, newStock={}, quantity={}",
                            materialId, currentStock, newStock, quantity);
                }
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("增加库存被中断: materialId={}", materialId, e);
        }
    }

    public void deleteStockCache(Long materialId) {
        String key = Constants.REDIS_STOCK_PREFIX + materialId;
        redisTemplate.delete(key);
        log.debug("删除Redis库存缓存: materialId={}", materialId);
    }
}
