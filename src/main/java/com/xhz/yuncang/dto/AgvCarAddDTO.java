package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * AGV搬运车添加数据传输对象
 * 用于接收前端添加AGV搬运车的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AgvCarAddDTO {
    /**
     * 搬运车编号（唯一标识）
     */
    private String carNumber;

    /**
     * 当前X坐标位置（单位：cm）
     */
    private Double locationX;

    /**
     * 当前Y坐标位置（单位：cm）
     */
    private Double locationY;

    /**
     * 最大载重量（单位：千克）
     */
    private Double maxWeight;
}