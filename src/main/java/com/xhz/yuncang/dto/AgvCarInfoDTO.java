package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * AGV搬运车信息修改数据传输对象
 * 用于接收前端修改AGV搬运车信息的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AgvCarInfoDTO {
    /**
     * 搬运车编号（唯一标识，不可修改）
     */
    private String carNumber;

    /**
     * 当前X坐标位置（单位：米）
     */
    private Double locationX;

    /**
     * 当前Y坐标位置（单位：米）
     */
    private Double locationY;

    /**
     * 最大载重量（单位：千克）
     */
    private Double maxWeight;
}