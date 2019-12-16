package com.lhcode.litemall.admin.service;

import com.lhcode.litemall.db.domain.*;
import com.lhcode.litemall.db.service.*;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.notify.NotifyService;
import com.lhcode.litemall.core.notify.NotifyType;
import com.lhcode.litemall.core.util.JacksonUtil;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lhcode.litemall.admin.util.AdminResponseCode.*;

@Service

public class AdminOrderService {
    private final Log logger = LogFactory.getLog(AdminOrderService.class);

    @Autowired
    private LitemallOrderGoodsService orderGoodsService;
    @Autowired
    private LitemallOrderService orderService;
    @Autowired
    private LitemallUserService userService;
    @Autowired
    private LitemallCommentService commentService;
    @Autowired
    private WxPayService wxPayService;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallGoodsSpecificationService goodsSpecificationService;
    @Autowired
    private LogHelper logHelper;

    public Object list(Integer adminId,String nickname, String orderSn, List<Short> orderStatusArray,
                       Integer page, Integer limit, String sort, String order) {
        List<LitemallOrder> orderList = orderService.querySelective(adminId,nickname, orderSn, orderStatusArray, page, limit, sort, order);
        long total = PageInfo.of(orderList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", orderList);

        return ResponseUtil.ok(data);
    }

    public Object detail(Integer id) {
        LitemallOrder order = orderService.findById(id);
        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(id);
        List list = new ArrayList();
        UserVo user = userService.findUserVoById(order.getUserId());
        for (LitemallOrderGoods orderGoods : orderGoodsList) {
            LitemallGoods goods = goodsService.findById(orderGoods.getGoodsId(),user.getId());
            LitemallGoodsSpecification goodsSpecification = goodsSpecificationService.findById(user.getId(), Integer.valueOf(orderGoods.getSpecifications()));
            Map map = new HashMap();
            map.put("goodsSn",goods.getGoodsSn());
            map.put("goodsName",goods.getName());
            map.put("specName",goodsSpecification.getSpecification());
            map.put("number",orderGoods.getNumber());
            map.put("pic",goods.getPicUrl());
            map.put("price",goodsSpecification.getPrice());
            list.add(map);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("orderGoods", list);
        data.put("user", user);

        return ResponseUtil.ok(data);
    }


    public Object detailList(Integer id) {
        LitemallOrder order = orderService.findById(id);
        List<LitemallOrderGoods> orderGoods = orderGoodsService.queryByOid(id);
        UserVo user = userService.findUserVoById(order.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("orderGoods", orderGoods);
        data.put("user", user);

        return data;
    }

    /**
     * 订单退款
     * <p>
     * 1. 检测当前订单是否能够退款;
     * 2. 微信退款操作;
     * 3. 设置订单退款确认状态；
     * 4. 订单商品库存回库。
     * <p>
     * TODO
     * 虽然接入了微信退款API，但是从安全角度考虑，建议开发者删除这里微信退款代码，采用以下两步走步骤：
     * 1. 管理员登录微信官方支付平台点击退款操作进行退款
     * 2. 管理员登录litemall管理后台点击退款操作进行订单状态修改和商品库存回库
     *
     * @param body 订单信息，{ orderId：xxx }
     * @return 订单退款操作结果
     */
    @Transactional
    public Object refund(String body) {
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        String refundMoney = JacksonUtil.parseString(body, "refundMoney");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }
        if (StringUtils.isEmpty(refundMoney)) {
            return ResponseUtil.badArgument();
        }

        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }

        if (order.getActualPrice().compareTo(new BigDecimal(refundMoney)) != 0) {
            return ResponseUtil.badArgumentValue();
        }

        // 如果订单不是退款状态，则不能退款
        if (!order.getOrderStatus().equals(OrderUtil.STATUS_REFUND)) {
            return ResponseUtil.fail(ORDER_CONFIRM_NOT_ALLOWED, "订单不能退款");
        }

//        // 微信退款
//        WxPayRefundRequest wxPayRefundRequest = new WxPayRefundRequest();
//        wxPayRefundRequest.setOutTradeNo(order.getOrderSn());
//        wxPayRefundRequest.setOutRefundNo("refund_" + order.getOrderSn());
//        // 元转成分
//        Integer totalFee = order.getActualPrice().multiply(new BigDecimal(100)).intValue();
//        wxPayRefundRequest.setTotalFee(totalFee);
//        wxPayRefundRequest.setRefundFee(totalFee);
//
//        WxPayRefundResult wxPayRefundResult = null;
//        try {
//            wxPayRefundResult = wxPayService.refund(wxPayRefundRequest);
//        } catch (WxPayException e) {
//            e.printStackTrace();
//            return ResponseUtil.fail(ORDER_REFUND_FAILED, "订单退款失败");
//        }
//        if (!wxPayRefundResult.getReturnCode().equals("SUCCESS")) {
//            logger.warn("refund fail: " + wxPayRefundResult.getReturnMsg());
//            return ResponseUtil.fail(ORDER_REFUND_FAILED, "订单退款失败");
//        }

        // 设置订单取消状态
        order.setOrderStatus(OrderUtil.STATUS_REFUND_CONFIRM);
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            throw new RuntimeException("更新数据已失效");
        }

        orderService.cancelOrder(order.getId());


        //TODO 发送邮件和短信通知，这里采用异步发送
        // 退款成功通知用户, 例如“您申请的订单退款 [ 单号:{1} ] 已成功，请耐心等待到账。”
        // 注意订单号只发后6位
        notifyService.notifySmsTemplate(order.getMobile(), NotifyType.REFUND, new String[]{order.getOrderSn().substring(8, 14)});

        logHelper.logOrderSucceed("退款", "订单编号 " + orderId);
        return ResponseUtil.ok();
    }

    /**
     * 发货
     * 1. 检测当前订单是否能够发货
     * 2. 设置订单发货状态
     *
     * @param body 订单信息，{ orderId：xxx, shipSn: xxx, shipChannel: xxx }
     * @return 订单操作结果
     * 成功则 { errno: 0, errmsg: '成功' }
     * 失败则 { errno: XXX, errmsg: XXX }
     */
    public Object ship(String body) {
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }

        // 如果订单不是已付款状态，则不能发货
        if (!order.getOrderStatus().equals(OrderUtil.STATUS_PAY)) {
            return ResponseUtil.fail(ORDER_CONFIRM_NOT_ALLOWED, "订单不能发货");
        }

        order.setOrderStatus(OrderUtil.STATUS_SHIP);
        order.setShipTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return ResponseUtil.updatedDateExpired();
        }

        //TODO 发送邮件和短信通知，这里采用异步发送
        // 发货会发送通知短信给用户:          *
        // "您的订单已经发货，快递公司 {1}，快递单 {2} ，请注意查收"
//        notifyService.notifySmsTemplate(order.getMobile(), NotifyType.SHIP, new String[]{"自配送", "无"});
//
//        logHelper.logOrderSucceed("发货", "订单编号 " + orderId);
        return ResponseUtil.ok();
    }


    /**
     * 回复订单商品
     *
     * @param body 订单信息，{ orderId：xxx }
     * @return 订单操作结果
     * 成功则 { errno: 0, errmsg: '成功' }
     * 失败则 { errno: XXX, errmsg: XXX }
     */
    public Object reply(String body) {
        Integer commentId = JacksonUtil.parseInteger(body, "commentId");
        if (commentId == null || commentId == 0) {
            return ResponseUtil.badArgument();
        }
        LitemallComment litemallComment = commentService.findById(commentId);
        // 目前只支持回复一次
        if ( litemallComment != null && !StringUtils.isEmpty(litemallComment.getReply())) {
            return ResponseUtil.fail(ORDER_REPLY_EXIST, "订单商品已回复！");
        }
        String reply = JacksonUtil.parseString(body, "content");
        if (StringUtils.isEmpty(reply)) {
            return ResponseUtil.badArgument();
        }
        // 创建评价回复

        litemallComment.setReply(reply);
        commentService.updateById(litemallComment);
        return ResponseUtil.ok();
    }


    public Object notarize(String body){
        Integer orderId = JacksonUtil.parseInteger(body,"orderId");
        Double notarizePrice = JacksonUtil.parseObject(body,"notarizeMoney",Double.class);
        LitemallOrder order = orderService.findById(orderId);
        if (order.getOrderStatus() != 301){
            return ResponseUtil.fail();
        }
        order.setOrderStatus((short) 401);
        order.setActualPrice(new BigDecimal(notarizePrice));
        order.setConfirmTime(LocalDateTime.now());
        orderService.updateOrder(order);
        return ResponseUtil.ok();
    }

}
