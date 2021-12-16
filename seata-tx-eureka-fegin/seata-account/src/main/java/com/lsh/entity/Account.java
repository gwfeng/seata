package com.lsh.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：LiuShihao
 * @date ：Created in 2021/3/22 4:09 下午
 * @desc ：
 */
@Data
public class Account {
    private Long id;

    /**用户id*/
    private Long userId;

    /**总额度*/
    private BigDecimal total;

    /**已用额度*/
    private BigDecimal used;

    /**剩余额度*/
    private BigDecimal residue;
}
