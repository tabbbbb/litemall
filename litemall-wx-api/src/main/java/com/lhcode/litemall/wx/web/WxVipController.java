package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.*;
import com.lhcode.litemall.db.service.*;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.lhcode.litemall.wx.annotation.LoginUser;
import com.lhcode.litemall.wx.service.VipLevelService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wx/vip")
@Validated
public class WxVipController {

    private final Log logger = LogFactory.getLog(WxVipController.class);



    @Autowired
    private VipLevelService vipLevelService;
    @Autowired
    private LitemallUserService litemallUserService;


    @GetMapping("vipdata")
    public Object index(@LoginUser Integer userId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        Map<String, Object> result = new HashMap<>();
        LitemallUser user= litemallUserService.findById(userId);
        Map<String,Object> map = vipLevelService.queryVipGrade(user.getVipIndex());
        if(map.size()>0 && map.get("discount")!=null){

            List<LitemallViplevel> viplist = vipLevelService.queryVipList();
            result.put("discount", map.get("discount"));
            result.put("viptype", map.get("viptype"));
            result.put("viplist", viplist);
        }
        String nextclass = vipLevelService.queryNextClass(user.getVipIndex());
        result.put("nextclass", nextclass);
        result.put("vipindex", user.getVipIndex());
        return ResponseUtil.ok(result);
    }



}
