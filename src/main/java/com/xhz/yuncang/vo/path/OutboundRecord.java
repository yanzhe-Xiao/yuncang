package com.xhz.yuncang.vo.path;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OutboundRecord {
    private String sku;
    private String productName;
    private String shelfCode;
    private Long quantity;
}
