package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.Inventory;
import com.xhz.yuncang.mapper.InventoryMapper;
import com.xhz.yuncang.service.InventoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
        implements InventoryService {


    @Override
    public Inventory getBySku(String sku) {
        return lambdaQuery()
                .eq(Inventory::getSku, sku)
                .one();
    }

    @Override
    public boolean deleteBySku(String sku) {
        return lambdaUpdate()
                .eq(Inventory::getSku, sku)
                .remove();
    }

    @Override
    public boolean increaseInventory(String sku, Long newQuantity) {
        return lambdaUpdate()
                .eq(Inventory::getSku, sku)
                .setSql("quantity = quantity + " + newQuantity)
                .update();
    }

    @Override
    public boolean decreaseInventory(String sku, Long newQuantity) {
        if(lambdaQuery().eq(Inventory::getSku,sku).one().getQuantity()>=newQuantity){
            return lambdaUpdate()
                    .eq(Inventory::getSku, sku)
                    .setSql("quantity = quantity - " + newQuantity)
                    .update();
        }
        return false;
    }


    @Override
    public List<Inventory> listBySkus(List<String> skuList) {
        return lambdaQuery()
                .in(Inventory::getSku, skuList)
                .list();
    }

    @Override
    public List<Inventory> listAll(){
        return lambdaQuery()
                .list();
    }

    @Override
    public Boolean addOneInventory(Inventory inventory) {
        return save(inventory);
    }
}
