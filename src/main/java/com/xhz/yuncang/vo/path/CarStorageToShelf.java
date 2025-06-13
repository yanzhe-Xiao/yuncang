package com.xhz.yuncang.vo.path;

import com.xhz.yuncang.dto.SalesOrderDetailAddDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.awt.*;
import java.util.List;

/**
 * 搬运车从仓库到货架的路径规划视图对象
 * 用于描述搬运车的路径信息、载货信息和目标货架信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CarStorageToShelf {
    /**
     * 搬运车编号
     */
    private String carNumber;
//    private Double maxWeight;
//    private Double SumWeight;
//    private Integer startX;
//    private Integer startY;
//    private Integer middleX;
//    private Integer middleY;
//    private Integer middleZ;
//    private Integer endX;
//    private Integer endY;
//    private String position;//交互点在货架的左边还是右边
//    private String shelfCode;
//    private List<SalesOrderDetailAddDTO> products;
//    private List<Point> paths;
    //新增
    private Integer dispatchTime; // 小车调度开始时间

    /**
     * 最大载重量（单位：kg）
     */
    private Double maxWeight;

    /**
     * 当前总载重量（单位：kg）
     */
    private Double SumWeight;

    /**
     * 起点X坐标
     */
    private Integer startX;

    /**
     * 起点Y坐标
     */
    private Integer startY;

    /**
     * 中间点（货架）X坐标
     */
    private Integer middleX;

    /**
     * 中间点（货架）Y坐标
     */
    private Integer middleY;

    /**
     * 中间点（货架）Z坐标（层数）
     */
    private Integer middleZ;

    /**
     * 终点X坐标
     */
    private Integer endX;

    /**
     * 终点Y坐标
     */
    private Integer endY;

    /**
     * 交互点位置
     */
    private String position;

    /**
     * 目标货架编码
     */
    private String shelfCode;

    /**
     * 搬运的商品列表
     */
    private List<SalesOrderDetailAddDTO> products;

    /**
     * 详细路径点列表
     */
    private List<Point> paths;
}