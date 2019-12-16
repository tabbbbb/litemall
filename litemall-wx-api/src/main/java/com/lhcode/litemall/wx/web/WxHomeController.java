package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.core.util.JacksonUtil;
import com.lhcode.litemall.db.domain.*;
import com.lhcode.litemall.db.service.*;
import com.lhcode.litemall.wx.annotation.LoginUser;
import com.lhcode.litemall.wx.service.HomeCacheManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 首页服务
 */
@RestController
@RequestMapping("/wx/home")
@Validated
@Api(value = "/wx/home",description = "首页展示数据")
public class WxHomeController {
    private final Log logger = LogFactory.getLog(WxHomeController.class);

    @Autowired
    private LitemallAdService adService;

    @Autowired
    private LitemallDirectService directService;

    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallCategoryService categoryService;

    @Autowired
    private LitemallGoodsSpecificationService goodsSpecificationService;

    private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(9);

    private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

    private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 9, 1000, TimeUnit.MILLISECONDS, WORK_QUEUE, HANDLER);

    @GetMapping("/cache")
    @ApiOperation(value = "清除缓存",response = LitemallDirect.class,notes = "data:{String}")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType="query",name="key",value="验证字符串",dataTypeClass = String.class)
    })
    public Object cache(@NotNull String key) {
        if (!key.equals("litemall_cache")) {
            return ResponseUtil.fail();
        }

        // 清除缓存
        HomeCacheManager.clearAll();
        return ResponseUtil.ok("缓存已清除");
    }


    @GetMapping("/index")
    @ApiOperation(value = "获取主页的所有图片和跳转链接",response =LitemallAd.class ,notes = "data:{ad:Map,direct:LitemallDirect}",nickname = "获取主页的所有图片和跳转链接")
    public Object index(@ApiIgnore @LoginUser Integer userId) {
//        if (HomeCacheManager.hasData(HomeCacheManager.INDEX)){
//            return ResponseUtil.ok(HomeCacheManager.getCacheData(HomeCacheManager.INDEX));
//        }
        Map<String,Object> data = new HashMap<>();
        Callable<List> adCallable = new Callable<List>() {
            @Override
            public List call() throws Exception {
                List<Map<String,Object>> list = new ArrayList<>();
                List<LitemallAd> adList = adService.getList();
                for (LitemallAd ad : adList) {
                    Map<String,Object> map = new HashMap<>();
                    list.add(map);
                    map.put("id",ad.getId());
                    map.put("position",ad.getPosition());
                    map.put("url",ad.getUrl());
                    map.put("link",ad.getLink());
                    map.put("name",ad.getName());
                    map.put("content",ad.getContent());
                    if (ad.getPosition() == 1)continue;
                    String [] goodsSn = ad.getLink().split(",");
                    List<Map<String,Object>> goodsList = new ArrayList<>();
                    for (String s : goodsSn) {
                        Map<String,Object> map1 = new HashMap<>();
                        LitemallGoods goods = goodsService.queryByGoodsSn(userId,s);
                        map1.put("goodsId",goods.getId());
                        map1.put("goodsName",goods.getName());
                        map1.put("price",goods.getPrice());
                        map1.put("goodsUrl",goods.getPicUrl());
                        LitemallGoodsSpecification defaultGoodsSpecification = goodsSpecificationService.getSpecByGoodsSn(goods.getId());
                        map1.put("specId",defaultGoodsSpecification.getId());
                        map1.put("specValue",defaultGoodsSpecification.getValue());
                        goodsList.add(map1);
                    }
                    map.put("goodsList",goodsList);
                }
                return list;
            }
        };
        Callable<List> directCallable = new Callable<List>() {
            @Override
            public List call() throws Exception {
                List list = new ArrayList();
                List <LitemallDirect> directList = directService.getList();
                for (LitemallDirect direct : directList) {
                    Map<String, String> map = JacksonUtil.toMap(direct.getLink());

                    if (map == null || map.get("id") == null){
                        list.add(direct);
                    }else {
                        String id = map.get("id");
                        LitemallCategory category = categoryService.findById(Integer.valueOf(id));
                        list.add(direct.toVo(category.getPid() == 0 ?category.getId():category.getPid()));
                    }
                }
                return list;
            }
        };
        FutureTask<List> adFutureTask = new FutureTask<>(adCallable);
        FutureTask<List> directFutureTask = new FutureTask<>(directCallable);
        executorService.submit(adFutureTask);
        executorService.submit(directFutureTask);
        try {
            data.put("ad",adFutureTask.get(10, TimeUnit.SECONDS));
            data.put("direct",directFutureTask.get(10, TimeUnit.SECONDS));

            //HomeCacheManager.loadData(HomeCacheManager.INDEX,data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return ResponseUtil.ok(data);
    }






}