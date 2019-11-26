package com.lhcode.litemall.wx.service;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单缓存的数据
 */
public class HomeCacheManager {
    public static final boolean ENABLE = false;
    public static final String INDEX = "index";
    public static final String CATALOG = "catalog";
    public static final String GOODS = "goods";
    public static final String REGION = "region";

    private static Map<String, Map<String, Object>> cacheDataList = new HashMap<>();

    /**
     * 缓存首页数据
     * synchronized 防止雪崩
     * @param data
     */
    public synchronized static void loadData(String cacheKey, Map<String, Object> data) {
        Map<String, Object> cacheData = cacheDataList.get(cacheKey);
        //有记录，退出
        if (hasData(cacheKey)) {
            return;
        }
        cacheData = new HashMap<>();
        //深拷贝
        cacheData.putAll(data);
        cacheData.put("isCache", "true");
        //设置缓存有效期为10分钟
        if (cacheKey != REGION){
            cacheData.put("expireTime", LocalDateTime.now().plusMinutes(10));
        }else{
            cacheData.put("expireTime", LocalDateTime.now().plusDays(10));
        }
        cacheDataList.put(cacheKey, cacheData);
    }

    public static Map<String, Object> getCacheData(String cacheKey) {
        return cacheDataList.get(cacheKey);
    }

    /**
     * 判断缓存中是否有数据
     *
     * @return
     */
    public static boolean hasData(String cacheKey) {


        Map<String, Object> cacheData = cacheDataList.get(cacheKey);
        if (cacheData == null) {
            return false;
        } else {
            LocalDateTime expire = (LocalDateTime) cacheData.get("expireTime");
            if (expire.isBefore(LocalDateTime.now())) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 清除所有缓存
     */
    public static void clearAll() {
        cacheDataList = new HashMap<>();
    }

    /**
     * 清除缓存数据
     */
    public static void clear(String cacheKey) {
        Map<String, Object> cacheData = cacheDataList.get(cacheKey);
        if (cacheData != null) {
            cacheDataList.remove(cacheKey);
        }
    }

}
