package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.admin.util.StatVo;
import com.lhcode.litemall.db.domain.LitemallAdmin;
import com.lhcode.litemall.db.util.AdminRoleFlag;
import io.swagger.models.auth.In;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.service.StatService;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/stat")
@Validated
@ApiIgnore
public class AdminStatController {
    private final Log logger = LogFactory.getLog(AdminStatController.class);

    @Autowired
    private StatService statService;

    @RequiresPermissions("admin:stat:user")
    @RequiresPermissionsDesc(menu={"统计管理" , "用户统计"}, button="查询")
    @GetMapping("/user")
    public Object statUser() {
        List<Map> rows = statService.statUser();
        String[] columns = new String[]{"day", "users"};
        StatVo statVo = new StatVo();
        statVo.setColumns(columns);
        statVo.setRows(rows);
        return ResponseUtil.ok(statVo);
    }

    @RequiresPermissions("admin:stat:order")
    @RequiresPermissionsDesc(menu={"统计管理" , "订单统计"}, button="查询")
    @GetMapping("/order")
    public Object statOrder() {

        List<Map> rows = statService.statOrder(getAdminId());
        String[] columns = new String[]{"day", "orders", "customers", "amount"};
        StatVo statVo = new StatVo();
        statVo.setColumns(columns);
        statVo.setRows(rows);

        return ResponseUtil.ok(statVo);
    }

    @RequiresPermissions("admin:stat:goods")
    @RequiresPermissionsDesc(menu={"统计管理" , "商品统计"}, button="查询")
    @GetMapping("/goods")
    public Object statGoods() {

        List<Map> rows = statService.statGoods(getAdminId());
        String[] columns = new String[]{"day", "orders", "products", "amount"};
        StatVo statVo = new StatVo();
        statVo.setColumns(columns);
        statVo.setRows(rows);
        return ResponseUtil.ok(statVo);
    }

    private Integer getAdminId(){
        Subject currentUser = SecurityUtils.getSubject();
        LitemallAdmin admin = (LitemallAdmin) currentUser.getPrincipal();
        admin.setId(AdminRoleFlag.toAdminId(admin));
        return admin.getId();
    }

}
