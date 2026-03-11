package com.congyu.word.generate;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface XDocReportBaseDataBean {

    Path getTemplatePath();

    String getTargetName();

    // 数据
    Map<String,Object> getMap();

    // 表格元数据
    Map<String,Class> getKeyClass();

    // 图片数据 key 格式 \{\{image:\d+(?:_\d+)*\}\}
    Map<String,byte[]> getImageFields();

    default void putMap(Map<String,Object> map,String key,Object valve,Object def){
        if (valve == null){
            map.put(key,def);
        }else if(valve instanceof List && ((List<?>) valve).isEmpty()){
            map.put(key,def);
        } else {
            map.put(key,valve);
        }
    }
}
