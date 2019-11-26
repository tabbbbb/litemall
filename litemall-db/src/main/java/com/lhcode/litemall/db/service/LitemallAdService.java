package com.lhcode.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.lhcode.litemall.db.dao.LitemallAdMapper;
import com.lhcode.litemall.db.domain.LitemallAd;
import com.lhcode.litemall.db.domain.LitemallAdExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallAdService {
    @Resource
    private LitemallAdMapper adMapper;

    public List<LitemallAd> queryIndex() {
        LitemallAdExample example = new LitemallAdExample();
        example.or().andPositionEqualTo((byte) 1).andDeletedEqualTo(false).andEnabledEqualTo(true);
        return adMapper.selectByExample(example);
    }

    public List<LitemallAd> querySelective(Byte position, Integer enabled, Integer page, Integer limit, String sort, String order) {
        LitemallAdExample example = new LitemallAdExample();
        LitemallAdExample.Criteria criteria = example.createCriteria();

        if (position != null && position != -1) {
            criteria.andPositionEqualTo(position);
        }
        if(enabled != null){
            if (enabled == 0){
                criteria.andEnabledEqualTo(false);
            }else{
                criteria.andEnabledEqualTo(true);
            }
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return adMapper.selectByExample(example);
    }


    public List<LitemallAd> getList(){
        LitemallAdExample example = new LitemallAdExample();
        LitemallAdExample.Criteria criteria = example.createCriteria();
        criteria.andEnabledEqualTo(true);
        criteria.andDeletedEqualTo(false);
        return adMapper.selectByExample(example);
    }

    public synchronized int updateById(LitemallAd ad) {
        ad.setUpdateTime(LocalDateTime.now());
        if (ad.getEnabled() == true){
            this.updateEnabled(Integer.valueOf(ad.getPosition()));
        }
        return adMapper.updateByPrimaryKeySelective(ad);
    }

    public void deleteById(Integer id) {
        adMapper.logicalDeleteByPrimaryKey(id);
    }

    public synchronized void add(LitemallAd ad) {
        ad.setAddTime(LocalDateTime.now());
        ad.setUpdateTime(LocalDateTime.now());
        if (ad.getEnabled() == true){
            this.updateEnabled(Integer.valueOf(ad.getPosition()));
        }
        adMapper.insertSelective(ad);
    }

    public LitemallAd findById(Integer id) {
        return adMapper.selectByPrimaryKey(id);
    }

    public void updateEnabled(Integer position){
        adMapper.updateEnabled(position);
    }
}
