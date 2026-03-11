package com.congyu.word.generate;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface XDocReportBaseDataBean {

    // 模板文件
    File getTemplateFile();

    // 目标文件
    String getTargetPath();

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
