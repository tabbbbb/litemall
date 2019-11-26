package com.lhcode.litemall.wx.dao;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("微信登录信息")
public class WxLoginInfo {

    @ApiModelProperty("微信登录的code")
    private String code;
    @ApiModelProperty("微信登录的用户信息")
    private UserInfo userInfo;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
