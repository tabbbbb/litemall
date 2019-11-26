package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.db.domain.LitemallFootprint;
import com.lhcode.litemall.db.domain.LitemallOrder;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.lhcode.litemall.wx.annotation.LoginUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.service.LitemallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务
 */
@RestController
@RequestMapping("/wx/user")
@Validated
@Api(value = "/wx/user",description = "用户服务")
public class WxUserController {
    private final Log logger = LogFactory.getLog(WxUserController.class);

    @Resource
    private LitemallOrderService orderService;
    @Resource
    private LitemallUserService userService;

    @GetMapping("userInfo")
    @ApiOperation(value = "用户页信息",response = LitemallFootprint.class,notes = "loginFlag:是否登录,nickName:用户昵称,avatar:头像,noPay:未付款数量,noSend:待发货数量,transport:待收货数量",nickname = "用户页信息")
    public Object userInfo(@ApiIgnore @LoginUser Integer userId){
        Map<String,Object> map = new HashMap<>();
        if (userId == null){
            map.put("loginFlag",false);
        }else{
            map.put("loginFlag",true);
            LitemallUser user = userService.findById(userId);
            Integer noPay = orderService.countByOrderStatus(userId, (short) 1);
            Integer noSend = orderService.countByOrderStatus(userId, (short) 2);
            Integer transport = orderService.countByOrderStatus(userId, (short) 3);
            map.put("user",user);
            map.put("noPay",noPay);
            map.put("noSend",noSend);
            map.put("transport",transport);
        }

        return map;
    }

}