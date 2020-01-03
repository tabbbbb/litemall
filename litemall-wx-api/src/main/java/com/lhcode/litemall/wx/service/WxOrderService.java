package com.lhcode.litemall.wx.service;

import com.lhcode.litemall.core.system.SystemConfig;
import com.lhcode.litemall.core.util.JacksonUtil;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.util.bcrypt.BCryptPasswordEncoder;
import com.lhcode.litemall.db.domain.*;
import com.lhcode.litemall.db.service.*;
import com.lhcode.litemall.wx.util.CancelOrder;
import io.swagger.models.auth.In;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service

public class WxOrderService {

    @Autowired
    private LitemallOrderService orderService;
    @Autowired
    private LitemallAddressService addressService;
    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallGoodsSpecificationService goodsSpecificationService;

    @Autowired
    private LitemallOrderGoodsService orderGoodsService;

    @Autowired
    private LitemallRegionService regionService;
    @Autowired
    private CancelOrder cancelOrder;

    private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(9);

    private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 9, 1000, TimeUnit.MILLISECONDS, WORK_QUEUE, HANDLER);



    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public Object add(Integer userId,List<LitemallOrderGoods> orderGoodsList,String body,String message){
        try {
            if (userId == null){
                return ResponseUtil.unlogin();
            }
            LocalDateTime time = LocalDateTime.now();
            String addressId = body;
            Object error = this.verify(orderGoodsList,body);
            if ( error != null){
                return error;
            }
            LitemallOrder order = new LitemallOrder();
            List<LitemallRegion> regionList = regionService.getAll();
            LitemallAddress address = null;
            if (addressId == null){
                address = addressService.findDefault(userId);
            }else{
                address = addressService.findById(Integer.valueOf(addressId));
            }
            LitemallAddress finalAddress = address;
            Callable<String> provinceCallable = () -> regionList.stream().filter(region -> region.getId().equals(finalAddress.getProvinceId())).findAny().orElse(null).getName();
            Callable<String> cityCallable = () -> regionList.stream().filter(region -> region.getId().equals(finalAddress.getCityId())).findAny().orElse(null).getName();
            Callable<String> areaCallable = () -> regionList.stream().filter(region -> region.getId().equals(finalAddress.getAreaId())).findAny().orElse(null).getName();
            FutureTask<String> provinceNameCallableTask = new FutureTask<>(provinceCallable);
            FutureTask<String> cityNameCallableTask = new FutureTask<>(cityCallable);
            FutureTask<String> areaNameCallableTask = new FutureTask<>(areaCallable);
            executorService.submit(provinceNameCallableTask);
            executorService.submit(cityNameCallableTask);
            executorService.submit(areaNameCallableTask);
            order.setUserId(userId);
            order.setOrderSn(getMD5( Math.floor(Math.random()*100000) +""+System.currentTimeMillis()));
            order.setAddress(provinceNameCallableTask.get()+" "+cityNameCallableTask.get()+" "+areaNameCallableTask.get()+" "+address.getAddress());
            order.setOrderStatus((short) 1);
            order.setConsignee(address.getName());
            order.setMobile(address.getMobile());
            order.setEndTime(LocalDateTime.now().plusDays(1));
            order.setAddTime(time);
            order.setMessage(message);
            order.setOrderStatus((short) 101);
            if (orderService.add(order) != 1){
                throw new RuntimeException("生成订单失败");
            }
            BigDecimal totalPrice = new BigDecimal(0);
            for (LitemallOrderGoods orderGoods :orderGoodsList) {
                LitemallGoodsSpecification spec = goodsSpecificationService.findById(userId,Integer.valueOf(orderGoods.getSpecifications()));
                LitemallGoods goods = goodsService.findById(spec.getGoodsId(),userId);
                if (!goods.getIsOnSale()){
                    throw new RuntimeException("商品已下架");
                }
                totalPrice = totalPrice.add(spec.getPrice().multiply(BigDecimal.valueOf(orderGoods.getNumber())));
                orderGoods.setOrderId(order.getId());
                orderGoods.setGoodsId(goods.getId());
                orderGoods.setGoodsName(goods.getName());
                orderGoods.setGoodsSn(goods.getGoodsSn());
                orderGoods.setPrice(spec.getPrice());
                orderGoods.setSpecifications(spec.getId()+"");
                orderGoods.setPicUrl(goods.getPicUrl());
                orderGoods.setAddTime(time);
                orderGoods.setSpecName(spec.getSpecification());
            }
            this.updateGoodsNum(orderGoodsList);
            if (orderGoodsService.add(orderGoodsList) != orderGoodsList.size()){
                throw new RuntimeException("生成订单失败");
            }
            BigDecimal freightPrice = SystemConfig.getFreight();
            BigDecimal freightMinPrice = SystemConfig.getFreightLimit();
            if (totalPrice.compareTo(freightMinPrice) == 1){
                freightPrice = new BigDecimal(0);
            }
            order.setGoodsPrice(totalPrice);
            order.setOrderPrice(totalPrice.add(freightPrice));
            order.setFreightPrice(freightPrice);
            if (orderService.updateOrder(order) != 1){
                throw new RuntimeException("生成订单失败");
            }

            Map<String,Object> map = new HashMap<>();
            map.put("order",order);
            map.put("orderGoodsList",orderGoodsList);
            cancelOrder.addCancel(order);
            return map;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object verify(List<LitemallOrderGoods> orderGoods,String addressId){


        if (orderGoods == null || orderGoods.size() == 0){
            return ResponseUtil.fail(444,"请选择商品");
        }
        for (LitemallOrderGoods orderGood : orderGoods) {
            if (orderGood.getGoodsId() == null){
                return ResponseUtil.fail(444,"请选择商品");
            }
            if (orderGood.getSpecifications() == null){
                return ResponseUtil.fail(444,"请选择商品规格");
            }
            if (orderGood.getNumber() == null || orderGood.getNumber()<=0){
                return ResponseUtil.fail(444,"请选择商品数量");
            }
            LitemallGoodsSpecification goodsSpecification = goodsSpecificationService.findById(null, Integer.valueOf(orderGood.getSpecifications()));
            if (goodsSpecification.getNumber() < orderGood.getNumber()){
                return ResponseUtil.fail(444,"库存不足");
            }
        }
        return null;
    }


    public Object getOrder(Integer userId,String orderSn){
        LitemallOrder order = orderService.findBySn(orderSn);
        Object error = this.orderVerify(userId,orderSn,order);
        if (error != null){
            return error;
        }
        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(order.getId());
        Map<String,Object> map = new HashMap<>();
        map.put("order",order);
        map.put("orderGoodsList",orderGoodsList);
        return ResponseUtil.ok(map);
    }


    private Object orderVerify(Integer userId,String orderSn,LitemallOrder order){
        if (order == null){
            return ResponseUtil.fail(9527,"订单未找到");
        }
        if (!order.getUserId().equals(userId)){
            return ResponseUtil.fail(9527,"你不是此订单的用户，立即停止你的行为");
        }
        if (userId == null){
            return ResponseUtil.unlogin();
        }else if (orderSn == null){
            return ResponseUtil.badArgument();
        }
        return null;
    }


    public Object updateOrder(Integer userId,String orderSn){
        LitemallOrder order = orderService.findBySn(orderSn);
        Object error = this.orderVerify(userId,orderSn,order);
        if (error != null){
            return error;
        }
        if (order.getOrderStatus().shortValue() == 101 ){
            order.setOrderStatus((short) 102); //用户取消
            order.setEndTime(LocalDateTime.now());
            orderService.cancelOrder(order.getId());
        }else if (order.getOrderStatus().shortValue() == 201 ){
            order.setOrderStatus((short) 202); //退款审核
        }else if (order.getOrderStatus().shortValue()== 202 ){
            order.setOrderStatus((short) 201); //待发货
        }
        orderService.updateWithOptimisticLocker(order);
        return ResponseUtil.ok();
    }







    public Object getOrderAll(Integer userId,Short orderStatus,Integer page,Integer size){
        if (userId == null){
            return ResponseUtil.unlogin();
        }
        List<Short> status = new ArrayList<>();
        if (orderStatus != null){
            if (orderStatus.shortValue() == 102 || orderStatus.shortValue()== 103){
                status.add((short) 103);
                status.add((short) 102);
            }else{
                status.add(orderStatus);
            }
        }
        List<LitemallOrder> orderList = orderService.getOrderAll(userId,status,page,size);
        List<Map<String,Object>> list = new ArrayList<>();
        for (LitemallOrder order : orderList) {
            List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(order.getId());
            HashMap<String,Object> map = new HashMap<>();
            map.put("order",order);
            map.put("orderGoodsList",orderGoodsList);
            list.add(map);
        }
        return ResponseUtil.ok(list);
    }

    private String getMD5(String value){
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] bs = digest.digest(value.getBytes());
            String hexString = "";
            for (byte b : bs) {
                int temp = b & 255;
                if (temp < 16 && temp >= 0) {
                    // 手动补上一个“0”
                    hexString = hexString + "0" + Integer.toHexString(temp);
                } else {
                    hexString = hexString + Integer.toHexString(temp);
                }
            }
            return hexString;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;// 发生异常返回空
    }



    public Object deleteOrder(Integer userId, String body){
        String orderSn = JacksonUtil.parseString(body,"orderSn");
        LitemallOrder order = orderService.findBySn(orderSn);
        if (order.getUserId().equals(userId)){
            if (order.getOrderStatus() == 102 || order.getOrderStatus() == 103 || order.getOrderStatus() == 401){
                orderService.deleteById(order.getId());
                return ResponseUtil.ok();
            }else{
                return ResponseUtil.fail(323,"订单未完成，不能删除");
            }
        }else{
            return ResponseUtil.fail(567,"不能删除不是自己的订单");
        }

    }

    private void updateGoodsNum(List<LitemallOrderGoods> orderGoodsList){
        for (LitemallOrderGoods orderGoods : orderGoodsList) {
            LitemallGoodsSpecification goodsSpecification = goodsSpecificationService.findById(null, Integer.valueOf(orderGoods.getSpecifications()));
            goodsSpecification.setNumber(goodsSpecification.getNumber()-orderGoods.getNumber());
            if (goodsSpecificationService.update(goodsSpecification) == 0) throw new RuntimeException();
        }
    }





}