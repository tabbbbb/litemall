package com.lhcode.litemall.db.dao;

import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.domain.LitemallUserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LitemallUserMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    long countByExample(LitemallUserExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    int deleteByExample(LitemallUserExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    int insert(LitemallUser record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    int insertSelective(LitemallUser record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    LitemallUser selectOneByExample(LitemallUserExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    LitemallUser selectOneByExampleSelective(@Param("example") LitemallUserExample example, @Param("selective") LitemallUser.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    List<LitemallUser> selectByExampleSelective(@Param("example") LitemallUserExample example, @Param("selective") LitemallUser.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    List<LitemallUser> selectByExample(LitemallUserExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    LitemallUser selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") LitemallUser.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    LitemallUser selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    LitemallUser selectByPrimaryKeyWithLogicalDelete(@Param("id") Integer id, @Param("andLogicalDeleted") boolean andLogicalDeleted);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") LitemallUser record, @Param("example") LitemallUserExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") LitemallUser record, @Param("example") LitemallUserExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(LitemallUser record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(LitemallUser record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int logicalDeleteByExample(@Param("example") LitemallUserExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_user
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int logicalDeleteByPrimaryKey(Integer id);


    /**
     * 用户数量
     * @return
     */
    int userCount(@Param("id") Integer id);


    String nickNameByUserId(@Param("id") Integer id);


    int updateAdminId(Integer adminId);

    int updateAdminIds(@Param("adminId") Integer adminId, @Param("ids")String[] ids);

    int updateUserLevel(@Param("id") Integer id ,@Param("level")Integer level);
}