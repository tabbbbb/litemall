/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: LitemallShoppingTrolley
 * Author:   Administrator
 * Date:     2019/11/18 0018 16:03
 * Description: 购物车
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.dao.LitemallShoppingTrolleyMapper;
import com.lhcode.litemall.db.domain.LitemallShoppingTrolley;
import com.lhcode.litemall.db.domain.LitemallShoppingTrolleyExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈购物车〉
 *
 * @author Administrator
 * @create 2019/11/18 0018
 * @since 1.0.0
 */
@Service
public class LitemallShoppingTrolleyService {


    @Resource
    private LitemallShoppingTrolleyMapper shoppingTrolleyMapper;


    public void addShoppingTrolley(LitemallShoppingTrolley trolley){
        LitemallShoppingTrolleyExample example = new LitemallShoppingTrolleyExample();
        LitemallShoppingTrolleyExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(trolley.getGoodsId());
        criteria.andGoodsSpecIdEqualTo(trolley.getGoodsSpecId());
        List<LitemallShoppingTrolley> shoppingTrolleyList = shoppingTrolleyMapper.selectByExample(example);
        if (shoppingTrolleyList.size() != 0){
            LitemallShoppingTrolley shoppingTrolley = shoppingTrolleyList.get(0);
            shoppingTrolley.setAddNum(trolley.getAddNum()+shoppingTrolley.getAddNum());
            shoppingTrolleyMapper.updateByPrimaryKey(shoppingTrolley);
        }else{
            trolley.setAddTime(new Date());
            shoppingTrolleyMapper.insertSelective(trolley);
        }

    }

    public void updateShoppingTrolley(LitemallShoppingTrolley trolley){
        shoppingTrolleyMapper.updateByPrimaryKeySelective(trolley);
    }

    public void deleteShoppingTrolley(String []ids){
        shoppingTrolleyMapper.deleteByIdAll(ids);
    }

    public List<LitemallShoppingTrolley> queryAllByUserId(Integer userId){
        LitemallShoppingTrolleyExample example = new LitemallShoppingTrolleyExample();
        LitemallShoppingTrolleyExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        return shoppingTrolleyMapper.selectByExample(example);
    }

}
