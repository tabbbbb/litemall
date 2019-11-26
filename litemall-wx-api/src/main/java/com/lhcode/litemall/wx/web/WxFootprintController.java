package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.db.domain.LitemallCollect;
import com.lhcode.litemall.wx.annotation.LoginUser;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.util.JacksonUtil;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallFootprint;
import com.lhcode.litemall.db.domain.LitemallGoods;
import com.lhcode.litemall.db.service.LitemallFootprintService;
import com.lhcode.litemall.db.service.LitemallGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户访问足迹服务
 */
@RestController
@RequestMapping("/wx/footprint")
@Validated
@Api(value = "/wx/footprint",description = "用户足迹")
public class WxFootprintController {
    private final Log logger = LogFactory.getLog(WxFootprintController.class);

    @Autowired
    private LitemallFootprintService footprintService;
    @Autowired
    private LitemallGoodsService goodsService;

    /**
     * 删除用户足迹
     *
     * @param userId 用户ID
     * @param body   请求内容， { id: xxx }
     * @return 删除操作结果
     */
    @PostMapping("delete")
    @ApiOperation(value = "删除用户足迹",response = LitemallFootprint.class,notes = "",nickname = "删除用户足迹")
    public Object delete(@ApiIgnore @LoginUser Integer userId, @ApiParam(name = "body",value = "请求内容 示例：{id:xxx}") @RequestBody String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        if (StringUtils.isEmpty(body)) {
            return ResponseUtil.badArgument();
        }

        Integer footprintId = JacksonUtil.parseInteger(body, "id");
        if (footprintId == null) {
            return ResponseUtil.badArgument();
        }
        LitemallFootprint footprint = footprintService.findById(footprintId);

        if (footprint == null) {
            return ResponseUtil.badArgumentValue();
        }
        if (!footprint.getUserId().equals(userId)) {
            return ResponseUtil.badArgumentValue();
        }

        footprintService.deleteById(footprintId);
        return ResponseUtil.ok();
    }

    /**
     * 用户足迹列表
     *
     * @param page 分页页数
     * @param size 分页大小
     * @return 用户足迹列表
     */
    @GetMapping("list")
    @ApiOperation(value = "用户足迹列表",response = LitemallFootprint.class,notes = "{data:{footprintList:[LitemallFootprint],totalPages:页数}}",nickname = "用户足迹列表")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType="query",name="page",value="页码",dataTypeClass = Integer.class,defaultValue = "1"),
            @ApiImplicitParam(paramType="query",name="size",value="本页条数",dataTypeClass = Integer.class,defaultValue = "10"),
    })
    public Object list(@ApiIgnore @LoginUser Integer userId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        List<LitemallFootprint> footprintList = footprintService.queryByAddTime(userId, page, size);
        long count = PageInfo.of(footprintList).getTotal();
        int totalPages = (int) Math.ceil((double) count / size);

        List<Object> footprintVoList = new ArrayList<>(footprintList.size());
        for (LitemallFootprint footprint : footprintList) {
            Map<String, Object> c = new HashMap<String, Object>();
            c.put("id", footprint.getId());
            c.put("goodsId", footprint.getGoodsId());
            c.put("addTime", footprint.getAddTime());

            LitemallGoods goods = goodsService.findById(footprint.getGoodsId(),userId);
            c.put("name", goods.getName());
            c.put("brief", goods.getBrief());
            c.put("picUrl", goods.getPicUrl());
            c.put("price", goods.getPrice());

            footprintVoList.add(c);
        }


        Map<String, Object> result = new HashMap<>();
        result.put("footprintList", footprintVoList);
        result.put("totalPages", totalPages);
        return ResponseUtil.ok(result);
    }

}