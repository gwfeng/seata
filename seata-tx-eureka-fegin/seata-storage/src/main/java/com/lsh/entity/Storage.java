package com.lsh.entity;

import lombok.Data;

/**
 * @author ：LiuShihao
 * @date ：Created in 2021/3/22 4:13 下午
 * @desc ：
 */
@Data
public class Storage {
    private Long id;

    /**产品id*/
    private Long productId;

    /**总库存*/
    private Integer total;

    /**已用库存*/
    private Integer used;

    /**剩余库存*/
    private Integer residue;
}
