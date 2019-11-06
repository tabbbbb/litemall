package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.dao.LitemallSpreadclassMapper;
import com.lhcode.litemall.db.domain.LitemallSpreadclass;
import com.lhcode.litemall.db.domain.LitemallSpreadclassExample;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LitemallSpreadClassService {
    @Resource
    private LitemallSpreadclassMapper spreadclassMapper;

    public List<LitemallSpreadclass> queryVO(int offset, int limit) {
        LitemallSpreadclassExample example = new LitemallSpreadclassExample();
        PageHelper.startPage(offset, limit);
        return spreadclassMapper.selectByExample(example);
    }

    public int queryTotalCount() {
        LitemallSpreadclassExample example = new LitemallSpreadclassExample();
        return (int) spreadclassMapper.countByExample(example);
    }

    public LitemallSpreadclass findById(Integer id) {
        return spreadclassMapper.selectByPrimaryKey(id);
    }

    public LitemallSpreadclass findByLevel(Integer level) {
        LitemallSpreadclassExample example = new LitemallSpreadclassExample();
        example.or().andSpreadLevelEqualTo(level);
        return spreadclassMapper.selectOneByExample(example);
    }

    public List<LitemallSpreadclass> querySelective(String id, Integer page, Integer size) {
        LitemallSpreadclassExample example = new LitemallSpreadclassExample();
        LitemallSpreadclassExample.Criteria criteria = example.createCriteria();
        example.setOrderByClause(" spread_level asc ");
        if (!StringUtils.isEmpty(id)) {
            criteria.andIdEqualTo(Integer.valueOf(id));
        }

        PageHelper.startPage(page, size);
        return spreadclassMapper.selectByExample(example);
    }

    public int updateById(LitemallSpreadclass brand) {
        return spreadclassMapper.updateByPrimaryKeySelective(brand);
    }

    public void deleteById(Integer id) {
        spreadclassMapper.deleteByPrimaryKey(id);
    }

    public void add(LitemallSpreadclass brand) {
        spreadclassMapper.insertSelective(brand);
    }

    public List<LitemallSpreadclass> all() {
        LitemallSpreadclassExample example = new LitemallSpreadclassExample();
        return spreadclassMapper.selectByExample(example);
    }
}
