package com.lhcode.litemall.wx.util;

import com.lhcode.litemall.core.config.WxProperties;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class AuthUtil {

    @Autowired
    private static WxProperties properties;

    public static final String APPID = properties.getAppId();
    public static final String APPSECRET = properties.getAppSecret();
    public static final String MCHID = properties.getMchId();
    public static final String PATERNERKEY = properties.getMchKey();
    public static final String CERTPATH = properties.getKeyPath();

    public static JSONObject doGetJson(String url) throws ClientProtocolException, IOException {
        JSONObject jsonObject = null;
        // 首先初始化HttpClient对象
        DefaultHttpClient client = new DefaultHttpClient();
        // 通过get方式进行提交
        HttpGet httpGet = new HttpGet(url);
        // 通过HTTPclient的execute方法进行发送请求
        HttpResponse response = client.execute(httpGet);
        // 从response里面拿自己想要的结果
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String result = EntityUtils.toString(entity, "UTF-8");
            jsonObject = jsonObject.fromObject(result);
        }
        // 把链接释放掉
        httpGet.releaseConnection();
        return jsonObject;
    }

    /**
     * @Title: getRequestIp
     * @Description: 获取用户的ip地址
     * @param:
     * @return:
     */
    public static String getRequestIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.indexOf(",") != -1) {
            String[] ips = ip.split(",");
            ip = ips[0].trim();
        }
        return ip;
    }

    /**
     * 生成随机数
     * @return
     */
    public static String createNonceStr() {
        String s = UUID.randomUUID().toString();
        // 去掉“-”符号
        return s.replaceAll( "\\-","").toUpperCase();
    }

    /**
     * 构造签名
     * @param params
     * @param paternerKey
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String Sign(Map<String, String> params, String paternerKey) throws UnsupportedEncodingException {
        String string1 = createSign(params, false);
        String stringSignTemp = string1 + "&key=" + paternerKey;
        //String signValue = MD5Tools.encode(stringSignTemp).toUpperCase();
        String signValue = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
        return signValue;
    }

    /**
     * 构造签名
     * @param params
     * @param encode
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String createSign(Map<String, String> params, boolean encode) throws UnsupportedEncodingException {
        Set<String> keysSet = params.keySet();
        Object[] keys = keysSet.toArray();
        Arrays.sort(keys);
        StringBuffer temp = new StringBuffer();
        boolean first = true;
        for (Object key : keys) {
            if (first) {
                first = false;
            } else {
                temp.append("&");
            }
            temp.append(key).append("=");
            Object value = params.get(key);
            String valueString = "";
            if (null != value) {
                valueString = value.toString();
            }
            if (encode) {
                temp.append(URLEncoder.encode(valueString, "UTF-8"));
            } else {
                temp.append(valueString);
            }
        }
        return temp.toString();
    }

    /**
     * map转xml
     * @param map
     * @return
     */
    public static String map2XmlString(Map<String, String> map) {
        String xmlResult = "";

        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        for (String key : map.keySet()) {
            System.out.println(key + "========" + map.get(key));

            String value = "<![CDATA[" + map.get(key) + "]]>";
            sb.append("<" + key + ">" + value + "</" + key + ">");
            System.out.println();
        }
        sb.append("</xml>");
        xmlResult = sb.toString();

        return xmlResult;
    }

    /**
     * xml转map
     * @param xml
     * @return
     */
    public static Map<String, String> readStringXmlOut(String xml) {
        Map<String, String> map = new HashMap<String, String>();
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            @SuppressWarnings("unchecked")
            List<Element> list = rootElt.elements();// 获取根节点下所有节点
            for (Element element : list) { // 遍历节点
                map.put(element.getName(), element.getText()); // 节点的name为map的key，text为map的value
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


}