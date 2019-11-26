package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallSpreadclass;
import com.lhcode.litemall.db.service.LitemallSpreadClassService;
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
@RequestMapping("/admin/spreadClass")
@Validated
@ApiIgnore
public class AdminSpreadClassController {
    private final Log logger = LogFactory.getLog(AdminBrandController.class);

    @Autowired
    private LitemallSpreadClassService spreadClassService;
//
//    @RequiresPermissions("admin:spreadClass:list")
//    @RequiresPermissionsDesc(menu={"推广管理" , "分销等级管理"}, button="查询")
//    @GetMapping("/list")
//    public Object list(String id,
//                       @RequestParam(defaultValue = "1") Integer page,
//                       @RequestParam(defaultValue = "10") Integer limit) {
//        List<LitemallSpreadclass> brandList = spreadClassService.querySelective(id, page, limit);
//        long total = PageInfo.of(brandList).getTotal();
//        Map<String, Object> data = new HashMap<>();
//        data.put("total", total);
//        data.put("items", brandList);
//
//        return ResponseUtil.ok(data);
//    }
//
//
//    @RequiresPermissions("admin:spreadClass:create")
//    @RequiresPermissionsDesc(menu={"推广管理" , "分销等级管理"}, button="添加")
//    @PostMapping("/create")
//    public Object create(@RequestBody LitemallSpreadclass brand) {
//        spreadClassService.add(brand);
//        return ResponseUtil.ok(brand);
//    }
//
//    @RequiresPermissions("admin:spreadClass:read")
//    @RequiresPermissionsDesc(menu={"推广管理" , "分销等级管理"}, button="详情")
//    @GetMapping("/read")
//    public Object read(@NotNull Integer id) {
//        LitemallSpreadclass brand = spreadClassService.findById(id);
//        return ResponseUtil.ok(brand);
//    }
//
//    @RequiresPermissions("admin:spreadClass:update")
//    @RequiresPermissionsDesc(menu={"推广管理" , "分销等级管理"}, button="编辑")
//    @PostMapping("/update")
//    public Object update(@RequestBody LitemallSpreadclass brand) {
//        if (spreadClassService.updateById(brand) == 0) {
//            return ResponseUtil.updatedDataFailed();
//        }
//        return ResponseUtil.ok(brand);
//    }
//
//    @RequiresPermissions("admin:spreadClass:delete")
//    @RequiresPermissionsDesc(menu={"推广管理" , "分销等级管理"}, button="删除")
//    @PostMapping("/delete")
//    public Object delete(@RequestBody LitemallSpreadclass brand) {
//        Integer id = brand.getId();
//        if (id == null) {
//            return ResponseUtil.badArgument();
//        }
//        spreadClassService.deleteById(id);
//        return ResponseUtil.ok();
//    }


}
