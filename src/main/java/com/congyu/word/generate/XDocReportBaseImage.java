package com.congyu.word.generate;

public interface XDocReportBaseImage {

    // 获取图片
    byte[] getImageBytes();
    // 图片类型 见：org.apache.poi.xwpf.usermodel.Document
    int getImageType();
    // 图片名称
    String getImageName();
    // 图片宽度
    int getWidthPx();
    // 图片高度
    int getHeightPx();
}
