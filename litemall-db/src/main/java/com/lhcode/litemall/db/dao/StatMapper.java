package com.lhcode.litemall.db.dao;

import io.swagger.annotations.ApiParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface StatMapper {
    List<Map> statUser();

    List<Map> statOrder(@Param("parentId") Integer parentId);

    List<Map> statGoods(@Param("parentId") Integer parentId);
}