package com.lhcode.litemall.db.dao;

import com.lhcode.litemall.db.domain.LitemallDirect;
import com.lhcode.litemall.db.domain.LitemallDirectExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LitemallDirectMapper {
    long countByExample(LitemallDirectExample example);

    int deleteByExample(LitemallDirectExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LitemallDirect record);

    int insertSelective(LitemallDirect record);

    List<LitemallDirect> selectByExample(LitemallDirectExample example);

    LitemallDirect selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") LitemallDirect record, @Param("example") LitemallDirectExample example);

    int updateByExample(@Param("record") LitemallDirect record, @Param("example") LitemallDirectExample example);

    int updateByPrimaryKeySelective(LitemallDirect record);

    int updateByPrimaryKey(LitemallDirect record);

    int disabledDirect(Integer position);
}