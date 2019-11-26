package com.lhcode.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.lhcode.litemall.admin.vo.RegionVO;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.domain.LitemallRegion;
import com.lhcode.litemall.db.service.LitemallRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/region")
@Validated
@ApiIgnore
public class AdminRegionController {
    private final Log logger = LogFactory.getLog(AdminRegionController.class);

    @Autowired
    private LitemallRegionService regionService;

    @GetMapping("/clist")
    public Object clist(@NotNull Integer id) {
        List<LitemallRegion> regionList = regionService.queryByPid(id);
        return ResponseUtil.ok(regionList);
    }

    @GetMapping("/list")
    public Object list() {
        List<RegionVO> regionVOList = new ArrayList<>();

        List<LitemallRegion> provinceList = regionService.queryByPid(0);
        for(LitemallRegion province : provinceList){
            RegionVO provinceVO = new RegionVO();
            provinceVO.setId(province.getId());
            provinceVO.setName(province.getName());
            provinceVO.setCode(province.getCode());
            provinceVO.setType(province.getType());

            List<LitemallRegion> cityList = regionService.queryByPid(province.getId());
            List<RegionVO> cityVOList = new ArrayList<>();
            for(LitemallRegion city : cityList){
                RegionVO cityVO = new RegionVO();
                cityVO.setId(city.getId());
                cityVO.setName(city.getName());
                cityVO.setCode(city.getCode());
                cityVO.setType(city.getType());

                List<LitemallRegion> areaList = regionService.queryByPid(city.getId());
                List<RegionVO> areaVOList = new ArrayList<>();
                for(LitemallRegion area : areaList){
                    RegionVO areaVO = new RegionVO();
                    areaVO.setId(area.getId());
                    areaVO.setName(area.getName());
                    areaVO.setCode(area.getCode());
                    areaVO.setType(area.getType());
                    areaVOList.add(areaVO);
                }

                cityVO.setChildren(areaVOList);
                cityVOList.add(cityVO);
            }
            provinceVO.setChildren(cityVOList);
            regionVOList.add(provinceVO);
        }

        return ResponseUtil.ok(regionVOList);
    }
}
