package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.core.sdk.WXPay;
import com.lhcode.litemall.core.system.SystemConfig;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallOrder;
import com.lhcode.litemall.db.domain.LitemallOrderGoods;
import com.lhcode.litemall.wx.annotation.LoginUser;
import com.lhcode.litemall.wx.dto.OrderDto;
import com.lhcode.litemall.wx.service.OrderPayService;
import com.lhcode.litemall.wx.service.WxOrderService;
import io.swagger.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wx/order")
@Validated
@Api(value = "WxOrderController",description = "订单")
public class WxOrderController {
    private final Log logger = LogFactory.getLog(WxOrderController.class);

    @Autowired
    private WxOrderService wxOrderService;

    @Autowired
    private OrderPayService payService;


    @ApiOperation(value = "生成订单" ,response=LitemallOrder.class,notes = "",nickname = "生成订单")
    @PutMapping("order")
    public Object createOrder(@ApiIgnore @LoginUser Integer userId , @ApiParam(name="orderDto",value = "订单的商品信息和规格,选择的地址id ps：可以只传id")@RequestBody OrderDto orderDto){
        return  wxOrderService.add(userId,orderDto.getOrderGoodsList(),orderDto.getAddressId(),orderDto.getMessage());
    }




    @GetMapping("order")
    @ApiOperation(value="根据goodsSn返回order",response=LitemallOrderGoods.class,notes = "  101: '未付款(待付款)',\n" +
            "  102: '用户取消'(已取消),\n" +
            "  103: '系统取消'(已取消),\n" +
            "  201: '已付款'(待发货),\n" +
            "  202: '申请退款'(退款中),\n" +
            "  203: '已退款'(退款成功),\n" +
            "  301: '已发货'(待收货),\n" +
            "  401: '用户收货(订单完成)'",nickname = "根据goodsSn返回order")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType="query",name="orderSn",value="订单编号",dataTypeClass = String.class)
    })
    public Object getOrder(@ApiIgnore @LoginUser Integer userId,String orderSn){
        return wxOrderService.getOrder(userId,orderSn);
    }

    @ApiOperation(value = "根据条件返回订单信息",response = LitemallOrder.class,notes="order:LitemallOrder,orderGoodsList:List<LitemallOrderGoods>\n \"  101: '未付款(待付款)',\\n\" +\n" +
            "            \"  102: '用户取消'(已取消),\\n\" +\n" +
            "            \"  103: '系统取消'(已取消),\\n\" +\n" +
            "            \"  201: '已付款'(待发货),\\n\" +\n" +
            "            \"  202: '申请退款'(退款中),\\n\" +\n" +
            "            \"  203: '已退款'(退款成功),\\n\" +\n" +
            "            \"  301: '已发货'(待收货),\\n\" +\n" +
            "            \"  401: '用户收货(订单完成)'\",")
    @GetMapping("orderall")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType="query",name="orderStatus",value="订单状态",dataTypeClass = Short.class)
    })
    public Object getOrderAll(Short orderStatus,
                              @ApiIgnore @LoginUser Integer userId,
                              @RequestParam(defaultValue = "1")Integer page,
                              @RequestParam(defaultValue = "10")Integer size){
        return wxOrderService.getOrderAll(userId,orderStatus,page,size);
    }


    @PostMapping("order")
    @ApiOperation(value = "取消订单或是退款",response = ResponseUtil.class,notes="")
    public Object updateOrder(@ApiIgnore @LoginUser Integer userId,@ApiParam(name = "order",value = "订单，ps：可以只填orderSn，但必须是对象的格式")@RequestBody LitemallOrder order){
        return wxOrderService.updateOrder(userId,order.getOrderSn());
    }


    @PostMapping ("prepay")
    @ApiOperation(value = "拉起微信付款",response = ResponseUtil.class,notes="")
    public Object prepay(@ApiIgnore @LoginUser Integer userId, @RequestBody @ApiParam(name = "body",value = "示例orderId:xxx") String body,@ApiIgnore HttpServletRequest request){
        return payService.prepay(userId,body,request);
    }

    @PostMapping ("pay-notify")
    public Object payNotify(HttpServletRequest request, HttpServletResponse response) {
        return payService.payNotify(request,response);
    }

    @DeleteMapping("order")
    @ApiOperation(value = "删除订单",response = ResponseUtil.class,notes="")
    public Object deleteOrder(@ApiIgnore @LoginUser Integer userId, @RequestBody @ApiParam(name = "body",value = "示例orderSn:xxx") String body) {
        return wxOrderService.deleteOrder(userId,body);
    }


}