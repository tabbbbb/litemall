package com.lhcode.litemall.wx;

import org.assertj.core.api.LocalDateAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class WxConfigTest {
    @Autowired
    private Environment environment;

    @Test
    public void test() {
        // 测试获取application-core.yml配置信息
        System.out.println(environment.getProperty("litemall.express.appId"));
        // 测试获取application-db.yml配置信息
        System.out.println(environment.getProperty("spring.datasource.druid.url"));
        // 测试获取application-wx.yml配置信息
        System.out.println(environment.getProperty("litemall.wx.app-id"));
        // 测试获取application-wx.yml配置信息
        System.out.println(environment.getProperty("litemall.wx.notify-url"));
        // 测试获取application.yml配置信息
        System.out.println(environment.getProperty("logging.level.org.linlinjava.litemall.wx"));
    }



    @Test
    public void md5(){
        final Timer timer = new Timer();
        System.out.println(timer.purge());
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //这里写修改方法，根据订单号修改状态就可以了
                System.out.println("1秒后执行此方法");

                // 不要忘记写中断定时器
                timer.cancel();
            }
            //1秒等于1000毫秒  这里是一天
        },1);

        System.out.println(timer.purge());

    }



}
