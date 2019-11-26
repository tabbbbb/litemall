package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.admin.util.AdminResponseCode;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallViplevel;
import com.lhcode.litemall.db.service.LitemallVipClassService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/vipclass")
@Validated
@ApiIgnore
public class AdminVipClassController {
    private final Log logger = LogFactory.getLog(AdminBrandController.class);

    @Autowired
    private LitemallVipClassService vipClassService;

    @RequiresPermissions("admin:vipclass:list")
    @RequiresPermissionsDesc(menu={"商场管理" , "会员等级管理"}, button="查询")
    @GetMapping("/list")
    public Object list(String id,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit) {
        List<LitemallViplevel> brandList = vipClassService.querySelective(id, page, limit);
        long total = PageInfo.of(brandList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", brandList);

        return ResponseUtil.ok(data);
    }


    @RequiresPermissions("admin:vipclass:create")
    @RequiresPermissionsDesc(menu={"商场管理" , "会员等级管理"}, button="添加")
    @PostMapping("/create")
    public Object create(@RequestBody LitemallViplevel brand) {
        if (vipClassService.add(brand) == null){
            return ResponseUtil.fail(AdminResponseCode.VIP_COUNT_EXCEED,"会员等级数量已经达到上限");
        }
        return ResponseUtil.ok(brand);
    }

    @RequiresPermissions("admin:vipclass:read")
    @RequiresPermissionsDesc(menu={"商场管理" , "会员等级管理"}, button="详情")
    @GetMapping("/read")
    public Object read(@NotNull Integer id) {
        LitemallViplevel brand = vipClassService.findById(id);
        return ResponseUtil.ok(brand);
    }

    @RequiresPermissions("admin:vipclass:update")
    @RequiresPermissionsDesc(menu={"商场管理" , "会员等级管理"}, button="编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallViplevel brand) {
        if (vipClassService.updateById(brand) == 0) {
            return ResponseUtil.updatedDataFailed();
        }
        return ResponseUtil.ok(brand);
    }

    @RequiresPermissions("admin:vipclass:delete")
    @RequiresPermissionsDesc(menu={"商场管理" , "会员等级管理"}, button="删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallViplevel brand) {
        Integer id = brand.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        vipClassService.deleteById(id);
        return ResponseUtil.ok();
    }


}
