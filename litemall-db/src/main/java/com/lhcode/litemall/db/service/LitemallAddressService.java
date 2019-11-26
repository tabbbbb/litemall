package com.lhcode.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.lhcode.litemall.db.dao.LitemallAddressMapper;
import com.lhcode.litemall.db.domain.LitemallAddress;
import com.lhcode.litemall.db.domain.LitemallAddressExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallAddressService {
    @Resource
    private LitemallAddressMapper addressMapper;

    public List<LitemallAddress> queryByUid(Integer uid) {
        LitemallAddressExample example = new LitemallAddressExample();
        example.or().andUserIdEqualTo(uid).andDeletedEqualTo(false);
        example.setOrderByClause("is_default desc ,add_time desc");
        return addressMapper.selectByExample(example);
    }

    public LitemallAddress findById(Integer id) {
        return addressMapper.selectByPrimaryKey(id);
    }

    public int add(LitemallAddress address) {
        address.setAddTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        return addressMapper.insertSelective(address);
    }

    public int update(LitemallAddress address) {
        address.setUpdateTime(LocalDateTime.now());
        return addressMapper.updateByPrimaryKeySelective(address);
    }

    public void delete(Integer id) {
        addressMapper.logicalDeleteByPrimaryKey(id);
    }

    public LitemallAddress findDefault(Integer userId) {
        LitemallAddressExample example = new LitemallAddressExample();
        example.or().andUserIdEqualTo(userId).andIsDefaultEqualTo(true).andDeletedEqualTo(false);
        return addressMapper.selectOneByExample(example);
    }

    public void resetDefault(Integer userId) {
        LitemallAddress address = new LitemallAddress();
        address.setIsDefault(false);
        address.setUpdateTime(LocalDateTime.now());
        LitemallAddressExample example = new LitemallAddressExample();
        example.or().andUserIdEqualTo(userId).andDeletedEqualTo(false);
        addressMapper.updateByExampleSelective(address, example);
    }

    public List<LitemallAddress> querySelective(Integer userId, String name, Integer page, Integer limit, String sort, String order) {
        LitemallAddressExample example = new LitemallAddressExample();
        LitemallAddressExample.Criteria criteria = example.createCriteria();

        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return addressMapper.selectByExample(example);
    }


    /**
     * 根据用户id获取地址
     * @param id
     * @return
     */
    public List<LitemallAddress> selectAddressByUserId(Integer id,String sort, String order){
        LitemallAddressExample example = new LitemallAddressExample();
        LitemallAddressExample.Criteria criteria = example.createCriteria();
        if (id != null){
            criteria.andUserIdEqualTo(id);
        }
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }
        return addressMapper.selectAddressByUserId(example);
    }
}
