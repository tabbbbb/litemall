package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.dao.LitemallUserMapper;
import com.lhcode.litemall.db.domain.LitemallAddress;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.domain.LitemallUserExample;
import com.lhcode.litemall.db.domain.UserVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class LitemallUserService {
    @Resource
    private LitemallUserMapper userMapper;
    @Autowired
    private LitemallAdminService adminService;

    public LitemallUser findById(Integer userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    public UserVo findUserVoById(Integer userId) {
        LitemallUser user = findById(userId);
        UserVo userVo = new UserVo();
        userVo.setNickname(user.getNickname());
        userVo.setAvatar(user.getAvatar());
        return userVo;
    }

    public LitemallUser queryByOid(String openId) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andWeixinOpenidEqualTo(openId).andDeletedEqualTo(false);
        return userMapper.selectOneByExample(example);
    }

    public void add(LitemallUser user) {
        user.setAddTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insertSelective(user);
    }

    public int updateById(LitemallUser user) {
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateByPrimaryKeySelective(user);
    }

    public List<LitemallUser> querySelective(Integer adminId,String username, String mobile, Integer page, Integer size, String sort, String order) {
        LitemallUserExample example = new LitemallUserExample();
        LitemallUserExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(username)) {
            //Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
            criteria.andNicknameLike("%" + username + "%");
            //if (pattern.matcher(username).matches()) {
            //    criteria.orUserId(Integer.valueOf(username));
           // }
        }
        if (!StringUtils.isEmpty(mobile)) {
            criteria.andMobileEqualTo(mobile);
        }

        if (adminId == -10086){
            criteria.andParentIdEqualTo(0);
        }else if (adminId != -1){
            criteria.andParentIdEqualTo(adminId);
        }

        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return userMapper.selectByExample(example);
    }

    public int count() {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andDeletedEqualTo(false);
        return (int) userMapper.countByExample(example);
    }

    public List<LitemallUser> queryByUsername(String username) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andUsernameEqualTo(username).andDeletedEqualTo(false);
        return userMapper.selectByExample(example);
    }

    public boolean checkByUsername(String username) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andUsernameEqualTo(username).andDeletedEqualTo(false);
        return userMapper.countByExample(example) != 0;
    }

    public List<LitemallUser> queryByMobile(String mobile) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andMobileEqualTo(mobile).andDeletedEqualTo(false);
        return userMapper.selectByExample(example);
    }

    public List<LitemallUser> queryByOpenid(String openid) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andWeixinOpenidEqualTo(openid).andDeletedEqualTo(false);
        return userMapper.selectByExample(example);
    }

    public void deleteById(Integer id) {
        userMapper.logicalDeleteByPrimaryKey(id);
    }

    public List<LitemallUser> queryByParentId(Integer parentId,Integer page, Integer size){
        LitemallUserExample example = new LitemallUserExample();
        example.or().andParentIdEqualTo(parentId).andDeletedEqualTo(false);
        PageHelper.startPage(page, size);
        return userMapper.selectByExample(example);

    }

    public LitemallUser selectByMobile(String mobile) {
        LitemallUserExample example = new LitemallUserExample();
        example.or().andMobileEqualTo(mobile).andDeletedEqualTo(false);
        return userMapper.selectOneByExample(example);
    }


    /**
     * 判断用户是否存在
     * @param id
     * @return
     */
    public boolean userCount(Integer id){
        if (userMapper.userCount(id) == 1){
            return true;
        }
        return false;
    }



    public String nickNameByUserId(Integer userId){
        return userMapper.nickNameByUserId(userId);
    }



    public void updateAdminId(Integer adminId){
        if (adminId != null){
            userMapper.updateAdminId(adminId);
        }
    }

    public int updateAdminIds(Integer adminId , String [] ids){
        return userMapper.updateAdminIds(adminId,ids);
    }

    public void updateUserLevel(Integer id,Integer level){
        userMapper.updateUserLevel(id,level);
    }
}
