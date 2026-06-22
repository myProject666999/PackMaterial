package com.pack.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pack.material.entity.PackInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PackInventoryMapper extends BaseMapper<PackInventory> {

    @Update("UPDATE pack_inventory SET current_stock = current_stock + #{quantity}, " +
            "available_stock = available_stock + #{quantity}, last_in_time = NOW() " +
            "WHERE material_id = #{materialId}")
    int increaseStock(@Param("materialId") Long materialId, @Param("quantity") Integer quantity);

    @Update("UPDATE pack_inventory SET current_stock = current_stock - #{quantity}, " +
            "available_stock = available_stock - #{quantity}, last_out_time = NOW() " +
            "WHERE material_id = #{materialId} AND available_stock >= #{quantity}")
    int decreaseStock(@Param("materialId") Long materialId, @Param("quantity") Integer quantity);

    @Update("UPDATE pack_inventory SET locked_stock = locked_stock + #{quantity}, " +
            "available_stock = available_stock - #{quantity} " +
            "WHERE material_id = #{materialId} AND available_stock >= #{quantity}")
    int lockStock(@Param("materialId") Long materialId, @Param("quantity") Integer quantity);

    @Update("UPDATE pack_inventory SET locked_stock = locked_stock - #{quantity}, " +
            "current_stock = current_stock - #{quantity}, last_out_time = NOW() " +
            "WHERE material_id = #{materialId} AND locked_stock >= #{quantity}")
    int confirmDeduct(@Param("materialId") Long materialId, @Param("quantity") Integer quantity);

    @Update("UPDATE pack_inventory SET locked_stock = locked_stock - #{quantity}, " +
            "available_stock = available_stock + #{quantity} " +
            "WHERE material_id = #{materialId} AND locked_stock >= #{quantity}")
    int unlockStock(@Param("materialId") Long materialId, @Param("quantity") Integer quantity);
}
