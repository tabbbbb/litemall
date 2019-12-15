/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: CancelOrder
 * Author:   Administrator
 * Date:     2019/11/25 0025 13:14
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.wx.util;


import com.lhcode.litemall.wx.service.WxOrderService;
import org.springframework.core.annotation.Order;
import com.lhcode.litemall.db.domain.LitemallGoods;
import com.lhcode.litemall.db.domain.LitemallGoodsSpecification;
import com.lhcode.litemall.db.domain.LitemallOrder;
import com.lhcode.litemall.db.domain.LitemallOrderGoods;
import com.lhcode.litemall.db.service.LitemallGoodsSpecificationService;
import com.lhcode.litemall.db.service.LitemallOrderGoodsService;
import com.lhcode.litemall.db.service.LitemallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/11/25 0025
 * @since 1.0.0
 */
@Component
@Order(value = 1)
public class CancelOrder implements ApplicationRunner {

    @Autowired
    private LitemallOrderService orderService;


    public void addCancel(LitemallOrder o1){
        final Timer timer = new Timer();
        Date date = Date.from(o1.getEndTime().atZone( ZoneId.systemDefault()).toInstant());
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LitemallOrder order = orderService.findById(o1.getId());
                if (order.getOrderStatus() == 101){
                    order.setOrderStatus((short) 103);
                    orderService.updateOrder(order);
                    orderService.cancelOrder(order.getId());
                }
                timer.cancel();
            }
        },date);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<LitemallOrder> orderList = orderService.getOrderStatus((short) 101);
        for (LitemallOrder order : orderList) {
            addCancel(order);
        }
    }
}
