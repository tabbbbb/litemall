package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.wx.annotation.LoginUser;
import com.lhcode.litemall.wx.vo.CategoryVo;
import io.swagger.annotations.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.util.JacksonUtil;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallCollect;
import com.lhcode.litemall.db.domain.LitemallGoods;
import com.lhcode.litemall.db.service.LitemallCollectService;
import com.lhcode.litemall.db.service.LitemallGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏服务
 */
@RestController
@RequestMapping("/wx/collect")
@Validated
@Api(value = "/wx/collect",description = "用户收藏")
public class WxCollectController {
    private final Log logger = LogFactory.getLog(WxCollectController.class);

    @Autowired
    private LitemallCollectService collectService;
    @Autowired
    private LitemallGoodsService goodsService;

    /**
     * 用户收藏列表
     *
     * @param userId 用户ID
     * @param type   类型，如果是0则是商品收藏
     * @param page   分页页数
     * @param size   分页大小
     * @return 用户收藏列表
     */
    @GetMapping("list")
    @ApiOperation(value = "用户收藏列表",response = LitemallCollect.class,notes = "{data:{totalPages:总页数}}",nickname = "用户收藏列表")
    @ResponseHeader(name="X-Litemall-Token",description="用户token")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType="query",name="page",value="页数",dataTypeClass = Integer.class),
            @ApiImplicitParam(paramType="query",name="size",value="每页条数",dataTypeClass = Integer.class),
    })
    public Object list(@ApiIgnore  @LoginUser Integer userId,
                       @ApiIgnore @RequestParam(defaultValue = "0")  Byte type,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        List<LitemallCollect> collectList = collectService.queryByType(userId, type, page, size);
        int count = collectService.countByType(userId, type);
        int totalPages = (int) Math.ceil((double) count / size);

        List<Object> collects = new ArrayList<>(collectList.size());
        for (LitemallCollect collect : collectList) {
            Map<String, Object> c = new HashMap<String, Object>();
            c.put("id", collect.getId());
            c.put("type", collect.getType());
            c.put("valueId", collect.getValueId());

            LitemallGoods goods = goodsService.findById(collect.getValueId(),userId);
            c.put("name", goods.getName());
            c.put("brief", goods.getBrief());
            c.put("picUrl", goods.getPicUrl());
            c.put("price", goods.getPrice());

            collects.add(c);
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("collectList", collects);
        result.put("totalPages", totalPages);
        return ResponseUtil.ok(result);
    }

    /**
     * 用户收藏添加或删除
     * <p>
     * 如果商品没有收藏，则添加收藏；如果商品已经收藏，则删除收藏状态。
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("addordelete")
    @ApiOperation(value = "用户收藏添加或删除",response = LitemallCollect.class,notes = "{data:{handleType:add(添加)|delete(删除)}}",nickname = "用户收藏添加或删除")
    public Object addordelete(@ApiIgnore @LoginUser Integer userId, @ApiParam(name="body",value = "请求内容 示例：{valueId:xxxx}") @RequestBody String body) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        if (StringUtils.isEmpty(body)) {
            return ResponseUtil.badArgument();
        }

        Integer valueId = JacksonUtil.parseInteger(body,"valueId");

        LitemallCollect collect = collectService.queryByTypeAndValue(userId, valueId);

        String handleType = null;
        if (collect != null) {
            handleType = "delete";
            collectService.deleteById(collect.getId());
        } else {
            handleType = "add";
            collect = new LitemallCollect();
            collect.setUserId(userId);
            collect.setValueId(valueId);
            collectService.add(collect);
        }
        return ResponseUtil.ok(handleType);
    }
}