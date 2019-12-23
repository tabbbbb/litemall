package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.admin.service.AdminOrderService;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallAdmin;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.lhcode.litemall.db.util.AdminRoleFlag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/order")
@Validated
@ApiIgnore
public class AdminOrderController {
    private final Log logger = LogFactory.getLog(AdminOrderController.class);

    @Autowired
    private AdminOrderService adminOrderService;
    @Autowired
    private LitemallUserService userService;

    /**
     * 查询订单
     *
     * @param orderSn
     * @param orderStatusArray
     * @param page
     * @param limit
     * @param sort
     * @param order
     * @return
     */
    @RequiresPermissions("admin:order:list")
    @RequiresPermissionsDesc(menu = {"商场管理", "订单管理"}, button = "查询")
    @GetMapping("/list")
    public Object list(String nickname, String orderSn,String mobile,
                       @RequestParam(required = false) List<Short> orderStatusArray,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        Subject currentUser = SecurityUtils.getSubject();
        LitemallAdmin admin = (LitemallAdmin) currentUser.getPrincipal();
        Integer adminId = AdminRoleFlag.toAdminId(admin);
        return adminOrderService.list(adminId,nickname,mobile, orderSn, orderStatusArray, page, limit, sort, order);

    }

    /**
     * 订单详情
     *
     * @param id
     * @return
     */
    @RequiresPermissions("admin:order:read")
    @RequiresPermissionsDesc(menu = {"商场管理", "订单管理"}, button = "详情")
    @GetMapping("/detail")
    public Object detail(@NotNull Integer id) {
        return adminOrderService.detail(id);
    }

    @GetMapping("/detailList")
    public Object detailList(String ids) {
        List list = new ArrayList();
        String [] str = ids.split(",");
        for (String s : str) {
            list.add(adminOrderService.detailList(Integer.valueOf(s)));
        }
        return ResponseUtil.ok(list);
    }



    /**
     * 订单退款
     *
     * @param body 订单信息，{ orderId：xxx }
     * @return 订单退款操作结果
     */
    @RequiresPermissions("admin:order:refund")
    @RequiresPermissionsDesc(menu = {"商场管理", "订单管理"}, button = "订单退款")
    @PostMapping("/refund")
    public Object refund(@RequestBody String body) {
        return adminOrderService.refund(body);
    }

    /**
     * 发货
     *
     * @param body 订单信息，{ orderId：xxx, shipSn: xxx, shipChannel: xxx }
     * @return 订单操作结果
     */
    @RequiresPermissions("admin:order:ship")
    @RequiresPermissionsDesc(menu = {"商场管理", "订单管理"}, button = "订单发货")
    @PostMapping("/ship")
    public Object ship(@RequestBody String body) {
        return adminOrderService.ship(body);
    }


//    /**
//     * 回复订单商品
//     *
//     * @param body 订单信息，{ orderId：xxx }
//     * @return 订单操作结果
//     */
//    @RequiresPermissions("admin:order:reply")
//    @RequiresPermissionsDesc(menu = {"商场管理", "订单管理"}, button = "订单商品回复")
//    @PostMapping("/reply")
//    public Object reply(@RequestBody String body) {
//        return adminOrderService.reply(body);
//    }

    @RequiresPermissions("admin:order:notarize")
    @RequiresPermissionsDesc(menu = {"商场管理", "订单管理"}, button = "订单商品收货")
    @PostMapping("/notarize")
    public Object notarize(@RequestBody String body){
        return adminOrderService.notarize(body);
    }

}
