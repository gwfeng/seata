package com.example.multipledatasourceseata.entiy;

import lombok.Data;

/*
 @Description
 *@author kang.li
 *@date 2021/1/11 10:41   
 */
@Data
public class OrderDO {
    /** 订单编号 **/
    private Integer id;

    /** 用户编号 **/
    private Long userId;

    /** 产品编号 **/
    private Long productId;

    /** 支付金额 **/
    private Integer payAmount;
}
