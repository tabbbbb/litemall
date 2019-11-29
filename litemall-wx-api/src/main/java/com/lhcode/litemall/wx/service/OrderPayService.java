/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: WXPayService
 * Author:   Administrator
 * Date:     2019/11/22 0022 12:58
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.wx.service;

import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.lhcode.litemall.core.notify.NotifyType;
import com.lhcode.litemall.core.sdk.WXPayConfig;
import com.lhcode.litemall.core.system.SystemConfig;
import com.lhcode.litemall.core.util.DateTimeUtil;
import com.lhcode.litemall.core.util.IpUtil;
import com.lhcode.litemall.core.util.JacksonUtil;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.*;
import com.lhcode.litemall.db.service.*;
import com.lhcode.litemall.db.util.OrderHandleOption;
import com.lhcode.litemall.db.util.OrderUtil;
import io.swagger.models.auth.In;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lhcode.litemall.wx.util.WxResponseCode.AUTH_OPENID_UNACCESS;
import static com.lhcode.litemall.wx.util.WxResponseCode.ORDER_INVALID_OPERATION;
import static com.lhcode.litemall.wx.util.WxResponseCode.ORDER_PAY_FAIL;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/11/22 0022
 * @since 1.0.0
 */
@Service
public class OrderPayService {

    @Autowired
    private WxPayService wxPayService;
    @Autowired
    private LitemallUserFormIdService formIdService;
    @Autowired
    private LitemallOrderService orderService;
    @Autowired
    private LitemallUserService userService;
    @Autowired
    private LitemallOrderGoodsService orderGoodsService;
    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallGoodsSpecificationService goodsSpecificationService;


    private  Double  earnest = SystemConfig.getDownPayment().doubleValue();

    /**
     * 付款订单的预支付会话标识
     * <p>
     * 1. 检测当前订单是否能够付款
     * 2. 微信商户平台返回支付订单ID
     * 3. 设置订单付款状态
     *
     * @param userId 用户ID
     * @param body   订单信息，{ orderId：xxx }
     * @return 支付订单ID
     */
    @Transactional
    public Object prepay(Integer userId, String body, HttpServletRequest request) {
        this.earnest = SystemConfig.getDownPayment().doubleValue();
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgumentValue();
        }
        if (!order.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

        // 检测是否能够取消
        OrderHandleOption handleOption = OrderUtil.build(order);
        if (!handleOption.isPay()) {
            return ResponseUtil.fail(ORDER_INVALID_OPERATION, "订单不能支付");
        }

        LitemallUser user = userService.findById(userId);
        String openid = user.getWeixinOpenid();
        if (openid == null) {
            return ResponseUtil.fail(AUTH_OPENID_UNACCESS, "订单不能支付");
        }
        WxPayMpOrderResult result = null;
        try {
            WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
            orderRequest.setOutTradeNo(order.getOrderSn());
            orderRequest.setOpenid(openid);
            orderRequest.setBody("订单：" + order.getOrderSn());
//            // 元转成分
//            int fee = 0;
//            BigDecimal actualPrice = order.getActualPrice();
//            fee = actualPrice.multiply(new BigDecimal(100)).intValue();
            orderRequest.setTotalFee((int) (earnest*100));
            orderRequest.setSpbillCreateIp(IpUtil.getIpAddr(request));

            result = wxPayService.createOrder(orderRequest);

            //缓存prepayID用于后续模版通知
            String prepayId = result.getPackageValue();
            prepayId = prepayId.replace("prepay_id=", "");
            LitemallUserFormid userFormid = new LitemallUserFormid();
            userFormid.setOpenid(user.getWeixinOpenid());
            userFormid.setFormid(prepayId);
            userFormid.setIsprepay(true);
            userFormid.setUseamount(3);
            userFormid.setExpireTime(LocalDateTime.now().plusDays(7));
            formIdService.addUserFormid(userFormid);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.fail(ORDER_PAY_FAIL, "订单不能支付");
        }

        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return ResponseUtil.updatedDateExpired();
        }
        return ResponseUtil.ok(result);
    }

    /**
     * 微信付款成功或失败回调接口
     * <p>
     * 1. 检测当前订单是否是付款状态;
     * 2. 设置订单付款成功状态相关信息;
     * 3. 响应微信商户平台.
     *
     * @param request  请求内容
     * @param response 响应内容
     * @return 操作结果
     */
    @Transactional
    public synchronized Object payNotify(HttpServletRequest request, HttpServletResponse response) {
        String xmlResult = null;
        try {
            xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
        } catch (IOException e) {
            e.printStackTrace();
            return WxPayNotifyResponse.fail(e.getMessage());
        }
        WxPayOrderNotifyResult result = null;
        try {
            result = wxPayService.parseOrderNotifyResult(xmlResult);
        } catch (WxPayException e) {
            e.printStackTrace();
            return WxPayNotifyResponse.fail(e.getMessage());
        }
        String orderSn = result.getOutTradeNo();
        String payId = result.getTransactionId();

        // 分转化成元
        String totalFee = BaseWxPayResult.fenToYuan(result.getTotalFee());
        LitemallOrder order = orderService.findBySn(orderSn);
        if (order == null) {
            return WxPayNotifyResponse.fail("订单不存在 sn=" + orderSn);
        }
        // 检查这个订单是否已经处理过
        if (OrderUtil.isPayStatus(order) && order.getPayId() != null) {
            return WxPayNotifyResponse.success("订单已经处理成功!");
        }
//        // 检查支付订单金额
//        if (!totalFee.equals(earnest+"")) {
//            return WxPayNotifyResponse.fail(order.getOrderSn() + " : 支付金额不符合 totalFee=" + totalFee);
//        }
        order.setPayId(payId);
        order.setPayTime(LocalDateTime.now());
        order.setOrderStatus(OrderUtil.STATUS_PAY);
        order.setActualPrice(BigDecimal.valueOf(Double.valueOf(totalFee)));
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            // 这里可能存在这样一个问题，用户支付和系统自动取消订单发生在同时
            // 如果数据库首先因为系统自动取消订单而更新了订单状态；
            // 此时用户支付完成回调这里也要更新数据库，而由于乐观锁机制这里的更新会失败
            // 因此，这里会重新读取数据库检查状态是否是订单自动取消，如果是则更新成支付状态。
            order = orderService.findBySn(orderSn);
            int updated = 0;
            if (OrderUtil.isAutoCancelStatus(order)) {
                order.setPayId(payId);
                order.setPayTime(LocalDateTime.now());
                order.setOrderStatus(OrderUtil.STATUS_PAY);
                order.setActualPrice(BigDecimal.valueOf(Integer.valueOf(totalFee)));
                updated = orderService.updateWithOptimisticLocker(order);
            }
            // 如果updated是0，那么数据库更新失败
            if (updated == 0) {
                return WxPayNotifyResponse.fail("更新数据已失效");
            }
        }

        updateGoodsNum(order.getId());
        // 请依据自己的模版消息配置更改参数
        String[] parms = new String[]{
                order.getOrderSn(),
                order.getOrderPrice().toString(),
                DateTimeUtil.getDateTimeDisplayString(order.getAddTime()),
                order.getConsignee(),
                order.getMobile(),
                order.getAddress()
        };

        return WxPayNotifyResponse.success("处理成功!");
    }

    private void updateGoodsNum(Integer id){
        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(id);
        Map<Integer,LitemallGoods> goodsMap = new HashMap<>();
        for (LitemallOrderGoods orderGoods : orderGoodsList) {
            Integer goodsId = orderGoods.getGoodsId();
            if (goodsMap.containsKey(goodsId)){
                LitemallGoods goods = goodsMap.get(goodsId);
                goods.setSales(orderGoods.getNumber()+ goods.getSales());
            }else {
                LitemallGoods goods = goodsService.findById(orderGoods.getGoodsId());
                goods.setSales(orderGoods.getNumber()+ goods.getSales());
                goodsMap.put(goods.getId(),goods);
            }
        }

        for (LitemallGoods goods:goodsMap.values()) {
            if (goodsService.updateById(goods) == 0) throw new RuntimeException();
        }
    }

}