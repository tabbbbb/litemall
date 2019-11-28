/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: WxConfigController
 * Author:   Administrator
 * Date:     2019/11/25 0025 17:46
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.core.system.SystemConfig;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.db.service.LitemallSystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/11/25 0025
 * @since 1.0.0
 */
@RestController
@RequestMapping("/wx/config")
@Validated
@Api(value = "WxConfigController",description = "配置信息")
public class WxConfigController {

    @Autowired
    private LitemallSystemConfigService systemConfigService;

    @ApiOperation(value="返回商城配置",response=ResponseUtil.class,notes = "name:名称,address:地址,phone:电话,qq:QQ",nickname = "返回运费配置")
    @GetMapping("/mall")
    public Object listMall() {
        Map<String, String> data = new HashMap<>();
        data.put("name",SystemConfig.getMallName());
        data.put("address",SystemConfig.getMallAddress());
        data.put("phone",SystemConfig.getMallPhone());
        data.put("qq",SystemConfig.getMallQQ());
        return ResponseUtil.ok(data);
    }

    @ApiOperation(value="返回运费配置",response=ResponseUtil.class,notes = "price:运费,min:运费减免的最小值",nickname = "返回运费配置")
    @GetMapping("/address")
    public Object getAddressPrice(){
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("price",SystemConfig.getFreight());
        responseMap.put("min",SystemConfig.getFreightLimit());
        return ResponseUtil.ok(responseMap);
    }


    @ApiOperation(value="返回订金配置",response=ResponseUtil.class,notes = "down:定金",nickname = "返回订金配置")
    @GetMapping("/down")
    public Object getDownPayment(){
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("down",SystemConfig.getDownPayment());
        return ResponseUtil.ok(responseMap);
    }
}
