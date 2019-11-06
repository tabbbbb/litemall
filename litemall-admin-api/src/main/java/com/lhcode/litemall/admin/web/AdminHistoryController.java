package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.admin.util.AdminResponseCode;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallSearchHistory;
import com.lhcode.litemall.db.service.LitemallSearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/history")
public class AdminHistoryController {
    private final Log logger = LogFactory.getLog(AdminHistoryController.class);

    @Autowired
    private LitemallSearchHistoryService searchHistoryService;
    @Autowired
    private LitemallUserService userService;

    @RequiresPermissions("admin:history:list")
    @RequiresPermissionsDesc(menu={"用户管理" , "搜索历史"}, button="查询")
    @GetMapping("/list")
    public Object list(String userId, String keyword,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallSearchHistory> footprintList = searchHistoryService.querySelective(userId, keyword, page, limit, sort, order);
        long total = PageInfo.of(footprintList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", toVo(footprintList));

        return ResponseUtil.ok(data);
    }



    private List<Map<String,Object>> toVo( List<LitemallSearchHistory> searchHistories){
        List<Map<String,Object>> list = new ArrayList<>();
        for (LitemallSearchHistory history : searchHistories) {
            Map<String,Object> map = new HashMap<>();
            map.put("nickName",userService.nickNameByUserId(history.getUserId()));
            map.put("keyword",history.getKeyword());
            map.put("from",history.getFrom());
            map.put("addTime",history.getAddTime());
            list.add(map);
        }
        return list;
    }


    @RequiresPermissionsDesc(menu={"用户管理" , "用户搜索历史"}, button="查询")
    @GetMapping("/searchHistory")
    public Object selectFootprintByUserId(Integer userId,String sort, String order){
        if (userId == null){
            return ResponseUtil.fail(AdminResponseCode.USER_ID_NULL,"用户ID为空");
        }
        if (!userService.userCount(userId)){
            return ResponseUtil.fail(AdminResponseCode.INVALID_USER_ID,"用户ID不存在");
        }
        return searchHistoryService.selectSearchHistoryByUserId(userId,sort,order);
    }

}
