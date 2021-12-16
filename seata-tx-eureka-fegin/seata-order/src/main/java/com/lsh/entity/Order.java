package com.lsh.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：LiuShihao
 * @date ：Created in 2021/3/22 4:06 下午
 * @desc ：
 */
@Data
public class Order {


    private Long id;

    private Long userId;

    private Long productId;

    private Integer count;
    /**
     * BigDecimal 高精度
     */
    private BigDecimal money;

    /**订单状态：0：创建中；1：已完结*/
    private Integer status;

}
