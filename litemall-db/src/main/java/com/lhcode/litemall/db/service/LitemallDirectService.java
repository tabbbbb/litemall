/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: LitemallDirect
 * Author:   Administrator
 * Date:     2019/11/12 0012 11:22
 * Description: 直通车
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.lhcode.litemall.db.dao.LitemallDirectMapper;
import com.lhcode.litemall.db.domain.LitemallDirect;
import com.lhcode.litemall.db.domain.LitemallDirectExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈直通车〉
 *
 * @author Administrator
 * @create 2019/11/12 0012
 * @since 1.0.0
 */
@Service
public class LitemallDirectService {

    @Resource
    private LitemallDirectMapper directMapper;


    public List<LitemallDirect> getList(Integer position, Integer isStart, Integer page, Integer limit, String sort, String order){
        LitemallDirectExample example = new LitemallDirectExample();
        LitemallDirectExample.Criteria criteria = example.createCriteria();

        if (position != null && position != -1) {
            criteria.andPositionEqualTo(position);
        }
        if(isStart != null){
            criteria.andIsStartEqualTo(isStart);
        }
        criteria.andDeletedEqualTo(0);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return directMapper.selectByExample(example);
    }


    public int createDirect(LitemallDirect direct){
        direct.setAddTime(new Date());
        direct.setIsStart(1);
        directMapper.disabledDirect(direct.getPosition());
        return directMapper.insertSelective(direct);
    }


    public int updateDirect(LitemallDirect direct){
        if (direct.getIsStart() == 1){
            directMapper.disabledDirect(direct.getPosition());
        }
        direct.setUpdateTime(new Date());
        return directMapper.updateByPrimaryKeySelective(direct);
    }



    public int deleteDirect(Integer id){
        LitemallDirect direct = new LitemallDirect();
        direct.setDeleteTime(new Date());
        direct.setId(id);
        direct.setDeleted(1);
        return directMapper.updateByPrimaryKeySelective(direct);
    }
}
