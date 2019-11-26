/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: WxShoppingTrollery
 * Author:   Administrator
 * Date:     2019/11/18 0018 15:46
 * Description: 购物车
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.core.util.JacksonUtil;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallAd;
import com.lhcode.litemall.db.domain.LitemallGoods;
import com.lhcode.litemall.db.domain.LitemallGoodsSpecification;
import com.lhcode.litemall.db.domain.LitemallShoppingTrolley;
import com.lhcode.litemall.db.service.LitemallGoodsService;
import com.lhcode.litemall.db.service.LitemallGoodsSpecificationService;
import com.lhcode.litemall.db.service.LitemallShoppingTrolleyService;
import com.lhcode.litemall.wx.annotation.LoginUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈购物车〉
 *
 * @author Administrator
 * @create 2019/11/18 0018
 * @since 1.0.0
 */
@RestController
@RequestMapping("/wx/trolley")
@Validated
@Api(value = "/wx/trolley",description = "购物车")
public class WxShoppingTrolleyController{

    @Autowired
    private LitemallShoppingTrolleyService shoppingTrolleyService;

    @Autowired
    private LitemallGoodsService goodsService;

    @Autowired
    private LitemallGoodsSpecificationService goodsSpecificationService;

    private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(6);

    private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(3, 6, 1000, TimeUnit.MILLISECONDS, WORK_QUEUE, HANDLER);




    @PostMapping("shoppingtrolley")
    @ApiOperation(value = "添加到购物车",response = ResponseUtil.class,notes = "添加到购物车")
    public Object addShoppingTrolley(@ApiIgnore @LoginUser Integer userId, @ApiParam(name="shoppingTrolley",value = "添加的商品",required = true) @RequestBody LitemallShoppingTrolley shoppingTrolley){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        shoppingTrolley.setUserId(userId);
        shoppingTrolleyService.addShoppingTrolley(shoppingTrolley);
        return ResponseUtil.ok();
    }

    @ApiOperation(value = "删除购物车的商品",response = ResponseUtil.class,notes = "删除购物车的商品")
    @DeleteMapping("shoppingtrolley")
    public Object deleteShoppingTrolley(@ApiIgnore @LoginUser Integer userId, @ApiParam(name="body",value = "{ids:xxx每一个id以逗号隔开}",required = true) @RequestBody String body){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        if (body == null || body.trim().length() == 0){
            return ResponseUtil.badArgument();
        }
        String [] ids = JacksonUtil.parseString(body,"ids").split(",");
        shoppingTrolleyService.deleteShoppingTrolley(ids);
        return ResponseUtil.ok();
    }

    @ApiOperation(value = "修改购物车的商品",response = ResponseUtil.class,notes = "{修改购物车的商品}")
    @PutMapping("shoppingtrolley")
    public Object updateShoppingTrolley(@ApiIgnore @LoginUser Integer userId, @ApiParam(name="shoppingTrolley",value = "购物车对象",required = true) @RequestBody LitemallShoppingTrolley shoppingTrolley){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        shoppingTrolley.setUserId(userId);
        Object response = validate(shoppingTrolley);
        if (response != null){
            return response;
        }
        shoppingTrolleyService.updateShoppingTrolley(shoppingTrolley);
        return ResponseUtil.ok();
    }

    @ApiOperation(value = "查询购物车的商品",response = LitemallShoppingTrolley.class,notes = "查询购物车的商品")
    @GetMapping("shoppingtrolley")
    public Object selectShoppingTrolley(@ApiIgnore @LoginUser Integer userId){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        List<LitemallShoppingTrolley> shoppingTrolleys = shoppingTrolleyService.queryAllByUserId(userId);
        List<Map<String,Object>> vo = new ArrayList<>();
        for (LitemallShoppingTrolley trolley : shoppingTrolleys) {
            Map<String,Object> map = new HashMap<>();
            Callable<LitemallGoods> goodsCallable = () -> goodsService.findById(trolley.getGoodsId());
            Callable<LitemallGoodsSpecification> specCallable = () -> goodsSpecificationService.findById(userId,trolley.getGoodsSpecId());
            FutureTask<LitemallGoods> goodsFutureTask = new FutureTask<>(goodsCallable);
            FutureTask<LitemallGoodsSpecification> goodsSpecificationFutureTask = new FutureTask<>(specCallable);
            executorService.submit(goodsFutureTask);
            executorService.submit(goodsSpecificationFutureTask);
            try {
                map.put("num",trolley.getAddNum());
                map.put("goods",goodsFutureTask.get(3,TimeUnit.SECONDS));
                map.put("goodsSpec",goodsSpecificationFutureTask.get(3,TimeUnit.SECONDS));
                map.put("id",trolley.getId());
                vo.add(map);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        return ResponseUtil.ok(vo);
    }

    private Object validate(LitemallShoppingTrolley shoppingTrolley) {
        Integer number = shoppingTrolley.getAddNum();
        Integer id = shoppingTrolley.getId();
        if (number == null || number < 1){
            return ResponseUtil.badArgument();
        }
        if (id == null){
            return ResponseUtil.badArgument();
        }
        return null;
    }




}