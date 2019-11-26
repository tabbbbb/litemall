package com.lhcode.litemall.db.service;

import com.lhcode.litemall.db.dao.LitemallGoodsSpecificationMapper;
import com.lhcode.litemall.db.domain.LitemallGoods;
import com.lhcode.litemall.db.domain.LitemallGoodsSpecification;
import com.lhcode.litemall.db.domain.LitemallGoodsSpecificationExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LitemallGoodsSpecificationService {
    @Resource
    private LitemallGoodsSpecificationMapper goodsSpecificationMapper;

    @Resource
    private LitemallVipClassService vipService;

    public List<LitemallGoodsSpecification> queryByGid(Integer id) {
        LitemallGoodsSpecificationExample example = new LitemallGoodsSpecificationExample();
        example.or().andGoodsIdEqualTo(id).andDeletedEqualTo(false);
        return goodsSpecificationMapper.selectByExample(example);
    }

    public List<LitemallGoodsSpecification> queryByGid(Integer userId,Integer goodsId){
        LitemallGoodsSpecificationExample example = new LitemallGoodsSpecificationExample();
        example.or().andGoodsIdEqualTo(goodsId).andDeletedEqualTo(false);
        String discount = "1";
        if (userId != null){
            discount = vipService.getUserLevel(userId);
        }
        List<LitemallGoodsSpecification> specificationList = goodsSpecificationMapper.selectByExample(example);
        for (LitemallGoodsSpecification specification : specificationList) {
            this.goodSpecPriceChoose(discount,specification);
        }
        return specificationList;
    }

    public LitemallGoodsSpecification findById(Integer userId,Integer id) {
        String discount = "1";

        if (userId != null){
            discount = vipService.getUserLevel(userId);
        }
        LitemallGoodsSpecification goodsSpecification = goodsSpecificationMapper.selectByPrimaryKey(id);
        goodSpecPriceChoose(discount,goodsSpecification);
        return goodsSpecification;
    }


    private void goodSpecPriceChoose(String discount,LitemallGoodsSpecification goodsSpec){
        if (discount.equals("1") ){
            goodsSpec.setPrice(goodsSpec.getOnePrice());
        }else if (discount.equals("2")){
            goodsSpec.setPrice(goodsSpec.getTwoPrice());
        }else {
            goodsSpec.setPrice(goodsSpec.getThreePrice());
        }
    }



    public void deleteByGid(Integer gid) {
        LitemallGoodsSpecificationExample example = new LitemallGoodsSpecificationExample();
        example.or().andGoodsIdEqualTo(gid);
        goodsSpecificationMapper.logicalDeleteByExample(example);
    }

    public void add(LitemallGoodsSpecification goodsSpecification) {
        goodsSpecification.setAddTime(LocalDateTime.now());
        goodsSpecification.setUpdateTime(LocalDateTime.now());
        goodsSpecificationMapper.insertSelective(goodsSpecification);
    }

    public void updateIsDefault(Integer goodsId){
        goodsSpecificationMapper.updateIsDefault(goodsId);
    }

    /**
     * [
     * {
     * name: '',
     * valueList: [ {}, {}]
     * },
     * {
     * name: '',
     * valueList: [ {}, {}]
     * }
     * ]
     *
     * @param id
     * @return
     */
    public Object getSpecificationVoList(Integer id) {
        List<LitemallGoodsSpecification> goodsSpecificationList = queryByGid(id);

        Map<String, VO> map = new HashMap<>();
        List<VO> specificationVoList = new ArrayList<>();

        for (LitemallGoodsSpecification goodsSpecification : goodsSpecificationList) {
            String specification = goodsSpecification.getSpecification();
            VO goodsSpecificationVo = map.get(specification);
            if (goodsSpecificationVo == null) {
                goodsSpecificationVo = new VO();
                goodsSpecificationVo.setName(specification);
                List<LitemallGoodsSpecification> valueList = new ArrayList<>();
                valueList.add(goodsSpecification);
                goodsSpecificationVo.setValueList(valueList);
                map.put(specification, goodsSpecificationVo);
                specificationVoList.add(goodsSpecificationVo);
            } else {
                List<LitemallGoodsSpecification> valueList = goodsSpecificationVo.getValueList();
                valueList.add(goodsSpecification);
            }
        }

        return specificationVoList;
    }

    private class VO {
        private String name;
        private List<LitemallGoodsSpecification> valueList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<LitemallGoodsSpecification> getValueList() {
            return valueList;
        }

        public void setValueList(List<LitemallGoodsSpecification> valueList) {
            this.valueList = valueList;
        }
    }


    /**
     * 首页默认的规格
     */
    public LitemallGoodsSpecification getSpecByGoodsSn(Integer goodsId){
        LitemallGoodsSpecificationExample example = new LitemallGoodsSpecificationExample();
        LitemallGoodsSpecificationExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId).andDeletedEqualTo(false).andIsDefault(1);
        return goodsSpecificationMapper.selectOneByExample(example);
    }

    public int update(LitemallGoodsSpecification goodsSpecification){
        return  goodsSpecificationMapper.updateByPrimaryKeySelective(goodsSpecification);
    }


}
