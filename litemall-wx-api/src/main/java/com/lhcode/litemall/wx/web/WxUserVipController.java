package com.lhcode.litemall.wx.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员中心
 */
@RestController
@RequestMapping("/wx/uservip")
@Validated
public class WxUserVipController {

    private final Log logger = LogFactory.getLog(WxTopicController.class);



}
