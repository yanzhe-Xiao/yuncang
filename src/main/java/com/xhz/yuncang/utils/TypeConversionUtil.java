package com.xhz.yuncang.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import java.util.HashMap;
import java.util.Map;

public class TypeConversionUtil {

    /**
     * Object 转化为 Map<String,String>
     * 注意：1、这里是强转
     *      2、object为null时，返回null，非字符串“null”
     * @param object
     * @return
     */
    public static Map<String,String> Obj2MapSS(Object object){
        if (object==null){
            return null;
        }
        String jsonString = JSON.toJSONString(object);
        return JSONObject.parseObject(jsonString, new TypeReference<Map<String, String>>() {
        });
    }

    /**
     * Map<Object, Object> 转换为 Map<String, String>
     * ！！！注意：1、强转（其他属性->String）
     *           2、Object key 或 value 为null时，转为 字符串“null”
     * @param originalMap
     * @return
     */
    public static Map<String, String> oo2ssMap(Map<Object, Object> originalMap) {
        Map<String, String> convertedMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : originalMap.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            // 处理键
            String stringKey = (key != null) ? key.toString() : "null";
            // 处理值
            String stringValue = (value != null) ? value.toString() : "null";
            convertedMap.put(stringKey, stringValue);
        }
        return convertedMap;
    }
}
