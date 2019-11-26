/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: CategoryVo
 * Author:   Administrator
 * Date:     2019/11/15 0015 10:59
 * Description: 类别值
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.wx.vo;

import com.lhcode.litemall.db.domain.LitemallCategory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈类别值〉
 *
 * @author Administrator
 * @create 2019/11/15 0015
 * @since 1.0.0
 */
@ApiModel(value = "CategoryVo",description = "类别返回值")
public class CategoryVo {

    @ApiModelProperty("主键id")
    private Integer id;
    @ApiModelProperty("类别名称")
    private String name;
    @ApiModelProperty("类别图标")
    private String icon;
    @ApiModelProperty("父级id")
    private Integer pid;
    @ApiModelProperty("类别状态")
    private Integer status;
    @ApiModelProperty("类别级别")
    private String level;
    @ApiModelProperty("下属类别")
    private List<CategoryVo> children;

    public List<CategoryVo> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryVo> children) {
        this.children = children;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void toVo(LitemallCategory category){
        this.id = category.getId();
        this.icon = category.getIconUrl();
        this.pid = category.getPid();
        this.status = category.getStatus();
        this.level = category.getLevel();
        this.name = category.getName();
    }
}
