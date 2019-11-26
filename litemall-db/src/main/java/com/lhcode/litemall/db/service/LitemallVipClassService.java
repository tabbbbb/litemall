package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.constant.LiteMallConstant;
import com.lhcode.litemall.db.dao.LitemallViplevelMapper;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.domain.LitemallViplevel;
import com.lhcode.litemall.db.domain.LitemallViplevelExample;
import com.github.pagehelper.Constant;
import com.github.pagehelper.PageHelper;
import com.lhcode.litemall.db.domain.LitemallBrand.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LitemallVipClassService {

    @Resource
    private LitemallViplevelMapper viplevelMapper;

    @Autowired
    private LitemallUserService userService;

    private Column[] columns = new Column[]{Column.id, Column.name, Column.desc, Column.picUrl, Column.floorPrice};

    public List<LitemallViplevel> queryVO(int offset, int limit) {
        LitemallViplevelExample example = new LitemallViplevelExample();
        PageHelper.startPage(offset, limit);
        return viplevelMapper.selectByExample(example);
    }

    public int queryTotalCount() {
        LitemallViplevelExample example = new LitemallViplevelExample();
        return (int) viplevelMapper.countByExample(example);
    }

    public LitemallViplevel findById(Integer id) {
        return viplevelMapper.selectByPrimaryKey(id);
    }

    public List<LitemallViplevel> querySelective(String id, Integer page, Integer size) {
        LitemallViplevelExample example = new LitemallViplevelExample();
        LitemallViplevelExample.Criteria criteria = example.createCriteria();
        example.setOrderByClause(" vip_count asc ");
        if (!StringUtils.isEmpty(id)) {
            criteria.andIdEqualTo(Integer.valueOf(id));
        }

        PageHelper.startPage(page, size);
        return viplevelMapper.selectByExample(example);
    }

    public int updateById(LitemallViplevel brand) {
        return viplevelMapper.updateByPrimaryKeySelective(brand);
    }

    public void deleteById(Integer id) {
        viplevelMapper.deleteByPrimaryKey(id);
    }

    public LitemallViplevel add(LitemallViplevel brand) {
        if (viplevelMapper.selectCount() >= LiteMallConstant.vipCount){
            return null;
        }
        viplevelMapper.insertSelective(brand);
        return brand;
    }

    public List<LitemallViplevel> all() {
        LitemallViplevelExample example = new LitemallViplevelExample();
        return viplevelMapper.selectByExample(example);
    }


    public String getUserLevel(Integer userId){
        LitemallUser user = userService.findById(userId);
        return findById(Integer.valueOf(user.getUserLevel())).getDiscount();
    }
}
