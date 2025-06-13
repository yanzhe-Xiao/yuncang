package com.xhz.yuncang.vo.path;

import com.xhz.yuncang.dto.SalesOrderDetailAddDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShelfPickTask {
    private String shelfCode;
    private Double locationX;
    private Double locationY;
    private Double locationZ;
    private List<SalesOrderDetailAddDTO> items;
    private Double totalWeight;
}
