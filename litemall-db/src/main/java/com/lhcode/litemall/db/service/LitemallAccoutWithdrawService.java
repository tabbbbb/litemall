package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.dao.LitemallAccountWithdrawMapper;
import com.lhcode.litemall.db.domain.LitemallAccountWithdrawExample;
import com.lhcode.litemall.db.domain.LitemallAccountWithdraw;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LitemallAccoutWithdrawService {

    @Resource
    private LitemallAccountWithdrawMapper accountWithdrawMapper;

    /**
     * 查询提现总额
     * @param userId
     * @return
     */
    public BigDecimal sumByUserId(Integer userId){
        Map param = new HashMap();
        param.put("userId",userId);
        param.put("status",1);
        return accountWithdrawMapper.selectSumMoney(param);
    }

    /**
     * 查询提现明细
     * @param userId
     * @return
     */
    public List<LitemallAccountWithdraw> withdrawalList(Integer userId,Integer page, Integer size){
        LitemallAccountWithdrawExample example = new LitemallAccountWithdrawExample();
        example.or().andUserIdEqualTo(userId);
        PageHelper.startPage(page, size);
        return accountWithdrawMapper.selectByExample(example);
    }

}
