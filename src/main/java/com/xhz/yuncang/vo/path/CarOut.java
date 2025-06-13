package com.xhz.yuncang.vo.path;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CarOut {
    private String carNumber;
    private Double maxWeight;
    private Double SumWeight;
    private Integer startX;
    private Integer startY;
    private Integer endX;
    private Integer endY;
    private List<CarOutDetail> details;

    //新增路径
    private List<Point> paths;

    //新增
    private Integer dispatchTime; // 小车调度开始时间

}
