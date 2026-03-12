package com.congyu.word.generate;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface XDocReportBaseData {

    // 模板文件
    File getTemplateFile();

    // 目标文件
    String getTargetPath();

    // 数据
    Map<String,Object> getDataMap();

    // 图片数据
    List<XDocReportBaseImage> getImageFields();
}
