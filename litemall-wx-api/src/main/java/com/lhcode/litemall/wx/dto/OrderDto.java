/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: OrderDto
 * Author:   Administrator
 * Date:     2019/11/20 0020 16:56
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.wx.dto;

import com.lhcode.litemall.db.domain.LitemallOrderGoods;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/11/20 0020
 * @since 1.0.0
 */
public class OrderDto {
    private List<LitemallOrderGoods> orderGoodsList;

    private String addressId;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public List<LitemallOrderGoods> getOrderGoodsList() {
        return orderGoodsList;
    }

    public void setOrderGoodsList(List<LitemallOrderGoods> orderGoodsList) {
        this.orderGoodsList = orderGoodsList;
    }
}
