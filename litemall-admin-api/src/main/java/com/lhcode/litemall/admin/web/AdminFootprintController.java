package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.admin.util.AdminResponseCode;
import com.lhcode.litemall.db.domain.LitemallCollect;
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
import com.lhcode.litemall.db.domain.LitemallFootprint;
import com.lhcode.litemall.db.service.LitemallFootprintService;
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
@RequestMapping("/admin/footprint")
@Validated
@ApiIgnore
public class AdminFootprintController {
    private final Log logger = LogFactory.getLog(AdminFootprintController.class);

    @Autowired
    private LitemallFootprintService footprintService;
    @Autowired
    private LitemallUserService userService;
    @Autowired
    private LitemallGoodsService goodsService;

    @RequiresPermissions("admin:footprint:list")
    @RequiresPermissionsDesc(menu={"用户管理" , "用户足迹"}, button="查询")
    @GetMapping("/list")
    public Object list(String userId, String goodsId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallFootprint> footprintList = footprintService.querySelective(userId, goodsId, page, limit, sort, order);
        long total = PageInfo.of(footprintList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", toVo(footprintList));

        return ResponseUtil.ok(data);
    }


    private List<Map<String,Object>> toVo( List<LitemallFootprint> list){
        List<Map<String,Object>> mapList = new ArrayList<>(list.size());
        for (LitemallFootprint footprint : list) {
            Map<String,Object> map = new HashMap<>();
            LitemallGoods goods = goodsService.getGoodsById(footprint.getGoodsId());
            map.put("nickName",userService.nickNameByUserId(footprint.getUserId()));
            map.put("goodsId",footprint.getGoodsId());
            map.put("goodsName",goods.getName());
            map.put("goodsPic",goods.getPicUrl());
            map.put("isOnSale",goods.getIsOnSale()?"在售中":"已下架");
            map.put("addTime",goods.getAddTime());
            mapList.add(map);
        }
        return mapList;
    }

    @RequiresPermissionsDesc(menu={"用户管理" , "用户足迹"}, button="查询")
    @GetMapping("/footprint")
    public Object selectFootprintByUserId(Integer userId,String sort, String order){
        if (userId == null){
            return ResponseUtil.fail(AdminResponseCode.USER_ID_NULL,"用户ID为空");
        }
        if (!userService.userCount(userId)){
            return ResponseUtil.fail(AdminResponseCode.INVALID_USER_ID,"用户ID不存在");
        }

        return ResponseUtil.ok(footprintService.selectFootprintByUserId(userId,sort,order));
    }

}
