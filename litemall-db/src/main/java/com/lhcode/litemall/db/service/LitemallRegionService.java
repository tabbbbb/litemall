package com.lhcode.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.lhcode.litemall.db.dao.LitemallRegionMapper;
import com.lhcode.litemall.db.domain.LitemallRegion;
import com.lhcode.litemall.db.domain.LitemallRegionExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LitemallRegionService {

    @Resource
    private LitemallRegionMapper regionMapper;

    public List<LitemallRegion> getAll(){
        LitemallRegionExample example = new LitemallRegionExample();
        byte b = 4;
        example.or().andTypeNotEqualTo(b);
        return regionMapper.selectByExample(example);
    }

    public List<LitemallRegion> queryByPid(Integer parentId) {
        LitemallRegionExample example = new LitemallRegionExample();
        example.or().andPidEqualTo(parentId);
        return regionMapper.selectByExample(example);
    }

    public LitemallRegion findById(Integer id) {
        return regionMapper.selectByPrimaryKey(id);
    }

    public List<LitemallRegion> querySelective(String name, Integer code, Integer page, Integer size, String sort, String order) {
        LitemallRegionExample example = new LitemallRegionExample();
        LitemallRegionExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        if (!StringUtils.isEmpty(code)) {
            criteria.andCodeEqualTo(code);
        }

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return regionMapper.selectByExample(example);
    }


    public List<LitemallRegion> queryChildren(Integer id) {
        LitemallRegionExample example = new LitemallRegionExample();
        example.or().andPidEqualTo(id);
        return regionMapper.selectByExample(example);
    }

    public  List<LitemallRegion> toAll(Integer pid){
        List<LitemallRegion> list = queryChildren(pid);
        if (list == null || list.size() == 0) return null;
        for (LitemallRegion region : list) {
            region.setChildren(toAll(region.getId()));
        }
        return list;
    }



}
