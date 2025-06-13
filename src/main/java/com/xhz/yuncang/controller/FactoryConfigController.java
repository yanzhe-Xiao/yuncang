package com.xhz.yuncang.controller;

import com.xhz.yuncang.dto.ConfigDTO;
import com.xhz.yuncang.entity.FactoryConfig;
import com.xhz.yuncang.entity.Inventory;
import com.xhz.yuncang.service.FactoryConfigService;
import com.xhz.yuncang.utils.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@PreAuthorize("hasRole('管理员')")
public class FactoryConfigController {

    @Autowired
    private FactoryConfigService factoryConfigService;

    @PutMapping("/factory/config")
    public ResponseEntity<AjaxResult> updateConfig(
            @RequestBody ConfigDTO config
            ){
        System.out.println(config);

        String allow = config.getAllowCollision().equals("true")?"是":"否";
        Double carMaxWeight = config.getCarMaxWeight()==null? 1000.0:Double.parseDouble(config.getCarMaxWeight()) ;
        List<Object> time = config.getTime();
        String weight_ratio=null;
        if (time==null){
            weight_ratio="1/1/2";
        }
        weight_ratio=time.getFirst()+"/"+time.get(1)+"/"+time.getLast();
        String path_strategy=config.getSize()==null?"balanced": config.getSize();
        Integer max_layer =config.getLayer()==null?10: Integer.parseInt(config.getLayer());
        Double maxLayerWeight=config.getLayerMaxWeight()==null?3000.0:Double.parseDouble(config.getLayerMaxWeight());
        Long maxShelfNumber=config.getLayerMaxNumber()==null?540:Long.parseLong(config.getLayerMaxNumber());
        Double maxCarWeight=config.getCarMaxWeight()==null?1000.0:Double.parseDouble(config.getCarMaxWeight());
        Integer inTime=config.getInOrOutTime()==null?2:Integer.parseInt(config.getInOrOutTime());
        Double carSpeed=config.getSpeed()==null?1.0:Double.parseDouble(config.getSpeed());
        FactoryConfig factoryConfig = new FactoryConfig(null, allow, weight_ratio, path_strategy, max_layer, maxLayerWeight, maxShelfNumber,
                maxCarWeight, inTime, carSpeed);
        System.out.println(factoryConfig);
        List<FactoryConfig> list = factoryConfigService.list();
        if (list==null||list.isEmpty()){
            factoryConfigService.save(factoryConfig);
        }else {
            Long id = list.getFirst().getId();
            factoryConfig.setId(id);
            factoryConfigService.updateById(factoryConfig);
        }
        return ResponseEntity.ok(AjaxResult.success());

//return ResponseEntity.ok(AjaxResult.success());
    }


    @GetMapping("/factory/config")
    public ResponseEntity<AjaxResult> getConfig(){
//        FactoryConfig all = factoryConfigService.getAll();
//        if (all==null){
//            return ResponseEntity.internalServerError().body(AjaxResult.error(500,"数据库错误"));
//        }
//
//        return ResponseEntity.ok(AjaxResult.success(all));

        FactoryConfig first = factoryConfigService.list().getFirst();
        String allow=null;
        if (first.getAllowCollision().equals("是")){
            allow="true";
        }else {
            allow="false";
        }
        List<Object> objects = new ArrayList<>();
        String weightRatio = first.getWeightRatio();
        String[] parts = weightRatio.split("/");
        objects.add(parts[0]);
        objects.add(parts[1]);
        objects.add(parts[2]);
        ConfigDTO configDTO = new ConfigDTO(first.getId().toString(), first.getMaxCarWeight().toString(), allow, first.getInAndOutTime().toString(),
                first.getMaxLayer().toString(), first.getMaxLayerWeight().toString(), first.getMaxShelfNumber().toString(), first.getPathStrategy(),
                first.getCarSpeed().toString(), objects);
        return ResponseEntity.ok(AjaxResult.success(configDTO));
    }
}
