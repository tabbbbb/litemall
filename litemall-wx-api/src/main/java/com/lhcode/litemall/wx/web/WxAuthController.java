package com.lhcode.litemall.wx.web;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.lhcode.litemall.db.domain.LitemallSpreadclass;
import com.lhcode.litemall.db.service.LitemallSpreadClassService;
import com.lhcode.litemall.wx.annotation.LoginUser;
import com.lhcode.litemall.wx.service.CaptchaCodeManager;
import com.lhcode.litemall.wx.service.UserTokenManager;
import com.lhcode.litemall.wx.service.VipLevelService;
import com.lhcode.litemall.wx.util.WxResponseCode;
import com.sun.org.apache.bcel.internal.classfile.Code;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import jodd.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.notify.NotifyService;
import com.lhcode.litemall.core.notify.NotifyType;
import com.lhcode.litemall.core.util.CharUtil;
import com.lhcode.litemall.core.util.JacksonUtil;
import com.lhcode.litemall.core.util.RegexUtil;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.util.bcrypt.BCryptPasswordEncoder;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.service.CouponAssignService;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.lhcode.litemall.wx.dao.UserInfo;
import com.lhcode.litemall.wx.dao.UserToken;
import com.lhcode.litemall.wx.dao.WxLoginInfo;
import com.lhcode.litemall.core.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 鉴权服务
 */
@RestController
@RequestMapping("/wx/auth")
@Validated
@Api(value = "/wx/auth",description = "用户登录")
public class WxAuthController {
    private final Log logger = LogFactory.getLog(WxAuthController.class);

    @Autowired
    private LitemallUserService userService;

    @Autowired
    private WxMaService wxService;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private VipLevelService vipLevelService;

    @Autowired
    private CouponAssignService couponAssignService;

    @Autowired
    private LitemallSpreadClassService spreadClassService;

    /**
     * 账号登录
     *
     * @param body    请求内容，{ username: xxx, password: xxx }
     * @param request 请求对象
     * @return 登录结果
     */

    //@PostMapping("login")
    public Object login(@RequestBody String body, HttpServletRequest request) {
        String username = JacksonUtil.parseString(body, "username");
        String password = JacksonUtil.parseString(body, "password");
        if (username == null || password == null) {
            return ResponseUtil.badArgument();
        }

        List<LitemallUser> userList = userService.queryByUsername(username);
        LitemallUser user = null;
        if (userList.size() > 1) {
            return ResponseUtil.serious();
        } else if (userList.size() == 0) {
            return ResponseUtil.badArgumentValue();
        } else {
            user = userList.get(0);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPassword())) {
            return ResponseUtil.fail(WxResponseCode.AUTH_INVALID_ACCOUNT, "账号密码不对");
        }

        // userInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setNickName(username);
        userInfo.setAvatarUrl(user.getAvatar());

        // token
        UserToken userToken = UserTokenManager.generateToken(user.getId());

        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("token", userToken.getToken());
        result.put("tokenExpire", userToken.getExpireTime().toString());
        result.put("userInfo", userInfo);
        return ResponseUtil.ok(result);
    }

    /**
     * 微信登录
     *
     * @param wxLoginInfo 请求内容，{ code: xxx, userInfo: xxx }
     * @param request     请求对象
     * @return 登录结果
     */
    @ApiOperation(value = "微信登录",response = ResponseUtil.class,notes = "tokenExpire:过期时间")
    @PostMapping("login_by_weixin")
    public Object loginByWeixin(@RequestBody @ApiParam(name = "wxLoginInfo",required = true,value = "微信的用户信息") WxLoginInfo wxLoginInfo, HttpServletRequest request) {
        String code = wxLoginInfo.getCode();
        UserInfo userInfo = wxLoginInfo.getUserInfo();
        if (code == null || userInfo == null) {
            return ResponseUtil.badArgument();
        }
        String sessionKey = null;
        String openId = null;
        try {
            WxMaJscode2SessionResult result = this.wxService.getUserService().getSessionInfo(code);
            sessionKey = result.getSessionKey();
            openId = result.getOpenid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sessionKey == null || openId == null) {
            return ResponseUtil.fail();
        }
        LitemallUser user = userService.queryByOid(openId);
        if (user == null) {
            user = new LitemallUser();
            user.setUsername(openId);
            user.setPassword(openId);
            user.setWeixinOpenid(openId);
            user.setAvatar(userInfo.getAvatarUrl());
            user.setNickname(userInfo.getNickName());
            user.setGender(userInfo.getGender());
            user.setUserLevel((byte) vipLevelService.getLowLevelId());
            user.setStatus((byte) 0);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(IpUtil.getIpAddr(request));
            user.setLevel(1);
            user.setVipIndex("0");
            userService.add(user);
        } else {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(IpUtil.getIpAddr(request));
            if (userService.updateById(user) == 0) {
                return ResponseUtil.updatedDataFailed();
            }
        }


        // token
        UserToken userToken = UserTokenManager.generateToken(user.getId());
        userToken.setSessionKey(sessionKey);

        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("isBindMobile",true);
        if (StringUtil.isEmpty(user.getMobile())){
            result.put("isBindMobile",false);
        }
        result.put("token", userToken.getToken());
        result.put("tokenExpire", userToken.getExpireTime().toString());
        result.put("userInfo", userInfo);
        return ResponseUtil.ok(result);
    }


    /**
     * 请求验证码
     *
     * @param body 手机号码{mobile}
     * @return
     */
    @PostMapping("regCaptcha")
    @ApiOperation(value = "请求验证码",response = ResponseUtil.class,notes = "")
    public Object registerCaptcha(@ApiParam(name = "body",value = "mobile:xxx")@RequestBody String body) {
        String phoneNumber = JacksonUtil.parseString(body, "mobile");
        if (StringUtils.isEmpty(phoneNumber)) {
            return ResponseUtil.badArgument();
        }
        if (!RegexUtil.isMobileExact(phoneNumber)) {
            return ResponseUtil.badArgumentValue();
        }

        if (!notifyService.isSmsEnable()) {
            return ResponseUtil.fail(WxResponseCode.AUTH_CAPTCHA_UNSUPPORT, "小程序后台验证码服务不支持");
        }

        String code = CharUtil.getRandomNum(6);
        boolean successful = CaptchaCodeManager.addToCache(phoneNumber, code);
        if (!successful) {
            return ResponseUtil.fail(WxResponseCode.AUTH_CAPTCHA_FREQUENCY, "验证码未超时1分钟，不能发送");
        }
        notifyService.notifySmsTemplate(phoneNumber, NotifyType.SHUIGUO, new String[]{code,"1"});
        return ResponseUtil.ok();
    }




    @PostMapping("bindMobile")
    @ApiOperation(value = "绑定手机号",response = ResponseUtil.class,notes = "")
    public Object bindMobile(@ApiParam(name = "body",value = "mobile: xxx\n" +
            "                     code: xxx\nauthCode:xxx") @RequestBody String body){
        String mobile = JacksonUtil.parseString(body,"mobile");
        String code = JacksonUtil.parseString(body,"code");
        String authCode = JacksonUtil.parseString(body,"authCode");
        if (StringUtil.isEmpty(mobile) || StringUtil.isEmpty(code) || StringUtil.isEmpty(authCode)){
            return ResponseUtil.badArgument();
        }
        if (CaptchaCodeManager.getCachedCaptcha(mobile).equals(authCode)){
            String errmsg = userService.bindMobile(mobile,code);
            if (errmsg.equals("成功")){
                return ResponseUtil.ok();
            }else{
                return ResponseUtil.fail(007,errmsg);
            }
        }else {
            return ResponseUtil.fail(231,"验证码输入错误");
        }
    }

    @PostMapping("verifyMobile")
    @ApiOperation(value = "修改时判断验证码是否正确",response = ResponseUtil.class,notes = "")
    public Object verify(@LoginUser @ApiIgnore Integer userId, @ApiParam(name = "body",value = "mobile: xxx\n" +
            "                     authCode:xxx") @RequestBody String body){
        String mobile = JacksonUtil.parseString(body,"mobile");
        String authCode = JacksonUtil.parseString(body,"authCode");
        if (StringUtil.isEmpty(mobile)|| StringUtil.isEmpty(authCode)){
            return ResponseUtil.badArgument();
        }
        if (userId == null)return ResponseUtil.unlogin();
        if (CaptchaCodeManager.getCachedCaptcha(mobile).equals(authCode)){
            CaptchaCodeManager.addUpdateMobil(mobile);
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.fail(231,"验证码输入错误");
        }
    }


    @PostMapping("updateMobile")
    @ApiOperation(value = "修改手机号",response = ResponseUtil.class,notes = "")
    public Object bindMobile(@LoginUser @ApiIgnore Integer userId,@ApiParam(name = "body",value = "mobile: xxx\n" +
            "                     authCode:xxx")@RequestBody String body){
        String mobile = JacksonUtil.parseString(body,"mobile");
        String authCode = JacksonUtil.parseString(body,"authCode");
        if (StringUtil.isEmpty(mobile) || StringUtil.isEmpty(authCode)){
            return ResponseUtil.badArgument();
        }
        LitemallUser user = userService.findById(userId);
        if (CaptchaCodeManager.isUpdateMobile(user.getMobile())){
            if (CaptchaCodeManager.getCachedCaptcha(mobile).equals(authCode)){
                user.setMobile(mobile);
                userService.updateMobile(user);
                return ResponseUtil.ok();
            }else{
                return ResponseUtil.fail(231,"验证码输入错误");
            }
        }else{
            return ResponseUtil.fail(231,"修改时间过期");
        }
    }








    /**
     * 账号注册
     *
     * @param body    请求内容
     *                {
     *                username: xxx,
     *                password: xxx,
     *                mobile: xxx
     *                code: xxx
     *                }
     *                其中code是手机验证码，目前还不支持手机短信验证码
     * @param request 请求对象
     * @return 登录结果
     * 成功则
     * {
     * errno: 0,
     * errmsg: '成功',
     * data:
     * {
     * token: xxx,
     * tokenExpire: xxx,
     * userInfo: xxx
     * }
     * }
     * 失败则 { errno: XXX, errmsg: XXX }
     */
    //@PostMapping("register")
    public Object register(@RequestBody String body, HttpServletRequest request) {
        String username = JacksonUtil.parseString(body, "username");
        String password = JacksonUtil.parseString(body, "password");
        String mobile = JacksonUtil.parseString(body, "mobile");
        String code = JacksonUtil.parseString(body, "code");
        String wxCode = JacksonUtil.parseString(body, "wxCode");
        String parentPhone = JacksonUtil.parseString(body, "parentId");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(wxCode) || StringUtils.isEmpty(code)) {
            return ResponseUtil.badArgument();
        }

        List<LitemallUser> userList = userService.queryByUsername(username);
        if (userList.size() > 0) {
            return ResponseUtil.fail(WxResponseCode.AUTH_NAME_REGISTERED, "用户名已注册");
        }

        userList = userService.queryByMobile(mobile);
        if (userList.size() > 0) {
            return ResponseUtil.fail(WxResponseCode.AUTH_MOBILE_REGISTERED, "手机号已注册");
        }
        if (!RegexUtil.isMobileExact(mobile)) {
            return ResponseUtil.fail(WxResponseCode.AUTH_INVALID_MOBILE, "手机号格式不正确");
        }
        //判断验证码是否正确
        String cacheCode = CaptchaCodeManager.getCachedCaptcha(mobile);
        if (cacheCode == null || cacheCode.isEmpty() || !cacheCode.equals(code)) {
            return ResponseUtil.fail(WxResponseCode.AUTH_CAPTCHA_UNMATCH, "验证码错误");
        }

        String openId = null;
        try {
            WxMaJscode2SessionResult result = this.wxService.getUserService().getSessionInfo(wxCode);
            openId = result.getOpenid();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.fail(WxResponseCode.AUTH_OPENID_UNACCESS, "openid 获取失败");
        }
        userList = userService.queryByOpenid(openId);
        if (userList.size() > 1) {
            return ResponseUtil.serious();
        }
        if (userList.size() == 1) {
            LitemallUser checkUser = userList.get(0);
            String checkUsername = checkUser.getUsername();
            String checkPassword = checkUser.getPassword();
            if (!checkUsername.equals(openId) || !checkPassword.equals(openId)) {
                return ResponseUtil.fail(WxResponseCode.AUTH_OPENID_BINDED, "openid已绑定账号");
            }
        }

        LitemallUser litemallUser = null;
        if(!StringUtils.isEmpty(parentPhone)){
            litemallUser = userService.selectByMobile(parentPhone);
        }

        LitemallUser user = null;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        user = new LitemallUser();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setMobile(mobile);
        user.setWeixinOpenid(openId);
        user.setAvatar("https://yanxuan.nosdn.127.net/80841d741d7fa3073e0ae27bf487339f.jpg?imageView&quality=90&thumbnail=64x64");
        user.setNickname(username);
        //设置分销数据
        if(null != litemallUser){
            user.setParentId(litemallUser.getParentId());
            user.setLevel(litemallUser.getLevel()+1);
            LitemallSpreadclass spreadclass = spreadClassService.findByLevel(user.getLevel());
            if(null != spreadclass) {
                user.setProfitPertage(new BigDecimal(spreadclass.getPertage()));
            }
        }
        user.setGender((byte) 0);
        user.setUserLevel((byte) 0);
        user.setStatus((byte) 0);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(IpUtil.getIpAddr(request));
        user.setVipIndex("0");
        user.setLevel(0);
        userService.add(user);

        // 给新用户发送注册优惠券
        couponAssignService.assignForRegister(user.getId());

        // userInfo
        UserInfo userInfo = new UserInfo();
        userInfo.setNickName(username);
        userInfo.setAvatarUrl(user.getAvatar());

        // token
        UserToken userToken = UserTokenManager.generateToken(user.getId());

        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("token", userToken.getToken());
        result.put("tokenExpire", userToken.getExpireTime().toString());
        result.put("userInfo", userInfo);
        return ResponseUtil.ok(result);
    }

    /**
     * 账号密码重置
     *
     * @param body    请求内容
     *                {
     *                password: xxx,
     *                mobile: xxx
     *                code: xxx
     *                }
     *                其中code是手机验证码，目前还不支持手机短信验证码
     * @param request 请求对象
     * @return 登录结果
     * 成功则 { errno: 0, errmsg: '成功' }
     * 失败则 { errno: XXX, errmsg: XXX }
     */
    //@PostMapping("reset")
    public Object reset(@RequestBody String body, HttpServletRequest request) {
        String password = JacksonUtil.parseString(body, "password");
        String mobile = JacksonUtil.parseString(body, "mobile");
        String code = JacksonUtil.parseString(body, "code");

        if (mobile == null || code == null || password == null) {
            return ResponseUtil.badArgument();
        }

        //判断验证码是否正确
        String cacheCode = CaptchaCodeManager.getCachedCaptcha(mobile);
        if (cacheCode == null || cacheCode.isEmpty() || !cacheCode.equals(code))
            return ResponseUtil.fail(WxResponseCode.AUTH_CAPTCHA_UNMATCH, "验证码错误");

        List<LitemallUser> userList = userService.queryByMobile(mobile);
        LitemallUser user = null;
        if (userList.size() > 1) {
            return ResponseUtil.serious();
        } else if (userList.size() == 0) {
            return ResponseUtil.fail(WxResponseCode.AUTH_MOBILE_UNREGISTERED, "手机号未注册");
        } else {
            user = userList.get(0);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(password);
        user.setPassword(encodedPassword);

        if (userService.updateById(user) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        return ResponseUtil.ok();
    }

   // @PostMapping("bindPhone")
    public Object bindPhone(@LoginUser Integer userId, @RequestBody String body) {
        String sessionKey = UserTokenManager.getSessionKey(userId);
        String encryptedData = JacksonUtil.parseString(body, "encryptedData");
        String iv = JacksonUtil.parseString(body, "iv");
        WxMaPhoneNumberInfo phoneNumberInfo = this.wxService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
        String phone = phoneNumberInfo.getPhoneNumber();
        List<LitemallUser>userList = userService.queryByMobile(phone);
        if (userList.size() > 0) {
            return ResponseUtil.fail(WxResponseCode.AUTH_MOBILE_REGISTERED, "手机号已绑定");
        }
        LitemallUser user = userService.findById(userId);
        user.setMobile(phone);
        if (userService.updateById(user) == 0) {
            return ResponseUtil.updatedDataFailed();
        }
        return ResponseUtil.ok();
    }

    //@PostMapping("logout")
    public Object logout(@LoginUser Integer userId) {
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        UserTokenManager.removeToken(userId);
        return ResponseUtil.ok();
    }

    @PostMapping("loginToken")
    @ApiOperation(value = "获取userId为9的用户token",response=UserToken.class)
    public Object login(
            @ApiParam(name = "body") @RequestBody String body
    ){
        // token
        Integer id = JacksonUtil.parseInteger(body,"userId");
        UserToken userToken = UserTokenManager.generateToken(id);
        return userToken;
    }
}
