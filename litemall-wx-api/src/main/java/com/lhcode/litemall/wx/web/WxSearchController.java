package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.wx.annotation.LoginUser;
import com.lhcode.litemall.wx.util.SearchGoods;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ResponseHeader;
import io.swagger.models.auth.In;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallKeyword;
import com.lhcode.litemall.db.domain.LitemallSearchHistory;
import com.lhcode.litemall.db.service.LitemallKeywordService;
import com.lhcode.litemall.db.service.LitemallSearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品搜索服务
 * <p>
 * 注意：目前搜索功能非常简单，只是基于关键字匹配。
 */
@RestController
@RequestMapping("/wx/search")
@Validated
@Api(value = "/wx/search",description = "搜索页")
public class WxSearchController {
    private final Log logger = LogFactory.getLog(WxSearchController.class);

    @Autowired
    private LitemallKeywordService keywordsService;
    @Autowired
    private LitemallSearchHistoryService searchHistoryService;

    @Autowired
    private SearchGoods searchGoods;

    /**
     * 搜索页面信息
     * <p>
     * 如果用户已登录，则给出用户历史搜索记录；
     * 如果没有登录，则给出空历史搜索记录。
     *
     * @param userId 用户ID，可选
     * @return 搜索页面信息
     */
    @GetMapping("index")
    @ApiOperation(value = "获取历史搜索",response = ResponseUtil.class,notes = "data:{historyKeywordList:LitemallSearchHistory}")
    public Object index(@ApiIgnore @LoginUser Integer userId) {
        List<LitemallSearchHistory> historyList = null;
        if (userId != null) {
            //取出用户历史关键字
            historyList = searchHistoryService.queryByUid(userId);
        } else {
            historyList = new ArrayList<>(0);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("historyKeywordList", historyList);
        return ResponseUtil.ok(data);
    }

//    /**
//     * 关键字提醒
//     * <p>
//     * 当用户输入关键字一部分时，可以推荐系统中合适的关键字。
//     *
//     * @param keyword 关键字
//     * @return 合适的关键字
//     */
//    @GetMapping("helper")
//    public Object helper(@NotEmpty String keyword,
//                         @RequestParam(defaultValue = "1") Integer page,
//                         @RequestParam(defaultValue = "10") Integer size) {
//        List<LitemallKeyword> keywordsList = keywordsService.queryByKeyword(keyword, page, size);
//        String[] keys = new String[keywordsList.size()];
//        int index = 0;
//        for (LitemallKeyword key : keywordsList) {
//            keys[index++] = key.getKeyword();
//        }
//        return ResponseUtil.ok(keys);
//    }

    /**
     * 清除用户搜索历史
     *
     * @param userId 用户ID
     * @return 清理是否成功
     */
    @PostMapping("clearhistory")
    @ApiOperation(value = "清除用户搜索历史",response = ResponseUtil.class,notes = "data:{}")
    public Object clearhistory(@ApiIgnore @LoginUser Integer userId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }

        searchHistoryService.deleteByUid(userId);
        return ResponseUtil.ok();
    }



    @GetMapping("searchGoods")
    @ApiOperation(value = "获取推荐商品",response = ResponseUtil.class,notes = "data:{}")
    public Object getGoods(@ApiIgnore @LoginUser Integer userId){
        return ResponseUtil.ok(searchGoods.getMap(userId));
    }



}
