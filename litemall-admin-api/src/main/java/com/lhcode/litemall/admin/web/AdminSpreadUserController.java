package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallAd;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.github.pagehelper.PageInfo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/spread")
@Validated
public class AdminSpreadUserController {

    @Autowired
    private LitemallUserService userService;

    @RequiresPermissions("admin:spread:list")
    @RequiresPermissionsDesc(menu={"推广管理" , "分销用户管理"}, button="查询")
    @GetMapping("/list")
    public Object list(String username, String mobile,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order){
        List<LitemallUser> userList = userService.querySelective(null,username, mobile, page, limit, sort, order);
        long total = PageInfo.of(userList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", userList);

        return ResponseUtil.ok(data);
    }

    @RequiresPermissions("admin:spread:update")
    @RequiresPermissionsDesc(menu={"推广管理" , "分销用户管理"}, button="编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallUser user){
        Object error = validate(user);
        if (error != null) {
            return error;
        }
        if (userService.updateById(user) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        return ResponseUtil.ok(user);
    }

    private Object validate(LitemallUser user) {
        BigDecimal balance = user.getBalance();
        if (StringUtils.isEmpty(balance)) {
            return ResponseUtil.badArgument();
        }
        return null;
    }

}
