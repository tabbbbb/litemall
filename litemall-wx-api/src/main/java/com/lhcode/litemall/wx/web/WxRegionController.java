package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.db.domain.LitemallFootprint;
import com.lhcode.litemall.wx.service.HomeCacheManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallRegion;
import com.lhcode.litemall.db.service.LitemallRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 区域服务
 */
@RestController
@RequestMapping("/wx/region")
@Validated
@Api(value = "/wx/region",description = "区域服务")
public class WxRegionController {
    private final Log logger = LogFactory.getLog(WxRegionController.class);

    @Autowired
    private LitemallRegionService regionService;

    /**
     * 区域数据
     * <p>
     * 获取所有区域数据
     *
     * @param
     * @return 区域数据
     */
    @GetMapping("list")
    @ApiOperation(value = "获取所有区域数据",response = LitemallRegion.class,notes = "region:LitemallRegion",nickname = "获取所有区域数据")
    public Object list() {
        if (HomeCacheManager.hasData(HomeCacheManager.REGION)){
            return ResponseUtil.ok(HomeCacheManager.getCacheData(HomeCacheManager.REGION));
        }
        List<LitemallRegion> regionList = regionService.toAll(0);
        Map<String,Object> map = new HashMap<>();
        map.put(HomeCacheManager.REGION,regionList);
        HomeCacheManager.loadData(HomeCacheManager.REGION,map);
        return ResponseUtil.ok(map);
    }


    @GetMapping("region")
    @ApiOperation(value = "获取某个区域信息",response = LitemallRegion.class,notes = "region:LitemallRegion",nickname = "获取某个区域信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(paramType = "query",name = "id" ,value="区域id",dataTypeClass = Integer.class)
    })
    public Object getRegion(Integer id){
        return ResponseUtil.ok(regionService.findById(id));
    }



}