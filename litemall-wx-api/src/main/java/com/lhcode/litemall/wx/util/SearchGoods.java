/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: SearchGoods
 * Author:   Administrator
 * Date:     2019/11/25 0025 10:46
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.wx.util;

import com.lhcode.litemall.db.domain.LitemallGoods;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.service.LitemallGoodsService;
import com.lhcode.litemall.db.service.LitemallVipClassService;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/11/25 0025
 * @since 1.0.0
 */
@Component
public class SearchGoods {

    private  Map<String,Object> map = new HashMap<>();

    @Resource
    private LitemallGoodsService goodsService;

    @Resource
    private LitemallVipClassService vipService;

    private synchronized void randomMap(){
        if (map.get("exceedTime") == null || ((LocalDateTime)map.get("exceedTime")).isBefore(LocalDateTime.now())){
            List<LitemallGoods> goodsHotList = goodsService.getHot();
            List<LitemallGoods> goodsNewList = goodsService.getNew();
            LitemallGoods [] hotList = new LitemallGoods[2];
            LitemallGoods [] newList = new LitemallGoods[2];
            hotList[0] = (goodsHotList.get((int) Math.floor(Math.random()*goodsHotList.size())));
            while (hotList[1] == null || hotList[0] == hotList[1]){
                hotList[1] =(goodsHotList.get((int) Math.floor(Math.random()*goodsHotList.size())));
            }
            newList[0] = (goodsNewList.get((int) Math.floor(Math.random()*goodsNewList.size())));
            while (newList[1] == null || newList[0] == newList[1]){
                newList[1] = (goodsNewList.get((int) Math.floor(Math.random()*goodsNewList.size())));
            }
            map.put("hotList",hotList);
            map.put("newList",newList);
            map.put("exceedTime",LocalDateTime.now().plusHours(2));
        }
    }

    public Map<String,LitemallGoods[]> getMap(Integer userId){
        if (map.get("exceedTime") == null || ((LocalDateTime)map.get("exceedTime")).isBefore(LocalDateTime.now())){
           this.randomMap();
        }
        String discount = "1";
        if (userId != null)discount = vipService.getUserLevel(userId);
        LitemallGoods [] hotList = (LitemallGoods[]) this.map.get("hotList");
        LitemallGoods [] newList = (LitemallGoods[]) this.map.get("newList");
        if (discount.equals("1")){
            for (int i = 0; i < 2; i++) {
               hotList[i].setPrice(hotList[i].getOnePrice());
               newList[i].setPrice(newList[i].getOnePrice());
            }
        }else if (discount.equals("2")){
            for (int i = 0; i < 2; i++) {
                hotList[i].setPrice(hotList[i].getTwoPrice());
                newList[i].setPrice(newList[i].getTwoPrice());
            }
        }else if (discount.equals("3")){
            for (int i = 0; i < 2; i++) {
                hotList[i].setPrice(hotList[i].getThreePrice());
                newList[i].setPrice(newList[i].getThreePrice());
            }
        }
        HashMap<String, LitemallGoods[]> map = new HashMap<>();
        map.put("hot", (LitemallGoods[]) this.map.get("hotList"));
        map.put("new", (LitemallGoods[]) this.map.get("newList"));
        return map;
    }
}
