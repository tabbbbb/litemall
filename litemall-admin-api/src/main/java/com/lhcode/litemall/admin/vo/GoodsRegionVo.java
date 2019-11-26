/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: GoodsRegionVo
 * Author:   Administrator
 * Date:     2019/11/16 0016 16:55
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.admin.vo;

import com.lhcode.litemall.db.domain.LitemallRegion;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/11/16 0016
 * @since 1.0.0
 */
public class GoodsRegionVo {
    private Integer value = null;
    private String label = null;
    private List children = null;

    public void toThis(LitemallRegion region){
        value = region.getId();
        label = region.getName();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List getChildren() {
        return children;
    }

    public void setChildren(List children) {
        this.children = children;
    }
}
