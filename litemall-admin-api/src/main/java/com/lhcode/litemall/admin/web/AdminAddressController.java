package com.lhcode.litemall.admin.web;

import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.admin.util.AdminResponseCode;
import com.lhcode.litemall.db.domain.LitemallUser;
import com.lhcode.litemall.db.service.LitemallUserService;
import com.github.pagehelper.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallAddress;
import com.lhcode.litemall.db.service.LitemallAddressService;
import com.lhcode.litemall.db.service.LitemallRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/address")
@Validated
@ApiIgnore
public class AdminAddressController {
    private final Log logger = LogFactory.getLog(AdminAddressController.class);

    @Autowired
    private LitemallAddressService addressService;
    @Autowired
    private LitemallRegionService regionService;
    @Autowired
    private LitemallUserService userService;

    private Map<String, Object> toVo(LitemallAddress address) {
        Map<String, Object> addressVo = new HashMap<>();
        addressVo.put("id", address.getId());
        addressVo.put("userId", address.getUserId());
        addressVo.put("name", address.getName());
        addressVo.put("mobile", address.getMobile());
        addressVo.put("isDefault", address.getIsDefault());
        addressVo.put("provinceId", address.getProvinceId());
        addressVo.put("cityId", address.getCityId());
        addressVo.put("areaId", address.getAreaId());
        addressVo.put("address", address.getAddress());
        String province = regionService.findById(address.getProvinceId()).getName();
        String city = regionService.findById(address.getCityId()).getName();
        String area = regionService.findById(address.getAreaId()).getName();
        addressVo.put("province", province);
        addressVo.put("city", city);
        addressVo.put("area", area);
        toVo(addressVo);
        return addressVo;
    }


    @RequiresPermissionsDesc(menu={"用户管理" , "收货地址"}, button="查询")
    @GetMapping("/list")
    public Object list(Integer userId, String name,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {

        List<LitemallAddress> addressList = addressService.querySelective(userId, name, page, limit, sort, order);
        long total = PageInfo.of(addressList).getTotal();

        List<Map<String, Object>> addressVoList = new ArrayList<>(addressList.size());
        for (LitemallAddress address : addressList) {
            Map<String, Object> addressVo = toVo(address);
            addressVoList.add(addressVo);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", addressVoList);

        return ResponseUtil.ok(data);
    }



    @RequiresPermissions("admin:address:address")
    @RequiresPermissionsDesc(menu={"用户管理" , "收货地址"}, button="根据id查询")
    @GetMapping("/address")
    public Object selectAddressByUserId(@RequestParam("userId") Integer userId, @RequestParam("order")String order ,@RequestParam("sort")String sort) {
        if (userId == null){
            return ResponseUtil.fail(AdminResponseCode.USER_ID_NULL,"用户ID为空");
        }
        if (!userService.userCount(userId)){
            return ResponseUtil.fail(AdminResponseCode.INVALID_USER_ID,"用户ID不存在");
        }
        List<LitemallAddress> addresses = addressService.selectAddressByUserId(userId, sort, order);
        List<Map<String, Object>> addressVoList = new ArrayList<>(addresses.size());
        for (LitemallAddress litemallAddress : addresses) {
            Map<String, Object> addressVo = toVo(litemallAddress);
            addressVoList.add(addressVo);
        }
        return ResponseUtil.ok(addressVoList);

    }

    private void toVo(Map<String, Object> address) {
        address.put("nickName",userService.nickNameByUserId((Integer) address.get("userId")));
        address.put("defaultText",(boolean)address.get("isDefault")?"是":"否");
    }

}
