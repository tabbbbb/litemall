package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.db.domain.LitemallAdmin;
import com.lhcode.litemall.db.service.LitemallAdminService;
import com.lhcode.litemall.db.util.AdminRoleFlag;
import com.github.pagehelper.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.service.LitemallUserService;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@Validated
public class AdminUserController {
    private final Log logger = LogFactory.getLog(AdminUserController.class);

    @Autowired
    private LitemallUserService userService;
    @Autowired
    private LitemallAdminService adminService;

    @RequiresPermissions("admin:user:list")
    @RequiresPermissionsDesc(menu={"用户管理" , "会员管理"}, button="查询")
    @GetMapping("/list")
    public Object list(String username, String mobile,Integer adminId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        Subject currentUser = SecurityUtils.getSubject();
        LitemallAdmin admin = (LitemallAdmin) currentUser.getPrincipal();
        if (adminId == null){
            adminId = AdminRoleFlag.toAdminId(admin);
        }
        List<LitemallUser> userList = userService.querySelective(adminId,username, mobile, page, limit, sort, order);
        long total = PageInfo.of(userList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", toVo(userList));
        return ResponseUtil.ok(data);
    }

    private List<Map<String,Object>> toVo(List<LitemallUser> list){
        List<Map<String,Object>> mapList = new ArrayList<>();
        for (LitemallUser user : list) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",user.getId());
            map.put("username",user.getUsername());
            map.put("password",user.getPassword());
            map.put("gender",user.getGender());
            map.put("birthday",user.getBirthday());
            map.put("lastLoginTime",user.getLastLoginTime());
            map.put("lastLoginIp",user.getLastLoginIp());
            map.put("userLevel",user.getUserLevel());
            map.put("nickname",user.getNickname());
            map.put("mobile",user.getMobile());
            map.put("avatar",user.getAvatar());
            map.put("weixinOpenid",user.getWeixinOpenid());
            map.put("status",user.getStatus());
            map.put("addTime",user.getAddTime());
            map.put("updateTime",user.getUpdateTime());
            map.put("deleted",user.getDeleted());
            map.put("level",user.getLevel());
            map.put("balance",user.getBalance());
            map.put("profitPertage",user.getProfitPertage());
            map.put("vipIndex",user.getVipIndex());
            LitemallAdmin admin = adminService.findById(user.getParentId());
            if (admin == null){
                map.put("adminId","未分配");
                map.put("adminName","未分配");
            }else{
                map.put("adminId",admin.getId());
                map.put("adminName",admin.getUsername());
            }
            mapList.add(map);
        }
        return mapList;
    }

    @RequiresPermissions("admin:user:adminIds")
    @PutMapping(value = "/adminIds")
    public Object updateAdminIds( Integer adminId,  String ids){
        String[] idsArray = ids.split(",");
        int result = userService.updateAdminIds(adminId,idsArray);
        if (result == 0){
            return ResponseUtil.fail(147,"分配失败");
        }else{
            return ResponseUtil.ok();
        }
    }
}
