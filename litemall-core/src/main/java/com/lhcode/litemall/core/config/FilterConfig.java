/**
 * Copyright (C), 2019, 蓝煌信息科技公司
 * FileName: FilterConfig
 * Author:   Administrator
 * Date:     2019/11/20 0020 16:17
 * Description: 过滤器配置
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.lhcode.litemall.core.config;

import com.lhcode.litemall.core.filter.RequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 〈一句话功能简述〉<br> 
 * 〈过滤器配置〉
 *
 * @author Administrator
 * @create 2019/11/20 0020
 * @since 1.0.0
 */
//@Configuration
//public class FilterConfig {
//    @Bean
//    public FilterRegistrationBean httpServletRequestReplacedRegistration() {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(new RequestFilter());
//        registration.addUrlPatterns("/*");
//        registration.addInitParameter("paramName", "paramValue");
//        registration.setName("httpServletRequestReplacedFilter");
//        registration.setOrder(1);
//        return registration;
//    }
//
//}
