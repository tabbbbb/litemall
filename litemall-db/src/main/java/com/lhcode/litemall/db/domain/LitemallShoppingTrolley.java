package com.lhcode.litemall.db.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(value = "LitemallShoppingTrolley",description = "购物车")
public class LitemallShoppingTrolley {

    @ApiModelProperty("主键id")
    private Integer id;
    @ApiModelProperty("用户id")
    private Integer userId;
    @ApiModelProperty("商品id")
    private Integer goodsId;
    @ApiModelProperty("商品规格id")
    private Integer goodsSpecId;
    @ApiModelProperty("添加的数量")
    private Integer addNum;

    private Date addTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getGoodsSpecId() {
        return goodsSpecId;
    }

    public void setGoodsSpecId(Integer goodsSpecId) {
        this.goodsSpecId = goodsSpecId;
    }

    public Integer getAddNum() {
        return addNum;
    }

    public void setAddNum(Integer addNum) {
        this.addNum = addNum;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}