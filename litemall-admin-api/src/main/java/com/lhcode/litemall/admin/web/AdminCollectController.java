package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.admin.util.AdminResponseCode;
import com.lhcode.litemall.db.domain.LitemallGoods;
import com.lhcode.litemall.db.service.LitemallGoodsService;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallCollect;
import com.lhcode.litemall.db.service.LitemallCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/collect")
@Validated
@ApiIgnore
public class AdminCollectController {
    private final Log logger = LogFactory.getLog(AdminCollectController.class);

    @Autowired
    private LitemallCollectService collectService;
    @Autowired
    private LitemallUserService userService;
    @Autowired
    private LitemallGoodsService goodsService;


    private List<Map<String,Object>> toVo( List<LitemallCollect> list){
        List<Map<String,Object>> mapList = new ArrayList<>(list.size());
        for (LitemallCollect collect : list) {
            Map<String,Object> map = new HashMap<>();
            LitemallGoods goods = goodsService.getGoodsById(collect.getValueId());
            map.put("nickName",userService.nickNameByUserId(collect.getUserId()));
            map.put("goodsId",collect.getValueId());
            map.put("goodsName",goods.getName());
            map.put("goodsPic",goods.getPicUrl());
            map.put("isOnSale",goods.getIsOnSale()?"在售中":"已下架");
            map.put("addTime",goods.getAddTime());
            mapList.add(map);
        }
        return mapList;
    }



    @RequiresPermissions("admin:collect:list")
    @RequiresPermissionsDesc(menu={"用户管理" , "用户收藏"}, button="查询")
    @GetMapping("/list")
    public Object list(String userId, String valueId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallCollect> collectList = collectService.querySelective(userId, valueId, page, limit, sort, order);
        long total = PageInfo.of(collectList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", toVo(collectList));

        return ResponseUtil.ok(data);
    }


    @RequiresPermissionsDesc(menu={"用户管理" , "用户收藏"}, button="查询")
    @GetMapping("/order")
    public Object selectCollectByUserId(Integer userId,String sort, String order){
        if (userId == null){
            return ResponseUtil.fail(AdminResponseCode.USER_ID_NULL,"用户ID为空");
        }
        if (!userService.userCount(userId)){
            return ResponseUtil.fail(AdminResponseCode.INVALID_USER_ID,"用户ID不存在");
        }
        return ResponseUtil.ok( collectService.selectCollectByUserId(userId,sort,order));
    }
}
