package com.lhcode.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.lhcode.litemall.db.dao.LitemallGoodsMapper;
import com.lhcode.litemall.db.domain.LitemallGoods;
import com.lhcode.litemall.db.domain.LitemallGoods.Column;
import com.lhcode.litemall.db.domain.LitemallGoodsExample;
import com.lhcode.litemall.db.domain.LitemallGoodsSpecification;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LitemallGoodsService {
    Column[] columns = new Column[]{Column.id, Column.name, Column.brief, Column.picUrl, Column.isHot, Column.isNew, Column.counterPrice, Column.retailPrice};
    @Resource
    private LitemallGoodsMapper goodsMapper;


    @Resource
    private LitemallUserService userService;

    @Resource
    private LitemallVipClassService vipService;

    @Resource
    private LitemallGoodsSpecificationService goodsSpecificationService;
    @Resource
    private LitemallCategoryService categoryService;

    /**
     * 获取热卖商品
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<LitemallGoods> queryByHot(int offset, int limit) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIsHotEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(offset, limit);

        return goodsMapper.selectByExampleSelective(example, columns);
    }

    public LitemallGoods queryByGoodsSn(Integer userId,String goodsSn){
        LitemallGoodsExample example = new LitemallGoodsExample();
        LitemallGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsSnEqualTo(goodsSn).andDeletedEqualTo(false);
        LitemallGoods goods = goodsMapper.selectOneByExample(example);
        String discount = "1";
        if (userId != null){
            discount = vipService.getUserLevel(userId);
        }

        this.goodsPriceChoose(discount,goods);
        return goods;
    }

    /**
     * 获取新品上市
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<LitemallGoods> queryByNew(int offset, int limit) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIsNewEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(offset, limit);

        return goodsMapper.selectByExampleSelective(example, columns);
    }

    /**
     * 获取分类下的商品
     *
     * @param catList
     * @param offset
     * @param limit
     * @return
     */
    public List<LitemallGoods> queryByCategory(List<Integer> catList, int offset, int limit) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andCategoryIdIn(catList).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        example.setOrderByClause("add_time  desc");
        PageHelper.startPage(offset, limit);

        return goodsMapper.selectByExampleSelective(example, columns);
    }


    /**
     * 获取分类下的商品
     *
     * @param catId
     * @param offset
     * @param limit
     * @return
     */
    public List<LitemallGoods> queryByCategory(Integer catId, int offset, int limit) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andCategoryIdEqualTo(catId).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(offset, limit);

        return goodsMapper.selectByExampleSelective(example, columns);
    }


    /**
     * 用户的搜索
     * @param address
     * @param catId
     * @param userId
     * @param keywords
     * @param typeId
     * @param offset
     * @param limit
     * @param sort
     * @param order
     * @return
     */
    public List<LitemallGoods> querySelective(String type,String address,Integer catId, Integer userId, String keywords, Integer typeId, Integer offset, Integer limit, String sort, String order) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        String [] addressId = {"-1"};
        if (address != null && address.length() > 0){
            addressId = address.split(",");
        }

        for (int i = 0; i < addressId.length; i++) {
            LitemallGoodsExample.Criteria criteria = example.or();


            if (typeId != null) {
                if (typeId == 1) {
                    criteria.andIsHotEqualTo(true);
                }else if (typeId == 2){
                    criteria.andIsSaleEqualTo(true);
                }else if (typeId == 3){
                    criteria.andIsNewEqualTo(true);
                }
            }

            if (i == 0 && !addressId[i].equals("-1")){
                criteria.andProvinceIdEqualTo(Integer.valueOf(addressId[i]));
            }else if (i == 1){
                criteria.andCityIdEqualTo(Integer.valueOf(addressId[i]));
            }else if (i == 2){
                criteria.andAreaIdEqualTo(Integer.valueOf(addressId[i]));
            }

            if (!StringUtils.isEmpty(keywords)) {
                criteria.andNameLike("%" + keywords + "%");
            }
            criteria.andIsOnSaleEqualTo(true);
            criteria.andDeletedEqualTo(false);
        }
        String discount = "1";
        if (userId != null){
            discount = vipService.getUserLevel(userId);
        }
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            if (sort.equals("price")){
                if (discount.equals("1")){
                    sort = "one_price";
                }else if (discount.equals("2")){
                    sort = "two_price";
                }else{
                    sort = "three_price";
                }
            }
            example.setOrderByClause(sort + " " + order);
        }
        PageHelper.startPage(offset, limit);
        List<LitemallGoods> goodsList = goodsMapper.selectByExampleWithBLOBs(example);

        for (int i = 0; i < goodsList.size(); i++) {
            this.goodsPriceChoose(discount,goodsList.get(i));
            if (type != null && !flagType(type,goodsList.get(i).getId())){
                goodsList.remove(i);
                i--;
            }


        }
        for (int i = 0; i < goodsList.size(); i++) {
            if (!StringUtils.isEmpty(catId) && catId != 0) {
                Integer categoryId = goodsList.get(i).getCategoryId();
                if (!catId.equals(categoryId)){
                    Integer pid = categoryService.findById(categoryId).getPid();
                    if(!pid.equals(catId)){
                        goodsList.remove(i);
                        i--;
                    }

                }
            }
        }
        return goodsList;
    }


    private boolean flagType(String type, Integer goodsId){
        List<LitemallGoodsSpecification> goodsSpecificationList = goodsSpecificationService.queryByGid(goodsId);
        String [] types = type.split(",");
        for (LitemallGoodsSpecification specification : goodsSpecificationList) {
            for (int i = 0; i < types.length; i++) {
                if (specification.getSpecification().indexOf(types[i]) != -1)return true;
            }
        }
        return false;
    }


    /**
     * 管理员查询方法
     * @param isNew
     * @param isHot
     * @param isSale
     * @param goodsSn
     * @param name
     * @param page
     * @param size
     * @param sort
     * @param order
     * @return
     */
    public List<LitemallGoods> querySelective(Boolean isNew,Boolean isHot,Boolean isSale,String goodsSn, String name, Integer page, Integer size, String sort, String order) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        LitemallGoodsExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(goodsSn)) {
            criteria.andGoodsSnEqualTo(goodsSn);
        }
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        criteria.andDeletedEqualTo(false);
        if (isNew!= null && isNew){
            criteria.andIsNewEqualTo(true);
        }else if(isHot!= null && isHot){
            criteria.andIsHotEqualTo(true);
        }else if (isSale!= null && isSale){
            criteria.andIsSaleEqualTo(true);
        }
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }
        PageHelper.startPage(page, size);
        return goodsMapper.selectByExampleWithBLOBs(example);
    }

    /**
     * 获取某个商品信息,包含完整信息
     *
     * @param id
     * @return
     */
    public LitemallGoods findById(Integer id) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIdEqualTo(id).andDeletedEqualTo(false);
        return goodsMapper.selectOneByExampleWithBLOBs(example);
    }


    public LitemallGoods findById(Integer id ,Integer userId){
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIdEqualTo(id).andDeletedEqualTo(false);
        LitemallGoods goods = goodsMapper.selectOneByExampleWithBLOBs(example);
        String discount = "1";
        if (userId != null){
            discount = vipService.getUserLevel(userId);
        }
        this.goodsPriceChoose(discount,goods);
        return goods;
    }


    private void goodsPriceChoose(String discount,LitemallGoods goods){
        if (discount.equals("1") ){
            goods.setPrice(goods.getOnePrice());
        }else if (discount.equals("2")){
            goods.setPrice(goods.getTwoPrice());
        }else {
            goods.setPrice(goods.getThreePrice());
        }
    }

    /**
     * 获取某个商品信息，仅展示相关内容
     *
     * @param id
     * @return
     */
    public LitemallGoods findByIdVO(Integer id) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIdEqualTo(id).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        return goodsMapper.selectOneByExampleSelective(example, columns);
    }


    /**
     * 获取所有在售物品总数
     *
     * @return
     */
    public Integer queryOnSale() {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        return (int) goodsMapper.countByExample(example);
    }

    public int updateById(LitemallGoods goods) {
        goods.setUpdateTime(LocalDateTime.now());
        return goodsMapper.updateByPrimaryKeySelective(goods);
    }

    public void deleteById(Integer id) {
        goodsMapper.logicalDeleteByPrimaryKey(id);
    }

    public void add(LitemallGoods goods) {
        goods.setAddTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.insertSelective(goods);
    }

    /**
     * 获取所有物品总数，包括在售的和下架的，但是不包括已删除的商品
     *
     * @return
     */
    public int count() {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andDeletedEqualTo(false);
        return (int) goodsMapper.countByExample(example);
    }

    public List<Integer> getCatIds( String keywords, Boolean isHot, Boolean isNew) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        LitemallGoodsExample.Criteria criteria1 = example.or();
        LitemallGoodsExample.Criteria criteria2 = example.or();


        if (!StringUtils.isEmpty(isNew)) {
            criteria1.andIsNewEqualTo(isNew);
            criteria2.andIsNewEqualTo(isNew);
        }
        if (!StringUtils.isEmpty(isHot)) {
            criteria1.andIsHotEqualTo(isHot);
            criteria2.andIsHotEqualTo(isHot);
        }
        if (!StringUtils.isEmpty(keywords)) {
            criteria1.andKeywordsLike("%" + keywords + "%");
            criteria2.andNameLike("%" + keywords + "%");
        }
        criteria1.andIsOnSaleEqualTo(true);
        criteria2.andIsOnSaleEqualTo(true);
        criteria1.andDeletedEqualTo(false);
        criteria2.andDeletedEqualTo(false);

        List<LitemallGoods> goodsList = goodsMapper.selectByExampleSelective(example, Column.categoryId);
        List<Integer> cats = new ArrayList<Integer>();
        for (LitemallGoods goods : goodsList) {
            cats.add(goods.getCategoryId());
        }
        return cats;
    }

    public boolean checkExistByName(String name) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andNameEqualTo(name).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        return goodsMapper.countByExample(example) != 0;
    }

    public LitemallGoods getGoodsById(Integer id){
        return goodsMapper.selectByPrimaryKey(id);
    }


    public List<LitemallGoods> getHot(){
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIsHotEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        return goodsMapper.selectByExample(example);
    }

    public List<LitemallGoods> getNew(){
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIsNewEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        return goodsMapper.selectByExample(example);
    }
}
