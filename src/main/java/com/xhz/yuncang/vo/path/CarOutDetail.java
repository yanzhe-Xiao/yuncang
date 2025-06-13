package com.xhz.yuncang.vo.path;

import com.xhz.yuncang.dto.SalesOrderDetailAddDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CarOutDetail{
    private String shelfCode;
    private Double locationX;//货架的位置X
    private Double locationY;//货架的位置Y
    private Double locationZ;//货架的位置Z
    private Double middleX;//小车的位置（交互点）X
    private Double middleY;//小车的位置Y
    private Double middleZ;//小车的位置（交互点）Z
    private Double sumWeight;
    private List<SalesOrderDetailAddDTO> productDetails;

    //新增
    private String position;//交互点在货架的左边还是右边
}