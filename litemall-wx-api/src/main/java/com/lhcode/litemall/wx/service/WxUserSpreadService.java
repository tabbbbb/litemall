package com.lhcode.litemall.wx.service;

import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallAccountWithdraw;
import com.lhcode.litemall.db.domain.LitemallBenefit;
import com.lhcode.litemall.db.domain.LitemallSpreadOrder;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.service.LitemallAccoutWithdrawService;
import com.lhcode.litemall.db.service.LitemallBenefitService;
import com.lhcode.litemall.db.service.LitemallSpreadOrderService;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.lhcode.litemall.wx.util.AuthUtil;
import com.lhcode.litemall.wx.util.CertHttpUtil;
import com.github.pagehelper.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 我的推广 服务
 */
@Service
public class WxUserSpreadService {

    @Autowired
    private LitemallUserService userService;

    @Autowired
    private LitemallAccoutWithdrawService accoutWithdrawService;

    @Autowired
    private LitemallSpreadOrderService spreadOrderService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Log logger = LogFactory.getLog(WxUserSpreadService.class);

    /**
     * 查询我的推广页面下相关信息
     * @param userId
     * @return
     */
    public Object spread(Integer userId){
        BigDecimal withdrawMoney = accoutWithdrawService.sumByUserId(userId);
        BigDecimal userBenefit = spreadOrderService.userBenefit(userId,"all");
        BigDecimal yesterdayUserBenefit = spreadOrderService.userBenefit(userId,"yesterday");
        Map<String, Object> result = new HashMap<>();
        result.put("withdrawMoney", BigDecimal.ZERO.equals(withdrawMoney)?0:withdrawMoney);
        result.put("userBenefit", BigDecimal.ZERO.equals(userBenefit)?0:userBenefit);
        result.put("yesterdayUserBenefit", BigDecimal.ZERO.equals(yesterdayUserBenefit)?0:yesterdayUserBenefit);
        return ResponseUtil.ok(result);
    }

    /**
     * 查询分销推广人员列表
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public Object userList(Integer userId,Integer page, Integer size){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        List<LitemallUser> litemallUsers = userService.queryByParentId(userId,page,size);
        long count = PageInfo.of(litemallUsers).getTotal();
        int totalPages = (int) Math.ceil((double) count / size);
        List<Map<String, Object>> userList = new ArrayList<>(litemallUsers.size());
        for (LitemallUser user : litemallUsers) {
            Map<String, Object> userVo = new HashMap<>();
            userVo.put("id", user.getId());
            userVo.put("avatar",user.getAvatar());
            userVo.put("nickName",user.getNickname());
            userVo.put("time",formatter.format(user.getAddTime()));
            userVo.put("numberCount",user.getBalance());
            userList.add(userVo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("data", userList);
        result.put("totalPages", totalPages);
        return ResponseUtil.ok(result);
    }

    /**
     * 查询分销订单列表
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public Object orderList(Integer userId, Integer page,Integer size){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        List<LitemallSpreadOrder> spreadOrders = spreadOrderService.spreadOrderList(userId,page,size);
        long count = PageInfo.of(spreadOrders).getTotal();
        int totalPages = (int) Math.ceil((double) count / size);
        List<Map<String, Object>> spreadOrderList = new ArrayList<>(spreadOrders.size());
        for (LitemallSpreadOrder spreadOrder : spreadOrders) {
            Map<String, Object> spreadOrderVo = new HashMap<>();
            spreadOrderVo.put("id", spreadOrder.getId());
            spreadOrderVo.put("orderSn",spreadOrder.getOrderSn());
            spreadOrderVo.put("nickName",spreadOrder.getNickname());
            spreadOrderVo.put("time",formatter.format(spreadOrder.getAddTime()));
            spreadOrderVo.put("money",spreadOrder.getMoney());
            spreadOrderList.add(spreadOrderVo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("data", spreadOrderList);
        result.put("totalPages", totalPages);
        return ResponseUtil.ok(result);
    }

    /**
     * 查询提现记录
     * @param userId
     * @return
     */
    public Object withdrawal(Integer userId, Integer page,Integer size){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        BigDecimal withdrawMoney = accoutWithdrawService.sumByUserId(userId);
        List<LitemallAccountWithdraw> withdrawList = accoutWithdrawService.withdrawalList(userId,page,size);
        long count = PageInfo.of(withdrawList).getTotal();
        int totalPages = (int) Math.ceil((double) count / size);
        List<Map<String, Object>> withdraws = new ArrayList<>(withdrawList.size());
        for (LitemallAccountWithdraw accountWithdraw : withdrawList) {
            Map<String, Object> accountWithdrawVo = new HashMap<>();
            accountWithdrawVo.put("id", accountWithdraw.getId());
            accountWithdrawVo.put("addTime", formatter.format(accountWithdraw.getAddTime()));
            accountWithdrawVo.put("channel", accountWithdraw.getChannel());
            accountWithdrawVo.put("amount", accountWithdraw.getAmount());
            accountWithdrawVo.put("status", withDrawalStatus(accountWithdraw.getStatus()));
            withdraws.add(accountWithdrawVo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("withdrawMoney",BigDecimal.ZERO.equals(withdrawMoney)?0:withdrawMoney);
        result.put("count", count);
        result.put("data", withdraws);
        result.put("totalPages", totalPages);
        return ResponseUtil.ok(result);
    }

    /**
     * 查询佣金明细
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public Object benefit(Integer userId,Integer page,Integer size){
        if (userId == null) {
            return ResponseUtil.unlogin();
        }
        List<LitemallSpreadOrder> benefits = spreadOrderService.benefitList(userId,page,size);
        BigDecimal userBenefit = spreadOrderService.userBenefit(userId,"all");
        long count = PageInfo.of(benefits).getTotal();
        int totalPages = (int) Math.ceil((double) count / size);
        List<Map<String, Object>> benefitList = new ArrayList<>(benefits.size());
        for (LitemallSpreadOrder spreadOrder : benefits) {
            Map<String, Object> spreadOrderVo = new HashMap<>();
            spreadOrderVo.put("id", spreadOrder.getId());
            spreadOrderVo.put("nickName", spreadOrder.getNickname());
            spreadOrderVo.put("orderSn", spreadOrder.getOrderSn());
            spreadOrderVo.put("goodsPrice", spreadOrder.getGoodsPrice());
            spreadOrderVo.put("money", spreadOrder.getMoney());
            spreadOrderVo.put("addTime", formatter.format(spreadOrder.getAddTime()));
            benefitList.add(spreadOrderVo);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("userBenefit",BigDecimal.ZERO.equals(userBenefit) ?0:userBenefit);
        result.put("count", count);
        result.put("data", benefitList);
        result.put("totalPages", totalPages);
        return ResponseUtil.ok(result);
    }

    private String withDrawalStatus(Integer status){
        if (0==status){
            return "提现中";
        }
        else if (1==status)
            return "提现成功";
        else
            return "提现失败";

    }

    /**
     * 查询用户可提现金额
     * @param userId
     * @return
     */
    public Object currentMoney (Integer userId){
        BigDecimal withdrawMoney = accoutWithdrawService.sumByUserId(userId);
        BigDecimal userBenefit = spreadOrderService.userBenefit(userId,"all");
        Map<String, Object> result = new HashMap<>();
        result.put("currentMoney", BigDecimal.ZERO.equals(userBenefit.subtract(withdrawMoney)) ?0:userBenefit.subtract(withdrawMoney));
        return ResponseUtil.ok(result);
    }

    public Object userWithDrawal(Integer userId,String wechatID,String money, HttpServletRequest request){
        // 1.0 拼凑企业支付需要的参数
        String appid = AuthUtil.APPID; // 微信公众号的appid
        String mch_id = AuthUtil.MCHID; // 商户号
        String nonce_str = AuthUtil.createNonceStr(); // 生成随机数
        String partner_trade_no = AuthUtil.createNonceStr(); // 生成商户订单号
        String openid = "用户的OpenId"; // 支付给用户openid
        String check_name = "NO_CHECK"; // 是否验证真实姓名呢
        String amount = money; // 企业付款金额，最少为100，单位为分
        String desc = "佣金收入"; // 企业付款操作说明信息。必填。
        String spbill_create_ip = AuthUtil.getRequestIp(request); // 用户的ip地址

        // 2.0 生成map集合
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("mch_appid", appid); // 微信公众号的appid
        packageParams.put("mchid", mch_id); // 商务号
        packageParams.put("nonce_str", nonce_str); // 随机生成后数字，保证安全性
        packageParams.put("partner_trade_no", partner_trade_no); // 生成商户订单号
        packageParams.put("openid", openid); // 支付给用户openid
        packageParams.put("check_name", check_name); // 是否验证真实姓名呢
        packageParams.put("amount", amount); // 企业付款金额，单位为分
        packageParams.put("desc", desc); // 企业付款操作说明信息。必填。
        packageParams.put("spbill_create_ip", spbill_create_ip); // 调用接口的机器Ip地址

        try {
            // 3.0 利用上面的参数，先去生成自己的签名
            String sign = AuthUtil.Sign(packageParams, AuthUtil.PATERNERKEY);

            // 4.0 将签名再放回map中，它也是一个参数
            packageParams.put("sign", sign);

            // 5.0将当前的map结合转化成xml格式
            String xml = AuthUtil.map2XmlString(packageParams);

            // 6.0获取需要发送的url地址
            String wxUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers"; // 获取退款的api接口

            System.out.println("发送前的xml为：" + xml);

            // 7,向微信发送请求转账请求
            String returnXml = CertHttpUtil.postData(wxUrl, xml, AuthUtil.MCHID, AuthUtil.CERTPATH);

            System.out.println("返回的returnXml为:" + returnXml);

            // 8，将微信返回的xml结果转成map格式
            Map<String, String> returnMap = AuthUtil.readStringXmlOut(returnXml);

            if (returnMap.get("return_code").equals("SUCCESS")) {
                // 付款成功
                return ResponseUtil.ok();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ResponseUtil.serious();
    }

}
