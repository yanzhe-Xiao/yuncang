package com.xhz.yuncang.vo.path;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 路径点视图对象
 * 用于表示搬运车路径中的一个坐标点及其到达时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Point {
    /**
     * X坐标
     */
    private int x;

    /**
     * Y坐标
     */
    private int y;

    /**
     * 从起点出发到达该点的总耗时（单位：秒）
     */
    private int time;
}