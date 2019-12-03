package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.dao.StatMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class StatService {
    @Resource
    private StatMapper statMapper;


    public List<Map> statUser() {
        return statMapper.statUser();
    }

    public List<Map> statOrder(Integer parentId) {
        return statMapper.statOrder(parentId);
    }

    public List<Map> statGoods(Integer parentId) {
        return statMapper.statGoods(parentId);
    }
}
