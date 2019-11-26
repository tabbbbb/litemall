package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.db.domain.LitemallCategory;
import com.lhcode.litemall.wx.service.HomeCacheManager;
import com.lhcode.litemall.wx.vo.CategoryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.service.LitemallCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类目服务
 */
@RestController
@RequestMapping("/wx/catalog")
@Validated
@Api(value = "/wx/catalog",description = "类目")
public class WxCatalogController {
    private final Log logger = LogFactory.getLog(WxCatalogController.class);

    @Autowired
    private LitemallCategoryService categoryService;

    /**
     * 分类id获取所有
     *
     * @param id   分类类目ID。
     *             获取类目和此类目下的类目
     * @return 分类详情
     */
    @GetMapping("index")
    @ApiOperation(value = "获取单个类目及其此类目下的类目",response = CategoryVo.class,notes = "{data:CategoryVo}",nickname = "获取单个类目及其此类目下的类目")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType="query",name="id",value="分类类目Id",dataTypeClass = Integer.class,required = true)
    })
    public Object index(Integer id) {
        //优先从缓存中读取
        if (HomeCacheManager.hasData(HomeCacheManager.CATALOG)) {
            Map<String,Object> map = HomeCacheManager.getCacheData(HomeCacheManager.CATALOG);
            List<CategoryVo> all = (List<CategoryVo>) map.get(HomeCacheManager.CATALOG);
            System.out.println(idAll(all,id));
            return ResponseUtil.ok(idAll(all,id));
        }
        LitemallCategory category = categoryService.findById(id);
        if (category == null){
            return ResponseUtil.fail();
        }
        CategoryVo vo = new CategoryVo();
        vo.toVo(category);
        vo.setChildren(allLevel(id));
        return ResponseUtil.ok(vo);
    }

    private CategoryVo idAll(List<CategoryVo> categoryVoList , Integer id){
        CategoryVo vo = null;
        for (CategoryVo categoryVo : categoryVoList) {
            if (categoryVo.getId().equals(id)){
                vo = categoryVo;
                break;
            }else if (categoryVo.getChildren() != null && categoryVo.getChildren().size() > 0){
                vo = idAll(categoryVo.getChildren(),id);
                if (vo != null){
                    break;
                }
            }
        }
        return vo;
    }

    /**
     * 所有分类数据
     *
     * @return 所有分类数据
     */
    @GetMapping("all")
    @ApiOperation(value = "获取所有类目",response = CategoryVo.class,notes = "{data:{catalog:[CategoryVo]}}",nickname = "获取所有类目")
    public Object queryAll() {
        //优先从缓存中读取
        if (HomeCacheManager.hasData(HomeCacheManager.CATALOG)) {
            return ResponseUtil.ok(HomeCacheManager.getCacheData(HomeCacheManager.CATALOG));
        }
        List<CategoryVo> all = allLevel(0);
        Map<String,Object> map = new HashMap<>();
        map.put(HomeCacheManager.CATALOG,all);
        HomeCacheManager.loadData(HomeCacheManager.CATALOG,map);
        return ResponseUtil.ok(map);
    }


    private List<CategoryVo> allLevel(Integer pid){
        List<CategoryVo> list = new ArrayList<>();
        List<LitemallCategory> categories =categoryService.queryByPid(pid);
        if (categories == null || categories.size() == 0)return null;
        for (LitemallCategory category : categories) {
            CategoryVo vo = new CategoryVo();
            vo.toVo(category);
            vo.setChildren(allLevel(vo.getId()));
            list.add(vo);
        }
        return list;

    }


}