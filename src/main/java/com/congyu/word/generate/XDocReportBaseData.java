package com.congyu.word.generate;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

public interface XDocReportBaseData {

    // 模板文件
    File getTemplateFile();

    // 目标文件
    String getTargetPath();

    // 数据
    Map<String,Object> getDataMap();

    // 图片占位符匹配方式，比如\{\{IMAGE:\d+(?:_\d+)*\}\}
    Pattern getImagePattern();

    // 图片数据
    Map<String,XDocReportBaseImage> getImageFields();
}
