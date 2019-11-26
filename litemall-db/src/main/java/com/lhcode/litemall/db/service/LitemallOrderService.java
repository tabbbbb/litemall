package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.domain.*;
import com.github.pagehelper.PageHelper;
import com.lhcode.litemall.db.dao.LitemallOrderMapper;
import com.lhcode.litemall.db.dao.OrderMapper;
import com.lhcode.litemall.db.util.OrderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LitemallOrderService {
    @Resource
    private LitemallOrderMapper litemallOrderMapper;
    @Resource
    private OrderMapper orderMapper;

    @Autowired
    private LitemallUserService userService;

    @Autowired
    private LitemallOrderGoodsService orderGoodsService;

    @Autowired
    private LitemallGoodsSpecificationService goodsSpecificationService;

    public int add(LitemallOrder order) {
        order.setAddTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return litemallOrderMapper.insertSelective(order);
    }

    public int count(Integer userId) {
        LitemallOrderExample example = new LitemallOrderExample();
        example.or().andUserIdEqualTo(userId).andDeletedEqualTo(false);
        return (int) litemallOrderMapper.countByExample(example);
    }

    public LitemallOrder findById(Integer orderId) {
        return litemallOrderMapper.selectByPrimaryKey(orderId);
    }

    private String getRandomNum(Integer num) {
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public int countByOrderSn(Integer userId, String orderSn) {
        LitemallOrderExample example = new LitemallOrderExample();
        example.or().andUserIdEqualTo(userId).andOrderSnEqualTo(orderSn).andDeletedEqualTo(false);
        return (int) litemallOrderMapper.countByExample(example);
    }

    public int countByOrderStatus(Integer userId,Short orderStatus){
        LitemallOrderExample example = new LitemallOrderExample();
        example.or().andUserIdEqualTo(userId).andOrderStatusEqualTo(orderStatus).andDeletedEqualTo(false);
        return (int) litemallOrderMapper.countByExample(example);
    }

    // TODO 这里应该产生一个唯一的订单，但是实际上这里仍然存在两个订单相同的可能性
    public String generateOrderSn(Integer userId) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
        String now = df.format(LocalDate.now());
        String orderSn = now + getRandomNum(6);
        while (countByOrderSn(userId, orderSn) != 0) {
            orderSn = getRandomNum(6);
        }
        return orderSn;
    }

    public List<LitemallOrder> queryByOrderStatus(Integer userId, List<Short> orderStatus, Integer page, Integer size) {
        LitemallOrderExample example = new LitemallOrderExample();
        example.setOrderByClause(LitemallOrder.Column.addTime.desc());
        LitemallOrderExample.Criteria criteria = example.or();
        criteria.andUserIdEqualTo(userId);
        if (orderStatus != null) {
            criteria.andOrderStatusIn(orderStatus);
        }
        criteria.andDeletedEqualTo(false);
        PageHelper.startPage(page, size);
        return litemallOrderMapper.selectByExample(example);
    }

    public List<LitemallOrder> querySelective(Integer adminId,String nickname, String orderSn, List<Short> orderStatusArray, Integer page, Integer size, String sort, String order) {
        LitemallOrderExample example = new LitemallOrderExample();
        LitemallOrderExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(nickname)) {
            criteria.andNicknameLike("%"+nickname+"%");
        }
        if (!StringUtils.isEmpty(orderSn)) {
            criteria.andOrderSnEqualTo(orderSn);
        }
        if (orderStatusArray != null && orderStatusArray.size() != 0) {
            criteria.andOrderStatusIn(orderStatusArray);
        }

        if (adminId != null && adminId != -1) {
            criteria.andAdminIdEqualTo(adminId);
        }


        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }
        PageHelper.startPage(page, size);

        return litemallOrderMapper.selectByAdminId(example);
    }

    public int updateWithOptimisticLocker(LitemallOrder order) {
        LocalDateTime preUpdateTime = order.getUpdateTime();
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.updateWithOptimisticLocker(preUpdateTime, order);
    }

    public void deleteById(Integer id) {
        litemallOrderMapper.logicalDeleteByPrimaryKey(id);
    }

    public int count() {
        LitemallOrderExample example = new LitemallOrderExample();
        example.or().andDeletedEqualTo(false);
        return (int) litemallOrderMapper.countByExample(example);
    }

    public List<LitemallOrder> queryUnpaid(int minutes) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.minusMinutes(minutes);
        LitemallOrderExample example = new LitemallOrderExample();
        example.or().andOrderStatusEqualTo(OrderUtil.STATUS_CREATE).andAddTimeLessThan(expired).andDeletedEqualTo(false);
        return litemallOrderMapper.selectByExample(example);
    }

    public List<LitemallOrder> queryUnconfirm(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.minusDays(days);
        LitemallOrderExample example = new LitemallOrderExample();
        example.or().andOrderStatusEqualTo(OrderUtil.STATUS_SHIP).andShipTimeLessThan(expired).andDeletedEqualTo(false);
        return litemallOrderMapper.selectByExample(example);
    }

    public LitemallOrder findBySn(String orderSn) {
        LitemallOrderExample example = new LitemallOrderExample();
        example.or().andOrderSnEqualTo(orderSn).andDeletedEqualTo(false);
        return litemallOrderMapper.selectOneByExample(example);
    }





    public List<LitemallOrder> queryComment(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.minusDays(days);
        LitemallOrderExample example = new LitemallOrderExample();
        example.or().andCommentsGreaterThan((short) 0).andConfirmTimeLessThan(expired).andDeletedEqualTo(false);
        return litemallOrderMapper.selectByExample(example);
    }


    public int updateOrder(LitemallOrder order){
        return litemallOrderMapper.updateByPrimaryKeySelective(order);
    }



    public List<LitemallOrder> getOrderAll(Integer userId,List<Short> status,Integer page,Integer size){
        LitemallOrderExample example = new LitemallOrderExample();
        if (status.size() == 0){
            LitemallOrderExample.Criteria criteria = example.or();
            criteria.andUserIdEqualTo(userId);
            criteria.andDeletedEqualTo(false);
        }else{
            for (Short aShort : status) {
                LitemallOrderExample.Criteria criteria = example.or();
                criteria.andUserIdEqualTo(userId);
                criteria.andOrderStatusEqualTo(aShort);
                criteria.andDeletedEqualTo(false);
            }
        }
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(page,size);
        return litemallOrderMapper.selectByExample(example);
    }


    public List<LitemallOrder> getOrderStatus(Short status){
        LitemallOrderExample example = new LitemallOrderExample();
        LitemallOrderExample.Criteria criteria = example.createCriteria();
        criteria.andOrderStatusEqualTo(status).andDeletedEqualTo(false);
        return litemallOrderMapper.selectByExample(example);
    }

    /**
     * 将库存返还到商品规格中去
     * @param orderId
     */
    public void cancelOrder(Integer orderId){
        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(orderId);
        for (LitemallOrderGoods orderGoods : orderGoodsList) {
            LitemallGoodsSpecification goodsSpecification = goodsSpecificationService.findById(null, Integer.valueOf(orderGoods.getSpecifications()));
            goodsSpecification.setNumber(goodsSpecification.getNumber()+orderGoods.getNumber());
            goodsSpecificationService.update(goodsSpecification);
        }
    }
}
