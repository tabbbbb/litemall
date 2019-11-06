package com.lhcode.litemall.wx.web;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.lhcode.litemall.core.config.WxConfig;
import com.lhcode.litemall.core.config.WxProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping("/wx/qrcode")
public class WxQRController {
	@Resource(name="ksRestTemplate")
	private RestTemplate rt;

	@Autowired
    private WxProperties properties;

    @RequestMapping("/createQRCode")
    public void createQRCode(HttpServletResponse response) {
        OutputStream out = null;
        try {
            byte[] b = createQRCode(token());
            out = response.getOutputStream();
            out.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String token() throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("grant_type", "client_credential");
        map.put("appid", properties.getAppId());//你的appid
        map.put("secret", properties.getAppSecret());//你的secret
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type={grant_type}&appid={appid}&secret={secret}";
        ResponseEntity<String> result = rt.getForEntity(url, String.class, map);

        JSONObject access_token_json = JSONObject.parseObject(result.getBody());
        String access_token = access_token_json.getString("access_token");

        return access_token;
        /*************************************************/
    }

    private byte[] createQRCode(String access_token) throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
        String url = "https://api.weixin.qq.com/wxa/getwxacode?access_token={access_token}";
        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put("access_token",access_token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("path","page/index/index");//你的二维码访问的地址
        paramMap.put("width","430");//你的二维码宽度大小 

        String jsonString = JSONObject.toJSONString(paramMap);

        HttpEntity<String> entity = new HttpEntity<String>(jsonString,headers);
        ResponseEntity<byte[]> responseEntity = rt.postForEntity(url,entity,byte[].class,urlParams);
        return responseEntity.getBody();
    }

}
