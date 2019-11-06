package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.wx.annotation.LoginUser;
import com.lhcode.litemall.wx.service.WxUserSpreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 我的推广
 */
@RestController
@RequestMapping("/wx/userspread")
@Validated
public class WxUserSpreadController {

    @Autowired
    private WxUserSpreadService wxUserSpreadService;

    /**
     * 查询当前用户我的推广页面下相关数据
     * @param userId
     * @return
     */
    @RequestMapping("/spread")
    public Object spread(@LoginUser Integer userId){
        return wxUserSpreadService.spread(userId);
    }
    /**
     * 查询用户分销人列表
     * @param userId
     * @return
     */
    @GetMapping("/userList")
    public Object userList(@LoginUser Integer userId,@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size){
        return wxUserSpreadService.userList(userId, page, size);
    }

    /**
     * 获取当前用户下的分销订单
     * @param userId
     * @return
     */
    @GetMapping("/orderList")
    public Object orderList(@LoginUser Integer userId,@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer size){
        return wxUserSpreadService.orderList(userId, page, size);
    }

    /**
     * 查询当前用户获得的佣金明细
     * @param userId
     * @return
     */
    @GetMapping("/benefit")
    public Object benefit(@LoginUser Integer userId,@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size){
        return wxUserSpreadService.benefit(userId, page, size);
    }

    /**
     * 查询当前用户提现记录
     * @param userId
     * @return
     */
    @GetMapping("/withdrawal")
    public Object withdrawal(@LoginUser Integer userId,@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size){
        return wxUserSpreadService.withdrawal(userId, page, size);
    }

    /**
     * 查询当前用户可提现金额
     * @param userId
     * @return
     */
    @GetMapping("/currentMoney")
    public Object currentMoney(@LoginUser Integer userId){
        return wxUserSpreadService.currentMoney(userId);
    }

    /**
     * 用户提现申请
     * @param userId
     * @param wechatID
     * @param money
     * @return
     */
    @PostMapping("/userWithDrawal")
    public Object userWithDrawal(@LoginUser Integer userId, @RequestParam("wechatId")String wechatID, @RequestParam("money")String money, HttpServletRequest request){
        return wxUserSpreadService.userWithDrawal(userId,wechatID,money,request);
    }

}
