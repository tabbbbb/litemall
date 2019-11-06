package com.lhcode.litemall.wx.service;


import com.lhcode.litemall.db.dao.LitemallViplevelMapper;
import com.lhcode.litemall.db.domain.LitemallViplevel;
import com.lhcode.litemall.db.domain.LitemallViplevelExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VipLevelService {

    @Resource
    private LitemallViplevelMapper viplevelMapper;


    //获取会员折扣等级
    public Map<String,Object> queryVipGrade(String vipIndex){
        Map<String,Object> map = new HashMap<>();
        LitemallViplevelExample example = new LitemallViplevelExample();
        example.setOrderByClause(" vip_count asc ");
        List<LitemallViplevel> vipl = viplevelMapper.selectByExample(example);
        String discount = "";
        String viptype = "";
        for(int i=0;i<vipl.size();i++){
            int vipcount = vipl.get(i).getVipCount();
            if(Integer.parseInt(vipIndex)>=vipcount){
                discount = vipl.get(i).getDiscount();
                viptype = vipl.get(i).getVipType();
            }
        }
        map.put("discount",discount);
        map.put("viptype",viptype);

        return map;
    }


    //获取下一个会员等级积分
    public String queryNextClass(String vipIndex){
        LitemallViplevelExample example = new LitemallViplevelExample();
        example.createCriteria().andVipCountGreaterThan(Integer.parseInt(vipIndex));
        example.setOrderByClause(" vip_count asc ");
        List<LitemallViplevel> vipl = viplevelMapper.selectByExample(example);
        //如果是最高等级
        if(vipl==null||vipl.size()<1){
            LitemallViplevelExample example1 = new LitemallViplevelExample();
            example1.setOrderByClause(" vip_count desc ");
            List<LitemallViplevel> vipl1 = viplevelMapper.selectByExample(example1);
            return vipl1.get(0).getVipCount().toString();
        }

        return vipl.get(0).getVipCount().toString();
    }

    //获取所有会员等级
    public List<LitemallViplevel> queryVipList(){
        LitemallViplevelExample example = new LitemallViplevelExample();
        example.setOrderByClause(" vip_count desc ");
        List<LitemallViplevel> vipl = viplevelMapper.selectByExample(example);
        return vipl;
    }






}
