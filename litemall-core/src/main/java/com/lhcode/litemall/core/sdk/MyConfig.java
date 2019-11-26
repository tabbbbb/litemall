/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: MyConfig
 * Author:   Administrator
 * Date:     2019/11/22 0022 13:04
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.core.sdk;

import com.lhcode.litemall.core.config.WxProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2019/11/22 0022
 * @since 1.0.0
 */
public class MyConfig extends WXPayConfig{

    @Autowired
    private WxProperties properties;


    @Override
    String getAppID() {
        return properties.getAppId();
    }

    @Override
    String getMchID() {
        return properties.getMchId();
    }

    @Override
    String getKey() {
        return properties.getMchKey();
    }

    @Override
    InputStream getCertStream() {
        return null;
    }

    @Override
    IWXPayDomain getWXPayDomain() {
        return null;
    }
}
