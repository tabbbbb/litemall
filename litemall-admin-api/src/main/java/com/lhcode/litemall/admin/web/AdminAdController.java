package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.github.pagehelper.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallAd;
import com.lhcode.litemall.db.service.LitemallAdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/ad")
@Validated
@ApiIgnore
public class AdminAdController {
    private final Log logger = LogFactory.getLog(AdminAdController.class);

    @Autowired
    private LitemallAdService adService;

    @RequiresPermissions("admin:ad:list")
    @RequiresPermissionsDesc(menu={"页面管理" , "轮播图与页面管理"}, button="查询")
    @GetMapping("/list")
    public Object list(Byte position, Integer enabled,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallAd> adList = adService.querySelective(position, enabled, page, limit, sort, order);
        for (LitemallAd litemallAd : adList) {
            litemallAd.setUrlList();
            litemallAd.setLinkList();
        }
        long total = PageInfo.of(adList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", adList);

        return ResponseUtil.ok(data);
    }

    private Object validate(LitemallAd ad) {
        String name = ad.getName();
        if (StringUtils.isEmpty(name) && ad.getPosition() != 1) {
            return ResponseUtil.badArgument();
        }
        if (StringUtils.isEmpty(ad.getUrl()) && ad.getPosition() == 1){
            return ResponseUtil.badArgument();
        }
        return null;
    }

    @RequiresPermissions("admin:ad:create")
    @RequiresPermissionsDesc(menu={"页面管理" , "轮播图与页面管理"}, button="添加")
    @PostMapping("/create")
    public Object create(@RequestBody LitemallAd ad) {
        Object error = validate(ad);
        if (error != null) {
            return error;
        }
        adService.add(ad);
        return ResponseUtil.ok(ad);
    }

    @RequiresPermissions("admin:ad:read")
    @RequiresPermissionsDesc(menu={"页面管理" , "轮播图与页面管理"}, button="详情")
    @GetMapping("/read")
    public Object read(@NotNull Integer id) {
        LitemallAd brand = adService.findById(id);
        return ResponseUtil.ok(brand);
    }

    @RequiresPermissions("admin:ad:update")
    @RequiresPermissionsDesc(menu={"页面管理" , "轮播图与页面管理"}, button="编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallAd ad) {
        Object error = validate(ad);
        if (error != null) {
            return error;
        }
        if (adService.updateById(ad) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        return ResponseUtil.ok(ad);
    }

    @RequiresPermissions("admin:ad:delete")
    @RequiresPermissionsDesc(menu={"页面管理" , "轮播图与页面管理"}, button="删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallAd ad) {
        Integer id = ad.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        adService.deleteById(id);
        return ResponseUtil.ok();
    }

}
