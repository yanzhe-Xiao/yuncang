package com.xhz.yuncang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String allowCollision ;
    private String weightRatio ;
    private String pathStrategy;
    private Integer maxLayer;
    private Double maxLayerWeight;
    private Long maxShelfNumber;
    private Double maxCarWeight;
    private Integer inAndOutTime;
    private Double carSpeed;
}
