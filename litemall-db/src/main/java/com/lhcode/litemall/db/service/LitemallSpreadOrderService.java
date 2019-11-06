package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.dao.LitemallSpreadOrderMapper;
import com.lhcode.litemall.db.domain.*;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LitemallSpreadOrderService {

    @Resource
    private LitemallSpreadOrderMapper spreadOrderMapper;

    /**
     * 查询累计推广订单
     * @param userId
     * @return
     */
    public List<LitemallSpreadOrder> spreadOrderList(Integer userId,Integer page, Integer size){
        LitemallSpreadOrderExample example = new LitemallSpreadOrderExample();
        example.or().andUserIdEqualTo(userId);
        PageHelper.startPage(page, size);
        return spreadOrderMapper.selectByExample(example);
    }

    /**
     * 查询用户佣金总计
     * @param userId
     * @param sign
     * @return
     */
    public BigDecimal userBenefit(Integer userId, String sign){
        Map params = new HashMap();
        params.put("userId",userId);
        params.put("sign",sign);
        params.put("status",3);
        return spreadOrderMapper.userBenfit(params);
    }

    /**
     * 查询佣金明细
     * @param userId
     * @param page
     * @param size
     * @return
     */
    public List<LitemallSpreadOrder> benefitList(Integer userId, Integer page, Integer size){
        LitemallSpreadOrderExample example = new LitemallSpreadOrderExample();
        example.or().andUserIdEqualTo(userId);
        PageHelper.startPage(page, size);
        return spreadOrderMapper.selectByExample(example);
    }

    /**
     * 添加分销订单
     * @param spreadOrder
     * @return
     */
    public int add(LitemallSpreadOrder spreadOrder) {
        spreadOrder.setAddTime(LocalDateTime.now());
        return spreadOrderMapper.insertSelective(spreadOrder);
    }

    /**
     * 根据订单号查询分销记录
     * @param orderId
     * @return
     */
    public LitemallSpreadOrder findByOrderId(String orderId){
        LitemallSpreadOrderExample example = new LitemallSpreadOrderExample();
        example.or().andOrderIdEqualTo(orderId);
        return spreadOrderMapper.selectOneByExample(example);
    }

    /**
     * 更新分销订单状态
     * @param spreadOrder
     * @return
     */
    public int updateWithOptimisticLocker(LitemallSpreadOrder spreadOrder) {
        spreadOrder.setConfirmTime(LocalDateTime.now());
        return spreadOrderMapper.updateByPrimaryKeySelective(spreadOrder);
    }

}
