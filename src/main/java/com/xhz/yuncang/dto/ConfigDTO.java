package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigDTO {
    private String id;
    private String carMaxWeight;
    private String allowCollision;
    private String inOrOutTime;
    private String layer;
    private String layerMaxWeight;
    private String layerMaxNumber;
    private String size;
    private String speed;
    private List<Object> time;
}
