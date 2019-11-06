/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: LiteMallConstant
 * Author:   Administrator
 * Date:     2019/10/31 0031 9:30
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.db.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 〈值〉<br>
 * 〈〉
 *
 * @author 曹智
 * @create 2019/10/31 0031
 * @since 1.0.0
 */
@Component
@ConfigurationProperties("litemall.constart")
public class LiteMallConstant {

    public static final Integer vipCount = 3 ;


}
