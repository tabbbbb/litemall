package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.dao.LitemallBenefitMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LitemallBenefitService {

    @Resource
    private LitemallBenefitMapper benefitMapper;

}
