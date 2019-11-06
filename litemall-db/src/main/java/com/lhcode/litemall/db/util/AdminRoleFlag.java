/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: AdminRoleFlag
 * Author:   Administrator
 * Date:     2019/11/5 0005 18:28
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.db.util;

import com.lhcode.litemall.db.domain.LitemallAdmin;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/11/5 0005
 * @since 1.0.0
 */
public class AdminRoleFlag {
    public static Integer toAdminId(LitemallAdmin admin){

        for (Integer role : admin.getRoleIds()) {
            if (role == 1){
                return -1;
            }
        }
        return admin.getId();
    }
}
