package com.lhcode.litemall.admin.web;

import com.github.pagehelper.PageInfo;
import com.lhcode.litemall.admin.annotation.RequiresPermissionsDesc;
import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.LitemallAd;
import com.lhcode.litemall.db.domain.LitemallDirect;
import com.lhcode.litemall.db.service.LitemallAdService;
import com.lhcode.litemall.db.service.LitemallDirectService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/direct")
@Validated
@ApiIgnore
public class AdminDirectController {
    private final Log logger = LogFactory.getLog(AdminDirectController.class);

    @Autowired
    private LitemallDirectService directService;

    @RequiresPermissions("admin:direct:list")
    @RequiresPermissionsDesc(menu={"页面管理" , "直通车设置"}, button="查询")
    @GetMapping("/list")
    public Object list(Integer position, Integer isStart,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "is_start") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallDirect> directList = directService.getList(position,isStart,page,limit,sort,order);
        long total = PageInfo.of(directList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", directList);
        return ResponseUtil.ok(data);
    }

    private Object validate(LitemallDirect direct) {
        String title = direct.getTitle();
        if (StringUtils.isEmpty(title)) {
            return ResponseUtil.badArgument();
        }
        if (StringUtils.isEmpty(direct.getUrl())){
            return ResponseUtil.badArgument();
        }
        if (StringUtils.isEmpty(direct.getLink())){
            return ResponseUtil.badArgument();
        }
        if (direct.getPosition() == null ){
            return ResponseUtil.badArgument();
        }
        return null;
    }

    @RequiresPermissions("admin:direct:create")
    @RequiresPermissionsDesc(menu={"页面管理" , "直通车设置"}, button="添加")
    @PostMapping("/create")
    public Object create(@RequestBody LitemallDirect direct) {
        Object error = validate(direct);
        if (error != null) {
            return error;
        }
        directService.createDirect(direct);
        return ResponseUtil.ok(direct);
    }

    @RequiresPermissions("admin:direct:update")
    @RequiresPermissionsDesc(menu={"页面管理" , "直通车设置"}, button="编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallDirect direct) {
        Object error = validate(direct);
        if (error != null) {
            return error;
        }
        if (directService.updateDirect(direct) == 0) {
            return ResponseUtil.updatedDataFailed();
        }

        return ResponseUtil.ok(direct);
    }

    @RequiresPermissions("admin:direct:delete")
    @RequiresPermissionsDesc(menu={"页面管理" , "直通车设置"}, button="删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallDirect direct) {
        Integer id = direct.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        directService.deleteDirect(id);
        return ResponseUtil.ok();
    }

}
