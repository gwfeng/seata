package com.example.multipledatasourceseata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.example.multipledatasourceseata.dao.OrderDao;
import com.example.multipledatasourceseata.entiy.OrderDO;
import com.example.multipledatasourceseata.service.AccountService;
import com.example.multipledatasourceseata.service.OrderService;
import com.example.multipledatasourceseata.service.ProductService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 @Description
 *@author kang.li
 *@date 2021/1/11 10:37   
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ProductService productService;

    @Override
    @DS(value = "order-ds") // <1>
    @GlobalTransactional // <2>
    public Integer createOrder(Long userId, Long productId, Integer price) throws Exception {
        Integer amount = 1; // 购买数量，暂时设置为 1。

        log.info("[createOrder] 当前 XID: {}", RootContext.getXID());

        // <3> 扣减库存
        productService.reduceStock(productId, amount);

        // <4> 扣减余额
        accountService.reduceBalance(userId, price);

        // <5> 保存订单
        OrderDO order = new OrderDO();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setPayAmount(amount * price);
        orderDao.saveOrder(order);
        log.info("[createOrder] 保存订单: {}", order.getId());

        // 返回订单编号
        return order.getId();
    }
}
