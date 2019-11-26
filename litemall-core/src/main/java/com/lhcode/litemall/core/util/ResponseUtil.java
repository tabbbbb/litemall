package com.lhcode.litemall.core.util;

import com.lhcode.litemall.db.domain.LitemallAd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应操作结果
 * <pre>
 *  {
 *      errno： 错误码，
 *      errmsg：错误消息，
 *      data：  响应数据
 *  }
 * </pre>
 *
 * <p>
 * 错误码：
 * <ul>
 * <li> 0，成功；
 * <li> 4xx，前端错误，说明前端开发者需要重新了解后端接口使用规范：
 * <ul>
 * <li> 401，参数错误，即前端没有传递后端需要的参数；
 * <li> 402，参数值错误，即前端传递的参数值不符合后端接收范围。
 * </ul>
 * <li> 5xx，后端错误，除501外，说明后端开发者应该继续优化代码，尽量避免返回后端错误码：
 * <ul>
 * <li> 501，验证失败，即后端要求用户登录；
 * <li> 502，系统内部错误，即没有合适命名的后端内部错误；
 * <li> 503，业务不支持，即后端虽然定义了接口，但是还没有实现功能；
 * <li> 504，更新数据失效，即后端采用了乐观锁更新，而并发更新时存在数据更新失效；
 * <li> 505，更新数据失败，即后端数据库更新失败（正常情况应该更新成功）。
 * </ul>
 * <li> 6xx，小商城后端业务错误码，
 * 具体见litemall-admin-api模块的AdminResponseCode。
 * <li> 7xx，管理后台后端业务错误码，
 * 具体见litemall-wx-api模块的WxResponseCode。
 * </ul>
 */
@ApiModel(value = "返回数据")
public class ResponseUtil {

    @ApiModelProperty(value = "错误码")
    private Integer errno;
    @ApiModelProperty(value = "数据")
    private Object data;
    @ApiModelProperty(value = "信息")
    private String errmsg;

    public Integer getErrno() {
        return errno;
    }

    public void setErrno(Integer errno) {
        this.errno = errno;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public static Object ok() {
        ResponseUtil ok = new ResponseUtil();
        ok.setErrmsg("成功");
        ok.setErrno(0);
        return ok;
    }

    public static Object ok(Object data) {
        ResponseUtil ok = new ResponseUtil();
        ok.setErrmsg("成功");
        ok.setErrno(0);
        ok.setData(data);

        return ok;
    }

    public static Object ok(String errmsg, Object data) {
        ResponseUtil ok = new ResponseUtil();
        ok.setErrmsg(errmsg);
        ok.setErrno(0);
        ok.setData(data);
        return ok;
    }

    public static Object fail() {
        ResponseUtil fail = new ResponseUtil();
        fail.setErrmsg("错误");
        fail.setErrno(-1);
        return fail;
    }

    public static Object fail(int errno, String errmsg) {
        ResponseUtil fail = new ResponseUtil();
        fail.setErrmsg(errmsg);
        fail.setErrno(errno);
        return fail;
    }

    public static Object badArgument() {
        return fail(401, "参数不对");
    }

    public static Object badArgumentValue() {
        return fail(402, "参数值不对");
    }

    public static Object unlogin() {
        return fail(501, "请登录");
    }

    public static Object serious() {
        return fail(502, "系统内部错误");
    }

    public static Object unsupport() {
        return fail(503, "业务不支持");
    }

    public static Object updatedDateExpired() {
        return fail(504, "更新数据已经失效");
    }

    public static Object updatedDataFailed() {
        return fail(505, "更新数据失败");
    }

    public static Object unauthz() {
        return fail(506, "无操作权限");
    }
}

