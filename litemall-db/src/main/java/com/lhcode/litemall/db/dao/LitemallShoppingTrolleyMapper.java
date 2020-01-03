package com.lhcode.litemall.db.dao;

import com.lhcode.litemall.db.domain.LitemallGoodsSpecification;
import com.lhcode.litemall.db.domain.LitemallShoppingTrolley;
import com.lhcode.litemall.db.domain.LitemallShoppingTrolleyExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LitemallShoppingTrolleyMapper {
    long countByExample(LitemallShoppingTrolleyExample example);

    int deleteByExample(LitemallShoppingTrolleyExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LitemallShoppingTrolley record);

    int insertSelective(LitemallShoppingTrolley record);

    List<LitemallShoppingTrolley> selectByExample(LitemallShoppingTrolleyExample example);

    LitemallShoppingTrolley selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") LitemallShoppingTrolley record, @Param("example") LitemallShoppingTrolleyExample example);

    int updateByExample(@Param("record") LitemallShoppingTrolley record, @Param("example") LitemallShoppingTrolleyExample example);

    int updateByPrimaryKeySelective(LitemallShoppingTrolley record);

    int updateByPrimaryKey(LitemallShoppingTrolley record);

    int deleteByIdAll(String [] ids);

    int deleteBySpecId(List<LitemallGoodsSpecification> list);
}